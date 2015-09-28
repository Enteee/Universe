package ch.duckpond.universe.utils.box2d;

import ch.duckpond.universe.pojo.BodyDefPojo;
import ch.duckpond.universe.simulation.Globals;

import org.bson.types.ObjectId;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

public class BodyUtils {

  /**
   * Clones the {@link BodyDef} from the given @{link Body}.
   *
   * @param body
   *          the @{link Body} to clone from
   * @return cloned @{link BodyDef}
   */
  public static BodyDef getBodyDef(final Body body) {
    final BodyDef bodyDef = new BodyDef();
    bodyDef.userData = body.getUserData();
    bodyDef.position = new Vec2(body.getPosition());
    bodyDef.angle = body.getAngle();
    bodyDef.linearVelocity = new Vec2(body.getLinearVelocity());
    bodyDef.angularVelocity = body.getAngularVelocity();
    bodyDef.linearDamping = body.getLinearDamping();
    bodyDef.angularDamping = body.getAngularDamping();
    bodyDef.allowSleep = (body.m_flags & Body.e_autoSleepFlag) > 0;
    bodyDef.awake = body.isAwake();
    bodyDef.fixedRotation = (body.m_flags & Body.e_fixedRotationFlag) > 0;
    bodyDef.bullet = body.isBullet();
    bodyDef.type = body.getType();
    bodyDef.active = body.isActive();
    bodyDef.gravityScale = body.getGravityScale();
    return bodyDef;
  }

  /**
   * Clones the {@link BodyDefPojo} from the given @{link BodyDefPojo}.
   *
   * @param bodyDefPojo
   *          the @{link BodyDefPojo} to clone from
   * @return cloned @{link BodyDef}
   */
  public static BodyDef getBodyDef(final BodyDefPojo bodyDefPojo) {
    final BodyDef bodyDef = new BodyDef();
    bodyDef.userData = bodyDefPojo.userData;
    bodyDef.position = new Vec2(bodyDefPojo.position);
    bodyDef.angle = bodyDefPojo.angle;
    bodyDef.linearVelocity = new Vec2(bodyDefPojo.linearVelocity);
    bodyDef.angularVelocity = bodyDefPojo.angularVelocity;
    bodyDef.linearDamping = bodyDefPojo.linearDamping;
    bodyDef.angularDamping = bodyDefPojo.angularDamping;
    bodyDef.allowSleep = bodyDefPojo.allowSleep;
    bodyDef.awake = bodyDefPojo.awake;
    bodyDef.fixedRotation = bodyDefPojo.fixedRotation;
    bodyDef.bullet = bodyDefPojo.bullet;
    bodyDef.type = bodyDefPojo.type;
    bodyDef.active = bodyDefPojo.active;
    bodyDef.gravityScale = bodyDefPojo.gravityScale;
    return bodyDef;
  }

  /**
   * Clones the {@link BodyDefPojo} from the given @{link BodyDef}.
   *
   * @param bodyDef
   *          the @{link BodyDef} to clone from
   * @return cloned @{link BodyDefPojo}
   */
  public static BodyDefPojo getBodyDefPojo(final BodyDef bodyDef) {
    final BodyDefPojo bodyDefPojo = new BodyDefPojo();
    bodyDefPojo.userData = (ObjectId) bodyDef.userData;
    bodyDefPojo.position = new Vec2(bodyDef.position);
    bodyDefPojo.angle = bodyDef.angle;
    bodyDefPojo.linearVelocity = new Vec2(bodyDef.linearVelocity);
    bodyDefPojo.angularVelocity = bodyDef.angularVelocity;
    bodyDefPojo.linearDamping = bodyDef.linearDamping;
    bodyDefPojo.angularDamping = bodyDef.angularDamping;
    bodyDefPojo.allowSleep = bodyDef.allowSleep;
    bodyDefPojo.awake = bodyDef.awake;
    bodyDefPojo.fixedRotation = bodyDef.fixedRotation;
    bodyDefPojo.bullet = bodyDef.bullet;
    bodyDefPojo.type = bodyDef.type;
    bodyDefPojo.active = bodyDef.active;
    bodyDefPojo.gravityScale = bodyDef.gravityScale;
    return bodyDefPojo;
  }

  /**
   * Get the energy of a {@link Body}
   *
   * @param body
   *          the {@link Body} to get the energy for.
   * @return the total energy
   */
  public static float getEnergy(final Body body) {
    return 0.5f * body.getMass() * body.getLinearVelocity().length()
        * body.getLinearVelocity().length();
  }

  public static float getRadiusFromMass(final float mass) {
    return (float) Math.sqrt(mass / (Globals.DENSITY * Math.PI));
  }

  private BodyUtils() {
  }
}
