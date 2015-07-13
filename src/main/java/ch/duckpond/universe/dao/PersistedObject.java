package ch.duckpond.universe.dao;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.lang.reflect.ParameterizedType;
import java.util.NoSuchElementException;

public abstract class PersistedObject<T> implements Comparable<PersistedObject<?>> {

  @Transient
  private T              persistedObject;
  @Transient
  private final Class<?> persistedObjectClass;

  @Transient
  private Datastore      datastore;

  @Id
  private ObjectId       id;

  /**
   * Default constructor.
   */
  protected PersistedObject() {
    // http://blog.xebia.com/2009/02/07/acessing-generic-types-at-runtime-in-java/
    persistedObjectClass = ((Class<?>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0]);
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
   * Get the @{link Object} again.
   * 
   * @return the @{link Object}
   */
  @SuppressWarnings("unchecked")
  public T get() {
    if (persistedObject == null) {
      // this happens when the @{link PersistedObject} is loaded from a
      // @{link Datastore}
      try {
        // get the local known object
        persistedObject = (T) LocalObjectRepository.getInstance()
            .get(persistedObjectClass, getId());
      } catch (final NoSuchElementException e) {
        // object locally unknown: construct a new object
        persistedObject = construct();
        LocalObjectRepository.getInstance().save(getId(), persistedObject);
      }
    }
    return persistedObject;
  }

  /**
   * Get the @{link Datastore} of this @{link Object}.
   * 
   * @return the datastore of this @{link Object}
   * @throws RuntimeException
   *           if the @{link Object} was never saved to a @{link Datastore}
   */
  public Datastore getDatastore() {
    if (datastore == null) {
      throw new RuntimeException("Tried to get datastore of a never saved object");
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
   */
  public void save(final Datastore datastore) {
    setDatastore(datastore);
    if (id == null) {
      // save twice: 1. get id, 2. save references
      getDatastore().save(this);
    }
    getDatastore().save(this);
    LocalObjectRepository.getInstance().save(getId(), persistedObject);
  }

  private void setDatastore(final Datastore datastore) {
    this.datastore = datastore;
  }

  /**
   * Constructs the persisted object.
   * 
   * @return a new @{link Object}.
   */
  protected abstract T construct();

}
