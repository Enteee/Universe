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

    public static final int KEEP_LAST_POSITIONS_COUNT = 50;
    public static final int KEEP_LAST_POSITION_EVERY_X = 10;

    private final GameScreen gameScreen;
    private final CircleMenu circleMenu;
    private final CircularFifoQueue<Vector3> lastPositions = new CircularFifoQueue(
            KEEP_LAST_POSITIONS_COUNT);
    private Player owner;
    private Body body;
    private int keepLastPositionCalls = 0;

    /**
     * Mass owned by the local player
     *
     * @param gameScreen
     */
    public Mass(final GameScreen gameScreen) {
        this(gameScreen, gameScreen.getSimulation().getThisPlayer());
    }

    /**
     * Mass owned by the given player
     *
     * @param gameScreen
     * @param owner
     */
    public Mass(final GameScreen gameScreen, final Player owner) {
        assert gameScreen != null;
        assert owner != null;

        this.gameScreen = gameScreen;
        this.owner = owner;
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
        assert lastPosition != null;
        if (keepLastPositionCalls % KEEP_LAST_POSITION_EVERY_X == 0) {
            lastPositions.add(lastPosition);
        }
        keepLastPositionCalls++;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // draw massess
        final ShapeRenderer shapeRenderer = gameScreen.getShapeRenderer();

        batch.end();
        BatchUtils.syncShapeRendererWithBatch(batch, shapeRenderer);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        {
            for (final Fixture fixture : body.getFixtureList()) {
                final CircleShape circleShape = (CircleShape) fixture.getShape();
                // velocity trail, draw from back to front
                final Color trailColor = new Color(owner.getColor());
                // alpha channel = 0 looks fancy in combination with shader, alternatively set
                // alpha channel = intensity in loop
                trailColor.a = 0;
                int i = 0;
                Vector3 lastlastPosition = null;
                for (final Vector3 lastPosition : lastPositions) {
                    if (lastlastPosition != null) {
                        final float intensity = (float) (Math.log(i) / Math.log(lastPositions.size()));
                        //trailColor.a = intensity;
                        shapeRenderer.setColor(trailColor);
                        shapeRenderer.rectLine(new Vector2(lastPosition.x, lastPosition.y),
                                               new Vector2(lastlastPosition.x, lastlastPosition.y),
                                               circleShape.getRadius() * 2 * intensity);
                    }
                    lastlastPosition = lastPosition;
                    i++;
                }
                if (lastlastPosition != null) {
                    shapeRenderer.rectLine(new Vector2(body.getPosition().x, body.getPosition().y),
                                           new Vector2(lastlastPosition.x, lastlastPosition.y),
                                           circleShape.getRadius() * 2);
                }


                // outer glow border
                shapeRenderer.setColor(owner.getColor());
                shapeRenderer.circle(body.getPosition().x,
                                     body.getPosition().y,
                                     circleShape.getRadius());

                // punch out inner border
                final float innerCircleRadius = circleShape.getRadius() - circleShape.getRadius() * Globals.REL_MASS_SURFACE_WIDTH;
                shapeRenderer.setColor(Globals.WORLD_BACKGROUND_COLOR);
                shapeRenderer.circle(body.getPosition().x, body.getPosition().y, innerCircleRadius);
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
