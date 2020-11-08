package zame.game.engine.visual;

import android.view.MotionEvent;

import zame.game.App;
import zame.game.core.util.Common;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.controls.OnScreenButton;
import zame.game.engine.controls.OnScreenController;
import zame.game.engine.controls.OnScreenFireAndRotate;
import zame.game.engine.controls.OnScreenPad;
import zame.game.engine.controls.OnScreenQuickWeapons;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.state.State;

public class Controls implements EngineObject {
    public static final int CONTROL_MOVE = 1;
    public static final int CONTROL_ROTATE = 2;
    public static final int CONTROL_FIRE = 4;
    public static final int CONTROL_WEAPONS = 8;
    public static final int CONTROL_QUICK_WEAPONS = 16;
    static final int CONTROL_MINIMAP = 32;
    static final int CONTROL_STATS_HEALTH = 64;
    static final int CONTROL_STATS_AMMO = 128;
    static final int CONTROL_STATS_ARMOR = 256;
    static final int CONTROL_STATS_KEYS = 512;
    public static final int CONTROL_MOVE_UP = 1024;
    public static final int CONTROL_MOVE_DOWN = 2048;
    public static final int CONTROL_MOVE_LEFT = 4096;
    public static final int CONTROL_MOVE_RIGHT = 8192;
    public static final int CONTROL_ROTATE_LEFT = 16384;
    public static final int CONTROL_ROTATE_RIGHT = 32768;

    public static final int SCHEME_STATIC_MOVE_PAD = 0;
    public static final int SCHEME_FREE_MOVE_PAD = 1;

    public static final int FORWARD = 1;
    public static final int BACKWARD = 2;
    public static final int STRAFE_LEFT = 4;
    public static final int STRAFE_RIGHT = 8;
    public static final int FIRE = 16;
    public static final int NEXT_WEAPON = 32;
    public static final int ROTATE_LEFT = 64;
    public static final int ROTATE_RIGHT = 128;
    public static final int STRAFE_MODE = 256;
    public static final int MASK_MAX = 512;

    private static final int POSITION_LEFT = 1;
    public static final int POSITION_HCENTER = 2;
    public static final int POSITION_RIGHT = 4;
    public static final int POSITION_TOP = 8;

    public static final int ACTION_FIRE_ONSCREEN = 1;
    public static final int ACTION_FIRE_KEYS = 2;

    private static final int POINTER_ACTION_DOWN = 1;
    private static final int POINTER_ACTION_MOVE = 2;
    private static final int POINTER_ACTION_UP = 3;
    private static final int MAX_POINTER_ID = 8;

    public static final float DIAG_SIZE = 0.075f;
    public static final float DIAG_SIZE_LG = 0.1f;
    static final float DIAG_SIZE_XLG = 0.2f;
    public static final float DIAG_SIZE_XXLG = 0.4f;
    private static final float ARROW_F_SIZE = 0.075f;
    private static final float ARROW_N_SIZE = ARROW_F_SIZE * 0.75f;
    private static final float ARROW_F_WEIGHT = ARROW_F_SIZE * 0.3f;
    private static final float LINE_WEIGHT = ARROW_F_SIZE * 0.05f;

    private Engine engine;
    private Renderer renderer;
    private Config config;
    private Game game;
    private Labels labels;
    private State state;
    private final OnScreenController[] activeControllers = new OnScreenController[MAX_POINTER_ID];

    private final OnScreenButton schemeMenuButton = new OnScreenButton(
            POSITION_LEFT | POSITION_TOP,
            OnScreenButton.TYPE_GAME_MENU);

    private final OnScreenQuickWeapons schemeQuickWeapons = new OnScreenQuickWeapons(POSITION_HCENTER | POSITION_TOP);
    private final OnScreenPad schemePad = new OnScreenPad(POSITION_LEFT, false);
    private final OnScreenFireAndRotate schemeFireAndRotate = new OnScreenFireAndRotate(POSITION_RIGHT);
    private final OnScreenButton schemeRestartButton = new OnScreenButton(POSITION_LEFT, OnScreenButton.TYPE_RESTART);

    private final OnScreenButton schemeContinueButton = new OnScreenButton(
            POSITION_RIGHT,
            OnScreenButton.TYPE_REWARDED_CONTINUE);

    private final OnScreenController[] scheme = { schemeMenuButton,
            schemeQuickWeapons,
            schemePad,
            schemeFireAndRotate, // must be after all game controls
            schemeRestartButton, // must be after fireAndRotate
            schemeContinueButton, // must be after fireAndRotate
    };

