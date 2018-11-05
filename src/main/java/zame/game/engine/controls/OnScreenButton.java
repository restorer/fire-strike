package zame.game.engine.controls;

import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.Labels;
import zame.game.engine.TextureLoader;

public class OnScreenButton extends OnScreenController {
    static final int TYPE_GAME_MENU = 1;

    private int type;
    private float fromX;
    private float fromY;
    private float toX;
    private float toY;
    private boolean active;

    OnScreenButton(int position, int type) {
        super();

        this.position = position;
        this.type = type;

        if (type == TYPE_GAME_MENU) {
            this.renderAnyway = true;
            this.controlFlags = Controls.CONTROL_WEAPONS;
            this.helpLabelId = Labels.LABEL_HELP_WEAPONS;
        }
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void surfaceSizeChanged() {
        super.surfaceSizeChanged();

        float btnOffsetX = owner.iconSize * 0.55f;
        float btnOffsetY = owner.iconSize * 0.55f;
        float btnClickArea = owner.iconSize * 0.5f;

        if ((position & Controls.POSITION_TOP) != 0) {
            startY = btnOffsetY;
        } else {
            startY = (float)engine.height - 1.0f - btnOffsetY;
        }

        if ((position & Controls.POSITION_RIGHT) != 0) {
            startX = (float)engine.width - 1.0f - btnOffsetX;
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
        if (x >= fromX && x <= toX && y >= fromY && y <= toY) {
            switch (type) {
                case TYPE_GAME_MENU:
                    game.actionGameMenu = true;
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
        }
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void render(GL10 gl, long elapsedTime, boolean canRenderHelp) {
        int tex = 0;

        switch (type) {
            case TYPE_GAME_MENU:
                tex = TextureLoader.ICON_MENU;
                break;
        }

        owner.drawIcon(startX, startY, tex, active);

        if (canRenderHelp && shouldDrawHelp()) {
            float dt = (float)elapsedTime * 0.0025f;

            owner.drawIcon(startX,
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
