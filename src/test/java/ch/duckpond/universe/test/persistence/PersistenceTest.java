package ch.duckpond.universe.test.persistence;

import ch.duckpond.universe.dao.LocalObjectRepository;
import ch.duckpond.universe.dao.PersistedBody;
import ch.duckpond.universe.dao.PersistedFixture;
import ch.duckpond.universe.dao.PersistedJoint;
import ch.duckpond.universe.dao.PersistedWorld;
import ch.duckpond.universe.simulation.Simulation;
import ch.duckpond.universe.test.utils.TestUtilsBody;
import ch.duckpond.universe.utils.box2d.BodyUtils;

import com.mongodb.MongoClient;

import org.bson.types.ObjectId;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PersistenceTest {

  private static Datastore     datastore;
  private static final Morphia morphia = new Morphia();

  private PersistedWorld       persistedWorld;

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

  private void dropData() {
    // empty database
    datastore.delete(datastore.createQuery(PersistedFixture.class));
    datastore.delete(datastore.createQuery(PersistedBody.class));
    datastore.delete(datastore.createQuery(PersistedJoint.class));
    datastore.delete(datastore.createQuery(PersistedWorld.class));
  }

  @Before
  public void init() {
    dropData();
  }

  @After
  public void tearDown() {
    dropData();
  }

  @Test
  public final void testPopulateWorld() {
    final World world = new World(new Vec2());
    persistedWorld = new PersistedWorld(world, datastore);
    Assert.assertNotNull(LocalObjectRepository.getInstance().get(World.class,
        persistedWorld.getId()));
    for (int i = 0; i < 100; i++) {
      final Body body = TestUtilsBody.randomBody(persistedWorld.get());
      persistedWorld.save(datastore);
      Assert.assertEquals(i + 1, datastore.getCollection(PersistedBody.class).count());
      Assert.assertNotNull(LocalObjectRepository.getInstance().get(Body.class,
          (ObjectId) body.getUserData()));
    }
  }

  @Test
  public final void testSaveGetBody() {
    final World world = new World(new Vec2());
    persistedWorld = new PersistedWorld(world, datastore);
    final Body body = TestUtilsBody.randomBody(world);
    persistedWorld.save(datastore);
    datastore.get(PersistedBody.class, body.getUserData());
  }

  @Test
  public final void testPersistSimulation() {
    testPopulateWorld();
    final Simulation simulation = new Simulation();
    // save body defs
    final Map<ObjectId, BodyDef> bodyDefsBefore = new HashMap<>();
    for (Body i = persistedWorld.get().getBodyList(); i != null; i = i.getNext()) {
      bodyDefsBefore.put((ObjectId) i.getUserData(), BodyUtils.getBodyDef(i));
    }
    simulation.update(persistedWorld.get());
    persistedWorld.save(datastore);
    // get new body defs of persisted objects
    for (final Entry<ObjectId, BodyDef> entry : bodyDefsBefore.entrySet()) {
      final PersistedBody body = datastore.get(PersistedBody.class, entry.getKey());
      final BodyDef bodyDefOld = entry.getValue();
      final BodyDef bodyDefNew = BodyUtils.getBodyDef(body.get());
      // position should have changed
      Assert.assertTrue((bodyDefNew.position.x != bodyDefOld.position.x)
          || (bodyDefNew.position.y != bodyDefOld.position.y));
    }
  }
}
