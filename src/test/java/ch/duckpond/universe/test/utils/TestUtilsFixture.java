package ch.duckpond.universe.test.utils;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import java.util.Random;

public class TestUtilsFixture {

  private static final Random RANDOM      = new Random();

  private static final float  MAX_DENSITY = 10;

  public static Fixture randomFixture(final Body body) {
    final FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.density = RANDOM.nextFloat() * MAX_DENSITY;
    fixtureDef.friction = RANDOM.nextFloat();
    fixtureDef.restitution = RANDOM.nextFloat();
    fixtureDef.shape = new CircleShape();
    return body.createFixture(fixtureDef);
  }

  private TestUtilsFixture() {
  }

}
