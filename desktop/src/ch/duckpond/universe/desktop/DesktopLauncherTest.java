package ch.duckpond.universe.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import ch.duckpond.universe.utils.libgdx.BatchUtils;

/**
 * Quick and dirty testbed.
 */
public class DesktopLauncherTest extends ApplicationAdapter {

    final OrthographicCamera fbCam = new OrthographicCamera();
    private Viewport viewport;
    private BitmapFont font;

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new DesktopLauncherTest(), config);
    }

    @Override
    public void create() {
        viewport = new FitViewport(Gdx.graphics.getWidth(),
                                   Gdx.graphics.getHeight(),
                                   new OrthographicCamera());

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(
                "fonts/FifteenNarrow.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 100;
        parameter.color = Color.RED;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        fbCam.setToOrtho(true);
    }

    @Override
    public void render() {
        Batch batch = new SpriteBatch();
        ShapeRenderer shapeRenderer = BatchUtils.buildShapeRendererFromBatch(batch);

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        FrameBuffer fb = new FrameBuffer(Pixmap.Format.RGBA8888,
                                         viewport.getScreenWidth(),
                                         viewport.getScreenHeight(),
                                         false);
        fb.begin();
        {

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            {
                shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                shapeRenderer.x(0, 0, 100);
            }
            shapeRenderer.end();

            batch.begin();
            {
                batch.setProjectionMatrix(viewport.getCamera().combined);
                font.draw(batch, "0", 0, 0);
            }
            batch.end();
        }
        fb.end();


        batch.begin();
        {
            batch.setProjectionMatrix(fbCam.combined);
            batch.draw(fb.getColorBufferTexture(), 0, 0);
        }
        batch.end();

        fb.dispose();
    }
}
