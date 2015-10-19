package ch.duckpond.universe.client.circlemenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Adaptor for {@link CircleMenuItem}
 *
 * @author ente
 */
public class CircleMenuItemAdaptor implements CircleMenuItem {

    @Override
    public void clicked(Object attachable) {
        Gdx.app.debug(getClass().getName(), String.format("clicked on %s", attachable));
    }


    @Override
    public void render(final Camera camera, final float radius) {
        // draw menu
        final ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.circle(camera.position.x, camera.position.y, radius);
        shapeRenderer.end();
    }
}
