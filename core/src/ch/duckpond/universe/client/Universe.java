package ch.duckpond.universe.client;

import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;
import com.badlogic.gdx.*;
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
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sun.javafx.Utils;

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
    private Vector3 massSpawnPointScreen = new Vector3();
    private Vector3 massSpawnVelocityScreen = new Vector3();
    private FrameBuffer neonTargetAFBO;
    private ShaderProgram glowShader;
    /**
     * Body on which to center the view.
     */
    private Body centeredBody;

    private Universe() {
    }

    public static Universe getInstance() {
        return universe;
    }

    public Vector3 getMassSpawnPointScreen() {
        return massSpawnPointScreen;
    }

    /**
     * Starts the spawn process of a new mass
     *
     * @param massSpawnPointScreen the spawn location in screen coordinates
     */
    public void setMassSpawnPointScreen(final Vector3 massSpawnPointScreen) {
        this.massSpawnPointScreen = new Vector3(massSpawnPointScreen);
        this.massSpawnVelocityScreen = new Vector3(massSpawnPointScreen);
        massSpawning = true;
    }

    public Body getCenteredBody() {
        return centeredBody;
    }

    public void setCenteredBody(Body centeredBody) {
        this.centeredBody = centeredBody;
    }

    public Player getThisPlayer() {
        return thisPlayer;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    /**
     * Spwan the mass which was previousely defined with {@link Universe#setMassSpawnPointScreen
     * (Vector3)} and {@link Universe#setMassSpawnVelocityScreen(Vector3)}
     */
    public void spawnMass() {
        if (isMassSpawning()) {
            final Vector3 massSpawnPointWorld = camera.unproject(new Vector3(massSpawnPointScreen));
            final Vector3 massSpawnVelocityWorld = camera.unproject(new Vector3(massSpawnVelocityScreen));
            simulation.spawnMass(massSpawnPointWorld,
                    Globals.MASS_SPAWN_RADIUS * camera.zoom, new Vector3(massSpawnPointWorld).sub(massSpawnVelocityWorld));
            massSpawnVelocityScreen = new Vector3();
            massSpawning = false;
        }
    }

    public boolean isMassSpawning() {
        return massSpawning;
    }

    /**
     * Sets the velocity point of a spawning mass
     *
     * @param massSpawnVelocityScreen velocity in screen coordinates
     */
    public void setMassSpawnVelocityScreen(final Vector3 massSpawnVelocityScreen) {
        this.massSpawnVelocityScreen = new Vector3(massSpawnVelocityScreen);
    }

    /**
     * Select a point on the map.
     *
     * @param selectPoint the selected point in world coordinates
     * @return {@code true} if something was selected, {@code false} otherwise
     */
    public boolean setSelectPoint(final Vector3 selectPoint) {
        for (final Body body : simulation.getBodies()) {
            final Vector3 bodyPosition = new Vector3(body.getPosition().x,
                    body.getPosition().y,
                    0f);
            for (final Fixture fixture : body.getFixtureList()) {
                // detect collision
                final CircleShape circleShape = (CircleShape) fixture.getShape();
                // body selected?
                if (bodyPosition.sub(selectPoint).len() < circleShape.getRadius()) {
                    centeredBody = body;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void create() {
        // gdx set up
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) {
            inputMultiplexer.addProcessor(new GestureDetector(new ch.duckpond.universe.client.input.UniverseGestureProcessor()));
        } else {
            inputMultiplexer.addProcessor(new ch.duckpond.universe.client.input.UniverseInputProcessor());
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

        // center camera on centered body
        if (centeredBody != null) {
            final Vector3 centerTranslate = new Vector3(centeredBody.getPosition().x, centeredBody.getPosition().y, 0f).sub(camera.position).scl(Globals.CAMERA_CENTER_FACTOR);
            camera.translate(centerTranslate);
            camera.update();
        }

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
                shapeRenderer.circle(body.getPosition().x,
                        body.getPosition().y,
                        circleShape.getRadius() + Globals.GLOW_SAMPLES / 2f * Globals.GLOW_QUALITY);
            }
        }
        shapeRenderer.end();

        drawMasses();

        // draw spawning mass
        if (isMassSpawning()) {
            final Vector3 massSpawnPointWorld = camera.unproject(new Vector3(massSpawnPointScreen));
            final Vector3 massSpawnVelocityWorld = camera.unproject(new Vector3(massSpawnVelocityScreen));

            Gdx.app.debug(getClass().getName(), String.format("massSpawnPointScreen: %s, massSpawnPointWorld: %s", this.massSpawnPointScreen, massSpawnPointWorld));
            Gdx.app.debug(getClass().getName(), String.format("massSpawnVelocityScreen: %s massSpawnVelocityWorld: %s", massSpawnVelocityScreen, massSpawnVelocityWorld));

            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.setColor(new Color(0f, 1f, 0f, 1f));
            shapeRenderer.circle(massSpawnPointWorld.x, massSpawnPointWorld.y, Globals.MASS_SPAWN_RADIUS * camera.zoom);
            shapeRenderer.line(massSpawnPointWorld, massSpawnVelocityWorld);

            shapeRenderer.end();
        }
        // draw (0,0) refernce
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setProjectionMatrix(camera.combined);
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

        // do a simulation step
        simulation.update();
    }

    private void drawMasses() {
        // draw massess
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.combined);
        for (final Body body : simulation.getBodies()) {
            shapeRenderer.setColor(((Mass) body.getUserData()).getOwner().getColor());
            for (final Fixture fixture : body.getFixtureList()) {
                final CircleShape circleShape = (CircleShape) fixture.getShape();
                final float innerCircleRadius = circleShape.getRadius() - Globals.MASS_SURFACE_WIDTH;
                // velocity trail
                final Vector3[] lastPositions = ((Mass) body.getUserData()).getLastPositions();
                if(lastPositions.length > 1) {
                    final Bezier<Vector3> bezier = new Bezier<>(lastPositions, 0, Utils.clamp(1,lastPositions.length, 4));
                    for (int i = 0; i < 100; ++i) {
                        final Vector3 start = new Vector3();
                        final Vector3 end = new Vector3();
                        bezier.valueAt(start, 101f / i);
                        bezier.valueAt(end, 101f / (i + 1));
                        shapeRenderer.line(start, end);
                    }
                }
                // outer glow border
                shapeRenderer.circle(body.getPosition().x, body.getPosition().y,
                        circleShape.getRadius());
                // punch out inner border
                shapeRenderer.setColor(new Color(0f, 0f, 0f, 1f));
                shapeRenderer.circle(body.getPosition().x, body.getPosition().y,
                        innerCircleRadius);
                shapeRenderer.setColor(new Color(0f, 0f, 0f, 0f));
                shapeRenderer.circle(body.getPosition().x,
                        body.getPosition().y,
                        innerCircleRadius - Globals.GLOW_SAMPLES / 2f * Globals.GLOW_QUALITY);
            }
        }
        shapeRenderer.end();
    }
}
