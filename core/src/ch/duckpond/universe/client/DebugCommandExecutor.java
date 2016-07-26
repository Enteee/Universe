package ch.duckpond.universe.client;

import com.badlogic.gdx.graphics.Color;
import com.strongjoshua.console.CommandExecutor;

import java.lang.reflect.Field;

import ch.duckpond.universe.client.game.GameScreen;

/**
 * The debug command executor, allows alls commands.
 *
 * @author ente
 */
public class DebugCommandExecutor extends CommandExecutor {

    public void newPlayer() throws IllegalAccessException, NoSuchFieldException {
        final Field thisPlayer = GameScreen.class.getDeclaredField("thisPlayer");
        thisPlayer.setAccessible(true);
        thisPlayer.set(UniverseGame.getInstance().getScreen(),
                       new Player(new Color(NeonColors.getRandomColor().getColorRGB888())));
    }

}
