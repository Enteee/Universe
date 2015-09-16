package ch.duckpond.universe.dao;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.DistanceJoint;
import org.jbox2d.dynamics.joints.Joint;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

import java.util.Set;
import java.util.TreeSet;

@Entity
public class PersistedWorld extends PersistedObject<World> {

  @Reference
  private final Set<PersistedBody> bodies = new TreeSet<>();

  private Vec2 gravity;

  @Reference
  private final Set<PersistedDistanceJoint> joints = new TreeSet<>();

  /**
   * Constructor.
   *
   * @param world
   *          the @{link World} to persist.
   * @param datastore
   *          the @{link CachedDatastore} to save this object in.
   */
  public PersistedWorld(final World world, final CachedDatastore datastore) {
    super(world);
    if (datastore == null) {
      throw new IllegalArgumentException("datasotre == null");
    }
    save(datastore);
  }

  /**
   * Morphia constructor.
   */
  @SuppressWarnings("unused")
  private PersistedWorld() {
  }

  @Override
  public void assemble(final World persistedObject) {
    bodies.stream().forEach(body -> {
      body.get(getDatastore());
    });
    joints.stream().forEach(joint -> {
      joint.get(getDatastore());
    });
    super.assemble(persistedObject);
  }

  @PrePersist
  private void prePersist() {
    gravity = get(getDatastore()).getGravity();
    if (getId() != null) {
      for (Body i = get(getDatastore()).getBodyList(); i != null; i = i.getNext()) {
        // no user data: not persisted yet
        if (i.getUserData() == null) {
          bodies.add(new PersistedBody(i, this, getDatastore()));
        }
      }
      for (Joint i = get(getDatastore()).getJointList(); i != null; i = i.getNext()) {
        // no user data: not persisted yet
        if (i.getUserData() == null) {
          switch (i.getType()) {
            case DISTANCE :
              joints.add(new PersistedDistanceJoint((DistanceJoint) i, this, getDatastore()));
              break;
            default :
              throw new RuntimeException(
                  String.format("Joint type: %s not implemented", i.getType()));
          }
        }
      }
      // save all bodies
      bodies.forEach(body -> {
        body.save(getDatastore());
      });
      // save all joints
      joints.forEach(joint -> {
        joint.save(getDatastore());
      });
    }
  }

  @Override
  protected World construct() {
    return new World(gravity);
  }

}
