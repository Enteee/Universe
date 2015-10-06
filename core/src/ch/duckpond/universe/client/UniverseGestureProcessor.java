package ch.duckpond.universe.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import ch.duckpond.universe.shared.simulation.Globals;

/**
 * Gesture processor for multitouch environment.
 */
class UniverseGestureProcessor extends GestureDetector.GestureAdapter {
    private final Universe universe;
    private float lastInitialDistance = 0;
    private float lastDistance = 0;

    UniverseGestureProcessor(final Universe universe) {
        this.universe = universe;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (universe.isMassSpawning()) {
            return false;
        }
        if (lastInitialDistance != initialDistance) {
            lastDistance = distance;
        }
        float newZoom = universe.getCamera().zoom + (lastDistance - distance) * Globals.CAMERA_ZOOM_FACTOR;
        if (newZoom > Globals.CAMERA_ZOOM_MIN && newZoom < Globals.CAMERA_ZOOM_MAX) {
            universe.getCamera().zoom = newZoom;
            universe.getCamera().update();
        }
        lastDistance = distance;
        lastInitialDistance = initialDistance;
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        universe.spawnMass();
        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        final Vector3 massSpawnPoint3 = universe.getCamera().unproject(new Vector3(x, y, 0));
        universe.setMassSpawnPoint(new Vector2(
                massSpawnPoint3.x,
                massSpawnPoint3.y
        ));
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (!universe.isMassSpawning()) {
            // move camera
            universe.getCamera().translate(
                    new Vector3(-deltaX, deltaY, 0).scl(universe.getCamera().zoom)
            );
            universe.getCamera().update();
        } else {
            // set mass spawn velocity
            final Vector3 panPoint = universe.getCamera().unproject(new Vector3(x, y, 0));
            universe.setMassSpawnVelocity(new Vector2(
                    universe.getMassSpawnPoint().x - panPoint.x,
                    universe.getMassSpawnPoint().y - panPoint.y
            ));
        }
        return true;
    }

}