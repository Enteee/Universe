package ch.duckpond.universe.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.GdxRuntimeException;

import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;

/**
 * The main game class.
 *
 * @author ente
 */
public class Universe extends ApplicationAdapter {

    private static final Universe universe = new Universe();
    private Player thisPlayer = new Player(new Color(NeonColors.getRandomColor().getColorRGB888()));
    private Simulation simulation;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera orthoCamera;
    private boolean massSpawning = false;
    private Vector2 massSpawnPoint = new Vector2();
    private Vector2 massSpawnVelocity = new Vector2();
    private FrameBuffer neonTargetAFBO;
    private ShaderProgram glowShader;
    private Fixture selectedFixture;

    private Universe() {
    }

    public static Universe getInstance() {
        return universe;
    }

    public Player getThisPlayer() {
        return thisPlayer;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    /**
     * Spwan the mass which was previousely defined with {@link Universe#setMassSpawnPoint
     * (Vector2)} and {@link Universe#setMassSpawnVelocity(Vector2)}
     */
    public void spawnMass() {
        if (isMassSpawning()) {
            simulation.spawnMass((int) massSpawnPoint.x,
                                 (int) massSpawnPoint.y,
                                 Globals.MASS_SPAWN_RADIUS * camera.zoom,
                                 massSpawnVelocity);
            setMassSpawnVelocity(new Vector2());
            massSpawning = false;
        }
    }

    public boolean isMassSpawning() {
        return massSpawning;
    }

    /**
     * Sets the velocity of a spawning mass in world coorinates.
     *
     * @param massSpawnVelocity velocity in world coordinates
     */
    public void setMassSpawnVelocity(Vector2 massSpawnVelocity) {
        this.massSpawnVelocity = new Vector2(massSpawnVelocity);
    }

    /**
     * Select a point on the map.
     *
     * @param selectPoint the selected point in world coordinates
     */
    public void setSelectPoint(final Vector3 selectPoint) {
        for (final Body body : simulation.getBodies()) {
            final Mass mass = (Mass) body.getUserData();
            for (final Fixture fixture : body.getFixtureList()) {
                // detect collision
                final CircleShape circleShape = (CircleShape) fixture.getShape();
                final Vector3 shapePosition = new Vector3(circleShape.getPosition().x,
                                                          circleShape.getPosition().y,
                                                          0f);
                // mass selected?
                if (shapePosition.sub(selectPoint).len() < circleShape.getRadius()) {
                    //mass.clicked()
                    break;
                }
            }
        }
    }

    @Override
    public void create() {
        // gdx set up
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) {
            inputMultiplexer.addProcessor(new GestureDetector(new UniverseGestureProcessor()));
        } else {
            inputMultiplexer.addProcessor(new UniverseInputProcessor());
        }
        Gdx.input.setInputProcessor(inputMultiplexer);
        // simulation set up
        Box2D.init();
        simulation = new Simulation();
        // set up rendering
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        final int width = Gdx.graphics.getWidth();
        final int height = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(200f, 200f * height / (float) width);
        orthoCamera = new OrthographicCamera();
        orthoCamera.setToOrtho(true);
        // create frame buffers
        neonTargetAFBO = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        glowShader = new ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"),
                                       Gdx.files.internal("shaders/glowshader.frag"));
        if (!glowShader.isCompiled()) {
            throw new GdxRuntimeException(String.format("Shader not compiled: %s",
                                                        glowShader.getLog()));
        }
    }

    @Override
    public void resize(int width, int height) {
        neonTargetAFBO = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        orthoCamera.setToOrtho(true);
    }

    @Override
    public void render() {

        // bind the neonTargetAFBO
        neonTargetAFBO.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw masses background
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.combined);
        // draw masses backgrounds
        for (final Body body : simulation.getBodies()) {
            // outer glow border
            for (final Fixture fixture : body.getFixtureList()) {
                final CircleShape circleShape = (CircleShape) fixture.getShape();
                shapeRenderer.setColor(new Color(0f, 0f, 0f, 1f));
                shapeRenderer.circle(fixture.getBody().getPosition().x,
                                     fixture.getBody().getPosition().y,
                                     circleShape.getRadius() + Globals.GLOW_SAMPLES / 2f * Globals.GLOW_QUALITY);
            }
        }
        shapeRenderer.end();

        drawMasses();

        // draw spawning mass
        if (isMassSpawning()) {
            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(new Color(0f, 1f, 0f, 1f));
            shapeRenderer.circle(massSpawnPoint.x,
                                 massSpawnPoint.y,
                                 Globals.MASS_SPAWN_RADIUS * camera.zoom);
            shapeRenderer.line(getMassSpawnPoint(),
                               new Vector2(getMassSpawnPoint()).sub(massSpawnVelocity));
            shapeRenderer.end();
        }
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(new Color(0f, 1f, 0f, 1f));
        shapeRenderer.x(0, 0, Globals.MASS_SPAWN_RADIUS * camera.zoom);
        shapeRenderer.end();

        neonTargetAFBO.end();

        //clear the background FBO
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.setProjectionMatrix(orthoCamera.combined);
        batch.setShader(glowShader);
        glowShader.setUniformf("size", neonTargetAFBO.getWidth(), neonTargetAFBO.getHeight());
        glowShader.setUniformi("samples", Globals.GLOW_SAMPLES);
        glowShader.setUniformf("quality", Globals.GLOW_QUALITY);
        glowShader.setUniformf("intensity", Globals.GLOW_INTENSITY);
        batch.draw(neonTargetAFBO.getColorBufferTexture(), 0, 0);
        batch.end();

        //drawMasses();

        // render circle menu
        if (selectedFixture != null) {
            final Vector3 circleMenuPos = new Vector3(selectedFixture.getBody().getPosition().x,
                                                      selectedFixture.getBody().getPosition().y,
                                                      0);
            final float circleMenuRadius = selectedFixture.getShape().getRadius() + Globals.CIRCLE_MENU_BUTTON_MARGIN + Globals.CIRCLE_MENU_BUTTON_SIZE / 2f;
        }

        // do a simulation step
        simulation.update();
    }

    private void drawMasses() {
        // draw massess
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.combined);
        for (final Body body : simulation.getBodies()) {
            shapeRenderer.setColor(((Mass) body.getUserData()).getOwner().getColor());
            // outer glow border
            for (final Fixture fixture : body.getFixtureList()) {
                final CircleShape circleShape = (CircleShape) fixture.getShape();
                final float innerCircleRadius = circleShape.getRadius() - Globals.MASS_SURFACE_WIDTH;
                shapeRenderer.circle(fixture.getBody().getPosition().x,
                                     fixture.getBody().getPosition().y,
                                     circleShape.getRadius());
                // punch out inner border
                shapeRenderer.setColor(new Color(0f, 0f, 0f, 1f));
                shapeRenderer.circle(fixture.getBody().getPosition().x,
                                     fixture.getBody().getPosition().y,
                                     innerCircleRadius);
                shapeRenderer.setColor(new Color(0f, 0f, 0f, 0f));
                shapeRenderer.circle(fixture.getBody().getPosition().x,
                                     fixture.getBody().getPosition().y,
                                     innerCircleRadius - Globals.GLOW_SAMPLES / 2f * Globals.GLOW_QUALITY);
            }
        }
        shapeRenderer.end();
    }

    public Vector2 getMassSpawnPoint() {
        return new Vector2(massSpawnPoint);
    }

    public void setMassSpawnPoint(Vector2 massSpawnPoint) {
        this.massSpawnPoint = new Vector2(massSpawnPoint);
        setMassSpawnVelocity(new Vector2());
        massSpawning = true;
    }
}
