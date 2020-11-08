package zame.game.engine;

import android.view.KeyEvent;

import zame.game.App;
import zame.game.R;
import zame.game.core.manager.PreferencesManager;
import zame.game.engine.visual.Controls;

public class Config implements EngineObject {
    protected Engine engine;

    public int controlScheme;
    public float controlsAlpha;
    public boolean accelerometerEnabled;
    public float accelerometerAcceleration;
    public float trackballAcceleration;
    public float verticalLookMult;
    public float horizontalLookMult;
    public boolean leftHandAim;
    public boolean fireButtonAtTop;
    public float moveSpeed;
    public float strafeSpeed;
    public float rotateSpeed;
    public int[] keyMappings;
    public boolean rotateScreen;

    public float gamma;
    boolean showCrosshair;
    @SuppressWarnings("MagicNumber") float wpDim = 0.5f;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
    }

    private void updateKeyMap(int keyResId, int type) {
        int keyCode = App.self.preferences.getInt(keyResId);

        if (keyCode > 0 && keyCode < keyMappings.length) {
            keyMappings[keyCode] = type;
        }
    }

    private float getAccel(
            int value,
            @SuppressWarnings("SameParameterValue") int valueMin,
            int valueMid,
            int valueMax,
            float accelMin,
            float accelMid,
            float accelMax) {

        if (value <= valueMin) {
            return accelMin;
        } else if (value < valueMid) {
            return (float)(value - valueMin) / (float)(valueMid - valueMin) * (accelMid - accelMin) + accelMin;
        } else if (value == valueMid) {
            return accelMid;
        } else if (value < valueMax) {
            return (float)(value - valueMid) / (float)(valueMax - valueMid) * (accelMax - accelMid) + accelMid;
        } else {
            return accelMax;
        }
    }

    @SuppressWarnings({ "MagicNumber", "ExplicitArrayFilling" })
    public void reload() {
        PreferencesManager preferences = App.self.preferences;

        String controlSchemeStr = preferences.getString(
                R.string.key_controls_scheme,
                R.string.val_scheme_static_move_pad);

        if (App.self.getString(R.string.val_scheme_free_move_pad).equals(controlSchemeStr)) {
            controlScheme = Controls.SCHEME_FREE_MOVE_PAD;
        } else {
            controlScheme = Controls.SCHEME_STATIC_MOVE_PAD;
        }

        moveSpeed = getAccel(preferences.getInt(R.string.key_move_speed, 8), 1, 8, 15, 0.25f, 0.5f, 1.0f);
        strafeSpeed = getAccel(preferences.getInt(R.string.key_strafe_speed, 8), 1, 8, 15, 0.25f, 0.5f, 1.0f) * 0.5f;
        rotateSpeed = getAccel(preferences.getInt(R.string.key_rotate_speed, 8), 1, 8, 15, 0.5f, 1.0f, 2.0f);
        verticalLookMult = preferences.getBoolean(R.string.key_invert_vertical_look) ? -1.0f : 1.0f;
        horizontalLookMult = preferences.getBoolean(R.string.key_invert_horizontal_look) ? -1.0f : 1.0f;
        leftHandAim = preferences.getBoolean(R.string.key_left_hand_aim);
        fireButtonAtTop = preferences.getBoolean(R.string.key_fire_button_at_top);
        controlsAlpha = 0.1f * (float)preferences.getInt(R.string.key_controls_alpha, 5);
        accelerometerEnabled = preferences.getBoolean(R.string.key_accelerometer_enabled);
        accelerometerAcceleration = (float)preferences.getInt(R.string.key_accelerometer_acceleration, 5);

        trackballAcceleration = getAccel(
                preferences.getInt(R.string.key_trackball_acceleration, 5),
                1,
                5,
                9,
                0.1f,
                1.0f,
                10.0f);

        keyMappings = new int[KeyEvent.getMaxKeyCode()];

        for (int i = 0; i < keyMappings.length; i++) {
            keyMappings[i] = 0;
        }

        updateKeyMap(R.string.key_hwkey_forward, Controls.FORWARD);
        updateKeyMap(R.string.key_hwkey_backward, Controls.BACKWARD);
        updateKeyMap(R.string.key_hwkey_rotate_left, Controls.ROTATE_LEFT);
        updateKeyMap(R.string.key_hwkey_rotate_right, Controls.ROTATE_RIGHT);
        updateKeyMap(R.string.key_hwkey_strafe_left, Controls.STRAFE_LEFT);
        updateKeyMap(R.string.key_hwkey_strafe_right, Controls.STRAFE_RIGHT);
        updateKeyMap(R.string.key_hwkey_fire, Controls.FIRE);
        updateKeyMap(R.string.key_hwkey_next_weapon, Controls.NEXT_WEAPON);
        updateKeyMap(R.string.key_hwkey_strafe_mode, Controls.STRAFE_MODE);

        gamma = (float)preferences.getInt(R.string.key_gamma, 1) * 0.04f;
        showCrosshair = preferences.getBoolean(R.string.key_show_crosshair, true);
        rotateScreen = preferences.getBoolean(R.string.key_rotate_screen);
    }
}
