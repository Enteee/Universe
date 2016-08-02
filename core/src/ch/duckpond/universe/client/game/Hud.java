package ch.duckpond.universe.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;
import ch.duckpond.universe.utils.libgdx.BatchUtils;

/**
 * UniverseGame Hud
 *
 * @author ente
 */
public class Hud extends Group {

    private final GameScreen gameScreen;
    private final Stage stage;

    public Hud(final GameScreen gameScreen) {
        assert gameScreen != null;

        this.gameScreen = gameScreen;
        this.stage = gameScreen.getStaticStage();

        setZIndex(Globals.Z_INDEX_HUD);
        addActor(new EnergyDisplay());
    }

    private class EnergyDisplay extends Group {

        private static final float REL_W = .4f;
        private static final float REL_H = .1f;

        /**
         * relative widths of elements, remaining width will become spacing.
         */
        private static final float TEXT_REL_W = 4 / 32f;
        private static final float BAR_REL_W = 27 / 32f;

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

        private static final String CHARSET = "0123456789";

        private BitmapFont font;
        private GlyphLayout layout = new GlyphLayout();

        private LevelIndicator() {
            updateFont();
        }

        private void updateFont() {
            final Simulation simulation = gameScreen.getSimulation();
            if (font != null) {
                font.dispose();
            }
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Globals.FONT_TTF));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = (int) getHeight();
            parameter.color = new Color(simulation.getThisPlayer().getColor());
            parameter.genMipMaps = true;
            parameter.characters = CHARSET;
            font = generator.generateFont(parameter);
            font.setFixedWidthGlyphs(CHARSET);
            generator.dispose();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            final Simulation simulation = gameScreen.getSimulation();

            final String level = String.format("%d", simulation.getLevel());

            // TODO: hacky update of font color by regenerating the font.
            if (simulation.getThisPlayer().getColor() != font.getColor()) {
                updateFont();
            }
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

        private static final float BAR_FRAME_REL_W = 0.05f;
        private static final float BAR_REL_W = 0.5f;

        @Override
        public void drawDebug(ShapeRenderer shapes) {
            shapes.x(getX(), getY(), 1);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            final Simulation simulation = gameScreen.getSimulation();
            final ShapeRenderer shapeRenderer = gameScreen.getShapeRenderer();

            // calculate progress
            final float energy = simulation.getThisPlayer().getEnergy();
            final float winEnergy = simulation.getWinEnergy();
            final float looseEnergy = simulation.getLooseEnergy();
            final float startEnergy = simulation.getStartEneryy();

            batch.end();
            BatchUtils.syncShapeRendererWithBatch(batch, shapeRenderer);
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

                shapeRenderer.setColor(simulation.getThisPlayer().getColor());

                // draw first tick
                shapeRenderer.rectLine(firstTickX, tickBeginY, firstTickX, tickEndY, tickWidth);

                if (simulation.hasLevelStarted()) {
                    // draw loose tick
                    shapeRenderer.rectLine(looseTickX, tickBeginY, looseTickX, tickEndY, tickWidth);
                } else {
                    // draw start tick, if level has not started yet.
                    shapeRenderer.rectLine(startTickX, tickBeginY, startTickX, tickEndY, tickWidth);
                }

                // draw middle line
                shapeRenderer.rectLine(firstTickX,
                                       getHeight() / 2,
                                       getX() + getWidth(),
                                       getHeight() / 2,
                                       barFrameWidth);

                // draw bar
                final float barWidth = getHeight() * BAR_REL_W;
                final float barY = getHeight() / 2;
                final float barStartX = firstTickX + tickWidth / 2;

                final Color barColor = new Color(shapeRenderer.getColor());
                final Color barMassSpawnColor = new Color(barColor.r, barColor.g, barColor.b, 0);

                final float barMassSpawnWidth = getWidth() * gameScreen.getBackground().getMassSpawnEnergy() / winEnergy;
                float barEndX = getX() + getWidth() * energy / winEnergy;

                // level has not started yet: mass spawn indicator in front of bar
                if (!simulation.hasLevelStarted()) {
                    barEndX += barMassSpawnWidth;
                }

                final float barMassSpawnX = barEndX - barMassSpawnWidth;

                shapeRenderer.rectLine(barStartX, barY, barEndX, barY, barWidth);

                shapeRenderer.setColor(barMassSpawnColor);
                {
                    shapeRenderer.rectLine(barMassSpawnX, barY, barEndX, barY, barWidth);
                }
                shapeRenderer.setColor(barColor);
            }
            shapeRenderer.end();
            batch.begin();
        }
    }

}
