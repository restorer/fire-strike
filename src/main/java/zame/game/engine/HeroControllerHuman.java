package zame.game.engine;

import android.view.MotionEvent;
import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.controls.AccelerometerController;
import zame.game.engine.controls.Controls;
import zame.game.engine.controls.KeysController;

public class HeroControllerHuman extends HeroController {
	private Engine engine;
	private Controls controls = new Controls();
	private AccelerometerController accelerometerController = new AccelerometerController();
	private KeysController keysController = new KeysController();

	@Override
	public void setEngine(Engine engine) {
		this.engine = engine;

		controls.setEngine(engine);
		accelerometerController.setEngine(engine);
		keysController.setEngine(engine);
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
	public void renderControls(GL10 gl, boolean canRenderHelp, long firstTouchTime) {
		controls.render(gl, canRenderHelp, engine.elapsedTime, firstTouchTime);
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
