package ch.duckpond.universe.dao;

import ch.duckpond.universe.pojo.DistanceJointDefPojo;
import ch.duckpond.universe.utils.box2d.DistanceJointUtils;

import org.jbox2d.dynamics.joints.DistanceJoint;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

/**
 * DAO for {@link PersistedDistanceJoint} TODO: this DAO can not be loaded
 * properly.
 * 
 * @author ente
 */
@Entity
public class PersistedDistanceJoint extends PersistedObject<DistanceJoint> {

  private DistanceJointDefPojo distanceJointDefPojo;

  @Reference
  private PersistedWorld persistedWorld;

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
  public PersistedDistanceJoint(final DistanceJoint joint, final PersistedWorld persistedWorld,
      final CachedDatastore datastore) {
    super(joint);
    if (persistedWorld == null) {
      throw new IllegalArgumentException("persistedWorld == null");
    }
    this.persistedWorld = persistedWorld;
    save(datastore);

  }

  /**
   * Morphia constructor.
   */
  @SuppressWarnings("unused")
  private PersistedDistanceJoint() {
  }

  @Override
  public DistanceJoint get(final CachedDatastore datastore) {
    final DistanceJoint joint = super.get(datastore);
    joint.setUserData(getId());
    return joint;
  }

  @Override
  public void save(final CachedDatastore datastore) {
    super.save(datastore);
    get(datastore).setUserData(getId());
  }

  @PrePersist
  private void prePersist() {
    distanceJointDefPojo = DistanceJointUtils.getDistanceJointDefPojo(
        DistanceJointUtils.getDistanceJointDef(get(getDatastore())), persistedWorld,
        getDatastore());
  }

  @Override
  protected DistanceJoint construct() {
    return (DistanceJoint) persistedWorld.get(getDatastore())
        .createJoint(DistanceJointUtils.getDistanceJointDef(distanceJointDefPojo, getDatastore()));
  }
}
