package ch.duckpond.universe.dao;

import ch.duckpond.universe.utils.box2d.JointUtils;

import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

@Entity
public class PersistedJoint extends PersistedObject<Joint> {

  public JointType       type;
  public Object          userData;
  @Reference
  private PersistedBody  bodyA;
  @Reference
  private PersistedBody  bodyB;
  public boolean         collideConnected;

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
   *          the @{link Datastore} to save this object in.
   */
  public PersistedJoint(final Joint joint, final PersistedWorld persistedWorld,
      final Datastore datastore) {
    super(joint);
    if (persistedWorld == null) {
      throw new IllegalArgumentException("persistedWorld == null");
    }
    this.persistedWorld = persistedWorld;
    save(datastore);
    get().setUserData(getId());
  }

  @PrePersist
  private void prePersist() {
    final JointDef jointDef = JointUtils.getJointDef(get());
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
    jointDef.bodyA = bodyA.get();
    jointDef.bodyB = bodyB.get();
    jointDef.collideConnected = collideConnected;
    return persistedWorld.get().createJoint(jointDef);
  }
}
