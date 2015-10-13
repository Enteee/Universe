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
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.GdxRuntimeException;

import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;
import ch.duckpond.universe.test.utils.TestUtilsBody;

/**
 * The main game class.
 *
 * @author ente
 */
public class Universe extends ApplicationAdapter {

    private final static float BLUR_RADIUS = 3f;
    private Simulation simulation;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera orthoCamera;
    private boolean massSpawning = false;
    private Vector2 massSpawnPoint = new Vector2();
    private Vector2 massSpawnVelocity = new Vector2();
    private FrameBuffer neonTargetAFBO;
    private FrameBuffer neonTargetBFBO;
    private ShaderProgram blurShader;
    private ShaderProgram glowShader;

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
                                 Globals.MASS_DEFAULT_RADIUS * camera.zoom,
                                 massSpawnVelocity);
            setMassSpawnVelocity(new Vector2());
            setMassSpawning(false);
        }
    }

    public boolean isMassSpawning() {
        return massSpawning;
    }

    public void setMassSpawning(boolean massSpawning) {
        this.massSpawning = massSpawning;
    }

    public void setMassSpawnVelocity(Vector2 massSpawnVelocity) {
        this.massSpawnVelocity = new Vector2(massSpawnVelocity);
    }

    @Override
    public void create() {
        // gdx set up
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) {
            inputMultiplexer.addProcessor(new GestureDetector(new UniverseGestureProcessor(this)));
        } else {
            inputMultiplexer.addProcessor(new UniverseInputProcessor(this));
        }
        Gdx.input.setInputProcessor(inputMultiplexer);
        // simulation set up
        Box2D.init();
        simulation = new Simulation();
        // some random bodies
        for (int i = 0; i < 10; ++i) {
            TestUtilsBody.randomBody(simulation.getWorld());
        }
        // set up rendering
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        final int w = Gdx.graphics.getWidth();
        final int h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(200f, 200f * h / (float) w);
        orthoCamera = new OrthographicCamera();
        orthoCamera.setToOrtho(true);
        // create frame buffers
        neonTargetAFBO = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        neonTargetBFBO = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
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
        neonTargetBFBO = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        orthoCamera.setToOrtho(true);
    }

    @Override
    public void render() {

        // bind the neonTargetAFBO
        neonTargetAFBO.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // set up projection
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // draw masses backgrounds
        for (final Fixture fixture : simulation.getFixtures()) {
            final CircleShape circleShape = (CircleShape) fixture.getShape();
            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(new Color(0f, 0f, 0f, 1f));

            shapeRenderer.circle(fixture.getBody().getPosition().x,
                                 fixture.getBody().getPosition().y,
                                 circleShape.getRadius() + Globals.GLOW_SAMPLES / 2f);

            shapeRenderer.end();
        }
        // draw massess
        for (final Fixture fixture : simulation.getFixtures()) {
            final CircleShape circleShape = (CircleShape) fixture.getShape();
            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(new Color(1f, 0f, 0f, 1f));
            shapeRenderer.circle(fixture.getBody().getPosition().x,
                                 fixture.getBody().getPosition().y,
                                 circleShape.getRadius());
            shapeRenderer.end();
        }
        // draw spaning mass

        if (isMassSpawning()) {
            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(new Color(0f, 1f, 0f, 1f));
            shapeRenderer.circle(massSpawnPoint.x,
                                 massSpawnPoint.y,
                                 Globals.MASS_DEFAULT_RADIUS * camera.zoom);
            shapeRenderer.line(getMassSpawnPoint(),
                               new Vector2(getMassSpawnPoint()).sub(massSpawnVelocity));
            shapeRenderer.end();
        }
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(new Color(0f, 1f, 0f, 1f));
        shapeRenderer.x(0, 0, Globals.MASS_DEFAULT_RADIUS * camera.zoom);
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

        // do a simulation step
        simulation.update();
    }

    public Vector2 getMassSpawnPoint() {
        return new Vector2(massSpawnPoint);
    }

    public void setMassSpawnPoint(Vector2 massSpawnPoint) {
        this.massSpawnPoint = new Vector2(massSpawnPoint);
        setMassSpawnVelocity(new Vector2());
        setMassSpawning(true);
    }

}
