package ch.duckpond.universe.client;


import ch.duckpond.universe.shared.simulation.Globals;

/**
 * Neon color palette
 *
 * @author ente
 */
public enum NeonColors {

    YELLOW(0xF3F315),
    C1FD33(0xC1FD33),
    GREEN(0x83F52C),
    ROUNGE(0xFF6600),
    PROCESS_PAGENTA(0xFF0099),
    RETRO_ORNAGE(0xFF9933),
    BARIBE_PINK(0xFC5AB8),
    BLUE_PEEPS(0x0DD5FC),
    ELECTRIC_PURPLE(0x6E0DD0);

    private final int colorRGB888;

    NeonColors(int colorRGB888) {
        this.colorRGB888 = colorRGB888;
    }

    public static NeonColors getRandomColor() {
        return NeonColors.values()[Globals.RANDOM.nextInt(NeonColors.values().length)];
    }

    public int getColorRGB888() {
        return colorRGB888;
    }
}
