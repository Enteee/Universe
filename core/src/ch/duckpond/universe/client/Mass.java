package ch.duckpond.universe.client;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import ch.duckpond.universe.client.circlemenu.CircleMenu;
import ch.duckpond.universe.client.screen.GameScreen;
import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.utils.libgdx.BatchUtils;

/**
 * A mass in the game
 *
 * @author ente
 */
public class Mass extends Actor {

    public static final int KEEP_LAST_POSITIONS_COUNT = 300;

    private final CircleMenu circleMenu;
    private final CircularFifoQueue<Vector3> lastPositions = new CircularFifoQueue(
            KEEP_LAST_POSITIONS_COUNT);
    private Player owner;
    private Body body;

    /**
     * Mass owned by the local player
     *
     * @param gameScreen gameScreen
     */
    public Mass(final GameScreen gameScreen) {
        assert gameScreen != null;

        circleMenu = new CircleMenu(gameScreen, this);
        owner = gameScreen.getThisPlayer();
    }

    public void setBody(final Body body) {
        this.body = body;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(final Player owner) {
        if (owner == null) {
            throw new GdxRuntimeException("Owner == null");
        }
        this.owner = owner;
    }

    /**
     * Add the last known position
     *
     * @param lastPosition position in world coordinates
     */
    public void addLastPosition(final Vector3 lastPosition) {
        if (lastPosition == null) {
            throw new GdxRuntimeException("lastPosition == null");
        }
        lastPositions.add(lastPosition);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // draw massess
        final ShapeRenderer shapeRenderer = BatchUtils.buildShapeRendererFromBatch(batch);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.setColor(owner.getColor());

        for (final Fixture fixture : body.getFixtureList()) {
            final CircleShape circleShape = (CircleShape) fixture.getShape();
            // velocity trail
            final Color renderColor = shapeRenderer.getColor();

            int lastPositionsLeft = lastPositions.size();
            Vector3 lastlastPosition = null;
            for (final Vector3 lastPosition : lastPositions) {
                final float intensity = (float) (Math.log(lastPositionsLeft) / Math.log(
                        lastPositions.size()));
                if (lastlastPosition != null) {
                    final Color alphaColor = new Color(renderColor);
                    alphaColor.a = intensity;
                    shapeRenderer.setColor(alphaColor);
                    shapeRenderer.rectLine(new Vector2(lastPosition.x, lastPosition.y),
                                           new Vector2(lastlastPosition.x, lastlastPosition.y),
                                           circleShape.getRadius() * 2 - circleShape.getRadius() * 2 *
                                                   intensity);
                }
                lastlastPosition = lastPosition;
                lastPositionsLeft--;
            }

            shapeRenderer.setColor(renderColor);
            // outer glow border
            shapeRenderer.circle(body.getPosition().x,
                                 body.getPosition().y,
                                 circleShape.getRadius());

            // punch out inner border
            final float innerCircleRadius = circleShape.getRadius() - Globals.MASS_SURFACE_WIDTH;
            shapeRenderer.setColor(new Color(0f, 0f, 0f, 1f));
            shapeRenderer.circle(body.getPosition().x, body.getPosition().y, innerCircleRadius);
            shapeRenderer.setColor(new Color(0f, 0f, 0f, 0f));
            shapeRenderer.circle(body.getPosition().x,
                                 body.getPosition().y,
                                 innerCircleRadius - GameScreen.GLOW_SAMPLES / 2f * GameScreen.GLOW_QUALITY);
        }

        shapeRenderer.end();
    }
}
