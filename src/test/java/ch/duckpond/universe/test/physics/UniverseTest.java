package ch.duckpond.universe.test.physics;

import ch.duckpond.universe.simulation.Simulation;

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

  private static final float DENSITY            = 1;
  private static final int   MASSES_COL_SPACING = 5;
  private static final int   MASSES_COLS        = 5;
  private static final int   MASSES_RADIUS      = 1;
  private static final int   MASSES_ROW_SPACING = 5;

  private static final int MASSES_ROWS = 5;

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
        circleShape.setRadius(MASSES_RADIUS);
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(MASSES_COL_SPACING * (j % MASSES_COLS),
            MASSES_ROW_SPACING * (i % MASSES_ROWS));
        bodyDef.angle = (float) (Math.PI / 4 * i);
        bodyDef.allowSleep = false;
        final Body body = world.createBody(bodyDef);
        body.createFixture(circleShape, DENSITY);
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
    model.addTest(new UniverseTest()); // add our testbed

    final TestbedPanel panel = new TestPanelJ2D(model); // create our
    // Testbed panel put both into our testbed frame etc.
    final JFrame testbed = new TestbedFrame(model, panel,
        TestbedController.UpdateBehavior.UPDATE_CALLED);
    testbed.setVisible(true);
    testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }

  private final Simulation simulation = new Simulation();

  final Logger logger = LogManager.getLogger(UniverseTest.class);

  /**
   * Morphia mongoDB object mapper.
   */
  final Morphia morphia = new Morphia().mapPackage("ch.duckpond.universe.persisted");

  @Override
  public String getTestName() {
    return "Universe";
  }

  @Override
  public void initTest(final boolean argDeserialized) {
    setTitle("Universe test");
    // no gravity
    getWorld().setGravity(new Vec2());

    // add some masses
    addMasses(getWorld());
  }

  @Override
  public void update() {
    super.update();
    simulation.update(getWorld());
  }

}
