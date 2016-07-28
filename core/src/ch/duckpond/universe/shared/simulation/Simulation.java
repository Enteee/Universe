package ch.duckpond.universe.shared.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.duckpond.universe.client.NeonColors;
import ch.duckpond.universe.client.Player;
import ch.duckpond.universe.client.game.Enemy;
import ch.duckpond.universe.client.game.GameScreen;
import ch.duckpond.universe.client.game.Mass;
import ch.duckpond.universe.utils.box2d.BodyUtils;

/**
 * The universe simulation
 *
 * @author ente
 */
public class Simulation {

    private static final float UPDATE_TIME_STEP = 1.0f / 60.0f;
    private static final int UPDATE_POSITION_ITERATIONS = 8;
    private static final int UPDATE_VELOCITY_ITERATIONS = 10;
    private static final float GRAVITATIONAL_CONSTANT = 1f;
    private static final int MAX_GRAVITY_BODIES_PER_UPDATE = 500;

    private final GameScreen gameScreen;
    private final World world;
    private final List<ContactTuple> contactTuples = new LinkedList<ContactTuple>();

    private float time = 0;
    private float timeAccumulator = 0;

    private List<Enemy> enemies = new ArrayList<>();

    /**
     * Construct.
     */
    public Simulation(final GameScreen gameScreen) {
        this(gameScreen, new World(new Vector2(), false));
    }

    /**
     * Construct.
     *
     * @param world the {@link World} to run this simulation with
     */
    public Simulation(final GameScreen gameScreen, final World world) {
        this.gameScreen = gameScreen;
        this.world = world;
        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(final Contact contact) {
                contactTuples.add(new ContactTuple(contact));
            }

            @Override
            public void endContact(final Contact contact) {
            }

            @Override
            public void preSolve(final Contact contact, final Manifold oldManifold) {
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
     * Updates the given world: Ensures that doUpdate is only called in UPDATE_TIME_STEP - intervals
     */
    public void update(final float deltaTime) {
        time += deltaTime;
        timeAccumulator += deltaTime;
        while (timeAccumulator >= UPDATE_TIME_STEP) {
            timeAccumulator -= UPDATE_TIME_STEP;
            doUpdate();
        }
    }

    private void doUpdate() {

        world.step(UPDATE_TIME_STEP, UPDATE_VELOCITY_ITERATIONS, UPDATE_POSITION_ITERATIONS);

        // Solve contactTuples
        final Set<Body> destroyedBodies = new HashSet<Body>();
        for (final ContactTuple contact : contactTuples) {
            if (!contact.isDestroyed(destroyedBodies)) {
                Gdx.app.debug(getClass().toString(),
                              String.format("handling contact: %s samePlayer: %b",
                                            contact,
                                            contact.isSamePlayerContact()));
                if (contact.isSamePlayerContact()) {
                    //                    // same player: combine masses

                    // first create resulting body
                    final BodyDef collisionResultDef = BodyUtils.getBodyDef(contact.getWinner());
                    final Body newBody = spawnBody(new Vector3(collisionResultDef.position.x,
                                                               collisionResultDef.position.y,
                                                               0),
                                                   BodyUtils.getRadiusFromMass(contact.getWinner().getMass() + contact.getLooser().getMass()),
                                                   new Vector3(collisionResultDef.linearVelocity.x,
                                                               collisionResultDef.linearVelocity.y,
                                                               0),
                                                   contact.getWinnerMass());
                    // remove loosing actor
                    contact.getLooserMass().remove();
                    // destroy old bodies
                    world.destroyBody(contact.getLooser());
                    destroyedBodies.add(contact.getLooser());
                    world.destroyBody(contact.getWinner());
                    destroyedBodies.add(contact.getWinner());
                    // was one of the destroyed bodies the centered body?
                    if (gameScreen.getCenteredBody() == contact.getLooser() || gameScreen.getCenteredBody() == contact.getWinner()) {
                        gameScreen.setCenteredBody(newBody);
                    }
                } else {
                    // not same player: change owner
                    ((Mass) contact.getLooser().getUserData()).setOwner(((Mass) contact.getWinner().getUserData()).getOwner());
                }
            }
        }
        contactTuples.clear();

        // Do enemy actions
        final int enemyCount = (int) Math.floor(1 + gameScreen.getLevel() * Globals.LEVEL_ENEMY_COUNT); // +1: always have at least one enemy
        if (enemyCount != enemies.size()) {
            // build new enemies
            enemies.clear();
            for (int i = 0; i < enemyCount; i++) {
                NeonColors enemyColor;
                do {
                    enemyColor = NeonColors.getRandomColor();
                }
                // don't pick the player color
                while (enemyColor.getColorRGB888() == Color.rgba8888(gameScreen.getThisPlayer().getColor()));
                enemies.add(new Enemy(gameScreen, new Color(enemyColor.getColorRGB888())));
            }
        }
        for (final Enemy enemy : enemies) {
            enemy.act(time);
        }

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
            bodyMass.addLastPosition(new Vector3(body.getPosition().x, body.getPosition().y, 0));
            // Update player energy
            if (!energies.containsKey(bodyOwner)) {
                energies.put(bodyOwner, 0f);
            }
            float ownerEnergy = energies.get(bodyOwner);
            ownerEnergy += BodyUtils.getEnergy(body);
            energies.put(bodyOwner, ownerEnergy);
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
            // update bounds for mass
            bodyMass.updateBounds();
        }

        // add energies to players
        for (Map.Entry<Player, Float> energy : energies.entrySet()) {
            energy.getKey().addEnergy(energy.getValue());
        }
    }

    public Body spawnBody(final Vector3 position, final float radius, final Vector3 velocity, final Mass mass) {
        assert position != null;
        assert radius > 0;
        assert velocity != null;
        assert mass != null;

        final CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radius);
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(position.x, position.y));
        bodyDef.linearVelocity.set(new Vector2(velocity.x, velocity.y));
        bodyDef.angle = (float) (Math.PI * 2 * Globals.RANDOM.nextFloat());
        bodyDef.allowSleep = false;
        final Body body = world.createBody(bodyDef);
        body.createFixture(circleShape, Globals.MASS_DENSITY);

