package zame.game.engine.controls;

import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.Game;
import zame.game.engine.State;

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

    int pointerId = -1;
    float startX;
    float startY;
    float offsetX;
    float offsetY;
    int renderModeMask = Game.RENDER_MODE_GAME;
    int position;
    boolean renderAnyway;
    int controlFlags;
    int helpLabelId = -1;

    void setOwner(Controls owner, Engine engine) {
        this.owner = owner;
        this.engine = engine;
        this.config = engine.config;
        this.game = engine.game;
        this.state = engine.state;
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

    public void render(GL10 gl, long elapsedTime, boolean canRenderHelp) {
    }

    public void updateHero() {
    }

    protected float getHelpDiagSize() {
        return Controls.DIAG_SIZE;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean shouldDrawHelp() {
        return ((renderAnyway || controlFlags == 0 || (state.disabledControlsMask & controlFlags) != controlFlags)
                && (state.controlsHelpMask & controlFlags) != 0
                && (renderModeMask & game.renderMode) != 0
                && helpLabelId >= 0);
    }

    protected void drawHelp(GL10 gl, long elapsedTime) {
        if (!shouldDrawHelp()) {
            return;
        }

        owner.drawHelpArrowWithText(gl,
                startX,
                startY,
                getHelpDiagSize(),
                (position & Controls.POSITION_RIGHT) == 0,
                (position & Controls.POSITION_TOP) == 0,
                helpLabelId);
    }
}
