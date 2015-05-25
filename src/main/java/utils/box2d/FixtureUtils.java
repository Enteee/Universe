package utils.box2d;

import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

public class FixtureUtils {

    private FixtureUtils() {
    };

    public static FixtureDef getFixtureDef(final Fixture fixture) {
        final FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = fixture.getDensity();
        fixtureDef.filter = fixture.getFilterData();
        fixtureDef.friction = fixture.getFriction();
        fixtureDef.isSensor = fixture.isSensor();
        fixtureDef.restitution = fixture.getRestitution();
        fixtureDef.shape = fixture.getShape();
        fixtureDef.userData = fixture.getUserData();
        return fixtureDef;
    }
}
