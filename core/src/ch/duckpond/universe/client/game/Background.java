package ch.duckpond.universe.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;

import ch.duckpond.universe.shared.simulation.Globals;

/**
 * The universe background
 *
 * @author ente
 */
public class Background extends Actor {

    final OrthographicCamera camera;
    private final GameScreen gameScreen;
    private boolean massSpawning = false;
    private Vector3 massSpawnPointActor = new Vector3();
    private Vector3 massSpawnVelocity = new Vector3();

    private Vector3 debugLastZoomPoint = new Vector3();
    private Vector3 debugLastZoomPointWorld = new Vector3();

    protected Background(final GameScreen gameScreen) {
        assert gameScreen != null;

        this.gameScreen = gameScreen;
        camera = this.gameScreen.getCamera();

        // send to background
        setBounds(0,
                  0,
                  gameScreen.getWorldStage().getWidth(),
                  gameScreen.getWorldStage().getHeight());
        setZIndex(Globals.Z_INDEX_BACKGROUND);

        // add input listener
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) {
            addListener(new BackgroundGestureProcessor());
        } else {
            addListener(new BackgroundInputListener());
        }
        updateBounds();
    }

    public void updateBounds() {
        setBounds(camera.position.x - camera.viewportWidth / 2 * camera.zoom,
                  camera.position.y - camera.viewportHeight / 2 * camera.zoom,
                  camera.viewportWidth * camera.zoom,
                  camera.viewportHeight * camera.zoom);
    }

    /**
     * Starts the spawn process of a new mass
     *
     * @param massSpawnPointActor the spawn location in actor coordinates
     */
    private void setMassSpawnPoint(final Vector3 massSpawnPointActor) {
        this.massSpawnPointActor = new Vector3(massSpawnPointActor);
        this.massSpawnVelocity = new Vector3();
        massSpawning = true;
    }

    /**
     * Sets the velocity point of a spawning mass
     *
     * @param massSpawnVelocity velocity in actor coordinates
     */
    private void setMassSpawnVelocity(final Vector3 massSpawnVelocity) {
        this.massSpawnVelocity = new Vector3(massSpawnVelocity);
    }

    /**
     * Spwan the mass which was previousely defined with {@link Background#setMassSpawnPoint
     * (Vector3)} and {@link Background#setMassSpawnVelocity(Vector3)}
     */
    private void spawnMass() {
        if (massSpawning) {
            Gdx.app.debug(getClass().getName(), "Mass spawned");
            final Mass mass = new Mass(gameScreen);
            final Body body = gameScreen.getSimulation().spawnBody(toWorldCoordinates(
                    massSpawnPointActor),
                                                                   Globals.MASS_SPAWN_RADIUS * camera.zoom,
                                                                   massSpawnVelocity,
                                                                   mass);
            gameScreen.getWorldStage().addActor(mass);

            massSpawnVelocity = new Vector3();
            massSpawning = false;
        }
    }

    private Vector3 toWorldCoordinates(final Vector3 actorCoordinates) {
        return new Vector3(actorCoordinates.x + getX(), actorCoordinates.y + getY(), 0);
    }

    /**
     * Set the new camera zoom focused on zoomPointActor
     *
     * @param zoom           the new zoom to set
     * @param zoomPointActor the point to keep static when zooming
     */
    private void zoom(final float zoom, final Vector3 zoomPointActor) {
        final Vector3 zoomPointWorld = toWorldCoordinates(zoomPointActor);
        final float newCameraZoom = MathUtils.clamp(zoom,
                                                    Globals.CAMERA_ZOOM_MIN,
                                                    Globals.CAMERA_ZOOM_MAX);
        final float zoomCorrection = newCameraZoom - camera.zoom;
        final Vector3 cameraToZoomPointWorld = new Vector3(zoomPointWorld).sub(camera.position);
        final Vector3 cameraCorrection = new Vector3(cameraToZoomPointWorld).scl(-zoomCorrection / camera.zoom);

        /*
         *enable mouse centric zoom if:
         * 1. we don't follow a body
         * 2. zoom has changed
         */
        if (gameScreen.getCenteredBody() == null && newCameraZoom != camera.zoom) {
            camera.translate(cameraCorrection);
        }
        camera.zoom = newCameraZoom;
        camera.update();
        updateBounds();

        debugLastZoomPoint = zoomPointActor;
        debugLastZoomPointWorld = toWorldCoordinates(zoomPointActor);
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        shapes.x(debugLastZoomPointWorld.x, debugLastZoomPointWorld.y, 4);

        shapes.setColor(Color.BLUE);
        shapes.circle(getX() + debugLastZoomPoint.x, getY() + debugLastZoomPoint.y, 2);
        shapes.x(getX() + debugLastZoomPoint.x, getY() + debugLastZoomPoint.y, 2);

        // draw spawning mass
        if (massSpawning) {
            final Vector3 massSpawnPointWorld = toWorldCoordinates(massSpawnPointActor);
            shapes.circle(massSpawnPointWorld.x,
                          massSpawnPointWorld.y,
                          Globals.MASS_SPAWN_RADIUS * camera.zoom);
            shapes.line(massSpawnPointWorld,
                        new Vector3(massSpawnPointWorld).sub(massSpawnVelocity));
        }
    }

    /**
     * Input listener for non-touch environment.
     *
     * @author ente
     */
    private class BackgroundInputListener extends InputListener {
        private Vector3 touchDownPos = new Vector3();
        private Vector3 dragPoint = new Vector3();

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            touchDownPos = new Vector3(x, y, 0);
            dragPoint = new Vector3(x, y, 0);
            Gdx.app.debug(getClass().getName(), String.format("touchDown %s", touchDownPos));
            switch (button) {
                case Input.Buttons.LEFT: {
                    setMassSpawnPoint(touchDownPos);
                    break;
                }
            }
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            final Vector3 touchUpPoint = new Vector3(x, y, 0);
            Gdx.app.debug(getClass().getName(), String.format("touchUp: %s", touchUpPoint));
            setMassSpawnVelocity(new Vector3(touchDownPos).sub(touchUpPoint));
            spawnMass();
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            final Vector3 newDragPoint = new Vector3(x, y, 0);
            Gdx.app.debug(getClass().getName(), String.format("touchDragged: %s", dragPoint));
            if (massSpawning) {
                // set spawn velocity
                setMassSpawnVelocity(new Vector3(touchDownPos).sub(dragPoint));
            } else {
                // move camera
                // TODO: for some reason this is wrong but works (should be newDragPoint -
                // dragPoint): investigate!
                final Vector3 dragMove = new Vector3(dragPoint).sub(newDragPoint);
                camera.translate(dragMove);
                camera.update();

                gameScreen.setCenteredBody(null);
                updateBounds();
            }
            dragPoint = newDragPoint;
        }

        @Override
        public boolean scrolled(InputEvent event, float x, float y, int amount) {
            Gdx.app.debug(getClass().getName(), String.format("scrolled: %d", amount));
            if (massSpawning) {
                return false;
            }
            zoom(camera.zoom + amount * Globals.CAMERA_ZOOM_FACTOR_INPUT, new Vector3(x, y, 0));
            return true;
        }

    }

    class BackgroundGestureProcessor extends ActorGestureListener {
        // TODO: implement
    }
}
