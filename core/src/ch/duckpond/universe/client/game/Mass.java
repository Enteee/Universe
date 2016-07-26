package ch.duckpond.universe.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import ch.duckpond.universe.client.Player;
import ch.duckpond.universe.client.circlemenu.CircleMenu;
import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.utils.libgdx.BatchUtils;

/**
 * A mass in the game
 *
 * @author ente
 */
public class Mass extends Actor {

    public static final int KEEP_LAST_POSITIONS_COUNT = 300;

    private final GameScreen gameScreen;
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

        setDebug(true);

        this.gameScreen = gameScreen;
        owner = gameScreen.getThisPlayer();
        circleMenu = new CircleMenu(gameScreen, this);

        setZIndex(Globals.Z_INDEX_MASS);

        addListener(new MassInputListener());
    }

    public void setBody(final Body body) {
        this.body = body;
        updateBounds();
    }

    public void updateBounds() {
        float radius = 0;
        for (final Fixture fixture : body.getFixtureList()) {
            radius = Math.max(fixture.getShape().getRadius(), radius);
        }
        setBounds(body.getPosition().x - radius,
                  body.getPosition().y - radius,
                  radius * 2,
                  radius * 2);
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

        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        {
            for (final Fixture fixture : body.getFixtureList()) {
                final CircleShape circleShape = (CircleShape) fixture.getShape();
                // velocity trail
                int lastPositionsLeft = lastPositions.size();
                Vector3 lastlastPosition = null;
                for (final Vector3 lastPosition : lastPositions) {
                    final float intensity = (float) (Math.log(lastPositionsLeft) / Math.log(
                            lastPositions.size()));
                    if (lastlastPosition != null) {
                        final Color trailColor = new Color(owner.getColor());
                        trailColor.a = 1 - intensity;
                        shapeRenderer.setColor(new Color(trailColor));
                        shapeRenderer.rectLine(new Vector2(lastPosition.x, lastPosition.y),
                                               new Vector2(lastlastPosition.x, lastlastPosition.y),
                                               circleShape.getRadius() - circleShape.getRadius() * intensity);
                    }
                    lastlastPosition = lastPosition;
                    lastPositionsLeft--;
                }

                // outer glow border
                shapeRenderer.setColor(owner.getColor());
                shapeRenderer.circle(body.getPosition().x,
                                     body.getPosition().y,
                                     circleShape.getRadius());

                // punch out inner border
                final float innerCircleRadius = circleShape.getRadius() - Globals.MASS_SURFACE_WIDTH;
                shapeRenderer.setColor(Globals.WORLD_BACKGROUND_COLOR);
                shapeRenderer.circle(body.getPosition().x, body.getPosition().y, innerCircleRadius);
                //                shapeRenderer.setColor(new Color(0f, 0f, 0f, 0f));
                //                shapeRenderer.circle(body.getPosition().x,
                //                                     body.getPosition().y,
                //                                     innerCircleRadius - GameScreen.GLOW_SAMPLES / 2f * GameScreen.GLOW_QUALITY);
            }
        }
        shapeRenderer.end();
        batch.begin();
    }

    private class MassInputListener extends InputListener {
        private Vector3 touchDownPos = new Vector3();

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            touchDownPos = new Vector3(x, y, 0);
            Gdx.app.debug(getClass().getName(), String.format("touchDown %s", touchDownPos));
            switch (button) {
                case Input.Buttons.LEFT: {
                    gameScreen.setCenteredBody(body);
                    return true;
                }
            }
            return false;
        }
    }
}
