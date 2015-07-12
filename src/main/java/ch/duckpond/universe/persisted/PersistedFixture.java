package ch.duckpond.universe.persisted;

import ch.duckpond.universe.utils.box2d.FixtureUtils;

import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

@Entity
public class PersistedFixture extends PersistedObject<Fixture> {

  private FixtureDef          fixtureDef;

  @Reference
  private final PersistedBody persistedBody;

  /**
   * Constructor.
   *
   * @param fixture
   *          the @{link Fixture} to persist.
   * @param persistedBody
   *          the {@link PersistedBody} the @{link Fixture} lives in.
   * @param datastore
   *          the @{link Datastore} to save this object in.
   */
  public PersistedFixture(final Fixture fixture, final PersistedBody persistedBody,
      final Datastore datastore) {
    super(fixture, datastore);
    if (persistedBody == null) {
      throw new IllegalArgumentException("persistedBody == null");
    }
    this.persistedBody = persistedBody;
    get().setUserData(getId());
    save();
  }

  @PrePersist
  private void prePersist() {
    fixtureDef = FixtureUtils.getFixtureDef(get());
  }

  @Override
  protected Fixture construct() {
    return persistedBody.get().createFixture(fixtureDef);
  }

}
