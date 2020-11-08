package zame.game.feature.achievements;

import java.util.Locale;

import zame.game.engine.Engine;
import zame.game.engine.state.Profile;
import zame.game.engine.state.State;
import zame.game.feature.sound.SoundManager;

public class Achievement {
    public int id;
    int statId;
    public int titleResourceId;
    int descriptionResourceId;
    boolean isAltBackground;

    private final int maxValue;

    Achievement(
            int id,
            int statId,
            int maxValue,
            int titleResourceId,
            int descriptionResourceId,
            boolean isAltBackground) {

        this.id = id;
        this.statId = statId;
        this.maxValue = maxValue;
        this.titleResourceId = titleResourceId;
        this.descriptionResourceId = descriptionResourceId;
        this.isAltBackground = isAltBackground;
    }

    public void update(Profile profile, Engine engine, State state) {
        if (engine.inWallpaperMode) {
            return;
        }

        if (!profile.achieved[id] && state.stats[statId] >= maxValue) {
            profile.achieved[id] = true;
            profile.update(engine.activity);

            engine.overlay.showAchievement(titleResourceId);
            engine.soundManager.playSound(SoundManager.SOUND_ACHIEVEMENT_UNLOCKED);
        }
    }

    boolean isAchieved(Profile profile) {
        return profile.achieved[id];
    }

    @SuppressWarnings({ "unused", "RedundantSuppression", "ManualMinMaxCalculation" })
    String getStatusText(Profile profile, State state) {
        return String.format(Locale.US, "%d/%d", (state.stats[statId] < 0 ? 0 : state.stats[statId]), maxValue);
    }
}
