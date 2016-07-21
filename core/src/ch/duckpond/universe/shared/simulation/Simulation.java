package ch.duckpond.universe.shared.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.duckpond.universe.client.Mass;
import ch.duckpond.universe.client.Player;
import ch.duckpond.universe.client.Universe;
import ch.duckpond.universe.utils.box2d.BodyUtils;

/**
 * The universe simulation
 *
 * @author ente
 */
public class Simulation {

    private static final int UPDATE_POSITION_ITERATIONS = 8;
    private static final float UPDATE_TIME_STEP = 1.0f / 60.0f;
    private static final int UPDATE_VELOCITY_ITERATIONS = 10;
    private static final float GRAVITATIONAL_CONSTANT = 1f;
    private static final int MAX_GRAVITY_BODIES_PER_UPDATE = 500;
    private final List<ContactTuple> contacts = new LinkedList<ContactTuple>();
    private final World world;

    /**
     * Construct.
     */
    public Simulation() {
        this(new World(new Vector2(), false));
    }

    /**
     * Construct.
     *
     * @param world the {@link World} to run this simulation with
     */
    public Simulation(final World world) {
        this.world = world;
        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(final Contact contact) {
            }

            @Override
            public void endContact(final Contact contact) {
            }

            @Override
            public void preSolve(final Contact contact, final Manifold oldManifold) {
                contacts.add(new ContactTuple(contact.getFixtureA().getBody(),
                                              contact.getFixtureB().getBody()));
            }

            @Override
            public void postSolve(final Contact contact, final ContactImpulse impulse) {
            }
        });
    }

    public World getWorld() {
        return world;
    }

    public Array<Body> getBodies() {
        final Array<Body> bodies = new Array<Body>(world.getBodyCount());
        world.getBodies(bodies);
        return bodies;
    }

    /**
     * Updates the given world.
     */
    public void update() {
        // Solve contacts
        final Set<Body> destroyedBodies = new HashSet<Body>();
        for (final ContactTuple i : contacts) {
            if (!i.isDestroyed(destroyedBodies)) {
                Gdx.app.debug(getClass().toString(), String.format("Contact: %s", i));
                // first create resulting body
                final BodyDef collisionResultDef = BodyUtils.getBodyDef(i.getWinner());
                final Body newBody = spawnMass(new Vector3(collisionResultDef.position.x,
                                                           collisionResultDef.position.y,
                                                           0),
                                               BodyUtils.getRadiusFromMass(i.getWinner().getMass() + i.getLooser().getMass()),
                                               new Vector3(collisionResultDef.linearVelocity.x,
                                                           collisionResultDef.linearVelocity.y,
                                                           0));
                newBody.setUserData(i.getWinner().getUserData());
                // destory old bodies
                world.destroyBody(i.getLooser());
                destroyedBodies.add(i.getLooser());
                world.destroyBody(i.getWinner());
                destroyedBodies.add(i.getWinner());
                // was one of the destroyed bodies the centered body?
                if (Universe.getInstance().getCenteredBody() == i.getLooser() || Universe.getInstance().getCenteredBody() == i.getWinner()) {
                    Universe.getInstance().setCenteredBody(newBody);
                }
            }
        }
        contacts.clear();
        // Reset energies
        final Map<Player, Float> energies = new HashMap<>();
        // Get list of bodies
        final Array<Body> bodies = new Array<Body>(world.getBodyCount());
        final Array<Body> otherBodies = new Array<Body>(world.getBodyCount());
        world.getBodies(bodies);
        world.getBodies(otherBodies);

        for (final Body body : bodies) {
            final Mass bodyMass = (Mass) body.getUserData();
            final Player bodyOwner = bodyMass.getOwner();
            // Remember position
            bodyMass.addLastPosition(new Vector3(body.getPosition().x,
                                                 body.getPosition().y,
                                                 0));
            // Update player energy
            if (!energies.containsKey(bodyOwner)) {
                energies.put(bodyOwner, 0f);
            }
            float playerEnergy = energies.get(bodyOwner);
            playerEnergy += BodyUtils.getEnergy(body);
            energies.put(bodyOwner, playerEnergy);
            // Body gravity:
            // Random select a maximum amount of bodies
            otherBodies.shuffle();
            for (int i = 0; (i < MAX_GRAVITY_BODIES_PER_UPDATE) && (i < bodies.size); ++i) {
                final Body otherBody = bodies.get(i);
                if (body != otherBody) {
                    // process gravitational force from this body towards
                    // some other bodies
                    final Vector2 delta = new Vector2(otherBody.getPosition());
                    delta.mulAdd(body.getPosition(), -1);
                    if (delta.len() != 0) {
                        delta.scl((otherBody.getMass() * body.getMass() * GRAVITATIONAL_CONSTANT) / (delta.len() * delta.len()));
                        // Gdx.app.debug(getClass().toString(),
                        // String.format("Force: %s -> %s = %s",
                        // body.getPosition(), otherBody.getPosition(), delta));
                        body.applyForceToCenter(delta, true);
                    }
                }
            }
        }

        // add energies to players
        for (Map.Entry<Player, Float> energy : energies.entrySet()) {
            energy.getKey().addEnergy(energy.getValue());
        }
        world.step(UPDATE_TIME_STEP, UPDATE_VELOCITY_ITERATIONS, UPDATE_POSITION_ITERATIONS);
    }

    public Body spawnMass(final Vector3 position, final float radius, final Vector3 velocity) {
        final CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radius);
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(position.x, position.y));
        bodyDef.linearVelocity.set(new Vector2(velocity.x, velocity.y));
        bodyDef.angle = (float) (Math.PI * 2 * Globals.RANDOM.nextFloat());
        bodyDef.allowSleep = false;
        final Body body = world.createBody(bodyDef);
        body.setUserData(new Mass());
        body.createFixture(circleShape, Globals.MASS_DENSITY);
        return body;
    }

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
                if (Globals.RANDOM.nextFloat() < 0.5f) {
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

        protected Body getWinner() {
            return winner;
        }

        protected Body getLooser() {
            return looser;
        }

        protected boolean isDestroyed(final Set<Body> destroyedBodies) {
            return destroyedBodies.contains(winner) || destroyedBodies.contains(looser);
        }
    }
}
