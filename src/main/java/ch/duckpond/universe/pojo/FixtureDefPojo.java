package ch.duckpond.universe.pojo;

import org.bson.types.ObjectId;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.FixtureDef;

/**
 * POJO for {@link FixtureDef}.
 *
 * @author ente
 */
public class FixtureDefPojo {

  public float    density;
  public Filter   filter;
  public float    friction;
  public boolean  isSensor;
  public float    restitution;
  public Shape    shape;
  public ObjectId userData;
}
