package ch.duckpond.universe.client.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import ch.duckpond.universe.client.Mass;
import ch.duckpond.universe.client.NeonColors;
import ch.duckpond.universe.client.Player;
import ch.duckpond.universe.client.scene.Hud;
import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;

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

    private final Stage worldStage;
    private final Viewport worldViewport;
    private final Stage staticStage;
    private final Viewport staticViewport;

    private final Hud hud;

    private Player thisPlayer = new Player(new Color(NeonColors.getRandomColor().getColorRGB888()));
    private Simulation simulation;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    /**
     * Camera only used to render final framebuffers with shaders
     */
    private OrthographicCamera fbCamera;
    private FrameBuffer neonTargetAFBO;
    private ShaderProgram glowShader;

    private int level = 1;

    private boolean massSpawning = false;
    private Vector3 massSpawnPointWorld = new Vector3();
    private Vector3 massSpawnVelocityWorld = new Vector3();

    /**
     * Body on which to center the view.
     */
    private Body centeredBody;

    public GameScreen(final InputMultiplexer inputMultiplexer) {
        // add input
        //        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) {
        //            inputMultiplexer.addProcessor(new GestureDetector(new UniverseGestureProcessor
        //            // (this)));
        //        } else {
        //            inputMultiplexer.addProcessor(new UniverseInputProcessor(this));
        //        }

        // simulation set up
        simulation = new Simulation(this);

        // set up rendering
        batch = new SpriteBatch();
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

        worldStage.addListener(new GameScreenInputListener());

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

        //initialize hud
        hud = new Hud(this, staticStage);
    }

    /**
     * Starts the spawn process of a new mass
     *
     * @param massSpawnPointWorld the spawn location in screen coordinates
     */
    private void setMassSpawnPointWorld(final Vector3 massSpawnPointWorld) {
        this.massSpawnPointWorld = new Vector3(massSpawnPointWorld);
        this.massSpawnVelocityWorld = new Vector3(massSpawnPointWorld);
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

    public Stage getWorldStage() {
        return worldStage;
    }

    public Stage getStaticStage() {
        return staticStage;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Sets the velocity point of a spawning mass
     *
     * @param massSpawnVelocityWorld velocity in screen coordinates
     */
    public void setMassSpawnVelocityWorld(final Vector3 massSpawnVelocityWorld) {
        this.massSpawnVelocityWorld = new Vector3(massSpawnVelocityWorld);
    }

    /**
     * Spwan the mass which was previousely defined with {@link GameScreen#setMassSpawnPointWorld
     * (Vector3)} and {@link GameScreen#setMassSpawnVelocityWorld(Vector3)}
     */
    public void spawnMass() {
        if (isMassSpawning()) {
            final Body body = simulation.spawnBody(massSpawnPointWorld,
                                                   Globals.MASS_SPAWN_RADIUS * camera.zoom,
                                                   new Vector3(massSpawnPointWorld).sub(
                                                           massSpawnVelocityWorld));
            final Mass mass = new Mass(this);
            mass.setBody(body);
            body.setUserData(mass);

            worldStage.addActor(mass);

            massSpawnVelocityWorld = new Vector3();
            massSpawning = false;
        }
    }

    public boolean isMassSpawning() {
        return massSpawning;
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
    public void show() {

    }

    @Override
    public void render(float delta) {

        // do a simulation step. Do this first so that we can assume that most structures are
        // already populated with at lest one value.
        simulation.update();

        //clear the background FBO
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //        // center camera on centered body
        //        if (centeredBody != null) {
        //            final Vector3 centerTranslate = new Vector3(centeredBody.getPosition().x,
        //                                                        centeredBody.getPosition().y,
        //                                                        0f).sub(camera.position).scl(Globals.CAMERA_CENTER_FACTOR);
        //            camera.translate(centerTranslate);
        //            camera.update();
        //        }
        //
        //        final ShapeRenderer shapeRenderer = BatchUtils.buildShapeRendererFromBatch(batch);
        //
        //        // bind the neonTargetAFBO
        //        neonTargetAFBO.begin();
        //        {
        //            Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        //            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //
        //            // draw masses background (black & full alpha for neon effect speedup)
        //            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        //            {
        //                shapeRenderer.setProjectionMatrix(camera.combined);
        //
        //                // draw masses backgrounds
        //                for (final Body body : simulation.getBodies()) {
        //                    // outer glow border
        //                    for (final Fixture fixture : body.getFixtureList()) {
        //                        final CircleShape circleShape = (CircleShape) fixture.getShape();
        //                        shapeRenderer.setColor(new Color(0f, 0f, 0f, 1f));
        //                        shapeRenderer.circle(body.getPosition().x,
        //                                             body.getPosition().y,
        //                                             circleShape.getRadius() + GLOW_SAMPLES / 2f * GLOW_QUALITY);
        //                    }
        //                }
        //            }
        //            shapeRenderer.end();
        //
        //            worldStage.draw();
        //
        //            // draw spawning mass
        //            if (isMassSpawning()) {
        //                Gdx.app.debug(getClass().getName(),
        //                              String.format("massSpawnPointWorld: %s", massSpawnPointWorld));
        //                Gdx.app.debug(getClass().getName(),
        //                              String.format("massSpawnVelocityWorld: %s", massSpawnVelocityWorld));
        //
        //                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        //                {
        //                    shapeRenderer.setProjectionMatrix(camera.combined);
        //                    shapeRenderer.setColor(new Color(0f, 1f, 0f, 1f));
        //                    shapeRenderer.circle(massSpawnPointWorld.x,
        //                                         massSpawnPointWorld.y,
        //                                         Globals.MASS_SPAWN_RADIUS * camera.zoom);
        //                    shapeRenderer.line(massSpawnPointWorld, massSpawnVelocityWorld);
        //                }
        //                shapeRenderer.end();
        //            }
        //
        //            // draw (0,0) refernce
        //            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        //            {
        //                shapeRenderer.setProjectionMatrix(camera.combined);
        //                shapeRenderer.setColor(new Color(0f, 1f, 0f, 1f));
        //                shapeRenderer.x(0, 0, Globals.MASS_SPAWN_RADIUS * camera.zoom);
        //            }
        //            shapeRenderer.end();
        //
        //        }
        //        neonTargetAFBO.end();
        //

        neonTargetAFBO.begin();
        {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            worldStage.draw();
        }
        neonTargetAFBO.end();

        batch.begin();
        {
            batch.setProjectionMatrix(fbCamera.combined);

            batch.setShader(glowShader);
            glowShader.setUniformf("size", neonTargetAFBO.getWidth(), neonTargetAFBO.getHeight());
            glowShader.setUniformi("samples", GLOW_SAMPLES);
            glowShader.setUniformf("quality", GLOW_QUALITY);
            glowShader.setUniformf("intensity", GLOW_INTENSITY);
            batch.draw(neonTargetAFBO.getColorBufferTexture(),
                       worldViewport.getScreenX(),
                       worldViewport.getScreenY());
        }
        batch.end();

        staticViewport.apply();
        staticStage.draw();

    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height);
        staticViewport.update(width, height);

        neonTargetAFBO = new FrameBuffer(Pixmap.Format.RGBA8888,
                                         worldViewport.getScreenWidth(),
                                         worldViewport.getScreenHeight(),
                                         false);
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

    }

    /**
     * Input listener for non-touch environment.
     *
     * @author ente
     */
    public class GameScreenInputListener extends InputListener {
        private Vector3 touchDownPosWorld = new Vector3();

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            touchDownPosWorld = new Vector3(x, y, 0);
            Gdx.app.debug(getClass().getName(), String.format("touchDown %s", touchDownPosWorld));
            switch (button) {
                case Input.Buttons.LEFT: {
                    setMassSpawnPointWorld(touchDownPosWorld);
                    break;
                }
            }
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            setMassSpawnVelocityWorld(new Vector3(x, y, 0));
            spawnMass();
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            final Vector3 dragPointWorld = new Vector3(x, y, 0);
            Gdx.app.debug(getClass().getName(), String.format("touchDragged: %s", dragPointWorld));
            if (!isMassSpawning()) {
                // move camera
                final Vector3 dragMove = new Vector3(touchDownPosWorld).sub(dragPointWorld);
                camera.translate(dragMove);
                camera.update();
                centeredBody = null;
            } else {
                // set spawn velocity
                setMassSpawnVelocityWorld(dragPointWorld);
            }
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
            if (isMassSpawning()) {
                return false;
            }
            final float zoomCorrection = amount * Globals.CAMERA_ZOOM_FACTOR_INPUT;
            final float newCameraZoom = MathUtils.clamp(camera.zoom + zoomCorrection,
                                                        Globals.CAMERA_ZOOM_MIN,
                                                        Globals.CAMERA_ZOOM_MAX);

            final Vector3 cameraToMouseWorld = new Vector3(x, y, 0).sub(camera.position);
            final Vector3 cameraCorrection = new Vector3(cameraToMouseWorld).scl(-zoomCorrection / camera.zoom);

            /*
             *enable mouse centric zoom if:
             * 1. we don't follow a body
             * 2. zoom has changed
             */
            if (centeredBody == null && newCameraZoom != camera.zoom) {
                camera.translate(cameraCorrection);
            }
            camera.zoom = newCameraZoom;
            camera.update();

            return true;
        }

    }
}
