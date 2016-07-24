package ch.duckpond.universe.client;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Deque;
import java.util.LinkedList;

import ch.duckpond.universe.client.circlemenu.CircleMenu;
import ch.duckpond.universe.client.screen.GameScreen;
import ch.duckpond.universe.shared.simulation.Globals;

/**
 * A mass in the game
 *
 * @author ente
 */
public class Mass extends Actor {

    private final CircleMenu circleMenu;
    private final Deque<Vector3> lastPositions = new LinkedList();
    private Player owner;

    /**
     * Mass owned by the local player
     * @param gameScreen gameScreen
     */
    public Mass(final GameScreen gameScreen) {
        owner = gameScreen.getThisPlayer();
        circleMenu = new CircleMenu(gameScreen, this);
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
