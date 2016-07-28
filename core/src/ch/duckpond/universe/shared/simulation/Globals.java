package ch.duckpond.universe.shared.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.Random;

/**
 * Global variables which have a massive influence on the gameplay / balancing
 *
 * @author ente
 */
public class Globals {
    public static final Random RANDOM = new Random();
    /**
     * Virtual sizes of the viewports: They should all have the same aspect ratio, if they don't
     * the viewports won't overlap.
     */
    public static final float ASPECT_RATIO = Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
    public static final int WORLD_V_WIDTH = 100;
    public static final int WORLD_V_HEIGTH = (int) (WORLD_V_WIDTH * ASPECT_RATIO);
    public static final int STATIC_V_WIDTH = 800;
    public static final int STATIC_V_HEIGTH = (int) (STATIC_V_WIDTH * ASPECT_RATIO);
    /**
     * Level scaling.
     */
    public static final int LEVEL_START_ENERGY = 5000;
    public static final int LEVEL_LOOSE_ENERGY = 1000;
    public static final int LEVEL_WIN_ENERGY = 10000;
    /**
     * Number of enemies per level to add.
     */
    public static final float LEVEL_ENEMY_COUNT = 0.1f;
    /**
     * Colors with specific meaning.
     */
    public static final Color WORLD_BACKGROUND_COLOR = new Color(0, 0, 0, 1);
    /**
     * The game font.
     */
    public static final String FONT_TTF = "fonts/FifteenNarrow.ttf";
    /**
     * Radius of new spawning mass
     */
    public static final float MASS_SPAWN_RADIUS = 1;
    public static final float MASS_DENSITY = 1;
    /**
     * Fraction of mass radius which should be surface
     */
    public static final float REL_MASS_SURFACE_WIDTH = 0.2f;
    /**
     * Camera behaviour.
     */
    public static final float CAMERA_ZOOM_FACTOR_GESTURE = 0.01f;
    public static final float CAMERA_ZOOM_FACTOR_INPUT = 0.1f;
    public static final float CAMERA_ZOOM_MAX = 5;
    public static final float CAMERA_ZOOM_MIN = 0.5f;
    public static final float CAMERA_CENTER_FACTOR = 0.1f;
    /**
     * Circle menu settings
     */
    public static final float CIRCLE_MENU_BUTTON_SIZE = 2;
    public static final float CIRCLE_MENU_BUTTON_MARGIN = 1;

    /**
     * Z-indices
     */
    public static final int Z_INDEX_BACKGROUND = 0;
    public static final int Z_INDEX_MASS = 10;
    public static final int Z_INDEX_HUD = 100;

    private Globals() {
    }
}