        // link body & mass
        body.setUserData(mass);
        mass.setBody(body);
        return body;
    }

    private class ContactTuple {

        private final Body winner;
        private final Body looser;
        private final Vector2 winnerVelocity;
        private final Vector2 looserVelocity;

        private ContactTuple(final Contact contact) {
            this(contact.getFixtureA().getBody(), contact.getFixtureB().getBody());
        }

        private ContactTuple(final Body body1, final Body body2) {
            assert body1 != null;
            assert body2 != null;

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
            winnerVelocity = new Vector2(winner.getLinearVelocity());
            looserVelocity = new Vector2(looser.getLinearVelocity());
        }

        private boolean isSamePlayerContact() {
            return ((Mass) winner.getUserData()).getOwner() == ((Mass) looser.getUserData()).getOwner();
        }

        private Mass getWinnerMass() {
            return (Mass) winner.getUserData();
        }

        private Mass getLooserMass() {
            return (Mass) looser.getUserData();
        }

        public Vector2 getWinnerVelocity() {
            return winnerVelocity;
        }

        public Vector2 getLooserVelocity() {
            return looserVelocity;
        }

        private boolean isDestroyed(final Set<Body> destroyedBodies) {
            return destroyedBodies.contains(winner) || destroyedBodies.contains(looser);
        }

        @Override
        public int hashCode() {
            return winner.hashCode() + looser.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ContactTuple) {
                final ContactTuple otherContact = (ContactTuple) obj;
                return otherContact.winner == winner && otherContact.looser == looser;
            }
            return obj.equals(obj);
        }

        @Override
        public String toString() {
            return String.format("(W:%s L:%s)", getWinner(), getLooser());
        }

        private Body getWinner() {
            return winner;
        }

        private Body getLooser() {
            return looser;
        }
    }
}
