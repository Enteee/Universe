package ch.duckpond.universe.client;


import com.badlogic.gdx.graphics.Color;

import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * A player in the game
 *
 * @author ente
 */
public class Player {
    public static final int KEEP_LAST_ENERGIES_COUNT = 500;

    private final Color color;
    private final CircularFifoQueue<Float> energies = new CircularFifoQueue<>(
            KEEP_LAST_ENERGIES_COUNT);
    private float energySubtractor = 0;

    public Player(final Color color) {
        this.color = color;
        // add base energy, so that we  won't get a NULL on Player#getEnergy()
        addEnergy(0f);
    }

    public void addEnergy(float energy) {
        energies.add(energy - energySubtractor);
    }

    public void addEnergySubtractor(float energySubtractor) {
        this.energySubtractor += energySubtractor;
    }

    public Color getColor() {
        return color;
    }

    public float getEnergy() {
        return Math.max(0, energies.get(energies.size() - 1));
    }

}
