package zame.game.engine.controller;

import android.view.MotionEvent;

import zame.game.engine.EngineObject;

public abstract class HeroController implements EngineObject {
    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public static HeroController newInstance(boolean isWallpaper) {
        return new HeroControllerHuman();
    }

    public abstract void updateHero();

    public void reload() {
    }

    public void surfaceSizeChanged() {
    }

    public void onDrawFrame() {
    }

    public void updateAfterLevelLoadedOrCreated() {
    }

    public void render(boolean canRenderHelp, long firstTouchTime) {
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
