package ch.duckpond.universe.dao;

import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class is being used for fast lookup of {@link PersistedObject} by id.
 *
 * @author ente
 */
public class PersistedObjectCache {

  private final Map<Class<?>, Map<ObjectId, Object>> repositories = new HashMap<>();

  PersistedObjectCache() {
  }

  /**
   * Gets an @{link Objects} stored.
   *
   * @param objectClass
   *          the @{link Class} of the object to get.
   * @param id
   *          the id of the @{link Object} to get.
   * @return the @{link Object}, @{code null} if nothing found.
   */
  public Object get(final Class<?> objectClass, final ObjectId id) {
    if (!repositories.containsKey(objectClass)) {
      throw new NoSuchElementException("objectClass not found");
    }
    final Map<ObjectId, Object> objects = repositories.get(objectClass);
    if (!objects.containsKey(id)) {
      throw new NoSuchElementException("id not found");
    }
    return repositories.get(objectClass).get(id);
  }

  /**
   * Save an @{link Object} of a given @{link Class}.
   *
   * @param id
   *          the id to save this @{link Object} under.
   * @param object
   *          the @{link Object} to save.
   */
  public void save(final ObjectId id, final Object object) {
    final Class<?> objectClass = object.getClass();
    if (!repositories.containsKey(objectClass)) {
      repositories.put(objectClass, new HashMap<>());
    }
    final Map<ObjectId, Object> objects = repositories.get(objectClass);
    objects.put(id, object);
  }

}
