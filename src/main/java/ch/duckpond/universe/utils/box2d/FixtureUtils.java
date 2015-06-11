package ch.duckpond.universe.utils.box2d;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

public class FixtureUtils {

  public static Filter cloneFilter(final Fixture fixture) {
    final Filter returnFilter = new Filter();
    returnFilter.set(fixture.getFilterData());
    return returnFilter;
  }

  public static Shape cloneShape(final Fixture fixture) {
    return fixture.getShape().clone();
  }

  public static FixtureDef getFixtureDef(final Fixture fixture) {
    final FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = fixture.getDensity();
    fixtureDef.filter = cloneFilter(fixture);
    fixtureDef.friction = fixture.getFriction();
    fixtureDef.isSensor = fixture.isSensor();
    fixtureDef.restitution = fixture.getRestitution();
    fixtureDef.shape = cloneShape(fixture);
    fixtureDef.userData = fixture.getUserData();
    return fixtureDef;
  }

  private FixtureUtils() {
  }

}
