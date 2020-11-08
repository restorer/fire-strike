package zame.game.feature.sound;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.SystemClock;

import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import zame.game.App;
import zame.game.R;
import zame.game.core.manager.PreferencesManager;
import zame.game.core.util.Common;

// https://stackoverflow.com/questions/7437505/how-to-properly-use-soundpool-on-a-game
// https://stackoverflow.com/questions/10181822/android-soundpool-play-sometimes-lags

public class SoundManagerInst extends SoundManager {
    public static class SoundThread extends Thread {
        public static class Item {
            int idx;
            float volume;
            float effectsVolume;

            public Item(int idx, float volume, float effectsVolume) {
                this.idx = idx;
                this.volume = volume;
                this.effectsVolume = effectsVolume;
            }
        }

        static final int MAX_PLAYING_SOUNDS = 64;

        AssetManager assetManager;
        SoundPool soundPool = new SoundPool(MAX_PLAYING_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        int[] soundIds = new int[SOUND_LAST];

        BlockingQueue<Item> queue = new LinkedBlockingQueue<>(MAX_PLAYING_SOUNDS);
        volatile boolean isActive = true;

        SoundThread(AssetManager assetManager) {
            super();
            this.assetManager = assetManager;

            loadSound("diforb_btn_press", SOUND_BTN_PRESS);
            loadSound("diforb_level_start", SOUND_LEVEL_START);
            loadSound("diforb_level_end", SOUND_LEVEL_END);
            loadSound("diforb_door_open", SOUND_DOOR_OPEN);
            loadSound("diforb_door_close", SOUND_DOOR_CLOSE);
            loadSound("diforb_boom", SOUND_BOOM);
            loadSound("custom_no_way", SOUND_NO_WAY);
            loadSound("diforb_switch", SOUND_SWITCH);
            loadSound("diforb_mark", SOUND_MARK);
            loadSound("diforb_pick_item", SOUND_PICK_ITEM);
            loadSound("freesfx_pick_ammo", SOUND_PICK_AMMO);
            loadSound("freesfx_pick_weapon", SOUND_PICK_WEAPON);
            loadSound("diforb_achievement_unlocked", SOUND_ACHIEVEMENT_UNLOCKED);
            loadSound("freesfx_death_hero", SOUND_DEATH_HERO);
            loadSound("diforb_shoot_knife", SOUND_SHOOT_KNIFE);
            loadSound("freesfx_shoot_pistol", SOUND_SHOOT_PISTOL);
            loadSound("freesfx_shoot_dblpistol", SOUND_SHOOT_DBLPISTOL);
            loadSound("freesfx_shoot_ak47", SOUND_SHOOT_AK47);
            loadSound("freesfx_shoot_tmp", SOUND_SHOOT_TMP);
            loadSound("freesfx_shoot_winchester", SOUND_SHOOT_WINCHESTER);
            loadSound("diforb_shoot_grenade", SOUND_SHOOT_GRENADE);
            loadSound("diforb_mon_1_ready", SOUND_MON_1_READY);
            loadSound("freesfx_mon_1_death", SOUND_MON_1_DEATH);
            loadSound("diforb_mon_2_ready", SOUND_MON_2_READY);
            loadSound("freesfx_mon_2_death", SOUND_MON_2_DEATH);
            loadSound("diforb_mon_3_ready", SOUND_MON_3_READY);
            loadSound("freesfx_mon_3_death", SOUND_MON_3_DEATH);
            loadSound("diforb_mon_4_ready", SOUND_MON_4_READY);
            loadSound("freesfx_mon_4_death", SOUND_MON_4_DEATH);
            loadSound("diforb_mon_5_ready", SOUND_MON_5_READY);
            loadSound("custom_mon_5_death", SOUND_MON_5_DEATH);
        }

        void loadSound(String name, int idx) {
            try {
                AssetFileDescriptor afd = assetManager.openFd("sounds/" + name + ".mp3");
                int soundId = soundPool.load(afd, 1);
                afd.close();

                soundIds[idx] = soundId;
            } catch (Exception ex) {
                Common.log(ex);
                soundIds[idx] = -1;
            }
        }

        @Override
        public void run() {
            try {
                Item item;

                while (isActive) {
                    item = queue.poll(10, TimeUnit.SECONDS);

                    if (item != null && item.idx >= 0 && item.idx < SOUND_LAST && soundIds[item.idx] >= 0) {
                        float actVolume = item.effectsVolume * item.volume;
                        soundPool.play(soundIds[item.idx], actVolume, actVolume, 0, 0, 1.0f);
                    }
                }
            } catch (InterruptedException ex) {
                // ignored
            }
        }
    }

    public class PauseTask extends TimerTask {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }

