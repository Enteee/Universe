package ch.duckpond.universe.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import ch.duckpond.universe.shared.simulation.Globals;

/**
 * Universe input processor for non-touch environment.
 *
 * @author ente
 */
class UniverseInputProcessor extends InputAdapter {

    private final Universe universe;
    private Vector3 lastMousePosScreen = new Vector3();
    private Vector3 lastMousePosWorld = new Vector3();

    UniverseInputProcessor(final Universe universe) {
        this.universe = universe;
    }

    @Override
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        setLastMousePosScreen(screenX, screenY);
        Gdx.app.debug(getClass().getName(), String.format("touchDown %s", lastMousePosWorld));
        switch (button) {
            case Input.Buttons.LEFT:
                universe.setMassSpawnPoint(new Vector2(lastMousePosWorld.x, lastMousePosWorld.y));
                break;
        }
        return true;
    }

    private void setLastMousePosScreen(final int screenX, final int screenY) {
        setLastMousePosScreen(new Vector3(screenX, screenY, 0));
    }

    private void setLastMousePosScreen(final Vector3 lastMousePosScreen) {
        this.lastMousePosScreen = new Vector3(lastMousePosScreen);
        lastMousePosWorld = universe.getCamera().unproject(new Vector3(lastMousePosScreen));
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        setLastMousePosScreen(screenX, screenY);
        universe.spawnMass();
        return true;
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        final Vector3 dragPointWorld = universe.getCamera().unproject(new Vector3(screenX,
                                                                                  screenY,
                                                                                  0));
        Gdx.app.debug(getClass().getName(), String.format("touchDragged: %s", dragPointWorld));
        if (!universe.isMassSpawning()) {
            // move camera
            final Vector3 dragMove = new Vector3(lastMousePosWorld).sub(dragPointWorld);
            universe.getCamera().translate(dragMove);
            universe.getCamera().update();
        } else {
            // set spawn velocity
            universe.setMassSpawnVelocity(new Vector2(universe.getMassSpawnPoint().x - dragPointWorld.x,
                                                      universe.getMassSpawnPoint().y - dragPointWorld.y));
        }
        setLastMousePosScreen(screenX, screenY);
        return true;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        setLastMousePosScreen(screenX, screenY);
        Gdx.app.debug(getClass().getName(), String.format("mouseMoved %s", lastMousePosWorld));
        return true;
    }

    @Override
    public boolean scrolled(final int amount) {
        if (universe.isMassSpawning()) {
            return false;
        }
        universe.getCamera().translate(new Vector3(lastMousePosWorld).sub(universe.getCamera().position));
        universe.getCamera().zoom = MathUtils.clamp(universe.getCamera().zoom + amount,
                                                    Globals.CAMERA_ZOOM_MIN,
                                                    Globals.CAMERA_ZOOM_MAX);

        universe.getCamera().update();
        final Vector3 screenMousePosUnprojected = universe.getCamera().unproject(new Vector3(
                lastMousePosScreen));
        Gdx.app.debug(getClass().getName(),
                      String.format("lmp: %s, campos: %s , smp: %s",
                                    lastMousePosWorld,
                                    universe.getCamera().position,
                                    screenMousePosUnprojected));
        universe.getCamera().translate(new Vector3(lastMousePosWorld).sub(screenMousePosUnprojected));
        universe.getCamera().update();
        return true;
    }

}