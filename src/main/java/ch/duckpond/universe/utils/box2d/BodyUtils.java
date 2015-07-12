package ch.duckpond.universe.utils.box2d;

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
    bodyDef.position = body.getPosition();
    bodyDef.angle = body.getAngle();
    bodyDef.linearVelocity = body.getLinearVelocity();
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

  private BodyUtils() {
  }
}
