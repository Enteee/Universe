package ch.duckpond.universe.client;

import ch.duckpond.universe.client.circlemenu.CircleMenu;
import ch.duckpond.universe.shared.simulation.Globals;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * A mass in the game
 *
 * @author ente
 */
public class Mass {

    private Player owner;
    private final CircleMenu circleMenu = new CircleMenu(this);
    private final List<Vector3> lastPositions = new ArrayList<>(Globals.KEEP_LAST_POSITIONS_COUNT + 1);

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

    public Vector3[] getLastPositions() {
        if (lastPositions.size() <= 0) {
            return new Vector3[0];
        }
        return (Vector3[]) lastPositions.toArray();
    }

    /**
     * Add the last known position
     *
     * @param lastPosition position in world coordinates
     */
    public void addLastPosition(final Vector3 lastPosition) {
        lastPositions.add(lastPosition);
        if (lastPositions.size() > Globals.KEEP_LAST_POSITIONS_COUNT) {
            lastPositions.remove(0);
        }
    }

    public void setOwner(Player owner) {
        if (owner == null) {
            throw new GdxRuntimeException("Owner == null");
        }
        this.owner = owner;
    }
}
