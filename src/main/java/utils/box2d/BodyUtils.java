package utils.box2d;

import org.jbox2d.dynamics.Body;

public class BodyUtils {

    private BodyUtils() {
    };

    public static float getEnergy(final Body body) {
        return 0.5f * body.getMass() * body.getLinearVelocity().length() * body.getLinearVelocity().length();
    }
}
