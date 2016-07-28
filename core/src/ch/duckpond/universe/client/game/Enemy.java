package ch.duckpond.universe.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

import ch.duckpond.universe.client.Player;
import ch.duckpond.universe.shared.simulation.Globals;

/**
 * An enemy player
 *
 * @author ente
 */
public class Enemy extends Player {
    /**
     * Mean of mass spawn interdistance in seconds.
     */
    public static final float SPAWN_MASS_INTERDISTANCE_MEAN = 10;
    /**
     * Standard deviation of mass spawn interdistance  in seconds.
     */
    public static final float SPAWN_MASS_INTERDISTANCE_STANDARD_DEVIATION = 1;
    /**
     * Disables enemy actions
     */
    private static boolean ENEMY_ACTION = true;
    private final GameScreen gameScreen;
    private float nextMassSpawnTime = 0;

    public Enemy(final GameScreen gameScreen, final Color color) {
        super(color);

        assert gameScreen != null;
        this.gameScreen = gameScreen;
        rollNextMassSpawnTime(0);
    }

    private void rollNextMassSpawnTime(final float time) {
        // draw from sample normal distribution
        final double sample = Globals.RANDOM.nextGaussian() * SPAWN_MASS_INTERDISTANCE_STANDARD_DEVIATION + SPAWN_MASS_INTERDISTANCE_MEAN;
        nextMassSpawnTime = time + (float) sample;
        Gdx.app.debug(getClass().getName(), String.format("mass spawning in %f seconds", sample));
    }

    public void act(final float time) {
        if (!ENEMY_ACTION) return;
        if (nextMassSpawnTime < time) {
            // spawn a mass: not visible by player
            final float massSpawnRadius = 1;
            final Camera camera = gameScreen.getCamera();
            final Vector3 spawnPosition = new Vector3(camera.viewportWidth / 2 + massSpawnRadius,
                                                      camera.viewportHeight / 2 + massSpawnRadius,
                                                      0).rotate(new Vector3(0, 0, 1),
                                                                360 * Globals.RANDOM.nextFloat()).add(
                    camera.position);
            final Mass mass = new Mass(gameScreen, this);
            final Body body = gameScreen.getSimulation().spawnBody(spawnPosition,
                                                                   massSpawnRadius,
                                                                   new Vector3(),
                                                                   mass);
            gameScreen.getWorldStage().addActor(mass);

            Gdx.app.debug(getClass().getName(),
                          String.format("Mass spawned, position %s", spawnPosition));

            rollNextMassSpawnTime(time);
        }
    }
}
