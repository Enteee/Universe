package ch.duckpond.universe.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import ch.duckpond.universe.client.Universe;
import ch.duckpond.universe.shared.simulation.Globals;

/**
 * Universe input processor for non-touch environment.
 *
 * @author ente
 */
public class UniverseInputProcessor extends InputAdapter {
    private Vector3 lastMousePosScreen = new Vector3();
    private Vector3 lastMousePosWorld = new Vector3();
    private boolean massSelected = false;

    @Override
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        setLastMousePosScreen(screenX, screenY);
        Gdx.app.debug(getClass().getName(), String.format("touchDown %s", lastMousePosWorld));
        switch (button) {
            case Input.Buttons.LEFT: {
                // check if clicked on mass
                massSelected = Universe.getInstance().setSelectPoint(lastMousePosWorld);
                if (!massSelected) {
                    Universe.getInstance().setMassSpawnPoint(new Vector2(lastMousePosWorld.x,
                                                                         lastMousePosWorld.y));
                }
                break;
            }
        }
        return true;
    }

    private void setLastMousePosScreen(final int screenX, final int screenY) {
        setLastMousePosScreen(new Vector3(screenX, screenY, 0));
    }

    private void setLastMousePosScreen(final Vector3 lastMousePosScreen) {
        this.lastMousePosScreen = new Vector3(lastMousePosScreen);
        lastMousePosWorld = Universe.getInstance().getCamera().unproject(new Vector3(
                lastMousePosScreen));
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        setLastMousePosScreen(screenX, screenY);
        Universe.getInstance().spawnMass();
        massSelected = false;
        return true;
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        final Vector3 dragPointWorld = Universe.getInstance().getCamera().unproject(new Vector3(
                screenX,
                screenY,
                0));
        Gdx.app.debug(getClass().getName(), String.format("touchDragged: %s", dragPointWorld));
        if (!Universe.getInstance().isMassSpawning() && !massSelected) {
            // move camera
            final Vector3 dragMove = new Vector3(lastMousePosWorld).sub(dragPointWorld);
            Universe.getInstance().getCamera().translate(dragMove);
            Universe.getInstance().getCamera().update();
            Universe.getInstance().setCenteredBody(null);
        } else {
            // set spawn velocity
            Universe.getInstance().setMassSpawnVelocity(new Vector2(Universe.getInstance().getMassSpawnPoint().x - dragPointWorld.x,
                                                                    Universe.getInstance().getMassSpawnPoint().y - dragPointWorld.y));
        }
        setLastMousePosScreen(screenX, screenY);
        return true;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        setLastMousePosScreen(screenX, screenY);
        //Gdx.app.debug(getClass().getName(), String.format("mouseMoved %s", lastMousePosWorld));
        return true;
    }

    @Override
    public boolean scrolled(final int amount) {
        if (Universe.getInstance().isMassSpawning()) {
            return false;
        }
        // disable center adjusted zoom if we follow a body
        if (Universe.getInstance().getCenteredBody() == null) {
            Universe.getInstance().getCamera().translate(new Vector3(lastMousePosWorld).sub(Universe.getInstance().getCamera().position));
        }
        Universe.getInstance().getCamera().zoom = MathUtils.clamp(Universe.getInstance().getCamera().zoom + amount * Globals.CAMERA_ZOOM_FACTOR_INPUT,
                                                                  Globals.CAMERA_ZOOM_MIN,
                                                                  Globals.CAMERA_ZOOM_MAX);

        Universe.getInstance().getCamera().update();
        if (Universe.getInstance().getCenteredBody() == null) {
            final Vector3 screenMousePosUnprojected = Universe.getInstance().getCamera().unproject(
                    new Vector3(lastMousePosScreen));
            Universe.getInstance().getCamera().translate(new Vector3(lastMousePosWorld).sub(
                    screenMousePosUnprojected));
            Universe.getInstance().getCamera().update();
        }
        return true;
    }

}