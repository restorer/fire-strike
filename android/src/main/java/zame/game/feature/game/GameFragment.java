package zame.game.feature.game;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import zame.game.R;
import zame.game.core.app.BaseFragment;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.Game;
import zame.game.engine.controller.HeroController;
import zame.game.feature.sound.SoundManager;

public class GameFragment extends BaseFragment implements SensorEventListener {
    public static GameFragment newInstance() {
        return new GameFragment();
    }

    private ViewGroup viewGroup;
    private GameView gameView;
    private Engine engine;
    private Game game;
    private SensorManager sensorManager;
    private int deviceRotation;
    private GameMenuDialogFragment gameMenuDialogFragment;
    private GameCodeDialogFragment gameCodeDialogFragment;

    public Config config;
    public HeroController heroController;

    public GameFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isForgottenFragment) {
            return;
        }

        this.engine = activity.engine;
        this.config = engine.config;
        this.heroController = engine.heroController;
        this.game = engine.game;

        gameMenuDialogFragment = GameMenuDialogFragment.newInstance();
        gameCodeDialogFragment = GameCodeDialogFragment.newInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewGroup = (ViewGroup)inflater.inflate(R.layout.game_fragment, container, false);
        return viewGroup;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onResume() {
        super.onResume();

        if (isForgottenFragment) {
            return;
        }

        if (gameView != null) {
            viewGroup.removeView(gameView);
        }

        gameView = new GameView(activity);

        viewGroup.addView(
                gameView,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        gameView.setRenderer(gameView);
        engine.onActivated();
        gameView.onResume();

        if (config.accelerometerEnabled) {
            sensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);

            if (sensorManager != null) {
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

                // documentation says that getOrientation() is deprecated, and we must use getRotation instead()
                // but getRotation() available only for >= 2.2
                // if we look for getRotation() into android sources, we found nice piece of code:
                // public int getRotation() { return getOrientation(); }
                // so it should be safe to use getOrientation() instead of getRotation()
                deviceRotation = activity.getWindowManager().getDefaultDisplay().getOrientation();
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } else {
            sensorManager = null;
        }

        if (game.renderMode == Game.RENDER_MODE_END_LEVEL) {
            activity.soundManager.setPlaylist(SoundManager.LIST_ENDL);
        } else if (game.renderMode == Game.RENDER_MODE_GAME_OVER) {
            activity.soundManager.setPlaylist(SoundManager.LIST_GAMEOVER);
        } else {
            activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isForgottenFragment) {
            return;
        }

        if (gameView != null) {
            gameView.onPause();
            viewGroup.removeView(gameView);
            gameView = null;
        }

        engine.onPause();

        if (config.accelerometerEnabled && (sensorManager != null)) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        if (isForgottenFragment) {
            return;
        }

        if (hasWindowFocus) {
            engine.onResume();
        } else {
            engine.onPause();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        if (isForgottenFragment) {
            return;
        }

        float sensorX;
        float sensorY;

        if (config.accelerometerEnabled && (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER)) {
            switch (deviceRotation) {
                case Surface.ROTATION_90:
                    sensorX = -e.values[1];
                    sensorY = e.values[0];
                    break;

                case Surface.ROTATION_180:
                    sensorX = -e.values[0];
                    sensorY = -e.values[1];
                    break;

                case Surface.ROTATION_270:
                    sensorX = e.values[1];
                    sensorY = -e.values[0];
                    break;

                default:
                    sensorX = e.values[0];
                    sensorY = e.values[1];
                    break;
            }

            float accelerometerX = sensorX / SensorManager.GRAVITY_EARTH;
            float accelerometerY = sensorY / SensorManager.GRAVITY_EARTH;

            if (config.rotateScreen) {
                heroController.setAccelerometerValues(-accelerometerX, -accelerometerY);
            } else {
                heroController.setAccelerometerValues(accelerometerX, accelerometerY);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void showGameMenu() {
        if (!isForgottenFragment) {
            gameMenuDialogFragment.show(getFragmentManager());
        }
    }

    @SuppressWarnings("deprecation")
    public void showGameCodeDialog() {
        if (!isForgottenFragment) {
            gameCodeDialogFragment.show(getFragmentManager());
        }
    }

    // public void hideDialogs() {
    // 	if (gameCodeDialogFragment.isVisible()) {
    // 		gameCodeDialogFragment.dismiss();
    // 	}
    //
    // 	if (gameMenuDialogFragment.isVisible()) {
    // 		gameMenuDialogFragment.dismiss();
    // 	}
    // }
}
