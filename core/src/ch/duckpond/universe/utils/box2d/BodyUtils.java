package ch.duckpond.universe.utils.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

import ch.duckpond.universe.shared.simulation.Globals;

public class BodyUtils {

    private BodyUtils() {
    }

    /**
     * Clones the {@link BodyDef} from the given @{link Body}.
     *
     * @param body the @{link Body} to clone from
     * @return cloned @{link BodyDef}
     */
    public static BodyDef getBodyDef(final Body body) {
        final BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(body.getPosition());
        bodyDef.angle = body.getAngle();
        bodyDef.linearVelocity.set(body.getLinearVelocity());
        bodyDef.angularVelocity = body.getAngularVelocity();
        bodyDef.linearDamping = body.getLinearDamping();
        bodyDef.angularDamping = body.getAngularDamping();
        bodyDef.allowSleep = body.isSleepingAllowed();
        bodyDef.awake = body.isAwake();
        bodyDef.fixedRotation = body.isFixedRotation();
        bodyDef.bullet = body.isBullet();
        bodyDef.type = body.getType();
        bodyDef.active = body.isActive();
        bodyDef.gravityScale = body.getGravityScale();
        return bodyDef;
    }

    /**
     * Get the energy of a {@link Body}
     *
     * @param body the {@link Body} to get the energy for.
     * @return the total energy
     */
    public static float getEnergy(final Body body) {
        return BodyUtils.getEnergy(body.getMass(), body.getLinearVelocity());
    }

    /**
     * Get the energy a @{link Body} would have with the given mass and velocity
     *
     * @param mass
     * @param velocity
     * @return energy of mass with velocity
     */
    public static float getEnergy(final float mass, final Vector2 velocity) {
        return 0.5f * mass * velocity.len() * velocity.len();
    }

    public static float getMassFromRadius(final float radius) {
        return (float) (radius * radius * Math.PI * Globals.MASS_DENSITY);
    }

    public static float getRadiusFromMass(final float mass) {
        return (float) Math.sqrt(mass / (Globals.MASS_DENSITY * Math.PI));
    }
}
