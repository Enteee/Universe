//package ch.duckpond.universe.client.input;
//
//import com.badlogic.gdx.input.GestureDetector;
//import com.badlogic.gdx.math.MathUtils;
//import com.badlogic.gdx.math.Vector3;
//
//import ch.duckpond.universe.client.screen.GameScreen;
//import ch.duckpond.universe.shared.simulation.Globals;
//
///**
// * Gesture processor for multitouch environment.
// *
// * @author ente
// */
//public class UniverseGestureProcessor extends GestureDetector.GestureAdapter {
//    private final GameScreen gameScreen;
//    private float lastInitialDistance = 0;
//    private float lastDistance = 0;
//
//    public UniverseGestureProcessor(final GameScreen gameScreen) {
//        this.gameScreen = gameScreen;
//    }
//
//    @Override
//    public boolean tap(float x, float y, int count, int button) {
//        final Vector3 massSpawnPoint3 = gameScreen.getCamera().unproject(new Vector3(x, y, 0));
//        gameScreen.setMassSpawnPointWorld(new Vector3(massSpawnPoint3.x, massSpawnPoint3.y, 0));
//        return true;
//    }
//
//    @Override
//    public boolean pan(float x, float y, float deltaX, float deltaY) {
//        if (!gameScreen.isMassSpawning()) {
//            // move camera
//            Vector3 move = gameScreen.getCamera().unproject(new Vector3());
//            Vector3 deltaMove = gameScreen.getCamera().unproject(new Vector3(deltaX, deltaY, 0));
//            move.sub(deltaMove);
//            gameScreen.getCamera().translate(move);
//            gameScreen.getCamera().update();
//        } else {
//            // set mass spawn velocity
//            final Vector3 panPoint = gameScreen.getCamera().unproject(new Vector3(x, y, 0));
//            gameScreen.setMassSpawnVelocityWorld(new Vector3(gameScreen.getMassSpawnPointWorld().x - panPoint.x,
//                                                             gameScreen.getMassSpawnPointWorld().y - panPoint.y,
//                                                             0));
//        }
//        return true;
//    }
//
//    @Override
//    public boolean panStop(float x, float y, int pointer, int button) {
//        gameScreen.spawnBody();
//        return true;
//    }
//
//    @Override
//    public boolean zoom(float initialDistance, float distance) {
//        if (gameScreen.isMassSpawning()) {
//            return false;
//        }
//        if (lastInitialDistance != initialDistance) {
//            lastDistance = distance;
//        }
//        gameScreen.getCamera().zoom = MathUtils.clamp(gameScreen.getCamera().zoom + (lastDistance - distance) * Globals.CAMERA_ZOOM_FACTOR_GESTURE,
//                                                      Globals.CAMERA_ZOOM_MIN,
//                                                      Globals.CAMERA_ZOOM_MAX);
//        gameScreen.getCamera().update();
//        lastDistance = distance;
//        lastInitialDistance = initialDistance;
//        return true;
//    }
//
//}