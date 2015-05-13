package ch.duckpond.universe.simulation;

import java.util.ArrayList;
import java.util.List;

import javax.activity.InvalidActivityException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newdawn.slick.geom.Vector2f;

import ch.duckpond.universe.elements.Mass;

public class Simulation {
    
    private static final Logger     logger = LogManager.getLogger(Simulation.class);
    private static final List<Mass> masses = new ArrayList<>();
    
    public static void main(final String argv[]) {
        
        logger.info("HELLO WORLD");
        try {
            masses.add(new Mass(10, new Vector2f(10, 0), new Vector2f(10, 0)));
        } catch (InvalidActivityException e) {
            logger.error("Mass test failed!");
        }
    }
}
