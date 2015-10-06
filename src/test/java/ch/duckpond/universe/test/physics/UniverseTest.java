package ch.duckpond.universe.test.physics;

import ch.duckpond.universe.server.dao.CachedDatastore;
import ch.duckpond.universe.server.dao.PersistedWorld;
import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;
import ch.duckpond.universe.utils.box2d.BodyUtils;

import com.mongodb.MongoClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedFrame;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.TestbedTest;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.mongodb.morphia.Morphia;

import javax.swing.JFrame;

public class UniverseTest extends TestbedTest {

  private static final float           MASSES_INITIAL_MASS = 5f;
  private static final int             MASSES_ROWS         = 5;
  private static final int             MASSES_COLS         = 5;
  private static final float           MASSES_ROW_SPACING  = 5f;
  private static final float           MASSES_COL_SPACING  = 5f;
  private static final Morphia         MORPHIA             = new Morphia()
      .mapPackage("ch.duckpond.universe.persisted");
  private static final CachedDatastore DATASTORE           = new CachedDatastore(MORPHIA,
      new MongoClient(), "test");
  private static final Logger          LOGGER              = LogManager
      .getLogger(UniverseTest.class);

  /**
   * Add some masses to the world.
   *
   * @param world
   *          where to add masses
   */
  public static void addMasses(final World world) {
    for (int i = 0; i < MASSES_ROWS; i++) {
      for (int j = 0; j < MASSES_COLS; j++) {
        final CircleShape circleShape = new CircleShape();
        circleShape.setRadius(BodyUtils.getRadiusFromMass(MASSES_INITIAL_MASS));
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(MASSES_COL_SPACING * (j % MASSES_COLS),
            MASSES_ROW_SPACING * (i % MASSES_ROWS));
        bodyDef.angle = (float) (Math.PI / 4 * i);
        bodyDef.allowSleep = false;
        final Body body = world.createBody(bodyDef);
        body.createFixture(circleShape, Globals.DENSITY);
        LOGGER.debug(String.format("body.getMass() = %s", body.getMass()));
      }
    }
  }

  /**
   * Starts this {@link TestbedTest}.
   */
  public static void start() {
    final TestbedModel model = new TestbedModel(); // create our model
    // add tests
    // TestList.populateModel(model); // populate the provided testbed tests
    model.addCategory("Universe"); // add a category
    // initialize one test per found world
    DATASTORE.find(PersistedWorld.class).forEach(persitedWorld -> {
      model.addTest(new UniverseTest(persitedWorld));
    });
    // initialize one fresh test
    final World newWorld = new World(new Vec2());
    // create empty body see:
    // http://box2d.org/forum/viewtopic.php?f=9&t=10121&p=41351
    newWorld.createBody(new BodyDef());
    addMasses(newWorld);
    final PersistedWorld newPersistedWorld = new PersistedWorld(newWorld, DATASTORE);
    model.addTest(new UniverseTest(newPersistedWorld));

    final TestbedPanel panel = new TestPanelJ2D(model); // create our
    // Testbed panel put both into our testbed frame etc.
    final JFrame testbed = new TestbedFrame(model, panel,
        TestbedController.UpdateBehavior.UPDATE_CALLED);
    testbed.setVisible(true);
    testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }

  private Simulation           simulation;
  private final Logger         logger = LogManager.getLogger(getClass());
  private final PersistedWorld persistedWorld;

  /**
   * Construct.
   *
   * @param persistedWorld
   *          the {@link PersistedWorld} to construct a test for.
   */
  public UniverseTest(final PersistedWorld persistedWorld) {
    super();
    this.persistedWorld = persistedWorld;
  }

  @Override
  public String getTestName() {
    return String.format("Universe - %s", persistedWorld.getId());
  }

  @Override
  public void init(final World argWorld, final boolean argDeserialized) {
    // hacky: set world from persisted world
    m_world = persistedWorld.get(DATASTORE);
    super.init(m_world, argDeserialized);
  }

  @Override
  public void initTest(final boolean argDeserialized) {
    // construct simulation
    simulation = new Simulation(persistedWorld);
  }

  @Override
  public void update() {
    super.update();
    simulation.step();
  }

}
