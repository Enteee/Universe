package ch.duckpond.universe.client;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Deque;
import java.util.LinkedList;

import ch.duckpond.universe.client.circlemenu.CircleMenu;
import ch.duckpond.universe.shared.simulation.Globals;

/**
 * A mass in the game
 *
 * @author ente
 */
public class Mass {

    private final CircleMenu circleMenu = new CircleMenu(this);
    private final Deque<Vector3> lastPositions = new LinkedList();
    private Player owner;

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

    public void setOwner(final Player owner) {
        if (owner == null) {
            throw new GdxRuntimeException("Owner == null");
        }
        this.owner = owner;
    }

    public Vector3[] getLastPositions() {
        final Vector3[] returnLastPositions = new Vector3[lastPositions.size()];
        lastPositions.toArray(returnLastPositions);
        return returnLastPositions;
    }

    /**
     * Add the last known position
     *
     * @param lastPosition position in world coordinates
     */
    public void addLastPosition(final Vector3 lastPosition) {
        if (lastPosition == null) {
            throw new GdxRuntimeException("lastPosition == null");
        }
        lastPositions.addFirst(lastPosition);
        if (lastPositions.size() > Globals.KEEP_LAST_POSITIONS_COUNT) {
            lastPositions.removeLast();
        }
    }
}