    public float iconSize;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
        this.config = engine.config;
        this.game = engine.game;
        this.labels = engine.labels;
        this.state = engine.state;
    }

    public void reload() {
        schemeMenuButton.position = (config.leftHandAim ? POSITION_RIGHT : POSITION_LEFT) | POSITION_TOP;
        schemeQuickWeapons.direction = (config.leftHandAim ? -1.0f : 1.0f);
        schemePad.position = (config.leftHandAim ? POSITION_RIGHT : POSITION_LEFT);
        schemePad.dynamic = (config.controlScheme == SCHEME_FREE_MOVE_PAD);

        schemeFireAndRotate.position = (config.leftHandAim ? POSITION_LEFT : POSITION_RIGHT)
                | (config.fireButtonAtTop ? POSITION_TOP : 0);

        if (engine.canShowRewardedVideo) {
            schemeRestartButton.position = (config.leftHandAim ? POSITION_RIGHT : POSITION_LEFT);
            schemeContinueButton.position = (config.leftHandAim ? POSITION_LEFT : POSITION_RIGHT);
            schemeContinueButton.isVisible = true;
        } else {
            schemeRestartButton.position = POSITION_HCENTER;
            schemeContinueButton.isVisible = false;
        }

        for (OnScreenController controller : scheme) {
            controller.setOwner(this, engine);
        }
    }

    public void surfaceSizeChanged() {
        //noinspection MagicNumber
        iconSize = (float)Math.min(engine.height, engine.width) / 5.0f * App.self.controlsScale;

        for (OnScreenController controller : scheme) {
            controller.surfaceSizeChanged();
            controller.pointerUp();
        }
    }

    private void processOnePointer(int pointerId, float x, float y, int pointerAction) {
        if (pointerId < 0 || pointerId >= MAX_POINTER_ID) {
            return;
        }

        if (config.rotateScreen) {
            x = (float)engine.width - x;
            y = (float)engine.height - y;
        }

        OnScreenController controller = activeControllers[pointerId];

        switch (pointerAction) {
            case POINTER_ACTION_DOWN:
                if (controller != null) {
                    controller.pointerUp();
                    activeControllers[pointerId] = null;
                    Common.log("Secondary POINTER_ACTION_DOWN for the same pointerId");
                }

                //noinspection ForLoopReplaceableByForEach
                for (int i = 0, len = scheme.length; i < len; i++) {
                    OnScreenController checkedController = scheme[i];

                    if ((
                            checkedController.renderAnyway
                                    || checkedController.controlFlags == 0
                                    || (state.disabledControlsMask & checkedController.controlFlags)
                                    != checkedController.controlFlags)
                            && checkedController.pointerId < 0
                            && (checkedController.renderModeMask & game.renderMode) != 0
                            && checkedController.pointerDown(x, y)) {

                        checkedController.pointerId = pointerId;
                        checkedController.pointerMove(x, y);
                        activeControllers[pointerId] = checkedController;
                        break;
                    }
                }
                break;

            case POINTER_ACTION_MOVE:
                if (controller != null && (controller.renderAnyway
                        || controller.controlFlags == 0
                        || (state.disabledControlsMask & controller.controlFlags) != controller.controlFlags)) {

                    if (!controller.pointerMove(x, y)) {
                        controller.pointerUp();
                        activeControllers[pointerId] = null;
                    }
                }
                break;

            default:
                if (controller != null) {
                    controller.pointerUp();
                    activeControllers[pointerId] = null;
                }
                break;
        }
    }

    public void touchEvent(MotionEvent event) {
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        int points = event.getPointerCount();

        int i;
        int aidx;

        switch (actionCode) {
            case MotionEvent.ACTION_DOWN:
                // fallthrough

            case MotionEvent.ACTION_POINTER_DOWN:
                aidx = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                if (aidx < points) {
                    processOnePointer(
                            event.getPointerId(aidx),
                            event.getX(aidx),
                            event.getY(aidx),
                            POINTER_ACTION_DOWN);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (i = 0; i < points; i++) {
                    processOnePointer(event.getPointerId(i), event.getX(i), event.getY(i), POINTER_ACTION_MOVE);
                }
                break;

            case MotionEvent.ACTION_UP:
                // fallthrough

            case MotionEvent.ACTION_POINTER_UP:
                // fallthrough

            case MotionEvent.ACTION_CANCEL:
                aidx = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                if (aidx < points) {
                    processOnePointer(event.getPointerId(aidx), event.getX(aidx), event.getY(aidx), POINTER_ACTION_UP);
                }
                break;
        }
    }

    public void batchIcon(float pointerX, float pointerY, int texNum, boolean pressed) {
        batchIcon(pointerX, pointerY, texNum, pressed, -1.0f, 1.0f);
    }

    public void batchIcon(
            float pointerX,
            float pointerY,
            int texNum,
            @SuppressWarnings("SameParameterValue") boolean pressed,
            float customAlpha) {

        batchIcon(pointerX, pointerY, texNum, pressed, customAlpha, 1.0f);
    }

    @SuppressWarnings("MagicNumber")
    public void batchIcon(
            float pointerX,
            float pointerY,
            int texNum,
            boolean pressed,
            float customAlpha,
            float customScale) {

        float screenX = pointerX / (float)engine.width * engine.ratio;
        float screenY = 1.0f - pointerY / (float)engine.height;

        float sx = screenX - 0.125f * App.self.controlsScale * customScale;
        float sy = screenY - 0.125f * App.self.controlsScale * customScale;

        renderer.setCoordsQuadRect(
                sx,
                sy,
                sx + 0.25f * App.self.controlsScale * customScale,
                sy + 0.25f * App.self.controlsScale * customScale);

        if (customAlpha >= 0.0f) {
            renderer.setColorQuadA(customAlpha);
        } else if (pressed) {
            renderer.setColorQuadA(1.0f);
        } else {
            renderer.setColorQuadA(config.controlsAlpha);
        }

        renderer.batchTexQuad(texNum);
    }

    public void batchBack(float pointerX, float pointerY, int texNum, boolean pressed) {
        batchBack(pointerX, pointerY, texNum, pressed, -1.0f, 1.0f);
    }

    @SuppressWarnings("MagicNumber")
    public void batchBack(float pointerX, float pointerY, int texNum, boolean pressed, float alpha, float scale) {
        float screenX = pointerX / (float)engine.width * engine.ratio;
        float screenY = 1.0f - pointerY / (float)engine.height;

        float sx = screenX - 0.25f * App.self.controlsScale * scale;
        float sy = screenY - 0.25f * App.self.controlsScale * scale;

        renderer.setCoordsQuadRect(
                sx,
                sy,
                sx + 0.5f * App.self.controlsScale * scale,
                sy + 0.5f * App.self.controlsScale * scale);

        if (alpha >= 0.0f) {
            renderer.setColorQuadA(alpha);
        } else if (pressed) {
            renderer.setColorQuadA(1.0f);
        } else {
            renderer.setColorQuadA(config.controlsAlpha);
        }

        renderer.batchTexQuad2x(texNum);
    }

    public void batchArrow(
            float sx,
            float sy,
            float ex,
            float ey,
            float hor,
            boolean drawLastSegment) {

        float dx = ex - sx;
        float dy = ey - sy;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        float mx = dx / dist;
        float my = dy / dist;
        float nx = -my;

        //noinspection UnnecessaryLocalVariable
        float ny = mx;

        renderer.x1 = sx + mx * ARROW_N_SIZE + nx * LINE_WEIGHT;
        renderer.y1 = sy + my * ARROW_N_SIZE + ny * LINE_WEIGHT;
        renderer.x2 = sx + mx * ARROW_N_SIZE;
        renderer.y2 = sy + my * ARROW_N_SIZE;
        renderer.x3 = sx;
        renderer.y3 = sy;
        renderer.x4 = sx + mx * ARROW_F_SIZE + nx * ARROW_F_WEIGHT;
        renderer.y4 = sy + my * ARROW_F_SIZE + ny * ARROW_F_WEIGHT;
        renderer.batchQuad();

        float tmpX = sx + mx * ARROW_N_SIZE - nx * LINE_WEIGHT;
        float tmpY = sy + my * ARROW_N_SIZE - ny * LINE_WEIGHT;

        renderer.x1 = sx;
        renderer.y1 = sy;
        renderer.x2 = sx + mx * ARROW_N_SIZE;
        renderer.y2 = sy + my * ARROW_N_SIZE;
        renderer.x3 = tmpX;
        renderer.y3 = tmpY;
        renderer.x4 = sx + mx * ARROW_F_SIZE - nx * ARROW_F_WEIGHT;
        renderer.y4 = sy + my * ARROW_F_SIZE - ny * ARROW_F_WEIGHT;
        renderer.batchQuad();

        renderer.x1 = ex + nx * LINE_WEIGHT;
        renderer.y1 = ey + ny * LINE_WEIGHT;
        renderer.x2 = ex - nx * LINE_WEIGHT;
        renderer.y2 = ey - ny * LINE_WEIGHT;
        renderer.x3 = tmpX;
        renderer.y3 = tmpY;
        renderer.x4 = sx + mx * ARROW_N_SIZE + nx * LINE_WEIGHT;
        renderer.y4 = sy + my * ARROW_N_SIZE + ny * LINE_WEIGHT;
        renderer.batchQuad();

        if (drawLastSegment) {
            renderer.x1 = ex + hor;
            renderer.y1 = ey + ny * LINE_WEIGHT;
            renderer.x2 = ex + hor;
            renderer.y2 = ey - ny * LINE_WEIGHT;
            renderer.x3 = ex - nx * LINE_WEIGHT;
            renderer.y3 = ey - ny * LINE_WEIGHT;
            renderer.x4 = ex + nx * LINE_WEIGHT;
            renderer.y4 = ey + ny * LINE_WEIGHT;
            renderer.batchQuad();
        }
    }

    private void subRenderControls(long elapsedTime, boolean canRenderHelp) {
        renderer.startBatch();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, len = scheme.length; i < len; i++) {
            OnScreenController controller = scheme[i];

            if ((controller.renderAnyway
                    || controller.controlFlags == 0
                    || (state.disabledControlsMask & controller.controlFlags) != controller.controlFlags)
                    && (controller.renderModeMask & game.renderMode) != 0) {

                controller.render(elapsedTime, canRenderHelp);
            }
        }

        renderer.renderBatch(Renderer.FLAG_BLEND, Renderer.TEXTURE_MAIN);
    }

    @SuppressWarnings("MagicNumber")
    public void renderHelpArrowWithText(
            float displayX,
            float displayY,
            float diagSize,
            boolean posLeft,
            boolean posBottom,
            int helpLabelId) {

        String helpText = labels.map[helpLabelId];
        float horSize = labels.getScaledWidth(helpText, 0.04f);

        float sx = displayX / (float)engine.width * engine.ratio;
        float sy = 1.0f - displayY / (float)engine.height;
        float ex = sx + (posLeft ? diagSize : -diagSize) * engine.ratio;
        float ey = sy + (posBottom ? diagSize : -diagSize);
        float hor = (posLeft ? horSize : -horSize);

        renderer.startBatch();
        renderer.setColorQuadA(0.5f);
        batchArrow(sx, sy, ex, ey, hor, true);
        renderer.renderBatch(Renderer.FLAG_BLEND);

        labels.startBatch(true);
        renderer.setColorQuadA(1.0f);

        if (posLeft) {
            if (posBottom) {
                labels.batch(ex, ey + 0.01f, ex + engine.ratio * 0.4f, ey + 0.06f, helpText, 0.04f, Labels.ALIGN_CL);
            } else {
                labels.batch(ex, ey - 0.05f, ex + engine.ratio * 0.4f, ey, helpText, 0.04f, Labels.ALIGN_CL);
            }
        } else {
            if (posBottom) {
                labels.batch(ex - engine.ratio * 0.4f, ey + 0.01f, ex, ey + 0.06f, helpText, 0.04f, Labels.ALIGN_CR);
            } else {
                labels.batch(ex - engine.ratio * 0.4f, ey - 0.05f, ex, ey, helpText, 0.04f, Labels.ALIGN_CR);
            }
        }

        labels.renderBatch();
    }

    private void subRenderBlackOverlay(long elapsedTime, long firstTouchTime) {
        //noinspection MagicNumber
        float op = 0.5f - (float)(elapsedTime - firstTouchTime) / 500.0f;

        if (op > 0.0f) {
            renderer.startBatch();
            renderer.setColorQuadRGBA(0.0f, 0.0f, 0.0f, op);
            renderer.setCoordsQuadRectFlat(0.0f, 0.0f, engine.ratio, 1.0f);
            renderer.batchQuad();
            renderer.renderBatch(Renderer.FLAG_BLEND);
        }
    }

    private void subRenderHelp(long elapsedTime) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, len = scheme.length; i < len; i++) {
            scheme[i].renderHelp(elapsedTime);
        }

        engine.stats.renderHelp(this);
        engine.autoMap.renderHelp(this);
    }

    public void render(boolean canRenderHelp, long elapsedTime, long firstTouchTime) {
        renderer.useOrtho(0.0f, engine.ratio, 0.0f, 1.0f, 0.0f, 1.0f);
        renderer.setCoordsQuadZ(0.0f);

        if (canRenderHelp && (state.controlsHelpMask != 0 || state.disabledControlsMask != 0)) {
            subRenderBlackOverlay(elapsedTime, firstTouchTime);
        }

        renderer.setColorQuadRGB(1.0f, 1.0f, 1.0f);
        subRenderControls(elapsedTime, canRenderHelp);

        if (canRenderHelp && state.controlsHelpMask != 0) {
            subRenderHelp(elapsedTime);
        }
    }

    public void updateHero() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, len = scheme.length; i < len; i++) {
            OnScreenController controller = scheme[i];

            if ((controller.renderAnyway
                    || controller.controlFlags == 0
                    || (state.disabledControlsMask & controller.controlFlags) != controller.controlFlags)
                    && (controller.renderModeMask & game.renderMode) != 0) {

                controller.updateHero();
            }
        }
    }
}
