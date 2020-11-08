package zame.game.engine.state;

import android.content.Context;

import androidx.collection.SparseArrayCompat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import zame.game.App;
import zame.game.R;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;
import zame.game.core.util.Common;
import zame.game.feature.achievements.Achievements;
import zame.game.flavour.config.ProfileConfig;

public class Profile extends BaseState {
    // public static final String BROADCAST_ACTION_UPDATED = "local:Profile.updated";

    private static final String FILE_NAME = "profile.data";

    private static final int FIELD_BUILD = 1;
    private static final int FIELD_EXP = 2;
    private static final int FIELD_ACHIEVED = 3;
    private static final int FIELD_ALREADY_COMPLETED_LEVELS = 4;

    public boolean autoSaveOnUpdate = true;
    public boolean isUnsavedUpdates;

    public int exp;
    public boolean[] achieved = new boolean[Achievements.LAST];
    public HashSet<String> alreadyCompletedLevels = new HashSet<>();

    public ProfileLevel[] levels = ProfileConfig.createLevelsList();
    private final HashMap<String, ProfileLevel> levelsMap = new HashMap<>();
    private final ProfileLevel dummyLevel = new ProfileLevel("", -1, 0, ProfileLevel.AD_NONE);
    @SuppressWarnings("FieldCanBeLocal") private boolean wasChangedOnVersionUpgrade;

    @SuppressWarnings("ConstantConditions")
    public Profile() {
        super();
        SparseArrayCompat<Integer> episodeIndices = new SparseArrayCompat<>();

        for (int i = 0, len = levels.length; i < len; i++) {
            int episodeIndex = (episodeIndices.indexOfKey(levels[i].episode) >= 0)
                    ? episodeIndices.get(levels[i].episode)
                    : 0;

            episodeIndices.put(levels[i].episode, episodeIndex + 1);

            levels[i].update(
                    episodeIndex,
                    (i > 0 ? levels[i - 1] : null),
                    (i < (len - 1) ? levels[i + 1] : null));

            levelsMap.put(levels[i].name, levels[i]);
        }

        for (ProfileLevel level : levels) {
            level.episodeLevelsCount = episodeIndices.get(level.episode);
        }

        dummyLevel.update(0, null, null);
    }

    public ProfileLevel getLevel(String name) {
        ProfileLevel level = levelsMap.get(name);
        return (level == null ? dummyLevel : level);
    }

    public void clear(Context context) {
        autoSaveOnUpdate = false;
        isUnsavedUpdates = false;

        exp = 0;

        for (int i = 0, len = achieved.length; i < len; i++) {
            achieved[i] = false;
        }

        alreadyCompletedLevels.clear();
        update(context);
    }

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_BUILD, App.self.getVersionName());
        writer.write(FIELD_EXP, exp);
        writer.write(FIELD_ACHIEVED, achieved);
        writer.write(FIELD_ALREADY_COMPLETED_LEVELS, alreadyCompletedLevels.toArray(new String[0]));
    }

    @Override
    public void readFrom(DataReader reader) {
        exp = reader.readInt(FIELD_EXP);
        achieved = reader.readBooleanArray(FIELD_ACHIEVED, Achievements.LAST);

        alreadyCompletedLevels.clear();

        alreadyCompletedLevels.addAll(Arrays.asList((String[])Common.defaultize(reader.readStringArray(
                FIELD_ALREADY_COMPLETED_LEVELS), new String[0])));
    }

    public void update(Context context) {
        update(context, true);
    }

    public void update(Context context, boolean changed) {
        // Additional profile updates may be here

        if (changed) {
            if (autoSaveOnUpdate) {
                save(context);
            } else {
                isUnsavedUpdates = true;
            }
        }

        // App.self.broadcastManager().sendBroadcast(BROADCAST_ACTION_UPDATED);
    }

    @SuppressWarnings("RedundantMethodOverride")
    @Override
    protected int getVersion() {
        return 1;
    }

    @SuppressWarnings("RedundantMethodOverride")
    @Override
    protected void versionUpgrade(int version) {
        // wasChangedOnVersionUpgrade = true;
    }

    public void loadInitial(Context context) {
        if (new File(App.self.internalRoot + FILE_NAME).exists()) {
            loadInternal(context);
        } else {
            update(context, false);
            save(context);
        }
    }

    private void loadInternal(Context context) {
        if (isUnsavedUpdates) {
            save(context);
        }

        wasChangedOnVersionUpgrade = false;

        if (load(App.self.internalRoot + FILE_NAME) != LOAD_RESULT_SUCCESS) {
            Common.showToast(context, R.string.engine_profile_cant_load);
        }

        update(context, wasChangedOnVersionUpgrade);
    }

    public void save(Context context) {
        autoSaveOnUpdate = true;
        isUnsavedUpdates = false;

        if (!save(App.self.internalRoot + FILE_NAME)) {
            Common.showToast(context, R.string.engine_profile_cant_save);
        }
    }
}
