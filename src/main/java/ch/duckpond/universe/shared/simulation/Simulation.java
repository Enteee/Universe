package ch.duckpond.universe.shared.simulation;

import ch.duckpond.universe.server.dao.CachedDatastore;
import ch.duckpond.universe.server.dao.PersistedWorld;
import ch.duckpond.universe.utils.box2d.BodyUtils;

import com.mongodb.MongoClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.mongodb.morphia.Morphia;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Simulation implements Runnable {

  private static class ContactTuple {

    private final Body winner;
    private final Body looser;

    protected ContactTuple(final Body body1, final Body body2) {
      // elect 'winning' body
      if (BodyUtils.getEnergy(body1) < BodyUtils.getEnergy(body2)) {
        winner = body2;
        looser = body1;
      } else if (BodyUtils.getEnergy(body1) > BodyUtils.getEnergy(body2)) {
        winner = body1;
        looser = body2;
      } else {
        // both have the same energy
        // randomly select one of the bodies as winner
        if (RANDOM.nextFloat() < 0.5f) {
          winner = body2;
          looser = body1;
        } else {
          winner = body1;
          looser = body2;
        }
      }
    }

    @Override
    public String toString() {
      return String.format("(W:%s L:%s)", getWinner(), getLooser());
    }

    protected Body getLooser() {
      return looser;
    }

    protected Body getWinner() {
      return winner;
    }

    protected boolean isDestroyed(final Set<Body> destroyedBodies) {
      return destroyedBodies.contains(winner) || destroyedBodies.contains(looser);
    }
  }

  private static final Random RANDOM = new Random();

  private static final int UPDATE_POSITION_ITERATIONS = 8;

  private static final float       UPDATE_TIME_STEP              = 1.0f / 60.0f;
  private static final int         UPDATE_VELOCITY_ITERATIONS    = 10;
  private static final float       GRAVITATIONAL_CONSTANT        = 0.1f;
  private static final int         MAX_GRAVITY_BODIES_PER_UPDATE = 500;
  private final Logger             logger                        = LogManager.getLogger(getClass());
  private final List<ContactTuple> contacts                      = new LinkedList<>();

  /**
   * Morphia mongoDB object mapper.
   */
  private final Morphia         morphia   = new Morphia()
      .mapPackage("ch.duckpond.universe.persisted");
  /**
   * Morphia datastore.
   */
  private final CachedDatastore datastore = new CachedDatastore(morphia, new MongoClient(), "test");
  /**
   * The persistence wrapper for the @{link World} object.
   */
  private final PersistedWorld  persistedWorld;

  /**
   * Construct.
   */
  public Simulation() {
    persistedWorld = new PersistedWorld(new World(new Vec2()), datastore);
  }

  /**
   * Construct.
   *
   * @param persistedWorld
   *          the {@link PersistedWorld} to run this simulation with
   */
  public Simulation(final PersistedWorld persistedWorld) {
    this.persistedWorld = persistedWorld;
    this.persistedWorld.get(datastore).setContactListener(new ContactListener() {

      @Override
      public void beginContact(final Contact contact) {
      }

      @Override
      public void endContact(final Contact contact) {
      }

      @Override
      public void postSolve(final Contact contact, final ContactImpulse impulse) {
      }

      @Override
      public void preSolve(final Contact contact, final Manifold oldManifold) {
        contacts.add(
            new ContactTuple(contact.getFixtureA().getBody(), contact.getFixtureB().getBody()));
      }
    });
  }

  @Override
  public void run() {
    while (true) {
      step();
    }
  }

  /**
   * Make a simulation step.
   */
  public void step() {
    // update physics
    update(persistedWorld.get(datastore));
    // persist elements
    // persistedWorld.save(datastore);
  }

  /**
   * Updates the given world.
   *
   * @param world
   *          the world to update.
   */
  public void update(final World world) {
    world.step(UPDATE_TIME_STEP, UPDATE_VELOCITY_ITERATIONS, UPDATE_POSITION_ITERATIONS);
    // Solve contacts
    final Set<Body> destroyedBodies = new HashSet<>();
    for (final ContactTuple i : contacts) {
      if (!i.isDestroyed(destroyedBodies)) {
        logger.info(String.format("Contact: %s", i));
        // first create resulting body
        final BodyDef collisionResultDef = BodyUtils.getBodyDef(i.getWinner());
        final Body collisionResult = world.createBody(collisionResultDef);
        final CircleShape circleShape = new CircleShape();
        circleShape.setRadius(
            BodyUtils.getRadiusFromMass(i.getWinner().getMass() + i.getLooser().getMass()));
        collisionResult.createFixture(circleShape, Globals.DENSITY);
        logger.info(String.format("collisionResult.getMass() = %s", collisionResult.getMass()));
        // destory old bodies
        world.destroyBody(i.getLooser());
        destroyedBodies.add(i.getLooser());
        world.destroyBody(i.getWinner());
        destroyedBodies.add(i.getWinner());
      }
    }
    contacts.clear();
    // Get list of bodies
    final List<Body> bodies = new ArrayList<>();
    for (Body i = world.getBodyList(); i != null; i = i.getNext()) {
      bodies.add(i);
    }
    // Body gravity
    bodies.stream().parallel().forEach(body -> {
      // Random select a maximum amount of bodies some bodies
      final List<Body> otherBodies = IntStream.range(0, MAX_GRAVITY_BODIES_PER_UPDATE)
          .mapToObj(i -> {
        return bodies.get(RANDOM.nextInt(bodies.size()));
      }).collect(Collectors.toList());
      // process gravity for selected bodies
      otherBodies.stream().filter(otherBody -> otherBody != body).forEach(otherBody -> {
        final Vec2 delta = new Vec2(body.getPosition()).mulLocal(-1)
            .addLocal(otherBody.getPosition());
        if (delta.length() != 0) {
          final Vec2 force = new Vec2(delta).mulLocal(otherBody.getMass() * body.getMass()
              * GRAVITATIONAL_CONSTANT / (delta.length() * delta.length()));
          logger.debug(String.format("Force: %s -> %s = %s", body.getPosition(),
              otherBody.getPosition(), force));
          body.applyForceToCenter(force);
        }
      });
    });
  }
}
