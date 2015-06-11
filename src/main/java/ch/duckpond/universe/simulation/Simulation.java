package ch.duckpond.universe.simulation;

import ch.duckpond.universe.utils.box2d.BodyUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.DistanceJointDef;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Simulation implements Runnable {

  private static final Random RANDOM = new Random();

  private final Logger        logger = LogManager.getLogger(Simulation.class);

  /**
   * Create world with no gravity.
   */
  private final World         world  = new World(new Vec2());

  /**
   * Updates the given world.
   *
   * @param world
   *          the world to update.
   */
  public void update(final World world) {

    // Get list of bodies
    final List<Body> bodies = new LinkedList<>();
    for (Body i = world.getBodyList(); i != null; i = i.getNext()) {
      bodies.add(i);
    }

    // Body gravity
    bodies.stream().forEach(
        body -> {
          bodies
              .stream()
              .filter(otherBody -> otherBody != body)
              .forEach(
                  otherBody -> {
                    final Vec2 delta = new Vec2(body.getPosition()).mulLocal(-1).addLocal(
                        otherBody.getPosition());
                    if (delta.length() != 0) {
                      final Vec2 force = new Vec2(delta).mulLocal((otherBody.getMass() * body
                          .getMass()) / (delta.length() * delta.length()));
                      logger.debug(String.format("Force: %s -> %s = %s", body.getPosition(),
                          otherBody.getPosition(), force));
                      body.applyForceToCenter(force);
                    }
                  });
        });

    // Solve contacts
    Contact contact = world.getContactList();
    while (contact != null) {

      final Fixture fixture1 = contact.getFixtureA();
      final Body body1 = fixture1.getBody();

      final Fixture fixture2 = contact.getFixtureB();
      final Body body2 = fixture2.getBody();

      logger.info(String.format("Contact: (%s, %s)", body1.getPosition(), body2.getPosition()));

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
      // join the two bodies
      final DistanceJointDef jointDef = new DistanceJointDef();
      jointDef.initialize(winningBody, loosingBody, winningBody.getPosition(),
          loosingBody.getPosition());
      world.createJoint(jointDef);
      // next
      contact = contact.getNext();
    }
  }

  @Override
  public void run() {
    while (true) {
      update(world);
    }
  }
}
