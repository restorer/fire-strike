package zame.game.engine;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import zame.game.App;
import zame.game.engine.controls.Controls;

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

    float gamma;
    boolean showCrosshair;
    @SuppressWarnings("MagicNumber") float wpDim = 0.5f;

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    private void updateKeyMap(SharedPreferences sp, String key, int type) {
        int keyCode = sp.getInt(key, 0);

        if (keyCode > 0 && keyCode < keyMappings.length) {
            keyMappings[keyCode] = type;
        }
    }

    private float getAccel(int value,
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

    @SuppressWarnings("MagicNumber")
    public void reload() {
        SharedPreferences sp = App.self.getSharedPreferences();
        String controlSchemeStr = sp.getString("ControlsScheme", "StaticMovePad");

        if ("FreeMovePad".equals(controlSchemeStr)) {
            controlScheme = Controls.SCHEME_FREE_MOVE_PAD;
        } else {
            controlScheme = Controls.SCHEME_STATIC_MOVE_PAD;
        }

        moveSpeed = getAccel(sp.getInt("MoveSpeed", 8), 1, 8, 15, 0.25f, 0.5f, 1.0f);
        strafeSpeed = getAccel(sp.getInt("StrafeSpeed", 8), 1, 8, 15, 0.25f, 0.5f, 1.0f) * 0.5f;
        rotateSpeed = getAccel(sp.getInt("RotateSpeed", 8), 1, 8, 15, 0.5f, 1.0f, 2.0f);
        verticalLookMult = (sp.getBoolean("InvertVerticalLook", false) ? -1.0f : 1.0f);
        horizontalLookMult = (sp.getBoolean("InvertHorizontalLook", false) ? -1.0f : 1.0f);
        leftHandAim = sp.getBoolean("LeftHandAim", false);
        fireButtonAtTop = sp.getBoolean("FireButtonAtTop", false);
        controlsAlpha = 0.1f * (float)sp.getInt("ControlsAlpha", 5);
        accelerometerEnabled = sp.getBoolean("AccelerometerEnabled", false);
        accelerometerAcceleration = (float)sp.getInt("AccelerometerAcceleration", 5);
        trackballAcceleration = getAccel(sp.getInt("TrackballAcceleration", 5), 1, 5, 9, 0.1f, 1.0f, 10.0f);

        keyMappings = new int[KeyEvent.getMaxKeyCode()];

        for (int i = 0; i < keyMappings.length; i++) {
            keyMappings[i] = 0;
        }

        updateKeyMap(sp, "KeyForward", Controls.FORWARD);
        updateKeyMap(sp, "KeyBackward", Controls.BACKWARD);
        updateKeyMap(sp, "KeyRotateLeft", Controls.ROTATE_LEFT);
        updateKeyMap(sp, "KeyRotateRight", Controls.ROTATE_RIGHT);
        updateKeyMap(sp, "KeyStrafeLeft", Controls.STRAFE_LEFT);
        updateKeyMap(sp, "KeyStrafeRight", Controls.STRAFE_RIGHT);
        updateKeyMap(sp, "KeyFire", Controls.FIRE);
        updateKeyMap(sp, "KeyNextWeapon", Controls.NEXT_WEAPON);
        // updateKeyMap(sp, "KeyToggleMap", Controls.TOGGLE_MAP);
        updateKeyMap(sp, "KeyStrafeMode", Controls.STRAFE_MODE);

        gamma = (float)sp.getInt("Gamma", 1) * 0.04f;
        showCrosshair = sp.getBoolean("ShowCrosshair", true);
        rotateScreen = sp.getBoolean("RotateScreen", false);
    }
}
