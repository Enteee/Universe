package ch.duckpond.universe.elements;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.newdawn.slick.geom.Vector2f;

import javax.activity.InvalidActivityException;

@Entity("masses")
public class Mass {

  @Id
  private ObjectId id;
  private int      mass;
  private Vector2f position;
  private Vector2f velocity;

  public Mass(final int mass, final Vector2f position, final Vector2f velocity)
      throws InvalidActivityException {
    setMass(mass);
    setPosition(position);
    setVelocity(velocity);
  }

  public ObjectId getId() {
    return id;
  }

  public int getMass() {
    return mass;
  }

  public Vector2f getPosition() {
    return position;
  }

  public Vector2f getVelocity() {
    return velocity;
  }

  public void setMass(final int mass) throws InvalidActivityException {
    if (mass <= 0) {
      throw new InvalidActivityException("mass <= 0");
    }
    this.mass = mass;
  }

  public void setPosition(final Vector2f position) {
    this.position = position;
  }

  public void setVelocity(final Vector2f velocity) {
    this.velocity = velocity;
  }

}