            pauseTask = null;
        }
    }

    private static final float VOLUME_THRESHOLD = 0.01f;

    private final AssetManager assetManager;
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private volatile SoundThread soundThread;

    private PlayList current;
    private boolean musicLoaded;
    private final Timer pauseTimer = new Timer();
    private TimerTask pauseTask;

    private boolean soundEnabled;
    private float musicVolume = 1.0f;
    private float effectsVolume = 1.0f;
    private int inFocusMask;
    private final long[] soundLastPlayTime = new long[SOUND_LAST];

    SoundManagerInst() {
        super();
        assetManager = App.self.getAssets();

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Common.log("MediaPlayer error: what=" + what + ", extra=" + extra);
                return false;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (current != null) {
                    current.idx = (current.idx + 1) % current.list.length;
                }

                play(true);
            }
        });
    }

    private SoundThread getSoundThread() {
        @SuppressWarnings("UnnecessaryLocalVariable") AssetManager assetManagerCached = assetManager;

        synchronized (this) {
            if (soundThread == null) {
                soundThread = new SoundThread(assetManagerCached);
                soundThread.start();
            }
        }

        return soundThread;
    }

    private void updateVolume() {
        PreferencesManager preferences = App.self.preferences;

        //noinspection CallToSimpleSetterFromWithinClass
        setSoundEnabledSetting(preferences.getBoolean(R.string.key_enable_sound));

        setMusicVolumeSetting(preferences.getInt(R.string.key_music_volume, 10));
        setEffectsVolumeSetting(preferences.getInt(R.string.key_effects_volume, 5));
    }

    private void play(boolean wasPlaying) {
        mediaPlayer.reset();
        musicLoaded = false;

        if (current != null) {
            int oldIdx = current.idx;

            for (; ; ) {
                String name = current.list[current.idx];

                try {
                    if (name.length() > 4 && "dlc_".equals(name.substring(0, 4))) {
                        File file = new File(App.self.internalRoot + name);

                        if (file.exists()) {
                            FileInputStream is = new FileInputStream(file);

                            try {
                                mediaPlayer.setDataSource(is.getFD());
                                mediaPlayer.prepare();
                                musicLoaded = true;
                            } catch (Exception ex) {
                                Common.log(ex);
                            }

                            is.close();
                        }
                    } else {
                        AssetFileDescriptor afd = assetManager.openFd("music/" + name);

                        try {
                            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                            mediaPlayer.prepare();
                            musicLoaded = true;
                        } catch (Exception ex) {
                            Common.log(ex);
                        }

                        afd.close();
                    }
                } catch (Exception ex) {
                    Common.log(ex);
                }

                if (musicLoaded) {
                    break;
                }

                current.idx = (current.idx + 1) % current.list.length;

                if (current.idx == oldIdx) {
                    break;
                }
            }
        }

        if (pauseTask != null) {
            pauseTask.cancel();
            pauseTimer.purge();
            pauseTask = null;
        } else if (musicLoaded && wasPlaying && (musicVolume > VOLUME_THRESHOLD)) {
            mediaPlayer.setVolume(musicVolume, musicVolume);
            mediaPlayer.start();
        }
    }

    @Override
    public void setSoundEnabledSetting(boolean enabled) {
        soundEnabled = enabled;
    }

    @Override
    public void setMusicVolumeSetting(int volume) {
        //noinspection MagicNumber
        musicVolume = (float)volume / 10.0f; // 0 .. 1.0
    }

    @Override
    public void setEffectsVolumeSetting(int volume) {
        //noinspection MagicNumber
        effectsVolume = (float)volume / 10.0f; // 0 .. 1.0
    }

    // @Override
    // public void onSettingsUpdated() {
    //     if (soundEnabled && musicLoaded && (musicVolume > VOLUME_THRESHOLD)) {
    //         mediaPlayer.setVolume(musicVolume, musicVolume);
    //         mediaPlayer.startBatch();
    //     } else if (mediaPlayer.isPlaying()) {
    //         mediaPlayer.pause();
    //     }
    // }

    private static final long SOUND_DEBOUNCE_THRESHOLD = 50L; // Для EndLevel нужно не более 90

    @Override
    public void playSound(int idx, float volume) {
        if (!soundEnabled || (effectsVolume <= VOLUME_THRESHOLD) || (volume <= VOLUME_THRESHOLD)) {
            return;
        }

        long currentTime = SystemClock.elapsedRealtime();

        if (soundLastPlayTime[idx] + SOUND_DEBOUNCE_THRESHOLD > currentTime) {
            return;
        }

        soundLastPlayTime[idx] = currentTime;
        getSoundThread().queue.offer(new SoundThread.Item(idx, volume, effectsVolume));
    }

    @Override
    public void setPlaylist(PlayList playlist, boolean shouldMoveToNextInPrevPlaylist) {
        updateVolume();

        if (current != playlist) {
            PlayList prev = current;
            boolean isPlaying = mediaPlayer.isPlaying();

            current = playlist;
            play(isPlaying || (soundEnabled && prev == null));

            if (shouldMoveToNextInPrevPlaylist && prev != null && isPlaying) {
                prev.idx = (prev.idx + 1) % prev.list.length;
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus, int focusMask) {
        // Common.log("hasFocus=" + String.valueOf(hasFocus) + ", focusMask=" + String.valueOf(focusMask) + ", inFocusMask=" + String.valueOf(inFocusMask) + ", instantPause=" + String.valueOf(instantPause));

        if (hasFocus) {
            inFocusMask |= focusMask;
            instantPause = true;
            updateVolume();

            if (pauseTask != null) {
                pauseTask.cancel();
                pauseTimer.purge();
                pauseTask = null;
            }

            if (soundEnabled && musicLoaded && (musicVolume > VOLUME_THRESHOLD)) {
                mediaPlayer.setVolume(musicVolume, musicVolume);
                mediaPlayer.start();
            } else if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } else {
            inFocusMask &= ~focusMask;

            if (inFocusMask == 0) {
                if (mediaPlayer.isPlaying()) {
                    if (instantPause) {
                        if (pauseTask != null) {
                            pauseTask.cancel();
                            pauseTimer.purge();
                            pauseTask = null;
                        }

                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                        }
                    } else if (pauseTask == null) {
                        pauseTask = new PauseTask();

                        //noinspection MagicNumber
                        pauseTimer.schedule(pauseTask, 2000);
                    }
                }
            }

            instantPause = true;
        }
    }

    @Override
    public void initialize() {
        getSoundThread(); // pre-load sounds
    }

    @Override
    public synchronized void shutdown() {
        if (soundThread != null && soundThread.isActive) {
            soundThread.isActive = false;
            soundThread = null;
        }
    }
}
