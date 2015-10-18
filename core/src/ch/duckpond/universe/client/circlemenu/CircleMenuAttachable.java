package ch.duckpond.universe.client.circlemenu;

import com.badlogic.gdx.math.Vector3;

import java.util.List;

/**
 * Objects which can have a {@link CircleMenu}
 *
 * @author ente
 */
public interface CircleMenuAttachable {
    Vector3 getWorldPosition();

    /**
     * Get the radius of the circle menu around this item.
     *
     * @return the radius
     */
    float getCircleRadius();

    /**
     * The radius of the circle menu items
     *
     * @return the radius of the items
     */
    float getCircleMenuItemRadius();

    /**
     * Get all the items this attachable has
     *
     * @return all the items of this attachable
     */
    List<CircleMenuItem> getCircleMenuItems();
}
