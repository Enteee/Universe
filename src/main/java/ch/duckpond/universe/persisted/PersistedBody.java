package ch.duckpond.universe.persisted;

import ch.duckpond.universe.utils.box2d.BodyUtils;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

import java.util.Set;
import java.util.TreeSet;

@Entity
public class PersistedBody extends PersistedObject<Body> {

  private BodyDef                     bodyDef;

  @Reference
  private final PersistedWorld        persistedWorld;

  @Reference
  private final Set<PersistedFixture> fixtures = new TreeSet<>();

  /**
   * Constructor.
   *
   * @param body
   *          the @{link Body} to persist.
   * @param persistedWorld
   *          the {@link PersistedWorld} the @{link Body} lives in.
   * @param datastore
   *          the @{link Datastore} to save this object in.
   */
  public PersistedBody(final Body body, final PersistedWorld persistedWorld,
      final Datastore datastore) {
    super(body, datastore);
    if (persistedWorld == null) {
      throw new IllegalArgumentException("persistedWorld == null");
    }
    this.persistedWorld = persistedWorld;
    get().setUserData(getId());
    save();
  }

  @PostLoad
  private void postLoad() {
    fixtures.stream().forEach(fixture -> {
      fixture.get();
    });
  }

  @PrePersist
  private void prePersist() {
    bodyDef = BodyUtils.getBodyDef(get());
    if (getId() != null) {
      for (Fixture i = get().getFixtureList(); i != null; i = i.getNext()) {
        // no user data: not persisted yet
        if (i.getUserData() == null) {
          fixtures.add(new PersistedFixture(i, this, getDatastore()));
        }
      }
    }
  }

  @Override
  protected Body construct() {
    return persistedWorld.get().createBody(bodyDef);
  }

}
