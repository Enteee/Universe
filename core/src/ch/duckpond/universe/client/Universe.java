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

import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;
import ch.duckpond.universe.test.utils.TestUtilsBody;

/**
 * The main game class.
 *
 * @author ente
 */
public class Universe extends ApplicationAdapter {

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
        //Viewport viewport = new FillViewport(1000, 1000, camera);
        // create frame buffers
        neonTargetAFBO = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        neonTargetBFBO = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        // create shaders
        blurShader = new ShaderProgram(Gdx.files.internal("shaders/blurshader.vert"),
                                       Gdx.files.internal("shaders/blurshader.frag"));
        if (!blurShader.isCompiled()) {
            Gdx.app.debug(getClass().getName(),
                          String.format("Shader not compiled: %s", blurShader.getLog()));
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

        // set up shader
        blurShader.begin();
        blurShader.setUniformf("dir", 0f, 0f); //direction of blur; nil for now
        blurShader.setUniformf("resolution", neonTargetAFBO.getWidth()); //size of FBO texture
        blurShader.setUniformf("radius", neonTargetAFBO.getWidth() * 5); //radius of blur

        // set up batch
        batch.begin();
        batch.setProjectionMatrix(camera.combined);

        // set up shape renderer
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.GRAY);

        // draw masses
        for (final Fixture fixture : simulation.getFixtures()) {
            final CircleShape circleShape = (CircleShape) fixture.getShape();
            shapeRenderer.circle(fixture.getBody().getPosition().x,
                                 fixture.getBody().getPosition().y,
                                 circleShape.getRadius());
        }
        shapeRenderer.setColor(Color.GOLD);
        // draw spaning mass
        if (isMassSpawning()) {
            shapeRenderer.circle(massSpawnPoint.x,
                                 massSpawnPoint.y,
                                 Globals.MASS_DEFAULT_RADIUS * camera.zoom);
            shapeRenderer.line(getMassSpawnPoint(),
                               new Vector2(getMassSpawnPoint()).sub(massSpawnVelocity));
        }
        blurShader.end();
        shapeRenderer.end();
        batch.end();
        neonTargetAFBO.end();

        //clear the background FBO
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.setProjectionMatrix(orthoCamera.combined);
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
