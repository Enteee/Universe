package ch.duckpond.universe.utils.box2d;

import org.jbox2d.dynamics.Body;

public class BodyUtils {

  /**
   * Get the energy of a {@link Body}
   * 
   * @param body
   *          the {@link Body} to get the energy for.
   * @return the total energy
   */
  public static float getEnergy(final Body body) {
    return 0.5f * body.getMass() * body.getLinearVelocity().length()
        * body.getLinearVelocity().length();
  }

  private BodyUtils() {
  }
}
