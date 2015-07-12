package ch.duckpond.universe.persisted;

import ch.duckpond.universe.utils.box2d.JointUtils;

import org.bson.types.ObjectId;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

@Entity
public class PersistedJoint extends PersistedObject<Joint> {

  public JointType             type;
  public Object                userData;
  private ObjectId             bodyA;
  private ObjectId             bodyB;
  public boolean               collideConnected;

  @Reference
  private final PersistedWorld persistedWorld;

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
    super(joint, datastore);
    if (persistedWorld == null) {
      throw new IllegalArgumentException("persistedWorld == null");
    }
    this.persistedWorld = persistedWorld;
    joint.setUserData(getId());
    save();
  }

  @PrePersist
  private void prePersist() {
    final JointDef jointDef = JointUtils.getJointDef(get());
    type = jointDef.type;
    userData = jointDef.userData;
    bodyA = (ObjectId) jointDef.bodyA.getUserData();
    bodyB = (ObjectId) jointDef.bodyB.getUserData();
    collideConnected = jointDef.collideConnected;
  }

  @Override
  protected Joint construct() {
    final JointDef jointDef = new JointDef();
    jointDef.type = type;
    jointDef.userData = userData;
    jointDef.bodyA = (Body) LocalObjectRepository.getInstance().get(Body.class, bodyA);
    jointDef.bodyB = (Body) LocalObjectRepository.getInstance().get(Body.class, bodyB);
    jointDef.collideConnected = collideConnected;
    return persistedWorld.get().createJoint(jointDef);
  }
}
