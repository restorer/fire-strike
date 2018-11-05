package zame.game.engine;

import android.view.MotionEvent;
import javax.microedition.khronos.opengles.GL10;

public abstract class HeroController implements EngineObject {
    public static HeroController newInstance(@SuppressWarnings("unused") boolean isWallpaper) {
        return new HeroControllerHuman();
    }

    public abstract void updateHero();

    public void reload() {
    }

    public void surfaceSizeChanged() {
    }

    public void onDrawFrame() {
    }

    @SuppressWarnings("WeakerAccess")
    public void updateAfterLoadOrCreate() {
    }

    public void renderControls(GL10 gl, boolean canRenderHelp, long firstTouchTime) {
    }

    public boolean onKeyUp(int keyCode) {
        return false;
    }

    public boolean onKeyDown(int keyCode) {
        return false;
    }

    public void onTouchEvent(MotionEvent event) {
    }

    public void onTrackballEvent(MotionEvent event) {
    }

    public void setAccelerometerValues(float accelerometerX, float accelerometerY) {
    }
}
