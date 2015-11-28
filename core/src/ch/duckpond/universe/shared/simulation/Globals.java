package ch.duckpond.universe.shared.simulation;

import java.util.Random;

/**
 * Global variables.
 *
 * @author ente
 */
public class Globals {
    public static final Random RANDOM = new Random();
    /**
     * Radius of new spawning mass
     */
    public static final float MASS_SPAWN_RADIUS = 1;
    public static final float MASS_DENSITY = 1;
    /**
     * Width of the mass surface.
     */
    public static final float MASS_SURFACE_WIDTH = 1f;
    /**
     * Camera behaviour
     */
    public static final float CAMERA_ZOOM_FACTOR_GESTURE = 0.01f;
    public static final float CAMERA_ZOOM_FACTOR_INPUT = 0.1f;
    public static final float CAMERA_ZOOM_MAX = 5;
    public static final float CAMERA_ZOOM_MIN = 0.5f;
    public static final float CAMERA_CENTER_FACTOR = 0.1f;
    /**
     * Glow filter settings
     */
    public static final int GLOW_SAMPLES = 5;
    public static final float GLOW_QUALITY = 2.5f;
    public static final float GLOW_INTENSITY = 2f;
    /**
     * Circle menu settings
     */
    public static final float CIRCLE_MENU_BUTTON_SIZE = 2;
    public static final float CIRCLE_MENU_BUTTON_MARGIN = 1;
    /**
     * Mass settings
     */
    public static final int KEEP_LAST_POSITIONS_COUNT = 10;

    private Globals() {
    }
}
