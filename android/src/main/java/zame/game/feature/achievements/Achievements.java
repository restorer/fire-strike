package zame.game.feature.achievements;

import zame.game.R;
import zame.game.engine.Engine;
import zame.game.engine.state.Profile;
import zame.game.engine.state.State;

public final class Achievements {
    public static final int STAT_MONSTERS_KILLED = 0;
    public static final int STAT_DOORS_OPENED = 1;
    public static final int STAT_SECRETS_FOUND = 2;
    public static final int STAT_P100_KILLS_ROW = 3;
    public static final int STAT_P100_SECRETS_ROW = 4;
    public static final int STAT_BARRELS_EXPLODED = 5;
    public static final int STAT_LAST = 6;

    private static final int MONSTERS_KILLED_5 = 0; // hands in blood
    private static final int MONSTERS_KILLED_50 = 1;
    private static final int MONSTERS_KILLED_500 = 2;
    private static final int MONSTERS_KILLED_2500 = 3;
    private static final int MONSTERS_KILLED_5000 = 4;
    private static final int DOORS_OPENED_5 = 5; // mastered doors
    private static final int DOORS_OPENED_10 = 6;
    private static final int DOORS_OPENED_25 = 7;
    private static final int DOORS_OPENED_50 = 8;
    private static final int DOORS_OPENED_75 = 9;
    private static final int SECRETS_FOUND_5 = 10; // mastered secrets
    private static final int SECRETS_FOUND_10 = 11;
    private static final int SECRETS_FOUND_15 = 12;
    private static final int SECRETS_FOUND_20 = 13;
    private static final int SECRETS_FOUND_30 = 14;
    private static final int P100_KILLS_ROW_5 = 15;
    private static final int P100_SECRETS_ROW_5 = 16;
    private static final int P100_KILLS_ROW_10 = 17;
    private static final int P100_SECRETS_ROW_10 = 18;
    private static final int BARRELS_EXPLODED_1 = 19;
    private static final int BARRELS_EXPLODED_5 = 20;
    private static final int BARRELS_EXPLODED_25 = 21;
    private static final int BARRELS_EXPLODED_50 = 22;
    private static final int BARRELS_EXPLODED_100 = 23;
    public static final int LAST = 24;

