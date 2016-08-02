package ch.duckpond.universe.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;
import ch.duckpond.universe.utils.libgdx.BatchUtils;

/**
 * Main game screen.
 */
public class GameScreen implements Screen {

    /**
     * Glow filter settings
     */
    public static final int GLOW_SAMPLES = 7;
    public static final float GLOW_QUALITY = 2.5f;
    public static final float GLOW_INTENSITY = 2f;
    /**
     * Important actors
     */
    final Background background;
    final Hud hud;
    private final Stage worldStage;
    private final Viewport worldViewport;
    private final Stage staticStage;
    private final Viewport staticViewport;
    private Simulation simulation;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    /**
     * Camera only used to render final framebuffers with shaders
     */
    private OrthographicCamera fbCamera;
    private FrameBuffer neonTargetAFBO;
    private ShaderProgram glowShader;
    /**
     * Body on which to center the view.
     */
    private Body centeredBody;

    public GameScreen(final InputMultiplexer inputMultiplexer) {
        // simulation set up
        simulation = new Simulation(this);

        // set up rendering
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        fbCamera = new OrthographicCamera();

        // initialize viewports & stages
        worldViewport = new FitViewport(Globals.WORLD_V_WIDTH, Globals.WORLD_V_HEIGTH, camera);
        worldStage = new Stage(worldViewport, batch);
        inputMultiplexer.addProcessor(worldStage);

        staticViewport = new FitViewport(Globals.STATIC_V_WIDTH, Globals.STATIC_V_HEIGTH,
                                         new OrthographicCamera());
        staticStage = new Stage(staticViewport, batch);
        inputMultiplexer.addProcessor(staticStage);

        // create frame buffers
        neonTargetAFBO = new FrameBuffer(Pixmap.Format.RGBA8888,
                                         worldViewport.getScreenWidth(),
                                         worldViewport.getScreenHeight(),
                                         false);
        glowShader = new ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"),
                                       Gdx.files.internal("shaders/glowshader.frag"));
        if (!glowShader.isCompiled()) {
            throw new GdxRuntimeException(String.format("Shader not compiled: %s",
                                                        glowShader.getLog()));
        }

        glowShader.begin();
        {
            glowShader.setUniformf("size", neonTargetAFBO.getWidth(), neonTargetAFBO.getHeight());
            glowShader.setUniformi("samples", GLOW_SAMPLES);
            glowShader.setUniformf("quality", GLOW_QUALITY);
            glowShader.setUniformf("intensity", GLOW_INTENSITY);
            batch.setShader(glowShader);
        }
        glowShader.end();

        //initialize hud
        hud = new Hud(this);
        staticStage.addActor(hud);

        // initialize background
        background = new Background(this);
        worldStage.addActor(background);
        worldStage.setScrollFocus(background);
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public Body getCenteredBody() {
        return centeredBody;
    }

    public void setCenteredBody(final Body centeredBody) {
        this.centeredBody = centeredBody;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Stage getWorldStage() {
        return worldStage;
    }

    public Stage getStaticStage() {
        return staticStage;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public Background getBackground() {
        return background;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float deltaTime) {

        // do simulation steps needed
        // Do this first so that we can assume that most structures are already populated with
        // at lest one value.
        simulation.update(deltaTime);

        // center camera on centered body
        if (centeredBody != null) {
            final Vector3 centerTranslate = new Vector3(centeredBody.getPosition().x,
                                                        centeredBody.getPosition().y,
                                                        0f).sub(camera.position).scl(Globals.CAMERA_CENTER_FACTOR);
            camera.translate(centerTranslate);
            camera.update();
            background.updateBounds();
        }

        neonTargetAFBO.begin();
        {
            Gdx.gl.glClearColor(Globals.WORLD_BACKGROUND_COLOR.r,
                                Globals.WORLD_BACKGROUND_COLOR.g,
                                Globals.WORLD_BACKGROUND_COLOR.b,
                                Globals.WORLD_BACKGROUND_COLOR.a);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


            // draw (0,0) reference
            BatchUtils.syncShapeRendererWithBatch(batch, shapeRenderer);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            {
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.setTransformMatrix(camera.combined);
                shapeRenderer.x(0, 0, 10);
            }
            shapeRenderer.end();

            worldStage.draw();
            staticStage.draw();
        }
        neonTargetAFBO.end();

        // clear background
        Gdx.gl.glClearColor(Globals.WORLD_BACKGROUND_COLOR.r,
                            Globals.WORLD_BACKGROUND_COLOR.g,
                            Globals.WORLD_BACKGROUND_COLOR.b,
                            Globals.WORLD_BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        {
            batch.setProjectionMatrix(fbCamera.combined);
            batch.draw(neonTargetAFBO.getColorBufferTexture(),
                       worldViewport.getScreenX(),
                       worldViewport.getScreenY());
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height);
        staticViewport.update(width, height);

        neonTargetAFBO = new FrameBuffer(Pixmap.Format.RGBA8888,
                                         worldViewport.getScreenWidth(),
                                         worldViewport.getScreenHeight(),
                                         false);
        glowShader.begin();
        {
            glowShader.setUniformf("size", neonTargetAFBO.getWidth(), neonTargetAFBO.getHeight());
        }
        glowShader.end();

        Gdx.app.debug(getClass().getName(),
                      String.format("Viewport (%d x %d)",
                                    worldViewport.getScreenWidth(),
                                    worldViewport.getScreenHeight()));
        fbCamera.setToOrtho(true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        worldStage.dispose();
        staticStage.dispose();

        neonTargetAFBO.dispose();
        glowShader.dispose();
    }
}
