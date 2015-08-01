package ch.duckpond.universe.utils.box2d;

import ch.duckpond.universe.dao.CachedDatastore;
import ch.duckpond.universe.dao.PersistedBody;
import ch.duckpond.universe.dao.PersistedWorld;
import ch.duckpond.universe.pojo.JointDefPojo;

import org.bson.types.ObjectId;
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
    jointDef.userData = joint.getUserData();
    jointDef.type = joint.getType();
    jointDef.bodyA = joint.getBodyA();
    jointDef.bodyB = joint.getBodyB();
    jointDef.collideConnected = joint.getCollideConnected();
    return jointDef;
  }

  /**
   * Clones a @{link {@link JointDef} from the given @{link JointDefPojo}.
   *
   * @param jointDefPojo
   *          the @{link JointDefPojo} from which to clone
   * @param datastore
   *          the @{link CachedDatastore} from where to load additional data
   * @return cloned @{link JointDef}
   */
  public static JointDef getJointDef(final JointDefPojo jointDefPojo,
      final CachedDatastore datastore) {
    final JointDef jointDef = new JointDef();
    jointDef.userData = jointDefPojo.userData;
    jointDef.type = jointDefPojo.type;
    jointDef.bodyA = jointDefPojo.bodyA.get(datastore);
    jointDef.bodyB = jointDefPojo.bodyB.get(datastore);
    jointDef.collideConnected = jointDefPojo.collideConnected;
    return jointDef;
  }

  /**
   * Clones a @{link {@link JointDefPojo} from the given @{link JointDef}.
   *
   * @param jointDef
   *          the @{link JointDef} from which to clone
   * @param persistedWorld
   *          the {@link PersistedWorld} where the {@link Joint} lives in.
   * @param datastore
   *          the @{link CachedDatastore} from where to load additional data
   * @return cloned @{link JointDefPojo}
   */
  public static JointDefPojo getJointDefPojo(final JointDef jointDef,
      final PersistedWorld persistedWorld, final CachedDatastore datastore) {
    final JointDefPojo jointDefPojo = new JointDefPojo();
    jointDefPojo.userData = (ObjectId) jointDef.userData;
    jointDefPojo.type = jointDef.type;
    jointDefPojo.bodyA = new PersistedBody(jointDef.bodyA, persistedWorld, datastore);
    jointDefPojo.bodyB = new PersistedBody(jointDef.bodyB, persistedWorld, datastore);
    jointDefPojo.collideConnected = jointDef.collideConnected;
    return jointDefPojo;
  }

  private JointUtils() {
  }

}
