package ch.duckpond.universe.dao;

import com.mongodb.MongoClient;

import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;

public class CachedDatastore extends DatastoreImpl {

  private final PersistedObjectCache persistedObjectCache = new PersistedObjectCache();

  /**
   * @see DatastoreImpl#DatastoreImpl(Morphia, Mapper, MongoClient, String).
   */
  public CachedDatastore(final Morphia morphia, final Mapper mapper, final MongoClient mongoClient,
      final String dbName) {
    super(morphia, mapper, mongoClient, dbName);
  }

  /**
   * @see DatastoreImpl#DatastoreImpl(Morphia, MongoClient, String).
   */
  public CachedDatastore(final Morphia morphia, final MongoClient mongoClient,
      final String dbName) {
    super(morphia, mongoClient, dbName);
  }

  public PersistedObjectCache getCache() {
    return persistedObjectCache;
  }

}
