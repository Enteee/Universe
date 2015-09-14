package ch.duckpond.universe.utils.box2d;

import ch.duckpond.universe.dao.CachedDatastore;
import ch.duckpond.universe.dao.PersistedWorld;
import ch.duckpond.universe.pojo.DistanceJointDefPojo;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.DistanceJoint;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;

public class DistanceJointUtils {

  /**
   * Assembles the {@link DistanceJointDef} from the given {@link DistanceJoint}
   * .
   * 
   * @param distanceJoint
   *          the {@link DistanceJoint} to assemble from
   * @param distanceJointDef
   *          the {@link DistanceJointDef} to assemble
   */
  private static void assembleDistanceJointDef(final DistanceJoint distanceJoint,
      final DistanceJointDef distanceJointDef) {
    JointUtils.assembleJointDef(distanceJoint, distanceJointDef);
    distanceJointDef.localAnchorA.set(distanceJoint.getLocalAnchorA());
    distanceJointDef.localAnchorB.set(distanceJoint.getLocalAnchorB());
    distanceJointDef.length = distanceJoint.getLength();
    distanceJointDef.frequencyHz = distanceJoint.getFrequency();
    distanceJointDef.dampingRatio = distanceJoint.getDampingRatio();
  }

  /**
   * Assembles the {@link DistanceJointDef} from the given
   * {@link DistanceJointPojo} .
   * 
   * @param distanceJointDefPojo
   *          the {@link DistanceJointDefPojo} to assemble from
   * @param distanceJointDef
   *          the {@link DistanceJointDef} to assemble
   * @param datastore
   *          the @{link CachedDatastore} from where to load additional data
   */
  private static void assembleDistanceJointDef(final DistanceJointDefPojo distanceJointDefPojo,
      final DistanceJointDef distanceJointDef, final CachedDatastore datastore) {
    JointUtils.assembleJointDef(distanceJointDefPojo, distanceJointDef, datastore);
    distanceJointDef.localAnchorA.set(distanceJointDefPojo.localAnchorA);
    distanceJointDef.localAnchorB.set(distanceJointDefPojo.localAnchorB);
    distanceJointDef.length = distanceJointDefPojo.length;
    distanceJointDef.frequencyHz = distanceJointDefPojo.frequencyHz;
    distanceJointDef.dampingRatio = distanceJointDefPojo.dampingRatio;
  }

  /**
   * Assembles a {@link DistanceJointDefPojo} from the given @{link
   * DistanceJointDef}.
   *
   * @param distanceJointDef
   *          the @{link DistanceJointDef} to assemble from
   * @param distanceJointDefPojo
   *          the @{DistanceJointDefPojo} to assemble
   * @param persistedWorld
   *          the {@link PersistedWorld} where the {@link Joint} lives in.
   * @param datastore
   *          the @{link CachedDatastore} from where to load additional data
   * @return cloned @{link DistanceJointDefPojo}
   */
  private static void assembleDistanceJointDefPojo(final DistanceJointDef distanceJointDef,
      final DistanceJointDefPojo distanceJointDefPojo, final PersistedWorld persistedWorld,
      final CachedDatastore datastore) {
    JointUtils.assembleJointDefPojo(distanceJointDef, distanceJointDefPojo, persistedWorld,
        datastore);
    distanceJointDefPojo.localAnchorA = new Vec2(distanceJointDef.localAnchorA);
    distanceJointDefPojo.localAnchorB = new Vec2(distanceJointDef.localAnchorB);
    distanceJointDefPojo.length = distanceJointDef.length;
    distanceJointDefPojo.frequencyHz = distanceJointDef.frequencyHz;
    distanceJointDefPojo.dampingRatio = distanceJointDef.dampingRatio;
  }

  /**
   * Clones a {@link DistanceJointDef} from the given {@link DistanceJoint}.
   *
   * @param distanceJoint
   *          the {@link DistanceJoint}} from which to clone
   * @return cloned {@link DistanceJointDef}
   */
  public static DistanceJointDef getDistanceJointDef(final DistanceJoint distanceJoint) {
    final DistanceJointDef distanceJointDef = new DistanceJointDef();
    assembleDistanceJointDef(distanceJoint, distanceJointDef);
    return distanceJointDef;
  }

  /**
   * Clones a {@link DistanceJointDef} from the given @{link
   * DistanceJointDefPojo}.
   *
   * @param distanceJointDefPojo
   *          the @{link DistanceJointDefPojo} from which to clone
   * @param datastore
   *          the @{link CachedDatastore} from where to load additional data
   * @return cloned @{link DistanceJointDef}
   */
  public static DistanceJointDef getDistanceJointDef(
      final DistanceJointDefPojo distanceJointDefPojo, final CachedDatastore datastore) {
    final DistanceJointDef distanceJointDef = new DistanceJointDef();
    assembleDistanceJointDef(distanceJointDefPojo, distanceJointDef, datastore);
    return distanceJointDef;
  }

  /**
   * Clones a {@link DistanceJointDefPojo} from the given @{link
   * DistanceJointDef}.
   *
   * @param distanceJointDef
   *          the @{link DistanceJointDef} from which to clone
   * @param persistedWorld
   *          the {@link PersistedWorld} where the {@link Joint} lives in.
   * @param datastore
   *          the @{link CachedDatastore} from where to load additional data
   * @return cloned @{link DistanceJointDefPojo}
   */
  public static DistanceJointDefPojo getDistanceJointDefPojo(
      final DistanceJointDef distanceJointDef, final PersistedWorld persistedWorld,
      final CachedDatastore datastore) {
    final DistanceJointDefPojo distanceJointDefPojo = new DistanceJointDefPojo();
    assembleDistanceJointDefPojo(distanceJointDef, distanceJointDefPojo, persistedWorld, datastore);
    return distanceJointDefPojo;
  }

  private DistanceJointUtils() {
  }

}
