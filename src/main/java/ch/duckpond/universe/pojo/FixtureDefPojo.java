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

  public Shape    shape;
  public ObjectId userData;
  public float    friction;
  public float    restitution;
  public float    density;
  public boolean  isSensor;
  public Filter   filter;
}
