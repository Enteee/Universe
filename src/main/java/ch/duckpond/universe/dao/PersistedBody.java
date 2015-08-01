package ch.duckpond.universe.dao;

import ch.duckpond.universe.pojo.BodyDefPojo;
import ch.duckpond.universe.utils.box2d.BodyUtils;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

import java.util.Set;
import java.util.TreeSet;

@Entity
public class PersistedBody extends PersistedObject<Body> {

  private BodyDefPojo bodyDefPojo;

  @Reference
  private PersistedWorld persistedWorld;

  @Reference
  private final Set<PersistedFixture> fixtures = new TreeSet<>();

  /**
   * Morphia constructor.
   */
  @SuppressWarnings("unused")
  private PersistedBody() {
  }

  /**
   * Constructor.
   *
   * @param body
   *          the @{link Body} to persist.
   * @param persistedWorld
   *          the {@link PersistedWorld} the @{link Body} lives in.
   * @param datastore
   *          the @{link CachedDatastore} to save this object in.
   */
  public PersistedBody(final Body body, final PersistedWorld persistedWorld,
      final CachedDatastore datastore) {
    super(body);
    if (persistedWorld == null) {
      throw new IllegalArgumentException("persistedWorld == null");
    }
    this.persistedWorld = persistedWorld;
    save(datastore);
  }

  @PrePersist
  private void prePersist() {
    bodyDefPojo = BodyUtils.getBodyDefPojo(BodyUtils.getBodyDef(get(getDatastore())));
    if (getId() != null) {
      for (Fixture fixture = get(getDatastore())
          .getFixtureList(); fixture != null; fixture = fixture.getNext()) {
        // no user data: not persisted yet
        if (fixture.getUserData() == null) {
          fixtures.add(new PersistedFixture(fixture, this, getDatastore()));
        }
      }
    }
    bodyDefPojo.userData = null;
  }

  @Override
  protected Body construct() {
    return persistedWorld.get(getDatastore()).createBody(BodyUtils.getBodyDef(bodyDefPojo));
  }

  @Override
  protected void assemble(final Body persistedBody) {
    fixtures.stream().forEach(fixture -> {
      fixture.get(getDatastore());
    });
    super.assemble(persistedBody);
  }

  @Override
  public void save(final CachedDatastore datastore) {
    super.save(datastore);
    get(datastore).setUserData(getId());
  }

  @Override
  public Body get(final CachedDatastore datastore) {
    final Body body = super.get(getDatastore());
    body.setUserData(getId());
    return body;
  }

}
