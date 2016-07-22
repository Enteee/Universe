package ch.duckpond.universe.client;


import com.badlogic.gdx.graphics.Color;

import java.util.Deque;
import java.util.LinkedList;

import ch.duckpond.universe.shared.simulation.Globals;

/**
 * A player in the game
 *
 * @author ente
 */
public class Player {
    private final Color color;
    private final Deque<Float> energies = new LinkedList();

    public Player(final Color color) {
        this.color = color;
        // add base energy, so that we  won't get a NULL on Player#getEnergy()
        addEnergy(0f);
    }

    public void addEnergy(float energy) {
        energies.addFirst(energy);
        if (energies.size() > Globals.KEEP_LAST_ENERGIES_COUNT) {
            energies.removeLast();
        }
    }

    public Color getColor() {
        return color;
    }

    public float getEnergy() {
        return energies.getFirst();
    }

}
