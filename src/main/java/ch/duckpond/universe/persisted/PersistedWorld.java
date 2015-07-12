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
import org.mongodb.morphia.annotations.Transient;

import java.util.Set;
import java.util.TreeSet;

@Entity
public class PersistedWorld extends PersistedObject {

  @Reference
  private final Set<PersistedBody>  bodies = new TreeSet<>();

  private Vec2                      gravity;

  @Reference
  private final Set<PersistedJoint> joints = new TreeSet<>();

  @Transient
  private World                     world;

  /**
   * Constructor.
   *
   * @param world
   *          the @{link World} to persist
   */
  public PersistedWorld(final World world, final Datastore datastore) {
    super(datastore);
    if (world == null) {
      throw new IllegalArgumentException("world == null");
    }
    this.world = world;
    save();
  }

  /**
   * Get the persisted @{link World}.
   *
   * @return the persisted @{link World}
   */
  public World getWorld() {
    return world;
  }

  @PostLoad
  private void postLoad() {
    world = new World(gravity);
    bodies.stream().forEach(body -> {
      body.getBody(world);
    });
    joints.stream().forEach(joint -> {
      joint.getJoint(world);
    });
  }

  @PrePersist
  private void prePersist() {
    gravity = world.getGravity();
    for (Body i = world.getBodyList(); i != null; i = i.getNext()) {
      // no user data: not persisted yet
      if (i.getUserData() == null) {
        bodies.add(new PersistedBody(i, getDatastore()));
      }
    }
    for (Joint i = world.getJointList(); i != null; i = i.getNext()) {
      // no user data: not persisted yet
      if (i.getUserData() == null) {
        joints.add(new PersistedJoint(i, getDatastore()));
      }
    }
  }
}
