package ch.duckpond.universe.client.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ch.duckpond.universe.client.screen.GameScreen;
import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.utils.libgdx.BatchUtils;

/**
 * Universe Hud
 *
 * @author ente
 */
public class Hud {

    private final GameScreen gameScreen;
    private final Stage stage;

    public Hud(final GameScreen gameScreen, final Stage stage) {
        assert gameScreen != null;
        assert stage != null;

        this.gameScreen = gameScreen;
        this.stage = stage;

        stage.addActor(new EnergyDisplay());
    }

    private class EnergyDisplay extends Group {

        private static final float REL_W = .4f;
        private static final float REL_H = .1f;

        /**
         * relative widths of elements, remaining width will become spacing.
         */
        private static final float TEXT_REL_W = 4 / 32f;
        private static final float BAR_REL_W = 27 / 32f;
        GlyphLayout layout = new GlyphLayout();
        private float with;
        private float heigth;

        private EnergyDisplay() {
            with = stage.getWidth() * REL_W;
            heigth = stage.getHeight() * REL_H;
            final float spacing = with * (1 - TEXT_REL_W - BAR_REL_W);

            setBounds(stage.getWidth() - with, stage.getHeight() - heigth, with, heigth);

            // add level indicator
            final LevelIndicator levelIndicator = new LevelIndicator();
            levelIndicator.setBounds(0, 0, with * TEXT_REL_W, heigth);
            addActor(levelIndicator);

            // add energy bar
            final EnergyBar energyBar = new EnergyBar();
            energyBar.setBounds(with * TEXT_REL_W + spacing, 0, with * BAR_REL_W, heigth);
            addActor(energyBar);
        }
    }

    private class LevelIndicator extends Actor {

        private BitmapFont font;
        private GlyphLayout layout = new GlyphLayout();

        private LevelIndicator() {
            updateFont();
        }

        private void updateFont() {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Globals.FONT_TTF));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = (int) getHeight();
            font = generator.generateFont(parameter);
            font.setFixedWidthGlyphs("0123456789");
            generator.dispose();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {

            final String level = String.format("%d", gameScreen.getLevel());
            layout.setText(font, level);
            font.draw(batch,
                      level,
                      getWidth() - layout.width,
                      getY() + getHeight() / 2 + layout.height / 2);

        }

        @Override
        protected void sizeChanged() {
            updateFont();
            super.sizeChanged();
        }

        @Override
        public void drawDebug(ShapeRenderer shapes) {
            shapes.x(getX(), getY(), 1);
        }


    }

    private class EnergyBar extends Actor {

        private static final float TICK_REL_W = 0.01f;
        private static final float TICK_REL_H = 0.7f;

        private static final float BAR_FRAME_REL_W = 0.1f;
        private static final float BAR_REL_W = 0.3f;

        @Override
        public void drawDebug(ShapeRenderer shapes) {
            shapes.x(getX(), getY(), 1);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            final ShapeRenderer shapeRenderer = BatchUtils.buildShapeRendererFromBatch(batch);

            // calculate progress
            final float energy = gameScreen.getThisPlayer().getEnergy();
            final float winEnergy = gameScreen.getLevel() * Globals.LEVE_WIN_ENERGY;
            final float looseEnergy = gameScreen.getLevel() * Globals.LEVE_LOOSE_ENERGY;
            final float startEnergy = gameScreen.getLevel() * Globals.LEVE_START_ENERGY;

            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            {
                final float tickWidth = getWidth() * TICK_REL_W;
                final float tickHeight = getHeight() * TICK_REL_H;
                final float tickBeginY = getY() + (getHeight() - tickHeight) / 2;
                final float tickEndY = tickBeginY + tickHeight;

                final float firstTickX = getX() + tickWidth / 2;
                final float looseTickX = getX() + getWidth() * looseEnergy / winEnergy +
                        tickWidth / 2;
                final float startTickX = getX() + getWidth() * startEnergy / winEnergy +
                        tickWidth / 2;

                final float barFrameWidth = getHeight() * BAR_FRAME_REL_W;

                // draw first tick
                shapeRenderer.rectLine(firstTickX, tickBeginY, firstTickX, tickEndY, tickWidth);

                // draw loose tick
                shapeRenderer.rectLine(looseTickX, tickBeginY, looseTickX, tickEndY, tickWidth);

                // draw start tick
                shapeRenderer.rectLine(startTickX, tickBeginY, startTickX, tickEndY, tickWidth);

                // draw middle line
                shapeRenderer.rectLine(firstTickX,
                                       getHeight() / 2,
                                       getX() + getWidth(),
                                       getHeight() / 2,
                                       barFrameWidth);

                final float barWidth = getHeight() * BAR_REL_W;
                final float barStartX = firstTickX + tickWidth / 2;
                final float barEndX = getX() + getWidth() * energy / winEnergy;

                shapeRenderer.rectLine(barStartX,
                                       getHeight() / 2,
                                       barEndX,
                                       getHeight() / 2,
                                       barWidth);
            }
            shapeRenderer.end();
            batch.begin();
        }
    }

}
