package zame.game.feature.sound;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import zame.game.App;

public abstract class SoundManager {
    public static class PlayList {
        public String[] list;
        int idx;

        PlayList(@NonNull String[] list) {
            this.list = list;
        }
    }

    public static final int FOCUS_MASK_MAIN_ACTIVITY = 1;
    public static final int FOCUS_MASK_GAME_CODE_DIALOG = 2;
    public static final int FOCUS_MASK_GAME_MENU_DIALOG = 4;
    public static final int FOCUS_MASK_RESTART_WARN_DIALOG = 8;
    public static final int FOCUS_MASK_CONSENT_FOR_ADS_CHANGED_DIALOG = 16;
    public static final int FOCUS_MASK_QUIT_WARN_DIALOG = 32;
    public static final int FOCUS_MASK_RATE_GAME_DIALOG = 64;

    public static final PlayList LIST_MAIN = new PlayList(new String[] { "bensound-high-octane.mp3",
            "incompetech-motherlode.mp3",
            "incompetech-ready-aim-fire.mp3",
            "incompetech-jet-fueled-vixen.mp3" });

    public static final PlayList LIST_ENDL = new PlayList(new String[] { "incompetech-exhilarate.mp3" });
    public static final PlayList LIST_GAMEOVER = new PlayList(new String[] { "dj2puredigital-intro-nice-flyer.mp3" });

    public static final int SOUND_BTN_PRESS = 0;

    public static final int SOUND_LEVEL_START = 1;
    public static final int SOUND_LEVEL_END = 2;
    public static final int SOUND_DOOR_OPEN = 3;
    public static final int SOUND_DOOR_CLOSE = 4;
    public static final int SOUND_BOOM = 5;

    public static final int SOUND_NO_WAY = 6;
    public static final int SOUND_SWITCH = 7;
    public static final int SOUND_MARK = 8;
    public static final int SOUND_PICK_ITEM = 9;
    public static final int SOUND_PICK_AMMO = 10;
    public static final int SOUND_PICK_WEAPON = 11;
    public static final int SOUND_ACHIEVEMENT_UNLOCKED = 12;
    public static final int SOUND_DEATH_HERO = 13;

    public static final int SOUND_SHOOT_KNIFE = 14;
    public static final int SOUND_SHOOT_PISTOL = 15;
    public static final int SOUND_SHOOT_DBLPISTOL = 16;
    public static final int SOUND_SHOOT_AK47 = 17;
    public static final int SOUND_SHOOT_TMP = 18;
    public static final int SOUND_SHOOT_WINCHESTER = 19;
    public static final int SOUND_SHOOT_GRENADE = 20;

    static final int SOUND_MON_1_READY = 21;
    private static final int SOUND_MON_1_ATTACK = SOUND_SHOOT_PISTOL;
    static final int SOUND_MON_1_DEATH = 22;

    static final int SOUND_MON_2_READY = 23;
    private static final int SOUND_MON_2_ATTACK = SOUND_SHOOT_AK47;
    static final int SOUND_MON_2_DEATH = 24;

    static final int SOUND_MON_3_READY = 25;
    private static final int SOUND_MON_3_ATTACK = SOUND_SHOOT_KNIFE;
    static final int SOUND_MON_3_DEATH = 26;

    static final int SOUND_MON_4_READY = 27;
    private static final int SOUND_MON_4_ATTACK = SOUND_SHOOT_GRENADE;
    static final int SOUND_MON_4_DEATH = 28;

    static final int SOUND_MON_5_READY = 29;
    private static final int SOUND_MON_5_ATTACK = SOUND_SHOOT_TMP;
    static final int SOUND_MON_5_DEATH = 30;

    static final int SOUND_LAST = 31;

    public static final int[] SOUNDLIST_ATTACK_MON = { SOUND_MON_1_ATTACK,
            SOUND_MON_2_ATTACK,
            SOUND_MON_3_ATTACK,
            SOUND_MON_4_ATTACK,
            SOUND_MON_5_ATTACK };

    public static final int[] SOUNDLIST_DEATH_MON = { SOUND_MON_1_DEATH,
            SOUND_MON_2_DEATH,
            SOUND_MON_3_DEATH,
            SOUND_MON_4_DEATH,
            SOUND_MON_5_DEATH };

    public static final int[] SOUNDLIST_READY_MON = { SOUND_MON_1_READY,
            SOUND_MON_2_READY,
            SOUND_MON_3_READY,
            SOUND_MON_4_READY,
            SOUND_MON_5_READY };

    public static SoundManager getInstance(boolean isWallpaper) {
        if (isWallpaper) {
            return new SoundManagerDummy();
        }

        if (App.self.soundManagerInstance == null) {
            App.self.soundManagerInstance = new SoundManagerInst();
        }

        return App.self.soundManagerInstance;
    }

    public boolean instantPause = true;

    public void playSound(int idx) {
        playSound(idx, 1.0f);
    }

    public abstract void setSoundEnabledSetting(boolean enabled);

    public abstract void setMusicVolumeSetting(int volume);

    public abstract void setEffectsVolumeSetting(int volume);

    // public abstract void onSettingsUpdated();

    public abstract void playSound(int idx, float volume);

    public void setPlaylist(@Nullable PlayList playlist) {
        setPlaylist(playlist, true);
    }

    public abstract void setPlaylist(@Nullable PlayList playlist, boolean shouldMoveToNextInPrevPlaylist);

    public abstract void onWindowFocusChanged(boolean hasFocus, int focusMask);

    public abstract void initialize();

    public abstract void shutdown();
}
