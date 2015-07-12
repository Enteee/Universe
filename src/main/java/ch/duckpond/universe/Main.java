package ch.duckpond.universe;

import ch.duckpond.universe.simulation.Simulation;
import ch.duckpond.universe.test.physics.UniverseTest;

public class Main {

  private enum RunType {
    /**
     * Start simulation.
     */
    SIMULATION,
    /**
     * Start testbed.
     */
    TESTBED,
  }

  private static final RunType RUNTYPE = RunType.SIMULATION;

  /**
   * Main.
   *
   * @param argv
   *          command line arguments
   */
  public static void main(final String[] argv) {
    switch (RUNTYPE) {
      default :
      case SIMULATION :
        new Simulation().run();
        break;
      case TESTBED :
        UniverseTest.start();
        break;
    }
  }
}