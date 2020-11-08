package zame.game.engine.controls;

import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.Game;
import zame.game.engine.state.State;
import zame.game.engine.visual.Controls;

public abstract class OnScreenController {
    Controls owner;
    Engine engine;
    Config config;
    Game game;
    State state;

    private float prevOffsetX;
    private float prevOffsetY;
    private float offsetXMinBound;
    private float offsetXMaxBound;
    private float offsetYMinBound;
    private float offsetYMaxBound;

    public int pointerId = -1;
    float startX;
    float startY;
    float offsetX;
    float offsetY;
    public int renderModeMask = Game.RENDER_MODE_GAME;
    public int position;
    public boolean renderAnyway;
    public int controlFlags;
    int helpLabelId = -1;

    public void setOwner(Controls owner, Engine engine) {
        this.owner = owner;
        this.engine = engine;
        this.config = engine.config;
        this.game = engine.game;
        this.state = engine.state;

        surfaceSizeChanged(); // refresh position for the great justice
    }

    @SuppressWarnings("MagicNumber")
    public void surfaceSizeChanged() {
        offsetXMinBound = -(float)engine.width * 0.5f;
        offsetXMaxBound = (float)engine.width * 0.5f;
        offsetYMinBound = -(float)engine.height * 0.5f;
        offsetYMaxBound = (float)engine.height * 0.5f;
    }

    public abstract boolean pointerDown(float x, float y);

    public boolean pointerMove(float x, float y) {
        offsetX = x - startX;
        offsetY = y - startY;

        float diffX = offsetX - prevOffsetX;
        float diffY = offsetY - prevOffsetY;

        if (diffX < offsetXMinBound || diffX > offsetXMaxBound || diffY < offsetYMinBound || diffY > offsetYMaxBound) {
            return false;
        }

        prevOffsetX = offsetX;
        prevOffsetY = offsetY;
        return true;
    }

    public void pointerUp() {
        pointerId = -1;
        offsetX = 0.0f;
        offsetY = 0.0f;
        prevOffsetX = 0.0f;
        prevOffsetY = 0.0f;
    }

    public void render(long elapsedTime, boolean canRenderHelp) {
    }

    public void updateHero() {
    }

    protected float getHelpDiagSize() {
        return Controls.DIAG_SIZE;
    }

    boolean shouldRenderHelp() {
        return ((renderAnyway || controlFlags == 0 || (state.disabledControlsMask & controlFlags) != controlFlags)
                && (state.controlsHelpMask & controlFlags) != 0
                && (renderModeMask & game.renderMode) != 0
                && helpLabelId >= 0);
    }

    public void renderHelp(long elapsedTime) {
        if (!shouldRenderHelp()) {
            return;
        }

        owner.renderHelpArrowWithText(
                startX,
                startY,
                getHelpDiagSize(),
                (position & Controls.POSITION_RIGHT) == 0,
                (position & Controls.POSITION_TOP) == 0,
                helpLabelId);
    }
}
