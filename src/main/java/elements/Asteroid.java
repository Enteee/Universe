package elements;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.newdawn.slick.geom.Vector2f;

@Entity("asteroids")
public class Asteroid {
    
    @Id
    private ObjectId id;
    private int      mass;
    private Vector2f velocity;
}
