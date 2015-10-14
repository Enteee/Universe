package ch.duckpond.universe.client.circlemenu;

import ch.duckpond.universe.client.Universe;

/**
 * Interface for circle menu items.
 *
 * @author ente
 */
public interface CircleMenuItem<T> {
    void setUniverse(final Universe universe);

    void clicked();
}
