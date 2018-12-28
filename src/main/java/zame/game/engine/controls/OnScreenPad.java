package zame.game.engine.controls;

import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.visual.Controls;

public class OnScreenPad extends OnScreenController {
    private float fromX;
    private float fromY;
    private float toX;
    private float toY;
    private float minDist;
    private float maxDist;
    private boolean active;

    public boolean dynamic;

    public OnScreenPad(int position, boolean dynamic) {
        super();

        this.position = position;
        this.dynamic = dynamic;

        this.controlFlags = Controls.CONTROL_MOVE
                | Controls.CONTROL_MOVE_UP
                | Controls.CONTROL_MOVE_DOWN
                | Controls.CONTROL_MOVE_LEFT
                | Controls.CONTROL_MOVE_RIGHT;

        this.helpLabelId = Labels.LABEL_HELP_MOVE;
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void surfaceSizeChanged() {
        super.surfaceSizeChanged();

        fromY = (dynamic ? ((float)engine.height * 0.25f) : ((float)engine.height - owner.iconSize * 3.0f));
        toY = (float)engine.height - 1.0f;
        startY = toY - owner.iconSize * 1.25f;

        if ((position & Controls.POSITION_RIGHT) != 0) {
            fromX = (dynamic ? ((float)engine.width * 0.6f) : ((float)engine.width - owner.iconSize * 3.0f));
            toX = (float)engine.width - 1.0f;
            startX = toX - owner.iconSize * 1.25f;
        } else {
            fromX = 0.0f;
            toX = (dynamic ? ((float)engine.width * 0.4f) : (owner.iconSize * 3.0f));
            startX = fromX + owner.iconSize * 1.25f;
        }

        minDist = owner.iconSize * 0.1f;
        maxDist = owner.iconSize * 1.0f;
    }

    @Override
    public boolean pointerDown(float x, float y) {
        if (x >= fromX && x <= toX && y >= fromY && y <= toY) {
            if (dynamic) {
                startX = x;
                startY = y;
            }

            active = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean pointerMove(float x, float y) {
        if (!super.pointerMove(x, y)) {
            return false;
        }

        float dist = (float)Math.sqrt(offsetX * offsetX + offsetY * offsetY);

        if (dist > minDist) {
            active = true;
        }

        if (dist > maxDist) {
            offsetX = offsetX / dist * maxDist;
            offsetY = offsetY / dist * maxDist;
        }

        return true;
    }

    @Override
    public void pointerUp() {
        super.pointerUp();
        active = false;
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void render(long elapsedTime, boolean canRenderHelp) {
        owner.batchBack(startX, startY, TextureLoader.BASE_BACKS, active);
        owner.batchIcon(startX + offsetX, startY + offsetY, TextureLoader.ICON_JOY, active);

        if (canRenderHelp
                && !active && shouldRenderHelp()
                && (state.controlsHelpMask & Controls.CONTROL_MOVE) == 0) {

            float dt = (float)elapsedTime * 0.0025f;
            float x = startX;
            float y = startY;

            float off = ((state.controlsHelpMask & Controls.CONTROL_MOVE) != 0)
                    ? (float)Math.cos(dt) * owner.iconSize
                    : (float)Math.cos(dt % Math.PI) * owner.iconSize;

            // if ((state.controlsHelpMask & Controls.CONTROL_MOVE) != 0) {
            // if (dt % (GameMath.PI_F * 4.0f) > GameMath.PI_F * 2.0f) {
            //     x -= off;
            // } else {
            //     y += off;
            // }
            // } else

            if ((state.controlsHelpMask & Controls.CONTROL_MOVE_UP) != 0) {
                y += off;
            } else if ((state.controlsHelpMask & Controls.CONTROL_MOVE_DOWN) != 0) {
                y -= off;
            } else if ((state.controlsHelpMask & Controls.CONTROL_MOVE_LEFT) != 0) {
                x += off;
            } else if ((state.controlsHelpMask & Controls.CONTROL_MOVE_RIGHT) != 0) {
                x -= off;
            }

            owner.batchIcon(x, y, TextureLoader.ICON_JOY, false, 0.5f - (float)Math.cos(dt * 2.0f) * 0.5f);
        }
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void updateHero() {
        if (!active) {
            return;
        }

        float accelX = offsetX / maxDist * 0.2f;
        float accelY = offsetY / maxDist * 0.2f;

        if (Math.abs(accelX) > 0.005f) {
            game.updateHeroPosition(engine.heroSn, engine.heroCs, accelX * config.strafeSpeed);
            engine.interacted = true;
        }

        if (Math.abs(accelY) > 0.005f) {
            game.updateHeroPosition(-engine.heroCs, engine.heroSn, accelY * config.moveSpeed);
            engine.interacted = true;
        }
    }

    @Override
    protected float getHelpDiagSize() {
        return Controls.DIAG_SIZE_LG;
    }
}
