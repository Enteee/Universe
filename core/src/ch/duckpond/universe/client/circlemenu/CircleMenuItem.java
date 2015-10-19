package ch.duckpond.universe.client.circlemenu;

import com.badlogic.gdx.graphics.Camera;

/**
 * Interface for circle menu items.
 *
 * @author ente
 */
public interface CircleMenuItem {
    /**
     * Event fired when this item was clicked
     */
    void clicked(Object attachable);

    /**
     * Render this item.
     *
     * @param camera the camera to render with
     * @param radius click radius
     */
    void render(final Camera camera, final float radius);
}
