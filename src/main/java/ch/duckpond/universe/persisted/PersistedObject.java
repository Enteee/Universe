package ch.duckpond.universe.persisted;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.lang.reflect.ParameterizedType;
import java.util.NoSuchElementException;

public abstract class PersistedObject<T> implements Comparable<PersistedObject<?>> {

  @Transient
  private final Datastore datastore;

  @Transient
  private T               persistedObject;
  @Transient
  private final Class<?>  persistedObjectClass;

  @Id
  private ObjectId        id;

  /**
   * Constructor.
   *
   * @param persistedObject
   *          the @{link Object} to persist.
   * @param datastore
   *          the @{link Datastore} in which the object will be persisted.
   */
  protected PersistedObject(final T persistedObject, final Datastore datastore) {
    if ((persistedObject == null) || (datastore == null)) {
      throw new IllegalArgumentException("(persistedObject == null) || (datastore == null)");
    }
    this.datastore = datastore;
    this.persistedObject = persistedObject;
    // http://blog.xebia.com/2009/02/07/acessing-generic-types-at-runtime-in-java/
    persistedObjectClass = ((Class<?>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0]);
  }

  @Override
  public int compareTo(final PersistedObject<?> other) {
    if (other == null) {
      throw new IllegalArgumentException("other == null");
    }
    return getId().compareTo(other.getId());
  }

  protected Datastore getDatastore() {
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
  public void save() {
    if (id == null) {
      // save twice: 1. get id, 2. save references
      datastore.save(this);
    }
    datastore.save(this);
    LocalObjectRepository.getInstance().save(getId(), persistedObject);
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
   * Constructs the persisted object.
   * 
   * @return a new @{link Object}.
   */
  protected abstract T construct();
}
