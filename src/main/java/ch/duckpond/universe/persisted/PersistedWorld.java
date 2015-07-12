package ch.duckpond.universe.persisted;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

import java.util.Set;
import java.util.TreeSet;

@Entity
public class PersistedWorld extends PersistedObject<World> {

  private Vec2                      gravity;

  @Reference
  private final Set<PersistedBody>  bodies = new TreeSet<>();

  @Reference
  private final Set<PersistedJoint> joints = new TreeSet<>();

  /**
   * Constructor.
   *
   * @param world
   *          the @{link World} to persist.
   * @param datastore
   *          the @{link Datastore} to save this object in.
   */
  public PersistedWorld(final World world, final Datastore datastore) {
    super(world, datastore);
    save();
  }

  @PostLoad
  private void postLoad() {
    bodies.stream().forEach(body -> {
      body.get();
    });
    joints.stream().forEach(joint -> {
      joint.get();
    });
  }

  @PrePersist
  private void prePersist() {
    gravity = get().getGravity();
    if (getId() != null) {
      for (Body i = get().getBodyList(); i != null; i = i.getNext()) {
        // no user data: not persisted yet
        if (i.getUserData() == null) {
          bodies.add(new PersistedBody(i, this, getDatastore()));
        }
      }
      for (Joint i = get().getJointList(); i != null; i = i.getNext()) {
        // no user data: not persisted yet
        if (i.getUserData() == null) {
          joints.add(new PersistedJoint(i, this, getDatastore()));
        }
      }
    }
  }

  @Override
  protected World construct() {
    return new World(gravity);
  }
}
