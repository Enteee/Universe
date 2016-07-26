//package ch.duckpond.universe.client.input;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Input;
//import com.badlogic.gdx.InputAdapter;
//import com.badlogic.gdx.math.MathUtils;
//import com.badlogic.gdx.math.Vector3;
//
//import ch.duckpond.universe.client.screen.GameScreen;
//import ch.duckpond.universe.shared.simulation.Globals;
//
///**
// * UniverseGame input processor for non-touch environment.
// *
// * @author ente
// */
//public class UniverseInputProcessor extends InputAdapter {
//    private final GameScreen gameScreen;
//    private Vector3 lastMousePosScreen = new Vector3();
//    private Vector3 lastMousePosWorld = new Vector3();
//    private boolean massSelected = false;
//
//    public UniverseInputProcessor(final GameScreen gameScreen) {
//        this.gameScreen = gameScreen;
//    }
//
//    @Override
//    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
//        setLastMousePosScreen(screenX, screenY);
//        Gdx.app.debug(getClass().getName(), String.format("touchDown %s", lastMousePosWorld));
//        switch (button) {
//            case Input.Buttons.LEFT: {
//                // check if clicked on mass
//                massSelected = gameScreen.setSelectPoint(lastMousePosWorld);
//                if (!massSelected) {
//                    gameScreen.setMassSpawnPointWorld(new Vector3(lastMousePosScreen.x,
//                                                                  lastMousePosScreen.y,
//                                                                  0));
//                }
//                break;
//            }
//        }
//        return true;
//    }
//
//    private void setLastMousePosScreen(final int screenX, final int screenY) {
//        setLastMousePosScreen(new Vector3(screenX, screenY, 0));
//    }
//
//    private void setLastMousePosScreen(final Vector3 lastMousePosScreen) {
//        this.lastMousePosScreen = new Vector3(lastMousePosScreen);
//        lastMousePosWorld = gameScreen.getCamera().unproject(new Vector3(lastMousePosScreen));
//    }
//
//    @Override
//    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
//        setLastMousePosScreen(screenX, screenY);
//        gameScreen.spawnBody();
//        massSelected = false;
//        return true;
//    }
//
//    @Override
//    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
//        final Vector3 dragPointScreen = new Vector3(screenX, screenY, 0);
//        final Vector3 dragPointWorld = gameScreen.getCamera().unproject(new Vector3(dragPointScreen));
//        Gdx.app.debug(getClass().getName(), String.format("touchDragged: %s", dragPointWorld));
//        if (!gameScreen.isMassSpawning() && !massSelected) {
//            // move camera
//            final Vector3 dragMove = new Vector3(lastMousePosWorld).sub(dragPointWorld);
//            gameScreen.getCamera().translate(dragMove);
//            gameScreen.getCamera().update();
//            gameScreen.setCenteredBody(null);
//        } else {
//            // set spawn velocity
//            gameScreen.setMassSpawnVelocity(new Vector3(dragPointScreen));
//        }
//        setLastMousePosScreen(dragPointScreen);
//        return true;
//    }
//
//    @Override
//    public boolean mouseMoved(final int screenX, final int screenY) {
//        setLastMousePosScreen(screenX, screenY);
//        //Gdx.app.debug(getClass().getName(), String.format("mouseMoved %s", lastMousePosWorld));
//        return true;
//    }
//
//    @Override
//    public boolean scrolled(final int amount) {
//        if (gameScreen.isMassSpawning()) {
//            return false;
//        }
//        // disable center adjusted zoom if we follow a body
//        if (gameScreen.getCenteredBody() == null) {
//            gameScreen.getCamera().translate(new Vector3(lastMousePosWorld).sub(gameScreen.getCamera().position));
//        }
//        gameScreen.getCamera().zoom = MathUtils.clamp(gameScreen.getCamera().zoom + amount * Globals.CAMERA_ZOOM_FACTOR_INPUT,
//                                                      Globals.CAMERA_ZOOM_MIN,
//                                                      Globals.CAMERA_ZOOM_MAX);
//
//        gameScreen.getCamera().update();
//        if (gameScreen.getCenteredBody() == null) {
//            final Vector3 screenMousePosUnprojected = gameScreen.getCamera().unproject(new Vector3(
//                    lastMousePosScreen));
//            gameScreen.getCamera().translate(new Vector3(lastMousePosWorld).sub(
//                    screenMousePosUnprojected));
//            gameScreen.getCamera().update();
//        }
//        return true;
//    }
//
//}