package ch.duckpond.universe;

import ch.duckpond.universe.simulation.Simulation;
import ch.duckpond.universe.test.physics.UniverseTest;

public class Main {

  private enum RunType {
    /**
     * Start normal application.
     */
    APPLICATION,
    /**
     * Start testbed.
     */
    TESTBED,
  }

  private static final RunType RUNTYPE = RunType.TESTBED;

  /**
   * Main.
   *
   * @param argv
   *          command line arguments
   */
  public static void main(final String[] argv) {
    switch (RUNTYPE) {
      default :
      case APPLICATION :
        new Simulation();
        break;
      case TESTBED :
        UniverseTest.start();
        break;
    }
    // world.step(dt, velocityIterations, positionIterations);
  }
}
