package zame.game.engine.visual;

import zame.game.App;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.Renderer;
import zame.game.feature.config.EventsConfig;
import zame.game.feature.sound.SoundManager;

public class GameOver implements EngineObject {
    private Engine engine;
    private Renderer renderer;
    private Labels labels;
    private SoundManager soundManager;
    private Game game;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
        this.labels = engine.labels;
        this.soundManager = engine.soundManager;
        this.game = engine.game;
    }

    public void update() {
        if (game.actionRestartButton) {
            App.self.tracker.trackEvent(EventsConfig.EV_GAME_GAME_OVER_RESTART, engine.state.levelName);
            soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
            soundManager.setPlaylist(SoundManager.LIST_MAIN);
            game.loadLevel(Game.LOAD_LEVEL_NORMAL); // analytics depends on "actionRestartButton"
        }

        if (game.actionContinueButton) {
            App.self.tracker.trackEvent(EventsConfig.EV_GAME_GAME_OVER_CONTINUE_REWARDED, engine.state.levelName);
            soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
            soundManager.setPlaylist(SoundManager.LIST_MAIN);
            engine.state.mustLoadAutosave = false;
            engine.changeView(Engine.VIEW_TYPE_REWARDED_VIDEO);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void render() {
        labels.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);

        float sx = -engine.ratio + 0.1f;
        float ex = engine.ratio - 0.1f;

        labels.batch(sx, 0.5f, ex, 0.8f, labels.map[Labels.LABEL_GAMEOVER], 0.25f, Labels.ALIGN_CC);

        int labelIndex = engine.canShowRewardedVideo ? (
                engine.config.leftHandAim
                        ? Labels.LABEL_GAMEOVER_SUBTITLE_LEFT_HAND_AIM
                        : Labels.LABEL_GAMEOVER_SUBTITLE)
                : Labels.LABEL_GAMEOVER_SUBTITLE_JUST_RESTART;

        labels.batch(sx, 0.2f, ex, 0.5f, labels.map[labelIndex], 0.25f, Labels.ALIGN_CC);
        labels.renderBatch();
    }
}
