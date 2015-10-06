package ch.duckpond.universe.test.persistence;

import ch.duckpond.universe.server.dao.CachedDatastore;
import ch.duckpond.universe.server.dao.PersistedBody;
import ch.duckpond.universe.server.dao.PersistedDistanceJoint;
import ch.duckpond.universe.server.dao.PersistedFixture;
import ch.duckpond.universe.server.dao.PersistedWorld;
import ch.duckpond.universe.shared.simulation.Simulation;
import ch.duckpond.universe.test.utils.TestUtilsBody;
import ch.duckpond.universe.utils.box2d.BodyUtils;

import com.mongodb.MongoClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.mongodb.morphia.Morphia;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PersistenceTest {

  private static final CachedDatastore datastore;

  private static final Morphia morphia = new Morphia().mapPackage("ch.duckpond.universe.persisted");

  static {
    // here because eclipse->sort members will mess up everything
    datastore = new CachedDatastore(morphia, new MongoClient(), "test");
  }

  /**
   * Setup db.
   *
   * @throws Exception
   *           failed
   */
  @BeforeClass
  public static void beforeClass() throws Exception {
    // set up morphia
    datastore.ensureIndexes();
  }

  private final Logger logger = LogManager.getLogger(PersistenceTest.class);

  private PersistedWorld persistedWorld;

  @Before
  public void init() {
    dropData();
  }

  @After
  public void tearDown() {
    dropData();
  }

  @Test
  public final void testPersistSimulation() {
    testPopulateWorld();
    final Simulation simulation = new Simulation();
    // save body defs
    final Map<ObjectId, BodyDef> bodyDefsBefore = new HashMap<>();
    for (Body i = persistedWorld.get(datastore).getBodyList(); i != null; i = i.getNext()) {
      bodyDefsBefore.put((ObjectId) i.getUserData(), BodyUtils.getBodyDef(i));
    }
    // update the simulation several times (simulate some time)
    simulation.update(persistedWorld.get(datastore));
    simulation.update(persistedWorld.get(datastore));
    simulation.update(persistedWorld.get(datastore));
    simulation.update(persistedWorld.get(datastore));
    simulation.update(persistedWorld.get(datastore));
    persistedWorld.save(datastore);
    // get new body defs of persisted objects
    for (final Entry<ObjectId, BodyDef> entry : bodyDefsBefore.entrySet()) {
      final PersistedBody body = datastore.get(PersistedBody.class, entry.getKey());
      final BodyDef bodyDefOld = entry.getValue();
      final BodyDef bodyDefNew = BodyUtils.getBodyDef(body.get(datastore));
      // position should have changed
      logger.info(String.format("bodyDefNew.position: %s != bodyDefOld.position: %s",
          bodyDefNew.position, bodyDefOld.position));
      Assert.assertTrue(bodyDefNew.position.x != bodyDefOld.position.x
          || bodyDefNew.position.y != bodyDefOld.position.y);
    }
  }

  @Test
  public final void testPopulateWorld() {
    final World world = new World(new Vec2());
    persistedWorld = new PersistedWorld(world, datastore);
    Assert.assertNotNull(datastore.getCache().get(World.class, persistedWorld.getId()));
    for (int i = 0; i < 100; i++) {
      final Body body = TestUtilsBody.randomBody(persistedWorld.get(datastore));
      persistedWorld.save(datastore);
      Assert.assertEquals(i + 1, datastore.getCollection(PersistedBody.class).count());
      Assert.assertNotNull(datastore.getCache().get(Body.class, (ObjectId) body.getUserData()));
    }
  }

  @Test
  public final void testSaveGetBody() {
    final World world = new World(new Vec2());
    persistedWorld = new PersistedWorld(world, datastore);
    final Body body = TestUtilsBody.randomBody(world);
    persistedWorld.save(datastore);
    // get @{link Body} again using the same @{link CachedDatastore}
    final PersistedBody persistedBody1 = datastore.get(PersistedBody.class, body.getUserData());
    final Body body1 = persistedBody1.get(datastore);
    // get @{link Body} again using an other @{link CachedDatastore} ->
    // simulates
    // remote client
    final CachedDatastore datastore2 = new CachedDatastore(morphia, new MongoClient(), "test");
    final PersistedBody persistedBody2 = datastore2.get(PersistedBody.class, body.getUserData());
    final Body body2 = persistedBody2.get(datastore2);
    // do some check if they are the same
    Assert.assertTrue(body.getPosition().x == body1.getPosition().x
        && body.getPosition().x == body2.getPosition().x
        && body.getPosition().y == body1.getPosition().y
        && body.getPosition().y == body2.getPosition().y);
    Assert.assertEquals(0,
        ((ObjectId) body.getUserData()).compareTo((ObjectId) body1.getUserData()));
    Assert.assertEquals(0,
        ((ObjectId) body.getUserData()).compareTo((ObjectId) body2.getUserData()));
  }

  @Test
  public final void testSaveGetDummy() {
    final String dummyObject = "Dummy data";
    final PersistedDummy dummy = new PersistedDummy(dummyObject, datastore);
    dummy.save(datastore);
    Assert.assertEquals(dummyObject, dummy.get(datastore));
    Assert.assertEquals(dummyObject, datastore.get(dummy).get(datastore));
  }

  private void dropData() {
    // empty database
    datastore.delete(datastore.createQuery(PersistedFixture.class));
    datastore.delete(datastore.createQuery(PersistedBody.class));
    datastore.delete(datastore.createQuery(PersistedDistanceJoint.class));
    datastore.delete(datastore.createQuery(PersistedWorld.class));
    datastore.delete(datastore.createQuery(PersistedDummy.class));
  }
}
