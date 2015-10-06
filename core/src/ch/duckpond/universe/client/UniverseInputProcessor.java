package ch.duckpond.universe.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

import ch.duckpond.universe.shared.simulation.Globals;
import javafx.scene.input.MouseButton;

/**
 * Universe input processor for non-touch environment.
 */
class UniverseInputProcessor extends InputAdapter {

    private final Universe universe;
    private Vector2 lastMousePos = new Vector2();

    UniverseInputProcessor(final Universe universe) {
        this.universe = universe;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        final Vector3 unprojectedPoint = universe.getCamera().unproject(
                new Vector3(screenX, screenY, 0)
        );
        Gdx.app.debug(getClass().getName(), String.format("mouseMoved %s", unprojectedPoint));
        lastMousePos = new Vector2(unprojectedPoint.x, unprojectedPoint.y);
        return true;
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        final Vector3 unprojectedPoint = universe.getCamera().unproject(
                new Vector3(screenX, screenY, 0)
        );
        Gdx.app.debug(getClass().getName(), String.format("touchDragged: %s", unprojectedPoint));
        if (!universe.isMassSpawning()) {
            // move camera
            final Vector2 touchDragged = new Vector2(unprojectedPoint.x, unprojectedPoint.y);
            final Vector2 dragMove = new Vector2(lastMousePos).sub(touchDragged);
            lastMousePos = touchDragged;
            universe.getCamera().translate(dragMove.x, dragMove.y);
            universe.getCamera().update();
        } else {
            // set spawn velocity
            final Vector3 panPoint = universe.getCamera().unproject(new Vector3(
                    screenX,
                    screenY,
                    0));
            universe.setMassSpawnVelocity(new Vector2(
                    universe.getMassSpawnPoint().x - panPoint.x,
                    universe.getMassSpawnPoint().y - panPoint.y
            ));
        }
        lastMousePos = new Vector2(unprojectedPoint.x, unprojectedPoint.y);
        return true;
    }

    @Override
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        final Vector3 unprojectedPoint = universe.getCamera().unproject(
                new Vector3(screenX, screenY, 0)
        );
        Gdx.app.debug(getClass().getName(), String.format("touchDown %s", unprojectedPoint));
        switch (button) {
            case Input.Buttons.LEFT:
                universe.setMassSpawnPoint(new Vector2(unprojectedPoint.x, unprojectedPoint.y));
                break;
        }
        return true;
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        universe.spawnMass();
        return true;
    }

    @Override
    public boolean scrolled(final int amount) {
        if (universe.isMassSpawning()) {
            return false;
        }
        float newZoom = universe.getCamera().zoom + (amount);
        if (newZoom > Globals.CAMERA_ZOOM_MIN && newZoom < Globals.CAMERA_ZOOM_MAX) {
            universe.getCamera().zoom = newZoom;
            universe.getCamera().update();
        }
        return true;
    }

}