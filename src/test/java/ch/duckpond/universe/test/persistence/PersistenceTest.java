package ch.duckpond.universe.test.persistence;

import ch.duckpond.universe.persisted.LocalObjectRepository;
import ch.duckpond.universe.persisted.PersistedBody;
import ch.duckpond.universe.persisted.PersistedFixture;
import ch.duckpond.universe.persisted.PersistedJoint;
import ch.duckpond.universe.persisted.PersistedWorld;
import ch.duckpond.universe.test.physics.UniverseTest;

import com.mongodb.MongoClient;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class PersistenceTest {

  private static Datastore     datastore;
  private static final Morphia morphia = new Morphia();

  /**
   * Setup db.
   * 
   * @throws Exception
   *           failed
   */
  @BeforeClass
  public static void beforeClass() throws Exception {
    // set up morphia
    morphia.mapPackage("ch.duckpond.universe.persisted");
    datastore = morphia.createDatastore(new MongoClient(), "test");
    datastore.ensureIndexes();
  }

  @After
  public void tearDown() {
    // empty database
    datastore.delete(datastore.createQuery(PersistedFixture.class));
    datastore.delete(datastore.createQuery(PersistedBody.class));
    datastore.delete(datastore.createQuery(PersistedJoint.class));
    datastore.delete(datastore.createQuery(PersistedWorld.class));
  }

  @Test
  public final void testAddWorld() {
    final World world = new World(new Vec2());
    final PersistedWorld persistedWorld = new PersistedWorld(world, datastore);
    Assert.assertNotNull(LocalObjectRepository.getInstance().get(World.class,
        persistedWorld.getId()));
    UniverseTest.addMasses(world);
    persistedWorld.save();
  }
}
