package ch.duckpond.universe.persisted;

import ch.duckpond.universe.utils.box2d.BodyUtils;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import java.util.Set;
import java.util.TreeSet;

@Entity
public class PersistedBody extends PersistedObject {

  @Transient
  private Body                        body;

  private BodyDef                     bodyDef;

  @Reference
  private final Set<PersistedFixture> fixtures = new TreeSet<>();

  /**
   * Constructor.
   *
   * @param body
   *          the @{link Body} to persist.
   */
  public PersistedBody(final Body body, final Datastore datastore) {
    super(datastore);
    if (body == null) {
      throw new IllegalArgumentException("body == null");
    }
    this.body = body;
    save();
    this.body.setUserData(getId());
  }

  /**
   * Get the persisted @{link Body}.
   *
   * @param world
   *          the @{link World} in which the @{link Body} lives.
   * @return the persisted @{link Body}
   */
  public Body getBody(final World world) {
    if (body == null) {
      body = world.createBody(bodyDef);
      fixtures.stream().forEach(fixture -> {
        fixture.getFixture(body);
      });
    }
    return body;
  }

  @PrePersist
  private void prePersist() {
    if (body == null) {
      // this might happen when we try to persist a body which is not assigned
      // to a world.
      throw new RuntimeException("body == null");
    }
    bodyDef = BodyUtils.getBodyDef(body);
    for (Fixture i = body.getFixtureList(); i != null; i = i.getNext()) {
      // no user data: not persisted yet
      if (i.getUserData() == null) {
        fixtures.add(new PersistedFixture(i, getDatastore()));
      }
    }
  }

}
