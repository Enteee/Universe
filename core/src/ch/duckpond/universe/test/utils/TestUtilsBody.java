package ch.duckpond.universe.test.utils;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

public class TestUtilsBody {

    private static final float MAX_DENSITY = 10;
    private static final float MAX_RADIUS = 1;
    private static final float MAX_X = 50;
    private static final float MAX_Y = 50;
    private static final Random RANDOM = new Random();

    private TestUtilsBody() {
    }

    public static Body randomBody(final World world) {
        final CircleShape circleShape = new CircleShape();
        circleShape.setRadius(MAX_RADIUS * RANDOM.nextFloat());
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set((MAX_X * 2 * RANDOM.nextFloat()) - MAX_X,
                             (MAX_Y * 2 * RANDOM.nextFloat()) - MAX_Y);
        bodyDef.angle = (float) (Math.PI * 2 * RANDOM.nextFloat());
        bodyDef.allowSleep = false;
        final Body body = world.createBody(bodyDef);
        body.createFixture(circleShape, MAX_DENSITY * RANDOM.nextFloat());
        return body;
    }
}
