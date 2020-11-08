package zame.game.engine.controls;

import android.view.KeyEvent;
import android.view.MotionEvent;

import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.state.State;
import zame.game.engine.visual.Controls;

public class KeysController implements EngineObject {
    public static class ControlAcceleration {
        public float value;
        boolean updated;

        public boolean active() {
            //noinspection MagicNumber
            return ((value <= -0.01f) || (value >= 0.01f));
        }
    }

    public static class ControlAccelerationBind {
        int controlType;
        int accelerationType;
        float mult;

        ControlAccelerationBind(int controlType, int accelerationType, float mult) {
            this.controlType = controlType;
            this.accelerationType = accelerationType;
            this.mult = mult;
        }
    }

    private static final int ACCELERATION_MOVE = 0;
    private static final int ACCELERATION_STRAFE = 1;
    private static final int ACCELERATION_ROTATE = 2;

    private Engine engine;
    private Config config;
    private Game game;
    private State state;
    private int keysActionsMask;
    private float trackballX;
    private float trackballY;
    private int trackballActionsMask;
    private final float[] relativeOffset = new float[Controls.MASK_MAX];
    private int actionsMask;

    private final ControlAcceleration[] accelerations = { new ControlAcceleration(), // ACCELERATION_MOVE
            new ControlAcceleration(), // ACCELERATION_STRAFE
            new ControlAcceleration(), // ACCELERATION_ROTATE
    };

