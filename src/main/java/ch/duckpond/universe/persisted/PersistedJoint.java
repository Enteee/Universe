package ch.duckpond.universe.persisted;

import ch.duckpond.universe.utils.box2d.JointUtils;

import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

@Entity
public class PersistedJoint extends PersistedObject {

  @Transient
  private Joint    joint;

  private JointDef jointDef;

  /**
   * Constructor.
   *
   * @param joint
   *          the @{link Joint} to persist.
   */
  public PersistedJoint(final Joint joint, final Datastore datastore) {
    super(datastore);
    if (joint == null) {
      throw new IllegalArgumentException("joint == null");
    }
    this.joint = joint;
    save();
    joint.setUserData(getId());
  }

  /**
   * Get the persisted @{link Joint}.
   *
   * @param world
   *          the @{link World} in which the @{link Joint} lives.
   * @return the persisted @{link Joint}
   */
  public Joint getJoint(final World world) {
    if (joint == null) {
      joint = world.createJoint(jointDef);
    }
    return joint;
  }

  @PrePersist
  private void prePersist() {
    if (joint == null) {
      throw new RuntimeException("joint == null");
    }
    jointDef = JointUtils.getJointDef(joint);
  }

}
