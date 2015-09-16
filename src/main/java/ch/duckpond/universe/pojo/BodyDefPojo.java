package ch.duckpond.universe.pojo;

import org.bson.types.ObjectId;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

/**
 * POJO for {@link BodyDefPojo}. We've to wrap {@link BodyDefPojo} in a POJO
 * because of the {@link BodyDefPojo#userData} which can not be an
 * {@link Object}
 *
 * @author ente
 */
public class BodyDefPojo {

  public boolean  active;
  public boolean  allowSleep;
  public float    angle;
  public float    angularDamping;
  public float    angularVelocity;
  public boolean  awake;
  public boolean  bullet;
  public boolean  fixedRotation;
  public float    gravityScale;
  public float    linearDamping;
  public Vec2     linearVelocity;
  public Vec2     position;
  public BodyType type;
  public ObjectId userData;
}
