package ch.duckpond.universe.test.persistence;

import ch.duckpond.universe.dao.CachedDatastore;
import ch.duckpond.universe.dao.PersistedObject;

import org.mongodb.morphia.annotations.PrePersist;

public class PersistedDummy extends PersistedObject<Object> {

  private String string;

  public PersistedDummy(final Object data, final CachedDatastore datastore) {
    super(data);
    save(datastore);
  }

  @PrePersist
  private void prePersist() {
    string = get(getDatastore()).toString();
  }

  @Override
  protected Object construct() {
    return string;
  }

}
