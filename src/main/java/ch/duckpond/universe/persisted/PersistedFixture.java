package ch.duckpond.universe.persisted;

import ch.duckpond.universe.utils.box2d.FixtureUtils;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

@Entity
public class PersistedFixture extends PersistedObject {

  @Transient
  private Fixture    fixture;

  private FixtureDef fixtureDef;

  /**
   * Constructor.
   *
   * @param fixture
   *          the @{link Fixture} to persist.
   */
  public PersistedFixture(final Fixture fixture, final Datastore datastore) {
    super(datastore);
    if (fixture == null) {
      throw new IllegalArgumentException("fixture == null");
    }
    this.fixture = fixture;
    save();
    this.fixture.setUserData(getId());
  }

  /**
   * Get the persisted @{link Fixture}.
   *
   * @param body
   *          the @{link Body} in which the @{link Fixture} lives.
   * @return the persisted @{link Fixture}
   */
  public Fixture getFixture(final Body body) {
    if (fixture == null) {
      fixture = body.createFixture(fixtureDef);
    }
    return fixture;
  }

  @PrePersist
  private void prePersist() {
    if (fixture == null) {
      // this might happen when we try to persist a fixture which is not
      // assigned to a body.
      throw new RuntimeException("fixture == null");
    }
    fixtureDef = FixtureUtils.getFixtureDef(fixture);
  }

}
