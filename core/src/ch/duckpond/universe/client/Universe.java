package ch.duckpond.universe.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;

import ch.duckpond.universe.shared.simulation.Globals;
import ch.duckpond.universe.shared.simulation.Simulation;
import ch.duckpond.universe.test.utils.TestUtilsBody;

public class Universe extends ApplicationAdapter {

    private Simulation simulation;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private boolean massSpawning = false;
    private Vector2 massSpawnPoint = new Vector2();
    private Vector2 massSpawnVelocity = new Vector2();
    private Sprite backgroundSprite;
    private Texture backgroundTexture;


    public boolean isMassSpawning() {
        return massSpawning;
    }

    public void setMassSpawning(boolean massSpawning) {
        this.massSpawning = massSpawning;
    }

    public Vector2 getMassSpawnPoint() {
        return new Vector2(massSpawnPoint);
    }

    public void setMassSpawnPoint(Vector2 massSpawnPoint) {
        this.massSpawnPoint = new Vector2(massSpawnPoint);
        setMassSpawnVelocity(new Vector2());
        setMassSpawning(true);
    }

    public Vector2 getMassSpawnVelocity() {
        return new Vector2(massSpawnVelocity);
    }

    public void setMassSpawnVelocity(Vector2 massSpawnVelocity) {
        this.massSpawnVelocity = new Vector2(massSpawnVelocity);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void spawnMass() {
        if (isMassSpawning()) {
            simulation.spawnMass(
                    (int) massSpawnPoint.x,
                    (int) massSpawnPoint.y,
                    Globals.MASS_DEFAULT_RADIUS * camera.zoom,
                    getMassSpawnVelocity()
            );
            setMassSpawnVelocity(new Vector2());
            setMassSpawning(false);
        }
    }

    @Override
    public void create() {
        // gdx set up
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) {
            inputMultiplexer.addProcessor(new GestureDetector(new UniverseGestureProcessor(this)));
        } else {
            inputMultiplexer.addProcessor(new UniverseInputProcessor(this));
        }
        Gdx.input.setInputProcessor(inputMultiplexer);
        // simulation set up
        Box2D.init();
        simulation = new Simulation();
        // some random bodies
        for (int i = 0; i < 10; ++i) {
            TestUtilsBody.randomBody(simulation.getWorld());
        }
        // set up rendering
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        final float w = Gdx.graphics.getWidth();
        final float h = Gdx.graphics.getHeight();
        camera = new OrthographicCamera(w, h);
        camera.update();
        // load sprites
        backgroundTexture= new Texture(Gdx.files.internal("data/Grid.png"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        backgroundSprite = new Sprite(backgroundTexture);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // set up batch
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        // set up shape renderer
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.combined);
        // draw background
        batch.draw(
                backgroundTexture,
                camera.position.x - camera.viewportWidth / 2,
                camera.position.y - camera.viewportHeight / 2,
                camera.viewportWidth,
                camera.viewportHeight,
                0,0,
                1,1
        );
        // draw masses
        shapeRenderer.setColor(Color.GRAY);
        for (final Fixture fixture : simulation.getFixtures()) {
            final CircleShape circleShape = (CircleShape) fixture.getShape();
            shapeRenderer.circle(fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, circleShape.getRadius());
            shapeRenderer.line(new Vector2(), circleShape.getPosition());
        }
        // draw spaning mass
        if (isMassSpawning()) {
            shapeRenderer.circle(
                    massSpawnPoint.x,
                    massSpawnPoint.y,
                    Globals.MASS_DEFAULT_RADIUS * camera.zoom
            );
            shapeRenderer.line(getMassSpawnPoint(),
                               new Vector2(getMassSpawnPoint()).sub(getMassSpawnVelocity())
            );
        }
        batch.end();
        shapeRenderer.end();
        simulation.update();
    }
}
