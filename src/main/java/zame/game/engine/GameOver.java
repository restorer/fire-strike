package zame.game.engine;

import javax.microedition.khronos.opengles.GL10;
import zame.game.App;
import zame.game.managers.SoundManager;

public class GameOver implements EngineObject {
    private Engine engine;
    private Renderer renderer;
    private Labels labels;
    private SoundManager soundManager;
    private Game game;

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
        this.labels = engine.labels;
        this.soundManager = engine.soundManager;
        this.game = engine.game;
    }

    public void update() {
        if (game.actionUpgradeButton) {
            App.self.trackerInst.send("Upgrade.GameOver", engine.state.levelName);
            soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
            soundManager.setPlaylist(SoundManager.LIST_MAIN);
            engine.state.mustLoadAutosave = false;
            engine.changeView(Engine.VIEW_TYPE_REWARDED_VIDEO);
        }

        if (game.actionFire != 0) {
            soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
            soundManager.setPlaylist(SoundManager.LIST_MAIN);
            game.loadLevel(Game.LOAD_LEVEL_NORMAL);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void render(GL10 gl) {
        labels.beginDrawing(gl);
        renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);

        float sx = -engine.ratio + 0.1f;
        float ex = engine.ratio - 0.1f;

        labels.draw(gl, sx, 0.1f, ex, 0.5f, labels.map[Labels.LABEL_GAMEOVER], 0.25f, Labels.ALIGN_CC);
        labels.draw(gl, sx, -0.25f, ex, 0.1f, labels.map[Labels.LABEL_GAMEOVER_LOAD_AUTOSAVE], 0.25f, Labels.ALIGN_CC);
        labels.endDrawing(gl);
    }
}
