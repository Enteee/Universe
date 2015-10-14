package ch.duckpond.universe.client.circlemenu;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.LinkedList;
import java.util.List;

import ch.duckpond.universe.client.Universe;

/**
 * Circle menu implementation.
 *
 * @author
 */
public class CircleMenu {
    private final Universe universe;
    private final List<CircleMenuItem> items = new LinkedList<CircleMenuItem>();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private boolean extended = false;
    private CircleMenuAttachable attachable;

    public CircleMenu(final Universe universe) {
        this.universe = universe;
    }

    public void addItem(final CircleMenuItem item) {
        items.add(item);
    }

    public void attach(final CircleMenuAttachable attachable) {
        this.attachable = attachable;
    }

    public void render() {
        if (!extended) {
            return;
        }
        if (items.size() <= 0) {
            throw new GdxRuntimeException("Can not render an empty CircleMenu");
        }

        // draw menu
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(universe.getCamera().combined);

        final Vector3 attachableTranslation = new Vector3(attachable.getWorldPosition()).sub(
                universe.getCamera().position);
        universe.getCamera().translate(attachableTranslation);
        universe.getCamera().update();

        final float itemDistance__degree = 360f / items.size();
        final Vector3 itemRadius = new Vector3(0, attachable.getCircleRadius(), 0);
        for (final CircleMenuItem item : items) {

            universe.getCamera().rotate(itemDistance__degree);
            universe.getCamera().update();
        }
        universe.getCamera().translate(attachableTranslation.scl(-1f));
        universe.getCamera().update();
    }
}
