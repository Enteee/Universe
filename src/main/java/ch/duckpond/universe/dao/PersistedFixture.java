package ch.duckpond.universe.dao;

import ch.duckpond.universe.pojo.FixtureDefPojo;
import ch.duckpond.universe.utils.box2d.FixtureUtils;

import org.jbox2d.dynamics.Fixture;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Reference;

@Entity
public class PersistedFixture extends PersistedObject<Fixture> {

  private FixtureDefPojo fixtureDefPojo;

  @Reference
  private PersistedBody persistedBody;

  /**
   * Morphia constructor.
   */
  @SuppressWarnings("unused")
  private PersistedFixture() {
  }

  /**
   * Constructor.
   *
   * @param fixture
   *          the @{link Fixture} to persist.
   * @param persistedBody
   *          the {@link PersistedBody} the @{link Fixture} lives in.
   * @param datastore
   *          the @{link CachedDatastore} to save this object in.
   */
  public PersistedFixture(final Fixture fixture, final PersistedBody persistedBody,
      final CachedDatastore datastore) {
    super(fixture);
    if (persistedBody == null) {
      throw new IllegalArgumentException("persistedBody == null");
    }
    this.persistedBody = persistedBody;
    save(datastore);
  }

  @PrePersist
  private void prePersist() {
    fixtureDefPojo = FixtureUtils
        .getFixtureDefPojo(FixtureUtils.getFixtureDef(get(getDatastore())));
  }

  @Override
  protected Fixture construct() {
    return persistedBody.get(getDatastore())
        .createFixture(FixtureUtils.getFixtureDef(fixtureDefPojo));
  }

  @Override
  public void save(final CachedDatastore datastore) {
    super.save(datastore);
    get(datastore).setUserData(getId());
  }

  @Override
  public Fixture get(final CachedDatastore datastore) {
    final Fixture fixture = super.get(datastore);
    fixture.setUserData(getId());
    return fixture;
  }
}
