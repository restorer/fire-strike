package zame.game.feature.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import zame.game.core.util.Common;
import zame.game.engine.Engine;
import zame.game.engine.controller.HeroController;
import zame.game.feature.main.MainActivity;

public class GameView extends zame.game.core.widget.GLSurfaceView21
        implements zame.game.core.widget.GLSurfaceView21.Renderer {

    private Engine engine;
    private HeroController heroController;

    public GameView(Context context) {
        super(context);
        initialize(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public void initialize(Context context) {
        this.engine = ((MainActivity)context).engine;
        this.heroController = engine.heroController;

        //noinspection MagicNumber
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        setFocusable(true);
        requestFocus();
        setFocusableInTouchMode(true);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        engine.onSurfaceCreated(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        engine.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        engine.onDrawFrame(gl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return Common.canUseKey(keyCode) && heroController.onKeyDown(keyCode) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return Common.canUseKey(keyCode) && heroController.onKeyUp(keyCode) || super.onKeyUp(keyCode, event);

    }

    @SuppressLint({ "ObsoleteSdkInt", "ClickableViewAccessibility" })
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        heroController.onTouchEvent(event);

        try {
            //noinspection MagicNumber
            Thread.sleep((Build.VERSION.SDK_INT < 8) ? 16 : 1);
        } catch (InterruptedException e) {
            // ignored
        }

        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        heroController.onTrackballEvent(event);
        return true;
    }
}
