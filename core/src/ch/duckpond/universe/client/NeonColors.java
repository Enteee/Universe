package ch.duckpond.universe.client;


import com.badlogic.gdx.Gdx;

import ch.duckpond.universe.shared.simulation.Globals;

/**
 * Neon color palette
 *
 * @author ente
 */
public enum NeonColors {

    YELLOW(0xF3F315FF),
    C1FD33(0xC1FD33FF),
    GREEN(0x83F52CFF),
    ROUNGE(0xFF6600FF),
    PROCESS_PAGENTA(0xFF0099FF),
    RETRO_ORNAGE(0xFF9933FF),
    BARIBE_PINK(0xFC5AB8FF),
    BLUE_PEEPS(0x0DD5FCFF),
    ELECTRIC_PURPLE(0x6E0DD0FF);

    private final int colorRGBA8888;

    NeonColors(int colorRGBA8888) {
        this.colorRGBA8888 = colorRGBA8888;
    }

    public static NeonColors getRandomColor() {
        final NeonColors randomColor = values()[Globals.RANDOM.nextInt(NeonColors.values().length)];
        Gdx.app.debug(NeonColors.class.getName(),
                      String.format("random color: %s, %x",
                                    randomColor,
                                    randomColor.getColorRGB888()));
        return randomColor;
    }

    public int getColorRGB888() {
        return colorRGBA8888;
    }
}
