package ch.duckpond.universe.client;

import com.badlogic.gdx.graphics.Color;
import com.strongjoshua.console.CommandExecutor;

import java.lang.reflect.Field;

/**
 * The debug command executor, allows alls commands.
 *
 * @author ente
 */
public class DebugCommandExecutor extends CommandExecutor {

    public void newPlayer() throws IllegalAccessException, NoSuchFieldException {
        final Field thisPlayer = Universe.class.getDeclaredField("thisPlayer");
        thisPlayer.setAccessible(true);
        thisPlayer.set(Universe.getInstance(),
                       new Player(new Color(NeonColors.getRandomColor().getColorRGB888())));
    }

}
