package ch.duckpond.universe.simulation;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedFrame;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;

public class Simulation {
    
    private static final Logger logger = LogManager.getLogger(Simulation.class);
    
    public static void main(final String argv[]) {
        logger.info("Universe");
        final TestbedModel model = new TestbedModel(); // create our model

        // add tests
        // TestList.populateModel(model); // populate the provided testbed tests
        model.addCategory("Universe"); // add a category
        model.addTest(new UniverseTest()); // add our test

        // add our custom setting "My Range Setting", with a default value of
        // 10, between 0 and 20
        // model.getSettings().addSetting(new TestbedSetting("My Range Setting",
        // SettingType.ENGINE, 10, 0, 20));

        final TestbedPanel panel = new TestPanelJ2D(model); // create our
        // testbed panel put both into our testbed frame etc
        final JFrame testbed = new TestbedFrame(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        testbed.setVisible(true);
        testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}
