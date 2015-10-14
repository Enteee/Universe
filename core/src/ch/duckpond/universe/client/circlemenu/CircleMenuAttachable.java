package ch.duckpond.universe.client.circlemenu;

import com.badlogic.gdx.math.Vector3;

/**
 * Objects which can have a {@link CircleMenu}
 *
 * @author ente
 */
public interface CircleMenuAttachable {
    Vector3 getWorldPosition();

    float getCircleRadius();
}
