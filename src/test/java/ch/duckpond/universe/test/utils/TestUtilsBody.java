package ch.duckpond.universe.test.utils;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import java.util.Random;

public class TestUtilsBody {

  private static final Random RANDOM      = new Random();

  private static final float  MAX_RADIUS  = 10;
  private static final float  MAX_X       = 500;
  private static final float  MAX_Y       = 500;
  private static final float  MAX_DENSITY = 10;

  public static Body randomBody(final World world) {
    final CircleShape circleShape = new CircleShape();
    circleShape.setRadius(MAX_RADIUS * RANDOM.nextFloat());
    final BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyType.DYNAMIC;
    bodyDef.position.set((MAX_X * 2 * RANDOM.nextFloat()) - MAX_X, (MAX_Y * 2 * RANDOM.nextFloat())
        - MAX_Y);
    bodyDef.angle = (float) (Math.PI * 2 * RANDOM.nextFloat());
    bodyDef.allowSleep = false;
    final Body body = world.createBody(bodyDef);
    body.createFixture(circleShape, MAX_DENSITY * RANDOM.nextFloat());
    return body;
  }

  private TestUtilsBody() {
  }

}