    // @formatter:off
    private static final ControlAccelerationBind[] accelerationBinds = {
            new ControlAccelerationBind(Controls.FORWARD, ACCELERATION_MOVE, 1.0f),
            new ControlAccelerationBind(Controls.BACKWARD, ACCELERATION_MOVE, -1.0f),
            new ControlAccelerationBind(Controls.STRAFE_LEFT, ACCELERATION_STRAFE, -1.0f),
            new ControlAccelerationBind(Controls.STRAFE_RIGHT, ACCELERATION_STRAFE, 1.0f),
            new ControlAccelerationBind(Controls.ROTATE_LEFT, ACCELERATION_ROTATE, -1.0f),
            new ControlAccelerationBind(Controls.ROTATE_RIGHT, ACCELERATION_ROTATE, 1.0f) };
    // @formatter:on

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.config = engine.config;
        this.game = engine.game;
        this.state = engine.state;
    }

    public void reload() {
        keysActionsMask = 0;
        trackballX = 0.0f;
        trackballY = 0.0f;
        trackballActionsMask = 0;

        for (int i = 1; i < Controls.MASK_MAX; i *= 2) {
            relativeOffset[i] = 0.0f;
        }
    }

    @SuppressWarnings("MagicNumber")
    public void onDrawFrame() {
        int maskLeft;
        int maskRight;
        int maskUp;
        int maskDown;

        if (config.rotateScreen) {
            trackballX = -trackballX;
            trackballY = -trackballY;

            maskLeft = config.keyMappings[KeyEvent.KEYCODE_DPAD_RIGHT];
            maskRight = config.keyMappings[KeyEvent.KEYCODE_DPAD_LEFT];
            maskUp = config.keyMappings[KeyEvent.KEYCODE_DPAD_DOWN];
            maskDown = config.keyMappings[KeyEvent.KEYCODE_DPAD_UP];
        } else {
            maskLeft = config.keyMappings[KeyEvent.KEYCODE_DPAD_LEFT];
            maskRight = config.keyMappings[KeyEvent.KEYCODE_DPAD_RIGHT];
            maskUp = config.keyMappings[KeyEvent.KEYCODE_DPAD_UP];
            maskDown = config.keyMappings[KeyEvent.KEYCODE_DPAD_DOWN];
        }

        trackballActionsMask = 0;

        if ((trackballX <= -0.01f) || (trackballX >= 0.01f)) {
            if ((trackballX < 0) && (maskLeft != 0)) {
                trackballActionsMask |= maskLeft;
                relativeOffset[maskLeft] = -trackballX;
            } else if (maskRight != 0) {
                trackballActionsMask |= maskRight;
                relativeOffset[maskRight] = trackballX;
            }
        }

        if ((trackballY <= -0.01f) || (trackballY >= 0.01f)) {
            if ((trackballY < 0) && (maskUp != 0)) {
                trackballActionsMask |= maskUp;
                relativeOffset[maskUp] = -trackballY;
            } else if (maskDown != 0) {
                trackballActionsMask |= maskDown;
                relativeOffset[maskDown] = trackballY;
            }
        }

        trackballX = 0.0f;
        trackballY = 0.0f;

        actionsMask = keysActionsMask | trackballActionsMask;

        if ((actionsMask & Controls.STRAFE_MODE) != 0) {
            actionsMask = actionsMask & ~(Controls.ROTATE_LEFT
                    | Controls.ROTATE_RIGHT
                    | Controls.STRAFE_LEFT
                    | Controls.STRAFE_RIGHT)
                    | ((actionsMask & Controls.ROTATE_LEFT) != 0 ? Controls.STRAFE_LEFT : 0)
                    | ((actionsMask & Controls.ROTATE_RIGHT) != 0 ? Controls.STRAFE_RIGHT : 0)
                    | ((actionsMask & Controls.STRAFE_LEFT) != 0 ? Controls.ROTATE_LEFT : 0)
                    | ((actionsMask & Controls.STRAFE_RIGHT) != 0 ? Controls.ROTATE_RIGHT : 0);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void updateHero() {
        for (ControlAcceleration ca : accelerations) {
            ca.updated = false;
        }

        for (ControlAccelerationBind cb : accelerationBinds) {
            if ((actionsMask & cb.controlType) != 0) {
                ControlAcceleration ca = accelerations[cb.accelerationType];

                if ((trackballActionsMask & cb.controlType) == 0) {
                    ca.updated = true;
                    ca.value += cb.mult * 0.1f;
                } else {
                    ca.value += cb.mult * relativeOffset[cb.controlType] * config.trackballAcceleration;
                }

                if (ca.value < -1.0f) {
                    ca.value = -1.0f;
                } else if (ca.value > 1.0f) {
                    ca.value = 1.0f;
                }
            }
        }

        for (ControlAcceleration ca : accelerations) {
            if (!ca.updated) {
                ca.value *= 0.5f;

                if (!ca.active()) {
                    ca.value = 0.0f;
                }
            }
        }

        if (accelerations[ACCELERATION_MOVE].active() && (state.disabledControlsMask & Controls.CONTROL_MOVE) == 0) {
            game.updateHeroPosition(
                    engine.heroCs,
                    -engine.heroSn,
                    accelerations[ACCELERATION_MOVE].value * 0.2f * config.moveSpeed);

            engine.interacted = true;
        }

        if (accelerations[ACCELERATION_STRAFE].active() && (state.disabledControlsMask & Controls.CONTROL_MOVE) == 0) {
            game.updateHeroPosition(
                    engine.heroSn,
                    engine.heroCs,
                    accelerations[ACCELERATION_STRAFE].value * 0.2f * config.strafeSpeed);

            engine.interacted = true;
        }

        if (accelerations[ACCELERATION_ROTATE].active()
                && (state.disabledControlsMask & Controls.CONTROL_ROTATE) == 0) {

            state.setHeroA(state.heroA - (accelerations[ACCELERATION_ROTATE].value * 3.0f * config.rotateSpeed));
            engine.interacted = true;
        }

        if ((actionsMask & Controls.FIRE) != 0 && (state.disabledControlsMask & Controls.CONTROL_FIRE) == 0) {
            engine.interacted = true;
        }
    }

    public boolean onKeyDown(int keyCode) {
        if (keyCode >= 0 && keyCode < config.keyMappings.length && config.keyMappings[keyCode] != 0) {
            int mapping = config.keyMappings[keyCode];

            if (mapping == Controls.NEXT_WEAPON && ((state.disabledControlsMask & Controls.CONTROL_WEAPONS) == 0
                    || (state.disabledControlsMask & Controls.CONTROL_QUICK_WEAPONS) == 0)) {

                game.actionNextWeapon = true;
            } else if (mapping == Controls.FIRE && (state.disabledControlsMask & Controls.CONTROL_FIRE) == 0) {
                game.actionFire |= Controls.ACTION_FIRE_KEYS;
            }

            keysActionsMask |= mapping;
            return true;
        }

        return false;
    }

    public boolean onKeyUp(int keyCode) {
        if (keyCode >= 0 && keyCode < config.keyMappings.length && config.keyMappings[keyCode] != 0) {
            int mapping = config.keyMappings[keyCode];

            if (mapping == Controls.FIRE) {
                game.actionFire &= ~Controls.ACTION_FIRE_KEYS;
            }

            keysActionsMask &= ~mapping;
            return true;
        }

        return false;
    }

    public void onTrackballEvent(MotionEvent event) {
        trackballX += event.getX();
        trackballY += event.getY();
    }
}