    public static final Achievement[] LIST = {
            new Achievement(
                    MONSTERS_KILLED_5,
                    STAT_MONSTERS_KILLED,
                    5,
                    R.string.achievements_t_monsters_killed_5,
                    R.string.achievements_d_monsters_killed_5,
                    false),
            new Achievement(
                    MONSTERS_KILLED_50,
                    STAT_MONSTERS_KILLED,
                    50,
                    R.string.achievements_t_monsters_killed_50,
                    R.string.achievements_d_monsters_killed_50,
                    false),
            new Achievement(
                    MONSTERS_KILLED_500,
                    STAT_MONSTERS_KILLED,
                    500,
                    R.string.achievements_t_monsters_killed_500,
                    R.string.achievements_d_monsters_killed_500,
                    false),
            new Achievement(
                    MONSTERS_KILLED_2500,
                    STAT_MONSTERS_KILLED,
                    2500,
                    R.string.achievements_t_monsters_killed_2500,
                    R.string.achievements_d_monsters_killed_2500,
                    false),
            new Achievement(
                    MONSTERS_KILLED_5000,
                    STAT_MONSTERS_KILLED,
                    5000,
                    R.string.achievements_t_monsters_killed_5000,
                    R.string.achievements_d_monsters_killed_5000,
                    false),
            new Achievement(
                    P100_KILLS_ROW_5,
                    STAT_P100_KILLS_ROW,
                    5,
                    R.string.achievements_t_p100_kills_row_5,
                    R.string.achievements_d_p100_kills_row_5,
                    false),
            new Achievement(
                    P100_KILLS_ROW_10,
                    STAT_P100_KILLS_ROW,
                    10,
                    R.string.achievements_t_p100_kills_row_10,
                    R.string.achievements_d_p100_kills_row_10,
                    false),

            new Achievement(
                    SECRETS_FOUND_5,
                    STAT_SECRETS_FOUND,
                    5,
                    R.string.achievements_t_secrets_found_5,
                    R.string.achievements_d_secrets_found_5,
                    true),
            new Achievement(
                    SECRETS_FOUND_10,
                    STAT_SECRETS_FOUND,
                    10,
                    R.string.achievements_t_secrets_found_10,
                    R.string.achievements_d_secrets_found_10,
                    true),
            new Achievement(
                    SECRETS_FOUND_15,
                    STAT_SECRETS_FOUND,
                    15,
                    R.string.achievements_t_secrets_found_15,
                    R.string.achievements_d_secrets_found_15,
                    true),
            new Achievement(
                    SECRETS_FOUND_20,
                    STAT_SECRETS_FOUND,
                    20,
                    R.string.achievements_t_secrets_found_20,
                    R.string.achievements_d_secrets_found_20,
                    true),
            new Achievement(
                    SECRETS_FOUND_30,
                    STAT_SECRETS_FOUND,
                    30,
                    R.string.achievements_t_secrets_found_30,
                    R.string.achievements_d_secrets_found_30,
                    true),
            new Achievement(
                    P100_SECRETS_ROW_5,
                    STAT_P100_SECRETS_ROW,
                    5,
                    R.string.achievements_t_p100_secrets_row_5,
                    R.string.achievements_d_p100_secrets_row_5,
                    true),
            new Achievement(
                    P100_SECRETS_ROW_10,
                    STAT_P100_SECRETS_ROW,
                    10,
                    R.string.achievements_t_p100_secrets_row_10,
                    R.string.achievements_d_p100_secrets_row_10,
                    true),

            new Achievement(
                    BARRELS_EXPLODED_1,
                    STAT_BARRELS_EXPLODED,
                    1,
                    R.string.achievements_t_barrels_exploded_1,
                    R.string.achievements_d_barrels_exploded_1,
                    false),
            new Achievement(
                    BARRELS_EXPLODED_5,
                    STAT_BARRELS_EXPLODED,
                    5,
                    R.string.achievements_t_barrels_exploded_5,
                    R.string.achievements_d_barrels_exploded_5,
                    false),
            new Achievement(
                    BARRELS_EXPLODED_25,
                    STAT_BARRELS_EXPLODED,
                    25,
                    R.string.achievements_t_barrels_exploded_25,
                    R.string.achievements_d_barrels_exploded_25,
                    false),
            new Achievement(
                    BARRELS_EXPLODED_50,
                    STAT_BARRELS_EXPLODED,
                    50,
                    R.string.achievements_t_barrels_exploded_50,
                    R.string.achievements_d_barrels_exploded_50,
                    false),
            new Achievement(
                    BARRELS_EXPLODED_100,
                    STAT_BARRELS_EXPLODED,
                    100,
                    R.string.achievements_t_barrels_exploded_100,
                    R.string.achievements_d_barrels_exploded_100,
                    false),

            new Achievement(
                    DOORS_OPENED_5,
                    STAT_DOORS_OPENED,
                    5,
                    R.string.achievements_t_doors_opened_5,
                    R.string.achievements_d_doors_opened_5,
                    true),
            new Achievement(
                    DOORS_OPENED_10,
                    STAT_DOORS_OPENED,
                    10,
                    R.string.achievements_t_doors_opened_10,
                    R.string.achievements_d_doors_opened_10,
                    true),
            new Achievement(
                    DOORS_OPENED_25,
                    STAT_DOORS_OPENED,
                    25,
                    R.string.achievements_t_doors_opened_25,
                    R.string.achievements_d_doors_opened_25,
                    true),
            new Achievement(
                    DOORS_OPENED_50,
                    STAT_DOORS_OPENED,
                    50,
                    R.string.achievements_t_doors_opened_50,
                    R.string.achievements_d_doors_opened_50,
                    true),
            new Achievement(
                    DOORS_OPENED_75,
                    STAT_DOORS_OPENED,
                    75,
                    R.string.achievements_t_doors_opened_75,
                    R.string.achievements_d_doors_opened_75,
                    true),
    };

    private Achievements() {}

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public static void resetStat(int statId, Profile profile, Engine engine, State state) {
        if (!engine.inWallpaperMode) {
            state.stats[statId] = 0;
        }
    }

    public static void updateStat(int statId, Profile profile, Engine engine, State state) {
        if (engine.inWallpaperMode) {
            return;
        }

        state.stats[statId]++;

        for (Achievement achievement : LIST) {
            if (achievement.statId == statId) {
                achievement.update(profile, engine, state);
            }
        }

        profile.update(engine.activity);
    }

    public static String cleanupTitle(String achievementTitle) {
        return achievementTitle.replaceAll("<font.+?font>", "").trim();
    }
}
