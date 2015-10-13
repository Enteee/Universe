package ch.duckpond.universe.shared.simulation;

/**
 * Global variables.
 *
 * @author ente
 */
public class Globals {
    public static final float DENSITY = 1;
    public static final float MASS_DEFAULT_RADIUS = 1;
    public static final float CAMERA_ZOOM_FACTOR = 0.001f;
    public static final float CAMERA_ZOOM_MAX = 5;
    public static final float CAMERA_ZOOM_MIN = 0.5f;
    // glow filter
    public static final int GLOW_SAMPLES = 5;
    public static final float GLOW_QUALITY = 1.5f;
    public static final float GLOW_INTENSITY = 5f;

    private Globals() {
    }
}
