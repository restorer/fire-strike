package zame.game.engine.controls;

import javax.microedition.khronos.opengles.GL10;
import zame.game.App;
import zame.game.engine.Game;
import zame.game.engine.GameMath;
import zame.game.engine.Labels;
import zame.game.engine.TextureLoader;

public class OnScreenFireAndRotate extends OnScreenController {
    private float fromX;
    private float toX;
    private float fireBtnX;
    private float fireBtnY;
    private float fireFromX;
    private float fireFromY;
    private float fireToX;
    private float fireToY;
    private float heroA;
    private float heroVertA;
    private boolean fireActive;

    OnScreenFireAndRotate(int position) {
        super();

        this.position = position;
        this.renderModeMask = Game.RENDER_MODE_ALL;

        this.controlFlags = Controls.CONTROL_FIRE
                | Controls.CONTROL_ROTATE
                | Controls.CONTROL_ROTATE_LEFT
                | Controls.CONTROL_ROTATE_RIGHT;

        this.helpLabelId = Labels.LABEL_HELP_FIRE;
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void surfaceSizeChanged() {
        super.surfaceSizeChanged();

        float btnOffsetX = owner.iconSize * 2.0f;
        float btnOffsetY = owner.iconSize * 1.25f;
        float btnClickArea = owner.iconSize * (App.self.isLargeDevice ? (0.75f * 0.5f) : 0.5f);

        if ((position & Controls.POSITION_TOP) != 0) {
            fireBtnY = btnOffsetY;
        } else {
            fireBtnY = (float)engine.height - 1.0f - btnOffsetY;
        }

        if ((position & Controls.POSITION_RIGHT) != 0) {
            fromX = (float)engine.width * 0.4f;
            toX = (float)engine.width;
            fireBtnX = (float)engine.width - 1.0f - btnOffsetX;
        } else {
            fromX = 0.0f;
            toX = (float)engine.width * 0.6f;
            fireBtnX = btnOffsetX;
        }

        fireFromX = fireBtnX - btnClickArea;
        fireFromY = fireBtnY - btnClickArea;
        fireToX = fireBtnX + btnClickArea;
        fireToY = fireBtnY + btnClickArea;
    }

    @Override
    public boolean pointerDown(float x, float y) {
        if (x < fromX || x > toX) {
            return false;
        }

        startX = x;
        startY = y;

        heroA = state.heroA;
        heroVertA = state.heroVertA;

        if (x >= fireFromX
                && x <= fireToX
                && y >= fireFromY
                && y <= fireToY
                && (state.disabledControlsMask & Controls.CONTROL_FIRE) == 0) {

            game.actionFire |= Controls.ACTION_FIRE_ONSCREEN;
            fireActive = true;
            engine.interacted = true;
        }

        return true;
    }

    @Override
    public void pointerUp() {
        super.pointerUp();

        game.actionFire &= ~Controls.ACTION_FIRE_ONSCREEN;
        fireActive = false;
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void render(GL10 gl, long elapsedTime, boolean canRenderHelp) {
        if ((state.disabledControlsMask & Controls.CONTROL_FIRE) == 0) {
            owner.drawIcon(fireBtnX, fireBtnY, TextureLoader.ICON_SHOOT, fireActive);
        }

        boolean isHelp = canRenderHelp && shouldDrawHelp();

        if (game.renderMode != Game.RENDER_MODE_GAME || isHelp) {
            float dt = (float)elapsedTime * 0.0025f;

            if (isHelp
                    && (state.disabledControlsMask & Controls.CONTROL_ROTATE) == 0
                    && (state.controlsHelpMask & (Controls.CONTROL_ROTATE
                    | Controls.CONTROL_ROTATE_LEFT
                    | Controls.CONTROL_ROTATE_RIGHT)) != 0) {

                boolean posLeft = (position & Controls.POSITION_RIGHT) == 0;
                float off = (float)Math.cos(dt % Math.PI) * 0.2f;

                //noinspection IfStatementWithIdenticalBranches
                if ((state.controlsHelpMask & Controls.CONTROL_ROTATE_RIGHT) != 0) {
                    off = -off;
                } else if (((state.controlsHelpMask & Controls.CONTROL_ROTATE) != 0) && (dt % (GameMath.PI_F * 4.0f)
                        > GameMath.PI_F * 2.0f)) {

                    off = -off;
                }

                owner.drawIcon(((posLeft ? 0.25f : 0.75f) + off) * (float)engine.width,
                        (float)engine.height * 0.5f,
                        TextureLoader.ICON_JOY,
                        false,
                        0.5f - (float)Math.cos(dt * 2.0f) * 0.5f);
            }

            if (game.renderMode != Game.RENDER_MODE_GAME || ((state.disabledControlsMask & Controls.CONTROL_FIRE) == 0
                    && (state.controlsHelpMask & Controls.CONTROL_FIRE) != 0)) {

                owner.drawIcon(fireBtnX,
                        fireBtnY,
                        TextureLoader.ICON_JOY,
                        false,
                        0.5f - (float)Math.cos(dt * 2.0f) * 0.5f,
                        (float)Math.cos(dt % Math.PI) * 0.5f + 1.5f);
            }
        }
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void updateHero() {
        if (pointerId < 0) {
            if (Math.abs(state.heroVertA) > 0.5f) {
                state.heroVertA *= 0.75f;

                if (Math.abs(state.heroVertA) <= 0.5f) {
                    state.heroVertA = 0.0f;
                }
            }

            return;
        }

        float sign = (offsetX < 0.0f ? -1.0f : 1.0f);
        float value = Math.abs(offsetX) / (float)engine.width;

        if (value < 0.25f) {
            value *= 5.0f;
            value *= value;
            value *= 0.16f;
        }

        float rotateHor = value * sign * 360.0f;

        sign = (offsetY < 0.0f ? -1.0f : 1.0f);
        value = Math.abs(offsetY) / (float)engine.height;

        if (value < 0.25f) {
            value *= 5.0f;
            value *= value;
            value *= 0.16f;
        }

        float rotateVert = value * sign * 45.0f;

        if (Math.abs(rotateHor) > 0.5f) {
            engine.interacted = true;
        }

        if ((state.disabledControlsMask & Controls.CONTROL_ROTATE) != 0) {
            engine.overlay.showLabel(Labels.LABEL_HELP_DO_NOT_ROTATE);
            return;
        }

        state.setHeroA(heroA - rotateHor * config.horizontalLookMult * config.rotateSpeed);
        state.setHeroVertA(heroVertA - rotateVert * config.verticalLookMult);
    }

    @SuppressWarnings("MagicNumber")
    @Override
    protected void drawHelp(GL10 gl, long elapsedTime) {
        if (!shouldDrawHelp()) {
            return;
        }

        boolean posLeft = (position & Controls.POSITION_RIGHT) == 0;

        if ((state.disabledControlsMask & Controls.CONTROL_FIRE) == 0
                && (state.controlsHelpMask & Controls.CONTROL_FIRE) != 0) {

            owner.drawHelpArrowWithText(gl,
                    fireBtnX,
                    fireBtnY,
                    getHelpDiagSize(),
                    posLeft,
                    (position & Controls.POSITION_TOP) == 0,
                    helpLabelId);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_ROTATE) != 0
                || (state.controlsHelpMask & (Controls.CONTROL_ROTATE
                | Controls.CONTROL_ROTATE_LEFT
                | Controls.CONTROL_ROTATE_RIGHT)) == 0) {

            return;
        }

        float sx = ((posLeft ? 0.25f : 0.75f) - 0.2f) * engine.ratio;
        float ex = ((posLeft ? 0.25f : 0.75f) + 0.2f) * engine.ratio;

        engine.renderer.init();

        engine.renderer.setQuadA(0.5f);
        owner.drawArrow(gl, ex, 0.55f, sx, 0.55f, 0.0f, false);
        owner.drawArrow(gl, sx, 0.45f, ex, 0.45f, 0.0f, false);
        engine.renderer.flush(gl, false);

        engine.labels.beginDrawing(gl, true);
        engine.renderer.setQuadA(1.0f);

        engine.labels.draw(gl,
                sx,
                0.45f,
                ex,
                0.55f,
                engine.labels.map[Labels.LABEL_HELP_ROTATE],
                0.04f,
                Labels.ALIGN_CC);

        engine.labels.endDrawing(gl, true);
    }
}
