package ch.duckpond.universe.client.circlemenu;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.duckpond.universe.client.screen.GameScreen;

/**
 * Circle menu implementation.
 *
 * @author ente
 */
public class CircleMenu {
    private final GameScreen gameScreen;
    private final Map<Circle, CircleMenuItem> circles = new HashMap<Circle, CircleMenuItem>();
    private final Object attachable;

    public CircleMenu(final GameScreen gameScreen, final Object attachable) {
        this.gameScreen = gameScreen;
        this.attachable = attachable;
    }

    /**
     * renders the menu
     *
     * @param camera               Camera positioned on the center of the menu
     * @param circleRadius         radius of the menu
     * @param circleMenuItemRadius radius of the items int he manu
     * @param circleMenuItems      menu item list
     */
    public void render(final OrthographicCamera camera, final float circleRadius, final float circleMenuItemRadius, final List<CircleMenuItem> circleMenuItems) {
        if (circleMenuItems.size() <= 0) {
            throw new GdxRuntimeException(String.format("Can not render %s without items",
                                                        getClass().getName()));
        }
        final float itemDistance__degree = 360f / circleMenuItems.size();
        final Vector3 itemRadius = new Vector3(0, circleRadius, 0);
        circles.clear();
        for (final CircleMenuItem item : circleMenuItems) {
            camera.rotate(itemDistance__degree);
            camera.update();
            camera.translate(itemRadius);
            camera.update();

            final Circle c = new Circle(new Vector2(camera.position.x, camera.position.y),
                                        circleMenuItemRadius);
            circles.put(c, item);
            item.render(camera, circleMenuItemRadius);

            camera.translate(itemRadius.scl(-1));
            camera.update();
        }
    }

    public InputProcessor getInputProcessor() {
        return new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // check if item was clicked
                final Vector3 lastMousePosScreen = gameScreen.getCamera().unproject(new Vector3(
                        screenX,
                        screenY,
                        0));
                // check if clicked on circle
                for (final Map.Entry<Circle, CircleMenuItem> circle : circles.entrySet()) {
                    if (circle.getKey().contains(lastMousePosScreen.x, lastMousePosScreen.y)) {
                        circle.getValue().clicked(attachable);
                        return true;
                    }
                }
                return false;
            }

        };
    }
}
