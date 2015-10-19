package ch.duckpond.universe.client;


import com.badlogic.gdx.graphics.Color;

/**
 * A player in the game
 *
 * @author ente
 */
public class Player {
    private final Color color;

    public Player(final Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
