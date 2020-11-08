package zame.game.engine.controller;

import android.view.MotionEvent;

import zame.game.engine.Engine;
import zame.game.engine.controls.AccelerometerController;
import zame.game.engine.controls.KeysController;
import zame.game.engine.visual.Controls;

public class HeroControllerHuman extends HeroController {
    private Engine engine;
    private final Controls controls = new Controls();
    private final AccelerometerController accelerometerController = new AccelerometerController();
    private final KeysController keysController = new KeysController();

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;

        controls.onCreate(engine);
        accelerometerController.onCreate(engine);
        keysController.onCreate(engine);
    }

    @Override
    public void onDrawFrame() {
        keysController.onDrawFrame();
    }

    @Override
    public void updateHero() {
        controls.updateHero();
        accelerometerController.updateHero();
        keysController.updateHero();
    }

    @Override
    public void reload() {
        controls.reload();
        accelerometerController.reload();
        keysController.reload();
    }

    @Override
    public void surfaceSizeChanged() {
        controls.surfaceSizeChanged();
    }

    @Override
    public void render(boolean canRenderHelp, long firstTouchTime) {
        controls.render(canRenderHelp, engine.elapsedTime, firstTouchTime);
    }

    @Override
    public boolean onKeyUp(int keyCode) {
        return keysController.onKeyUp(keyCode);
    }

    @Override
    public boolean onKeyDown(int keyCode) {
        return keysController.onKeyDown(keyCode);
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        controls.touchEvent(event);
    }

    @Override
    public void onTrackballEvent(MotionEvent event) {
        keysController.onTrackballEvent(event);
    }

    @Override
    public void setAccelerometerValues(float accelerometerX, float accelerometerY) {
        accelerometerController.setAccelerometerValues(accelerometerX, accelerometerY);
    }
}
