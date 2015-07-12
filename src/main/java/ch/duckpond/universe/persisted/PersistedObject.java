package ch.duckpond.universe.persisted;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

public abstract class PersistedObject implements Comparable<PersistedObject> {

  /**
   * The @{link Datastore} in which the object will be persisted.
   */
  @Transient
  private final Datastore datastore;

  @Id
  private ObjectId        id;

  /**
   * Constructor.
   *
   * @param datastore
   *          the @{link Datastore} in which the object will be persisted.
   */
  protected PersistedObject(final Datastore datastore) {
    if (datastore == null) {
      throw new IllegalArgumentException("datastore == null");
    }
    this.datastore = datastore;
  }

  @Override
  public int compareTo(final PersistedObject other) {
    if (other == null) {
      throw new IllegalArgumentException("other == null");
    }
    return getId().compareTo(other.getId());
  }

  /**
   * Persist / save the object.
   */
  public void save() {
    datastore.save(this);
  }

  protected Datastore getDatastore() {
    return datastore;
  }

  protected ObjectId getId() {
    return id;
  }
}
