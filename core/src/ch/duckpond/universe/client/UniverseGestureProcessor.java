package ch.duckpond.universe.client;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import ch.duckpond.universe.shared.simulation.Globals;

/**
 * Gesture processor for multitouch environment.
 *
 * @author ente
 */
class UniverseGestureProcessor extends GestureDetector.GestureAdapter {
    private float lastInitialDistance = 0;
    private float lastDistance = 0;

    @Override
    public boolean tap(float x, float y, int count, int button) {
        final Vector3 massSpawnPoint3 = Universe.getInstance().getCamera().unproject(new Vector3(x,
                                                                                                 y,
                                                                                                 0));
        Universe.getInstance().setMassSpawnPoint(new Vector2(massSpawnPoint3.x, massSpawnPoint3.y));
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (!Universe.getInstance().isMassSpawning()) {
            // move camera
            Vector3 move = Universe.getInstance().getCamera().unproject(new Vector3());
            Vector3 deltaMove = Universe.getInstance().getCamera().unproject(new Vector3(deltaX,
                                                                                         deltaY,
                                                                                         0));
            move.sub(deltaMove);
            Universe.getInstance().getCamera().translate(move);
            Universe.getInstance().getCamera().update();
        } else {
            // set mass spawn velocity
            final Vector3 panPoint = Universe.getInstance().getCamera().unproject(new Vector3(x,
                                                                                              y,
                                                                                              0));
            Universe.getInstance().setMassSpawnVelocity(new Vector2(Universe.getInstance().getMassSpawnPoint().x - panPoint.x,
                                                                    Universe.getInstance().getMassSpawnPoint().y - panPoint.y));
        }
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        Universe.getInstance().spawnMass();
        return true;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (Universe.getInstance().isMassSpawning()) {
            return false;
        }
        if (lastInitialDistance != initialDistance) {
            lastDistance = distance;
        }
        Universe.getInstance().getCamera().zoom = MathUtils.clamp(Universe.getInstance().getCamera().zoom + (lastDistance - distance) * Globals.CAMERA_ZOOM_FACTOR_GESTURE,
                                                    Globals.CAMERA_ZOOM_MIN,
                                                    Globals.CAMERA_ZOOM_MAX);
        Universe.getInstance().getCamera().update();
        lastDistance = distance;
        lastInitialDistance = initialDistance;
        return true;
    }

}