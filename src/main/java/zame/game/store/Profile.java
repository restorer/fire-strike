package zame.game.store;

import android.support.v4.util.SparseArrayCompat;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import zame.game.App;
import zame.game.Common;
import zame.game.R;
import zame.game.engine.BaseState;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class Profile extends BaseState {
    // public static final String BROADCAST_ACTION_UPDATED = "local:Profile.updated";

    private static final int FIELD_BUILD = 1;
    private static final int FIELD_EXP = 5;
    private static final int FIELD_ACHIEVED = 7;
    private static final int FIELD_ALREADY_COMPLETED_LEVELS = 10;

    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized") public boolean autoSaveOnUpdate = true;
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized") public boolean isUnsavedUpdates;

    public int exp;
    public boolean[] achieved = new boolean[Achievements.LAST];
    public HashSet<String> alreadyCompletedLevels = new HashSet<>();

    public ProfileLevel[] levels = { new ProfileLevel("e00m00", -1, R.drawable.char_commander),

            new ProfileLevel("e01m01", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m02", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m03", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m04", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m05", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m06", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m07", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m08", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m09", 1, R.drawable.char_soldier),
            new ProfileLevel("e01m10", 1, R.drawable.char_soldier),

            new ProfileLevel("e02m01", 2, R.drawable.char_commander),
            new ProfileLevel("e02m02", 2, R.drawable.char_commander),
            new ProfileLevel("e02m03", 2, R.drawable.char_commander),
            new ProfileLevel("e02m04", 2, R.drawable.char_commander),
            new ProfileLevel("e02m05", 2, R.drawable.char_commander),
            new ProfileLevel("e02m06", 2, R.drawable.char_commander),
            new ProfileLevel("e02m07", 2, R.drawable.char_commander),
            new ProfileLevel("e02m08", 2, R.drawable.char_commander),
            new ProfileLevel("e02m09", 2, R.drawable.char_commander),
            new ProfileLevel("e02m10", 2, R.drawable.char_commander),

            new ProfileLevel("e03m01", 3, R.drawable.char_doctor),
            new ProfileLevel("e03m02", 3, R.drawable.char_doctor),
            new ProfileLevel("e03m03", 3, R.drawable.char_doctor),
            new ProfileLevel("e03m04", 3, R.drawable.char_doctor),
            new ProfileLevel("e03m05", 3, R.drawable.char_doctor),

            new ProfileLevel("e99m99", -2, R.drawable.char_doctor),

            new ProfileLevel("e77m01", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m02", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m03", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m04", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m05", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m06", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m07", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m08", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m09", -3, R.drawable.char_soldier),
            new ProfileLevel("e77m10", -3, R.drawable.char_soldier), };

    private HashMap<String, ProfileLevel> levelsMap = new HashMap<>();
    private ProfileLevel dummyLevel = new ProfileLevel("", -1, 0);
    @SuppressWarnings("FieldCanBeLocal") private boolean wasChangedOnVersionUpgrade;

    public Profile() {
        super();
        SparseArrayCompat<Integer> episodeIndices = new SparseArrayCompat<>();

        for (int i = 0, len = levels.length; i < len; i++) {
            int episodeIndex = (episodeIndices.indexOfKey(levels[i].episode) >= 0)
                    ? episodeIndices.get(levels[i].episode)
                    : 0;

            episodeIndices.put(levels[i].episode, episodeIndex + 1);
            levels[i].update(episodeIndex, (i < (len - 1) ? levels[i + 1] : null));
            levelsMap.put(levels[i].name, levels[i]);
        }

        for (ProfileLevel level : levels) {
            level.episodeLevelsCount = episodeIndices.get(level.episode);
        }

        dummyLevel.update(0, null);
    }

    public ProfileLevel getLevel(String name) {
        ProfileLevel level = levelsMap.get(name);
        return (level == null ? dummyLevel : level);
    }

    public void clear() {
        autoSaveOnUpdate = false;
        isUnsavedUpdates = false;

        exp = 0;

        for (int i = 0, len = achieved.length; i < len; i++) {
            achieved[i] = false;
        }

        alreadyCompletedLevels.clear();
        update();
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

    public synchronized void update() {
        update(true);
    }

    public synchronized void update(boolean changed) {
        // Additional profile updates may be here

        if (changed) {
            if (autoSaveOnUpdate) {
                save();
            } else {
                isUnsavedUpdates = true;
            }
        }

        // MyApplication.self.getLocalBroadcastManager().sendBroadcast(new Intent(BROADCAST_ACTION_UPDATED));
    }

    @Override
    protected int getVersion() {
        return 5;
    }

    @SuppressWarnings("RedundantMethodOverride")
    @Override
    protected void versionUpgrade(int version) {
    }

    public void load() {
        load(true);
    }

    public void load(boolean showErrorMessage) {
        if (isUnsavedUpdates) {
            save();
        }

        wasChangedOnVersionUpgrade = false;

        if (load(App.self.internalRoot + "profile.data") != LOAD_RESULT_SUCCESS && showErrorMessage) {
            Common.showToast(R.string.msg_cant_load_profile);
        }

        update(wasChangedOnVersionUpgrade);
    }

    public void save() {
        autoSaveOnUpdate = true;
        isUnsavedUpdates = false;

        if (!save(App.self.internalRoot + "profile.data")) {
            Common.showToast(R.string.msg_cant_save_profile);
        }
    }
}
