package ch.duckpond.universe.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.strongjoshua.console.Console;
import com.strongjoshua.console.GUIConsole;

import ch.duckpond.universe.client.screen.GameScreen;

/**
 * The main game class.
 *
 * @author ente
 */
public class UniverseGame extends Game {

    private static final UniverseGame UNIVERSE_GAME = new UniverseGame();
    private Console console;

    /**
     * Body on which to center the view.
     */
    private Body centeredBody;

    private UniverseGame() {
    }

    public static UniverseGame getInstance() {
        return UNIVERSE_GAME;
    }

    @Override
    public void create() {
        // gdx set up
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);

        Box2D.init();

        // We need to set this for some reason in order to make the console work, TODO: // FIXME: 7/22/16
        Gdx.app.getGraphics().setWindowedMode(Gdx.app.getGraphics().getWidth() * 2,
                                              Gdx.app.getGraphics().getHeight() * 2);
        console = new GUIConsole(true);
        console.setCommandExecutor(new DebugCommandExecutor());
        console.setKeyID(Input.Keys.ESCAPE);
        console.setMaxEntries(16);
        console.setSizePercent(100, 33);
        console.setPosition(0, 0);

        // jump to game
        setScreen(new GameScreen(inputMultiplexer));
    }


    @Override
    public void render() {
        super.render();
        console.draw();
    }
}
