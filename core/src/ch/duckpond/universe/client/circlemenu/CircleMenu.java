package ch.duckpond.universe.client.circlemenu;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.duckpond.universe.client.Universe;

/**
 * Circle menu implementation.
 *
 * @author ente
 */
public class CircleMenu {
    private final Map<Circle, CircleMenuItem> circles = new HashMap<Circle, CircleMenuItem>();
    private List<CircleMenuItem> items = new LinkedList<CircleMenuItem>();
    private CircleMenuAttachable attachable;

    public void addItem(final CircleMenuItem item) {
        items.add(item);
    }

    public void clearItems() {
        items.clear();
    }

    public void attach(final CircleMenuAttachable attachable) {
        this.attachable = attachable;
        this.items = attachable.getCircleMenuItems();
    }

    public void detatch() {
        attachable = null;
    }

    public void render(final OrthographicCamera camera) {
        if (attachable == null) {
            return;
        }
        if (items.size() <= 0) {
            throw new GdxRuntimeException(String.format("Can not render %s without items",
                                                        getClass().getName()));
        }
        final Vector3 attachableTranslation = new Vector3(attachable.getWorldPosition()).sub(camera.position);
        camera.translate(attachableTranslation);
        camera.update();

        final float itemDistance__degree = 360f / items.size();
        final Vector3 itemRadius = new Vector3(0, attachable.getCircleRadius(), 0);
        circles.clear();
        for (final CircleMenuItem item : items) {
            camera.rotate(itemDistance__degree);
            camera.update();
            camera.translate(itemRadius);
            camera.update();

            final float radius = attachable.getCircleMenuItemRadius();
            final Circle c = new Circle(new Vector2(camera.position.x, camera.position.y), radius);
            circles.put(c, item);
            item.render(camera, radius);

            camera.translate(itemRadius.scl(-1));
            camera.update();
        }
        camera.translate(attachableTranslation.scl(-1f));
        camera.update();
    }

    public InputProcessor getInputProcessor() {
        return new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (attachable == null) {
                    return false;
                }
                // check if item was clicked
                final Vector3 lastMousePosScreen = Universe.getInstance().getCamera().unproject(new Vector3(
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
