package ch.duckpond.universe.utils.box2d;

import ch.duckpond.universe.pojo.FixtureDefPojo;

import org.bson.types.ObjectId;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

public class FixtureUtils {

  /**
   * Clones a filter from a fixture.
   *
   * @param filter
   *          the {@link Filter} to clone
   * @return cloned {@link Filter}
   */
  private static Filter cloneFilter(final Filter filter) {
    final Filter returnFilter = new Filter();
    returnFilter.set(filter);
    return returnFilter;
  }

  /**
   * Clones a shape from a fixture.
   *
   * @param shape
   *          the @{link Shape} to clone
   * @return cloned @{link Shape}
   */
  private static Shape cloneShape(final Shape shape) {
    return shape.clone();
  }

  /**
   * Clones a @{link FixtureDef} from the given @{link Fixture}.
   *
   * @param fixture
   *          the @{link Fixture} from which to clone
   * @return cloned @{link FixtureDef}
   */
  public static FixtureDef getFixtureDef(final Fixture fixture) {
    final FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.userData = fixture.getUserData();
    fixtureDef.density = fixture.getDensity();
    fixtureDef.filter = cloneFilter(fixture.getFilterData());
    fixtureDef.friction = fixture.getFriction();
    fixtureDef.isSensor = fixture.isSensor();
    fixtureDef.restitution = fixture.getRestitution();
    fixtureDef.shape = cloneShape(fixture.getShape());
    return fixtureDef;
  }

  /**
   * Clones a @{link FixtureDef} from the given {@link FixtureDefPojo}.
   *
   * @param fixtureDefPojo
   *          the @{link FixtureDefPojo} from which to clone
   * @return cloned @{link FixtureDef}
   */
  public static FixtureDef getFixtureDef(final FixtureDefPojo fixtureDefPojo) {
    final FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.userData = fixtureDefPojo.userData;
    fixtureDef.density = fixtureDefPojo.density;
    fixtureDef.filter = cloneFilter(fixtureDefPojo.filter);
    fixtureDef.friction = fixtureDefPojo.friction;
    fixtureDef.isSensor = fixtureDefPojo.isSensor;
    fixtureDef.restitution = fixtureDefPojo.restitution;
    fixtureDef.shape = cloneShape(fixtureDefPojo.shape);
    return fixtureDef;
  }

  /**
   * Clones a @{link FixtureDefPojo} from the given {@link FixtureDef}.
   *
   * @param fixtureDef
   *          the @{link FixtureDef} from which to clone
   * @return cloned @{link FixtureDefPojo}
   */
  public static FixtureDefPojo getFixtureDefPojo(final FixtureDef fixtureDef) {
    final FixtureDefPojo fixtureDefPojo = new FixtureDefPojo();
    fixtureDefPojo.userData = (ObjectId) fixtureDef.userData;
    fixtureDefPojo.density = fixtureDef.density;
    fixtureDefPojo.filter = cloneFilter(fixtureDef.filter);
    fixtureDefPojo.friction = fixtureDef.friction;
    fixtureDefPojo.isSensor = fixtureDef.isSensor;
    fixtureDefPojo.restitution = fixtureDef.restitution;
    fixtureDefPojo.shape = cloneShape(fixtureDef.shape);
    return fixtureDefPojo;
  }

  private FixtureUtils() {
  }

}
