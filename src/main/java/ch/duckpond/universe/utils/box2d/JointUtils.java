package ch.duckpond.universe.utils.box2d;

import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;

public class JointUtils {

  /**
   * Clones a @{link {@link JointDef} from the given @{link Joint}.
   *
   * @param joint
   *          the @{link joint} from which to clone
   * @return cloned @{link FixtureDef}
   */
  public static JointDef getJointDef(final Joint joint) {
    final JointDef jointDef = new JointDef();
    jointDef.type = joint.getType();
    jointDef.userData = joint.getUserData();
    jointDef.bodyA = joint.getBodyA();
    jointDef.bodyB = joint.getBodyB();
    jointDef.collideConnected = joint.getCollideConnected();
    return jointDef;
  }

  private JointUtils() {
  }

}
