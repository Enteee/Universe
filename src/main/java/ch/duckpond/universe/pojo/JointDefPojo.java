package ch.duckpond.universe.pojo;

import ch.duckpond.universe.dao.PersistedBody;

import org.bson.types.ObjectId;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointType;
import org.mongodb.morphia.annotations.Reference;

/**
 * POJO for {@link JointDef}.
 *
 * @author ente
 */
public class JointDefPojo {

  @Reference
  public PersistedBody bodyA;
  @Reference
  public PersistedBody bodyB;
  public boolean       collideConnected;
  public JointType     type;
  public ObjectId      userData;
}
