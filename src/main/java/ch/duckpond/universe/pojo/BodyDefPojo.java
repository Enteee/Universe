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

  public BodyType type;
  public ObjectId userData;
  public Vec2     position;
  public float    angle;
  public Vec2     linearVelocity;
  public float    angularVelocity;
  public float    linearDamping;
  public float    angularDamping;
  public boolean  allowSleep;
  public boolean  awake;
  public boolean  fixedRotation;
  public boolean  bullet;
  public boolean  active;
  public float    gravityScale;
}
