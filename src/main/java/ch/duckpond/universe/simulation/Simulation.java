package ch.duckpond.universe.simulation;

import ch.duckpond.universe.dao.CachedDatastore;
import ch.duckpond.universe.dao.PersistedWorld;
import ch.duckpond.universe.test.physics.UniverseTest;
import ch.duckpond.universe.utils.box2d.BodyUtils;

import com.mongodb.MongoClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.mongodb.morphia.Morphia;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Simulation implements Runnable {

  private static final Random RANDOM = new Random();

  private static final int UPDATE_POSITION_ITERATIONS = 8;

  private static final float    UPDATE_TIME_STEP           = 1.0f / 60.0f;
  private static final int      UPDATE_VELOCITY_ITERATIONS = 10;
  /**
   * Morphia datastore.
   */
  private final CachedDatastore datastore;

  private final Logger logger = LogManager.getLogger(Simulation.class);

  /**
   * Morphia mongoDB object mapper.
   */
  private final Morphia morphia = new Morphia().mapPackage("ch.duckpond.universe.persisted");

  /**
   * The persistence wrapper for the @{link World} object.
   */
  private PersistedWorld persistedWorld;

  /**
   * Create world with no gravity.
   */
  private World world = new World(new Vec2());

  /**
   * Construct.
   */
  public Simulation() {
    // set up morphia
    datastore = new CachedDatastore(morphia, new MongoClient(), "test"); // here
                                                                         // thx
                                                                         // to
                                                                         // eclipse->sort
                                                                         // member
    datastore.ensureIndexes();
    // try to load an existing world
    persistedWorld = datastore.find(PersistedWorld.class).get();
    if (persistedWorld != null) {
      world = persistedWorld.get(datastore);
    } else {
      world = new World(new Vec2());
      persistedWorld = new PersistedWorld(world, datastore);
    }
  }

  @Override
  public void run() {
    // TEST only: Add random masses
    UniverseTest.addMasses(world);

    while (true) {
      // update physics
      update(world);
      // persist elements
      persistedWorld.save(datastore);
    }
  }

  /**
   * Updates the given world.
   *
   * @param world
   *          the world to update.
   */
  public void update(final World world) {

    world.step(UPDATE_TIME_STEP, UPDATE_VELOCITY_ITERATIONS, UPDATE_POSITION_ITERATIONS);

    // Get list of bodies
    final List<Body> bodies = new LinkedList<>();
    for (Body i = world.getBodyList(); i != null; i = i.getNext()) {
      bodies.add(i);
    }

    // Body gravity
    bodies.stream().forEach(body -> {
      bodies.stream().filter(otherBody -> otherBody != body).forEach(otherBody -> {
        final Vec2 delta = new Vec2(body.getPosition()).mulLocal(-1)
            .addLocal(otherBody.getPosition());
        if (delta.length() != 0) {
          final Vec2 force = new Vec2(delta)
              .mulLocal(otherBody.getMass() * body.getMass() / (delta.length() * delta.length()));
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
        // randomly select one of the bodies as winner
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
}
