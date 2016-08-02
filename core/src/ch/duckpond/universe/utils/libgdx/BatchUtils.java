package ch.duckpond.universe.utils.libgdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Varios utils for Batches
 * Created by ente on 7/24/16.
 */
public class BatchUtils {

    /**
     * Synchronizes the shape renderer with the given batch renderer
     *
     * @param batch
     * @param shapeRenderer
     */
    public static void syncShapeRendererWithBatch(final Batch batch, final ShapeRenderer shapeRenderer) {
        shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.setColor(batch.getColor());
    }
}
