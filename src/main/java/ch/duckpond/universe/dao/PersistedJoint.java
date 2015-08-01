package ch.duckpond.universe.dao;

import ch.duckpond.universe.pojo.JointDefPojo;
import ch.duckpond.universe.utils.box2d.JointUtils;

import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

@Entity
public class PersistedJoint extends PersistedObject<Joint> {

  private JointDefPojo  jointDefPojo;
  private JointType     type;
  private Object        userData;
  @Reference
  private PersistedBody bodyA;
  @Reference
  private PersistedBody bodyB;
  private boolean       collideConnected;

  @Reference
  private PersistedWorld persistedWorld;

  /**
   * Morphia constructor.
   */
  @SuppressWarnings("unused")
  private PersistedJoint() {
  }

  /**
   * Constructor.
   *
   * @param joint
   *          the @{link Joint} to persist.
   * @param persistedWorld
   *          the {@link PersistedWorld} the @{link Joint} lives in.
   * @param datastore
   *          the @{link CachedDatastore} to save this object in.
   */
  public PersistedJoint(final Joint joint, final PersistedWorld persistedWorld,
      final CachedDatastore datastore) {
    super(joint);
    if (persistedWorld == null) {
      throw new IllegalArgumentException("persistedWorld == null");
    }
    this.persistedWorld = persistedWorld;
    save(datastore);

  }

  @PrePersist
  private void prePersist() {
    final JointDef jointDef = JointUtils.getJointDef(get(getDatastore()));
    type = jointDef.type;
    userData = jointDef.userData;
    bodyA = new PersistedBody(jointDef.bodyA, persistedWorld, getDatastore());
    bodyB = new PersistedBody(jointDef.bodyB, persistedWorld, getDatastore());
    collideConnected = jointDef.collideConnected;
  }

  @Override
  protected Joint construct() {
    final JointDef jointDef = new JointDef();
    jointDef.type = type;
    jointDef.userData = userData;
    jointDef.bodyA = bodyA.get(getDatastore());
    jointDef.bodyB = bodyB.get(getDatastore());
    jointDef.collideConnected = collideConnected;
    return persistedWorld.get(getDatastore()).createJoint(jointDef);
  }

  @Override
  public void save(final CachedDatastore datastore) {
    super.save(datastore);
    get(datastore).setUserData(getId());
  }

  @Override
  public Joint get(final CachedDatastore datastore) {
    final Joint joint = super.get(getDatastore());
    joint.setUserData(getId());
    return joint;
  }
}
