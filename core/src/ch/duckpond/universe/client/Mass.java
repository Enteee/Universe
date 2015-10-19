package ch.duckpond.universe.client;

import com.badlogic.gdx.utils.GdxRuntimeException;

import ch.duckpond.universe.client.circlemenu.CircleMenu;

/**
 * A mass in the game
 *
 * @author ente
 */
public class Mass {

    private Player owner;
    private CircleMenu circleMenu = new CircleMenu(this);

    /**
     * Mass owned by the local player
     */
    public Mass() {
        this(Universe.getInstance().getThisPlayer());
    }

    /**
     * @param owner Player this mass belongs to
     */
    public Mass(final Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        if (owner == null) {
            throw new GdxRuntimeException("Owner == null");
        }
        this.owner = owner;
    }
}
