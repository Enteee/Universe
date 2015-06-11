package ch.duckpond.universe.utils.box2d;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

public class FixtureUtils {

  /**
   * Clones a filter from a fixture.
   * 
   * @param fixture
   *          the fixture to clone the {@link Filter} from
   * @return cloned {@link Filter}
   */
  public static Filter cloneFilter(final Fixture fixture) {
    final Filter returnFilter = new Filter();
    returnFilter.set(fixture.getFilterData());
    return returnFilter;
  }

  /**
   * Clones a shape from a fixture.
   * 
   * @param fixture
   *          the fixture to clone the @{link Shape} from
   * @return cloned @{link Shape}
   */
  public static Shape cloneShape(final Fixture fixture) {
    return fixture.getShape().clone();
  }

  /**
   * Clones the fixture definition from the given fixture.
   * 
   * @param fixture
   *          the fixture to clone the {@link Fixture} from
   * @return cloned @ Fixture}
   */
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
