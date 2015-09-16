package ch.duckpond.universe.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.lang.reflect.ParameterizedType;
import java.util.NoSuchElementException;

public abstract class PersistedObject<T> implements Comparable<PersistedObject<?>> {

  @Transient
  private CachedDatastore datastore;
  @Id
  private ObjectId        id;

  @Transient
  private T persistedObject;

  @Transient
  private final Class<?> persistedObjectClass;

  /**
   * Default constructor.
   */
  protected PersistedObject() {
    // http://blog.xebia.com/2009/02/07/acessing-generic-types-at-runtime-in-java/
    persistedObjectClass = (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
  }

  /**
   * Constructor and persist.
   *
   * @param persistedObject
   *          the @{link Object} to persist.
   */
  protected PersistedObject(final T persistedObject) {
    this();
    if (persistedObject == null) {
      throw new IllegalArgumentException("persistedObject == null");
    }
    this.persistedObject = persistedObject;
  }

  @Override
  public int compareTo(final PersistedObject<?> other) {
    if (other == null) {
      throw new IllegalArgumentException("other == null");
    }
    return getId().compareTo(other.getId());
  }

  /**
   * Get the T which is stored in this @{link PersistedObject}.
   *
   * @param datastore
   *          to get from
   * @return T
   */
  @SuppressWarnings("unchecked")
  public T get(final CachedDatastore datastore) {
    setDatastore(datastore);
    if (persistedObject == null) {
      // this happens when the @{link PersistedObject} is loaded from a
      // @{link Datastore}
      try {
        // get the local known object
        persistedObject = (T) getDatastore().getCache().get(persistedObjectClass, getId());
      } catch (final NoSuchElementException e) {
        // object locally unknown: construct a new object
        persistedObject = construct();
        getDatastore().getCache().save(getId(), persistedObject);
        assemble(persistedObject);
      }
    }
    return persistedObject;
  }

  /**
   * Get the {@link CachedDatastore} of this @{link Object}.
   *
   * @return the {@link CachedDatastore} of this @{link Object}
   * @throws RuntimeException
   *           if the @{link Object} was never saved to a
   *           {@link CachedDatastore}
   */
  public CachedDatastore getDatastore() {
    if (datastore == null) {
      throw new RuntimeException("Tried to get the datastore of a never saved object");
    }
    return datastore;
  }

  /**
   * Get the id of this @{link PersistedObject}.
   *
   * @return the id
   */
  public ObjectId getId() {
    return id;
  }

  /**
   * Persist / save the object.
   *
   * @param datastore
   *          to save into
   */
  public void save(final CachedDatastore datastore) {
    setDatastore(datastore);
    if (id == null) {
      // save twice: 1. get id, 2. save references
      getDatastore().save(this);
    }
    getDatastore().save(this);
    getDatastore().getCache().save(getId(), persistedObject);
  }

  private void setDatastore(final CachedDatastore datastore) {
    this.datastore = datastore;
  }

  /**
   * Assembles the @{link PersistedObject}.
   */
  protected void assemble(final T persistedObject) {
  }

  /**
   * Constructs / create a new T out of the @{link PersistedObject} data.
   *
   * @return a new @{link Object}.
   */
  protected abstract T construct();

}
