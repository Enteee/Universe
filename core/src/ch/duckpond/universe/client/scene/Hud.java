package ch.duckpond.universe.client.scene;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import ch.duckpond.universe.client.screen.GameScreen;
import ch.duckpond.universe.shared.simulation.Globals;

/**
 * Universe Hud
 *
 * @author ente
 */
public class Hud {
    public Stage stage;
    private Viewport viewport;

    private Label energyLabel;
    private Label energy;

    public Hud(final GameScreen game, final SpriteBatch batch) {
        viewport = new FitViewport(Globals.V_WIDTH, Globals.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, batch);

        energyLabel = new Label("Energy",
                                new Label.LabelStyle(new BitmapFont(),
                                                     game.getThisPlayer().getColor()));
        energyLabel = new Label(String.format("%05d", (int) game.getThisPlayer().getEnergy()),
                                new Label.LabelStyle(new BitmapFont(),
                                                     game.getThisPlayer().getColor()));

        final Table table = new Table();
        table.top();
        table.setFillParent(true);
        table.add(energyLabel).expandX().padTop(1);
        table.row();
        table.add(energy).expandX();
        stage.addActor(table);
    }
}
