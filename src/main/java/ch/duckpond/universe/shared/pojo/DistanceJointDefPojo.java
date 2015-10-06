package ch.duckpond.universe.shared.pojo;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.DistanceJointDef;

/**
 * POJO from {@link DistanceJointDef}.
 *
 * @author ente
 */
public class DistanceJointDefPojo extends JointDefPojo {

  public float dampingRatio;
  public float frequencyHz;
  public float length;
  public Vec2  localAnchorA;
  public Vec2  localAnchorB;
}
