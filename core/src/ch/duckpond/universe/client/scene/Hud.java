package ch.duckpond.universe.client.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ch.duckpond.universe.client.screen.GameScreen;
import ch.duckpond.universe.shared.simulation.Globals;

/**
 * Universe Hud
 *
 * @author ente
 */
public class Hud {

    private static final BitmapFont HUD_FONT = new BitmapFont(Gdx.files.internal(
            "fonts/neons_regular_48" + ".fnt"));
    /**
     * Relative HUD size to stage size.
     */
    private static float HUD_SIZE_W = .3f;
    private static float HUD_SIZE_H = .1f;
    private final GameScreen game;

    public Hud(final GameScreen game, final Stage stage) {
        this.game = game;
        stage.addActor(new EnergyDisplay());
    }

    private class EnergyDisplay extends Actor {

        GlyphLayout layout = new GlyphLayout();

        @Override
        public void draw(Batch batch, float parentAlpha) {
            HUD_FONT.setColor(game.getThisPlayer().getColor());
            batch.setColor(game.getThisPlayer().getColor());

            layout.setText(HUD_FONT,
                           humanReadableCount((long) game.getThisPlayer().getEnergy(), true));
            HUD_FONT.draw(batch,
                          layout,
                          game.getStaticStage().getWidth() - layout.width - Globals.GLOW_SAMPLES,
                          game.getStaticStage().getHeight() - Globals.GLOW_SAMPLES);
        }

        // from: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java/3758880#3758880
        public String humanReadableCount(long count, boolean si) {
            int unit = si ? 1000 : 1024;
            if (count < unit) return String.format("%d", count);
            int exp = (int) (Math.log(count) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
            return String.format("%.1f %s", count / Math.pow(unit, exp), pre);
        }
    }
}
