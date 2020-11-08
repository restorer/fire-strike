package zame.game.engine.controls;

import zame.game.App;
import zame.game.engine.Game;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.util.GameMath;
import zame.game.engine.visual.Controls;

public class OnScreenButton extends OnScreenController {
    public static final int TYPE_GAME_MENU = 1;
    public static final int TYPE_RESTART = 2;
    public static final int TYPE_REWARDED_CONTINUE = 3;

    public boolean isVisible = true;
    private final int type;
    private float fromX;
    private float fromY;
    private float toX;
    private float toY;
    private boolean active;

    public OnScreenButton(int position, int type) {
        super();

        this.position = position;
        this.type = type;

        switch (type) {
            case TYPE_GAME_MENU:
                this.renderAnyway = true;
                this.controlFlags = Controls.CONTROL_WEAPONS;
                this.helpLabelId = Labels.LABEL_HELP_WEAPONS;
                break;

            case TYPE_RESTART:
                // fallthrough

            case TYPE_REWARDED_CONTINUE:
                this.renderModeMask = Game.RENDER_MODE_GAME_OVER;
                break;
        }
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void surfaceSizeChanged() {
        super.surfaceSizeChanged();

        float btnOffsetX = (type == TYPE_GAME_MENU ? owner.iconSize * 0.55f : owner.iconSize * 2.0f);
        float btnOffsetY = (type == TYPE_GAME_MENU ? owner.iconSize * 0.55f : owner.iconSize * 1.5f);
        float btnClickArea = (type == TYPE_GAME_MENU ? owner.iconSize * 0.5f : owner.iconSize * 1.5f);

        if ((position & Controls.POSITION_TOP) != 0) {
            startY = btnOffsetY;
        } else {
            startY = (float)engine.height - 1.0f - btnOffsetY;
        }

        if ((position & Controls.POSITION_RIGHT) != 0) {
            startX = (float)engine.width - 1.0f - btnOffsetX;
        } else if ((position & Controls.POSITION_HCENTER) != 0) {
            startX = ((float)engine.width - 1.0f) * 0.5f;
        } else {
            startX = btnOffsetX;
        }

        fromX = startX - btnClickArea;
        fromY = startY - btnClickArea;
        toX = startX + btnClickArea;
        toY = startY + btnClickArea;
    }

    @Override
    public boolean pointerDown(float x, float y) {
        if (!isVisible || (type == TYPE_REWARDED_CONTINUE && !App.self.mediadtor.isRewardedVideoLoaded())) {
            return false;
        }

        if (x >= fromX && x <= toX && y >= fromY && y <= toY) {
            switch (type) {
                case TYPE_GAME_MENU:
                    game.actionGameMenu = true;
                    break;

                case TYPE_RESTART:
                    game.actionRestartButton = true;
                    break;

                case TYPE_REWARDED_CONTINUE:
                    game.actionContinueButton = true;
                    break;
            }

            active = true;
            return true;
        }

        return false;
    }

    @Override
    public void pointerUp() {
        super.pointerUp();
        active = false;

        switch (type) {
            case TYPE_GAME_MENU:
                game.actionGameMenu = false;
                break;

            case TYPE_RESTART:
                game.actionRestartButton = false;
                break;

            case TYPE_REWARDED_CONTINUE:
                game.actionContinueButton = false;
                break;
        }
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void render(long elapsedTime, boolean canRenderHelp) {
        if (!isVisible) {
            return;
        }

        int tex = 0;
        float dt = (float)elapsedTime * 0.0025f;

        switch (type) {
            case TYPE_GAME_MENU:
                tex = TextureLoader.ICON_MENU;
                break;

            case TYPE_RESTART:
                tex = TextureLoader.BASE_BACKS + 2;
                break;

            case TYPE_REWARDED_CONTINUE:
                tex = TextureLoader.BASE_BACKS + 4;
                break;
        }

        if (type == TYPE_GAME_MENU) {
            // "&& engine.gameMenuPendingStep == 0" to fix button flickering when game menu is opened
            owner.batchIcon(startX, startY, tex, active && engine.gameMenuPendingStep == 0);
        } else if (type == TYPE_REWARDED_CONTINUE && !App.self.mediadtor.isRewardedVideoLoaded()) {
            owner.batchBack(startX, startY, tex, false, 0.125f, 1.0f);
        } else {
            float ratio = (float)Math.cos(dt * 2.0f + (type == TYPE_RESTART ? GameMath.PI_F : 0.0f));

            owner.batchBack(startX,
                    startY,
                    tex,
                    active,
                    ratio * 0.25f + 0.75f,
                    ratio * 0.05f + (type == TYPE_RESTART ? 0.95f : 1.05f));
        }

        if (canRenderHelp && shouldRenderHelp()) {
            owner.batchIcon(startX,
                    startY,
                    TextureLoader.ICON_JOY,
                    false,
                    0.5f - (float)Math.cos(dt * 2.0f) * 0.5f,
                    (float)Math.cos(dt % Math.PI) * 0.5f + 1.5f);
        }
    }

    @Override
    protected float getHelpDiagSize() {
        return (type == TYPE_GAME_MENU ? Controls.DIAG_SIZE_XXLG : Controls.DIAG_SIZE);
    }
}
