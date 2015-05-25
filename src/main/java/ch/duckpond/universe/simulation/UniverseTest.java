package ch.duckpond.universe.simulation;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.testbed.framework.TestbedTest;

import utils.box2d.BodyUtils;
import utils.box2d.FixtureUtils;

public class UniverseTest extends TestbedTest {

    final private static int        MASSES_ROWS        = 5;
    final private static int        MASSES_COLS        = 5;
    final private static int        MASSES_ROW_SPACING = 5;
    final private static int        MASSES_COL_SPACING = 5;

    final private static float      DENSITY            = 1;

    final private static Random     RANDOM             = new Random();

    final Logger                    logger             = LogManager.getLogger(UniverseTest.class);

    private static final List<Body> removeBodies       = new LinkedList<>();

    @Override
    public void initTest(final boolean argDeserialized) {
        setTitle("Couple of Things Test");

        getWorld().setGravity(new Vec2());

        for (int i = 0; i < MASSES_ROWS; i++) {
            for (int j = 0; j < MASSES_COLS; j++) {
                final PolygonShape polygonShape = new PolygonShape();
                polygonShape.setAsBox(1, 1);
                final BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyType.DYNAMIC;
                bodyDef.position.set(MASSES_COL_SPACING * (j % MASSES_COLS), MASSES_ROW_SPACING * (i % MASSES_ROWS));
                bodyDef.angle = (float) ((Math.PI / 4) * i);
                bodyDef.allowSleep = false;
                final Body body = getWorld().createBody(bodyDef);
                body.createFixture(polygonShape, DENSITY);
                logger.info(String.format("Mass: %f", body.getMass()));
            }
        }
    }

    @Override
    public void update() {
        super.update();
        // Get list of bodies
        final List<Body> bodies = new LinkedList<>();
        for (Body i = getWorld().getBodyList(); i != null; i = i.getNext()) {
            bodies.add(i);
        }
        // Gravity
        bodies.stream().forEach(body -> {
            bodies.stream().filter(otherBody -> otherBody != body).forEach(otherBody -> {
                final Vec2 delta = new Vec2(body.getPosition()).mulLocal(-1).addLocal(otherBody.getPosition());
                if (delta.length() != 0) {
                    final Vec2 force = new Vec2(delta).mulLocal((otherBody.getMass() * body.getMass()) / (delta.length() * delta.length()));
                    logger.debug(String.format("Force: %s -> %s = %s", body.getPosition(), otherBody.getPosition(), force));
                    body.applyForceToCenter(force);
                }
            });
        });

        removeBodies.stream().forEach(body -> {
            getWorld().destroyBody(body);
        });
        removeBodies.clear();
    }

    @Override
    public void beginContact(final Contact contact) {
        super.beginContact(contact);
        logger.info(String.format("Contact: (%s, %s)", contact.getFixtureA().getBody().getPosition(), contact.getFixtureB().getBody().getPosition()));

        final Fixture fixture1 = contact.getFixtureA();
        final Body body1 = fixture1.getBody();

        final Fixture fixture2 = contact.getFixtureB();
        final Body body2 = fixture2.getBody();

        Body winningBody;
        Body loosingBody;
        // elect 'winning' body
        if (BodyUtils.getEnergy(body1) < BodyUtils.getEnergy(body2)) {
            winningBody = body2;
            loosingBody = body1;
        } else if (BodyUtils.getEnergy(body1) > BodyUtils.getEnergy(body2)) {
            winningBody = body1;
            loosingBody = body2;
        } else {
            // both have the same energy
            // randomly destroy one of the bodies
            if (RANDOM.nextFloat() < 0.5f) {
                winningBody = body2;
                loosingBody = body1;
            } else {
                winningBody = body1;
                loosingBody = body2;
            }
        }
        // combine bodies
        final Fixture loosingFixture = loosingBody.getFixtureList();
        while (loosingFixture != null) {
            final FixtureDef fixtureDef = FixtureUtils.getFixtureDef(loosingFixture);
            winningBody.createFixture(fixtureDef);
            loosingFixture.getNext();
        }
        // destroy loosing body
        removeBodies.add(loosingBody);
    }

    @Override
    public void preSolve(final Contact contact, final Manifold oldManifold) {
        // disable contact of two fixtures if they belong to the same body
        if (contact.getFixtureA().getBody() == contact.getFixtureB().getBody()) {
            logger.info("Disabling contact: (%s, %s)", contact.getFixtureA(), contact.getFixtureB());

            contact.setEnabled(false);
        }
        super.preSolve(contact, oldManifold);
    }

    @Override
    public String getTestName() {
        return "Universe";
    }

}
