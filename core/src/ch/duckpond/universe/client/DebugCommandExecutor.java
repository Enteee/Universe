package ch.duckpond.universe.client;

import com.badlogic.gdx.graphics.Color;
import com.strongjoshua.console.CommandExecutor;
import com.strongjoshua.console.Console;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import ch.duckpond.universe.client.game.Enemy;
import ch.duckpond.universe.shared.simulation.Simulation;

/**
 * The debug command executor, allows alls commands.
 *
 * @author ente
 */
public class DebugCommandExecutor extends CommandExecutor {

    private final Console console;

    public DebugCommandExecutor(final Console console) {
        this.console = console;
    }

    public void newPlayer() throws IllegalAccessException, NoSuchFieldException {
        setPrivate(UniverseGame.getInstance().getGameScreen().getSimulation(),
                   Simulation.class.getDeclaredField("thisPlayer"),
                   new Player(new Color(NeonColors.getRandomColor().getColorRGB888())));
    }

    /**
     * Sets a private static  field of the given object
     *
     * @param targetObject the obejct to modify
     * @param field        the private (optionally final) field to change
     * @param newValue     the new value of the field
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void setPrivate(final Object targetObject, final Field field, final Object newValue) throws NoSuchFieldException, IllegalAccessException {
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        final int modifiers = field.getModifiers();
        final boolean accessible = field.isAccessible();

        // disable private, set not final, make accessible
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
        field.setAccessible(true);

        field.set(targetObject, newValue);

        // restore private, restore modifiers, restore accessible
        field.setAccessible(accessible);
        modifiersField.setInt(field, modifiers);
        modifiersField.setAccessible(false);

    }

    public void toggleEnemyAction() throws Exception {
        final Field enemyAction = Enemy.class.getDeclaredField("ENEMY_ACTION");
        final Boolean newEnemyActionValue = !(boolean) getPrivateStatic(enemyAction);
        console.log(String.format("enemy action %s",
                                  (newEnemyActionValue) ? "enabled" : "disabled"));
        setPrivateStatic(enemyAction, newEnemyActionValue);
    }

    private static Object getPrivateStatic(final Field field) throws IllegalAccessException {
        Object ret;
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        ret = field.get(field.getDeclaringClass());
        field.setAccessible(accessible);
        return ret;
    }

    /**
     * Sets a private static (optionally final) field,
     * IMPORTANT: It's It's unlikely that this technique works with a primitive private static
     * final boolean, because it's inlineable as a compile-time constant and thus the "new" value
     * may not be observable
     * * http://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection
     * * http://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.28
     *
     * @param field    the private (optionally final) static field to change
     * @param newValue the new value of the field
     * @throws Exception
     */
    private static void setPrivateStatic(final Field field, final Object newValue) throws NoSuchFieldException, IllegalAccessException {
        setPrivate(null, field, newValue);
    }

    public void startLevel() throws NoSuchFieldException, IllegalAccessException {
        final Field levelStarted = Simulation.class.getDeclaredField("levelStarted");
        setPrivate(UniverseGame.getInstance().getGameScreen().getSimulation(), levelStarted, true);
    }

    public void printPlayerEnergy() {
        console.log(String.format("player energy: %f",
                                  UniverseGame.getInstance().getGameScreen().getSimulation().getThisPlayer().getEnergy()));

    }

}
