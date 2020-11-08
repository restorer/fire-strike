package zame.game.engine.visual;

import zame.game.core.util.Common;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.Renderer;
import zame.game.feature.sound.SoundManager;

public class EndLevel implements EngineObject {
    private static final float LINE_HEIGHT = 0.25f;
    private static final float LINE_OFFSET = LINE_HEIGHT / 2.0f;

    private Engine engine;
    private Renderer renderer;
    private Labels labels;
    private SoundManager soundManager;
    private Game game;

    private int totalKills;
    private int totalSecrets;
    private int totalSeconds;
    private float currentKills;
    private float currentSecrets;
    private float currentSeconds;
    private float currentAdd;
    private int timeout;
    private float startY;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
        this.labels = engine.labels;
        this.soundManager = engine.soundManager;
        this.game = engine.game;
    }

    public void init(int totalKills, int totalSecrets, int totalSeconds) {
        this.totalKills = totalKills;
        this.totalSecrets = totalSecrets;
        this.totalSeconds = totalSeconds;

        currentKills = 0.0f;
        currentSecrets = 0.0f;
        currentSeconds = 0.0f;

        currentAdd = 1.0f;
        timeout = 0;
        startY = 0.0f;

        if (totalKills >= 0) {
            startY -= LINE_OFFSET;
        }

        if (totalSecrets >= 0) {
            startY -= LINE_OFFSET;
        }
    }

    public void update() {
        if (game.actionFire != 0) {
            soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
            game.loadLevel(Game.LOAD_LEVEL_RELOAD);
            return;
        }

        if (timeout > 0) {
            timeout--;
            return;
        }

        timeout = 3;
        boolean playSound = false;

        if (currentKills < totalKills) {
            currentKills = Math.min(totalKills, currentKills + currentAdd);
            playSound = true;
        }

        if (currentSecrets < totalSecrets) {
            currentSecrets = Math.min(totalSecrets, currentSecrets + currentAdd);
            playSound = true;
        }

        if (currentSeconds < totalSeconds) {
            currentSeconds = Math.min(totalSeconds, currentSeconds + currentAdd);
            playSound = true;
        }

        //noinspection MagicNumber
        currentAdd += 0.2f;

        if (playSound) {
            soundManager.playSound(SoundManager.SOUND_SHOOT_DBLPISTOL);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void render() {
        labels.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);

        float sx = -engine.ratio + 0.1f;
        float ex = engine.ratio - 0.1f;
        float my = startY;

        labels.batch(
                sx,
                my - LINE_OFFSET,
                ex,
                my + LINE_OFFSET,
                String.format(labels.map[Labels.LABEL_ENDL_TIME], Common.getTimeString((int)currentSeconds)),
                0.225f,
                Labels.ALIGN_CC);

        my += LINE_HEIGHT;

        if (totalSecrets >= 0) {
            labels.batch(
                    sx,
                    my - LINE_OFFSET,
                    ex,
                    my + LINE_OFFSET,
                    String.format(labels.map[Labels.LABEL_ENDL_SECRETS], (int)currentSecrets),
                    0.225f,
                    Labels.ALIGN_CC);

            my += LINE_HEIGHT;
        }

        if (totalKills >= 0) {
            labels.batch(
                    sx,
                    my - LINE_OFFSET,
                    ex,
                    my + LINE_OFFSET,
                    String.format(labels.map[Labels.LABEL_ENDL_KILLS], (int)currentKills),
                    0.225f,
                    Labels.ALIGN_CC);
        }

        labels.renderBatch();
    }
}
