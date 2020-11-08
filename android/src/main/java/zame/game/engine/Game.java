package zame.game.engine;

import android.text.TextUtils;

import zame.game.App;
import zame.game.R;
import zame.game.core.util.Common;
import zame.game.engine.controller.HeroController;
import zame.game.engine.entity.Bullet;
import zame.game.engine.entity.Door;
import zame.game.engine.entity.Explosion;
import zame.game.engine.entity.LookPoint;
import zame.game.engine.entity.Mark;
import zame.game.engine.entity.Monster;
import zame.game.engine.entity.ObjectContainer;
import zame.game.engine.entity.Timeout;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.level.Level;
import zame.game.engine.level.LevelRenderer;
import zame.game.engine.state.Profile;
import zame.game.engine.state.State;
import zame.game.engine.util.GameMath;
import zame.game.engine.visual.AutoMap;
import zame.game.engine.visual.EndLevel;
import zame.game.engine.visual.GameOver;
import zame.game.engine.visual.Overlay;
import zame.game.engine.visual.Weapons;
import zame.game.feature.achievements.Achievements;
import zame.game.feature.config.EventsConfig;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.GameConfig;

public class Game implements EngineObject {
    public static final int RENDER_MODE_GAME = 1;
    public static final int RENDER_MODE_END_LEVEL = 2;
    public static final int RENDER_MODE_GAME_OVER = 4;
    public static final int RENDER_MODE_ALL = RENDER_MODE_GAME | RENDER_MODE_END_LEVEL;

    public static final float WALK_WALL_DIST = 0.2f;
    private static final float USE_IGNORE_THRESHOLD = 0.5f;
    private static final int LOAD_LEVEL_JUST_NEXT_NAME = 4;
    private static final float CROSSHAIR_SIZE = 0.005f;

    private static final String P = "p"; // Please
    private static final String G = "g"; // Give
    private static final String T = "t"; // The
    private static final String D = "d"; // Debug
    private static final String PGT = P + G + T; // Please Give The
    private static final String PD = P + D; // Please Debug

    public static final int LOAD_LEVEL_NORMAL = 1;
    private static final int LOAD_LEVEL_NEXT = 2;
    public static final int LOAD_LEVEL_RELOAD = 3;

    private Engine engine;
    private Config config;
    private State state;
    private Weapons weapons;
    private Labels labels;
    private Overlay overlay;
    private Level level;
    private LevelRenderer levelRenderer;
    private AutoMap autoMap;
    private SoundManager soundManager;
    private Renderer renderer;
    private HeroController heroController;
    private Profile profile;
    private EndLevel endLevel;
    private GameOver gameOver;

    private long prevMovedTime;
    private long nextLevelTime;
    private float killedAngle;
    private float killedHeroAngle;
    private boolean isGameOverFlag;
    private boolean playStartLevelSound;
    private boolean skipEndLevelScreenOnce;
    private int heroCellX;
    private int heroCellY;
    private float prevUseX = -1.0f;
    private float prevUseY = -1.0f;
    private int maxHealth;
    private int maxArmor;
    private int maxAmmoClip;
    private int maxAmmoShell;
    private int maxAmmoGrenade;
    public float healthHitMonsterMult;
    public float healthHitHeroMult;
    private long firstTouchTime;
    private boolean hasMoved;

    public long killedTime;

    public int actionFire;
    public boolean actionGameMenu;
    public boolean actionNextWeapon;
    public boolean[] actionQuickWeapons = { false, false, false };
    public boolean actionRestartButton;
    public boolean actionContinueButton;
    public String savedGameParam = "";
    public String unprocessedGameCode = "";
    public int renderMode;
    boolean isRewardedVideoWatched;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.config = engine.config;
        this.state = engine.state;
        this.weapons = engine.weapons;
        this.labels = engine.labels;
        this.overlay = engine.overlay;
        this.level = engine.level;
        this.levelRenderer = engine.levelRenderer;
        this.autoMap = engine.autoMap;
        this.soundManager = engine.soundManager;
        this.renderer = engine.renderer;
        this.heroController = engine.heroController;
        this.profile = engine.profile;
        this.endLevel = engine.endLevel;
        this.gameOver = engine.gameOver;
    }

    private void reload() {
        nextLevelTime = 0;
        killedTime = 0;
        isGameOverFlag = false;
        playStartLevelSound = false;
        skipEndLevelScreenOnce = false;
        firstTouchTime = -1;
    }

    void onActivated() {
        renderMode = RENDER_MODE_GAME;
        soundManager.setPlaylist(SoundManager.LIST_MAIN);

        reload();
        labels.reload();
        overlay.reload();
        state.reload();
        level.reload();
        weapons.reload();

        if (TextUtils.isEmpty(savedGameParam)) {
            loadLevel(LOAD_LEVEL_NORMAL);
            playStartLevelSound = true;
        } else {
            int result = state.load(savedGameParam);

            if (result == State.LOAD_RESULT_SUCCESS && !state.mustReload && !state.mustLoadAutosave) {
                engine.updateAfterLevelLoadedOrCreated();

                if (!savedGameParam.equals(engine.instantName)) {
                    engine.createAutosave();
                }
            } else {
                loadLevel(LOAD_LEVEL_NORMAL);
                playStartLevelSound = true;
            }
        }

        savedGameParam = engine.instantName;
        updatePurchases();
    }

    private void updatePurchases() {
        maxHealth = GameConfig.HEALTH_MAX;
        maxArmor = GameConfig.ARMOR_MAX;
        maxAmmoClip = GameConfig.AMMO_MAX_CLIP;
        maxAmmoShell = GameConfig.AMMO_MAX_SHELL;
        maxAmmoGrenade = GameConfig.AMMO_MAX_GRENADE;
        healthHitMonsterMult = 1.0f;
        healthHitHeroMult = 1.0f;
    }

    private void processGameCode(String codes) {
        // this function is called from another thread,
        // when reloading level rendering thread can fail
        // so here is self-made lock
        // why not to use normal lock? because in rare cases it causes ANR
        // (due to multithreading)
        engine.renderBlackScreen = true;

        String[] codeList = codes.toLowerCase().split(" ");

        for (String code : codeList) {
            if (code.length() < 2) {
                continue;
            }

            if ((PGT + "a").equals(code)) {
                if (!state.cheatsDisabled) {
                    // Please Give The Ammo
                    state.heroHasWeapon[Weapons.WEAPON_PISTOL] = true;
                    state.heroHasWeapon[Weapons.WEAPON_DBLPISTOL] = true;
                    state.heroHasWeapon[Weapons.WEAPON_WINCHESTER] = true;
                    state.heroHasWeapon[Weapons.WEAPON_AK47] = true;
                    state.heroHasWeapon[Weapons.WEAPON_TMP] = true;
                    state.heroHasWeapon[Weapons.WEAPON_GRENADE] = true;

                    state.heroAmmo[Weapons.AMMO_CLIP] = maxAmmoClip;
                    state.heroAmmo[Weapons.AMMO_SHELL] = maxAmmoShell;
                    state.heroAmmo[Weapons.AMMO_GRENADE] = maxAmmoGrenade;
                } else {
                    Common.showToast(engine.activity, R.string.engine_cheats_disabled);
                }
            } else if ((PGT + "h").equals(code)) {
                if (!state.cheatsDisabled) {
                    // Please Give The Health
                    state.heroHealth = maxHealth;
                    state.heroArmor = maxArmor;
                } else {
                    Common.showToast(engine.activity, R.string.engine_cheats_disabled);
                }
            } else if ((PGT + "k").equals(code)) {
                // Please Give The Keys
                state.heroKeysMask = 7;
                level.requestBuildPathToWave();
            } else if ((P + "lnl").equals(code)) {
                // Please Load Next Level
                loadLevel(LOAD_LEVEL_NEXT);
            } else if ((P + G + "gm").equals(code)) {
                if (!state.cheatsDisabled) {
                    // Please Give God Mode
                    state.godMode = !state.godMode;
                } else {
                    Common.showToast(engine.activity, R.string.engine_cheats_disabled);
                }
            } else if ("iddqd".equals(code) || "iwgm".equals(code) || "gmgm".equals(code)) {
                state.godMode = false;
                state.heroHealth = 1;
                state.heroArmor = 0;
            } else if ((P + "o" + T + "m").equals(code)) {
                // Please Open The Map
                autoMap.openAllMap();
            } else if ((PD + "fps").equals(code)) {
                // Please Debug FPS
                engine.showFps = !engine.showFps;
            } else if ((PD + "rl").equals(code)) {
                // Please Debug Reload Level
                loadLevel(LOAD_LEVEL_RELOAD);
            } else if ((PD + "rli").equals(code)) {
                // Please Debug Reload Level Initial
                for (int i = 0; i < Weapons.WEAPON_LAST; i++) {
                    state.heroHasWeapon[i] = false;
                }

                for (int i = 0; i < Weapons.AMMO_LAST; i++) {
                    state.heroAmmo[i] = 0;
                }

                state.heroHasWeapon[Weapons.WEAPON_KNIFE] = true;
                state.heroArmor = GameConfig.ARMOR_ADD_GREEN;

                loadLevel(LOAD_LEVEL_RELOAD);
            } else if ((PD + "oa").equals(code)) {
                // Please Debug On Automap
                levelRenderer.debugOnAutomap = !levelRenderer.debugOnAutomap;
            } else if ((PD + T + "s").equals(code)) {
                // Please Debug The Stats
                Common.showToast(state.levelName);
            } else if (code.startsWith(PD + "l")) {
                // Please Debug Load ...
                String saveName = code.substring(3);

                if (engine.hasGameSave(Engine.DGB_SAVE_PREFIX + saveName)) {
                    if (state.load(Engine.DGB_SAVE_PREFIX + saveName) == State.LOAD_RESULT_SUCCESS) {
                        engine.updateAfterLevelLoadedOrCreated();
                        Common.showToast(engine.activity, R.string.engine_dbg_loaded);
                    } else {
                        Common.showToast(engine.activity, R.string.engine_state_cant_load);
                    }
                }
            } else if (code.startsWith(PD + "s")) {
                // Please Debug Save ...
                String saveName = code.substring(3);

                if (saveName.matches("^[0-9]+$")) {
                    state.save(Engine.DGB_SAVE_PREFIX + saveName);
                    Common.showToast(engine.activity, R.string.engine_dbg_saved);
                }
            } else if (code.startsWith(P + "m")) {
                // Please Map ...
                String newLevelName = code.substring(2);

                if (level.exists(newLevelName)) {
                    state.levelName = newLevelName;
                    loadLevel(LOAD_LEVEL_RELOAD);
                }
            }
        }

        if (engine.gameViewActive) {
            engine.renderBlackScreen = false;
        }
    }

    public void loadLevel(int loadLevelType) {
        if (loadLevelType == LOAD_LEVEL_NEXT || loadLevelType == LOAD_LEVEL_JUST_NEXT_NAME) {
            String nextLevelName = profile.getLevel(state.levelName).getNextLevelName();

            if (!level.exists(nextLevelName)) {
                return;
            }

            state.levelName = nextLevelName;
            state.showEpisodeSelector = true;
        }

        if (loadLevelType != LOAD_LEVEL_JUST_NEXT_NAME) {
            // This event is very inaccurate (because just after that Episode Selector will be shown,
            // and user can go away, and load this level later, also this event also counts level restarts).

            if (!engine.inWallpaperMode) {
                App.self.tracker.trackEvent(EventsConfig.EV_GAME_LEVEL_STARTED, state.levelName);
            }

            reload();
            state.mustReload = false;

            if (state.mustLoadAutosave) {
                if (state.load(engine.autosaveName) == State.LOAD_RESULT_SUCCESS) {
                    state.showEpisodeSelector = false;
                    engine.updateAfterLevelLoadedOrCreated();
                } else {
                    level.load(state.levelName);
                    weapons.setHeroWeaponImmediate(weapons.getBestWeapon(-1));
                }

                updatePurchases();
                renderMode = RENDER_MODE_GAME;
            } else {
                level.load(state.levelName);
                weapons.setHeroWeaponImmediate(weapons.getBestWeapon(-1));
                engine.createAutosave();
            }
        } else {
            state.mustReload = true;
        }

        state.mustLoadAutosave = false;

        if (loadLevelType == LOAD_LEVEL_NEXT || loadLevelType == LOAD_LEVEL_RELOAD) {
            engine.changeView(Engine.VIEW_TYPE_SELECT_EPISODE);
        }
    }

    private void showGameOverScreen() {
        if (engine.inWallpaperMode) {
            loadLevel(LOAD_LEVEL_NEXT);
            return;
        }

        state.mustLoadAutosave = true;
        state.overallDeaths++;

        renderMode = RENDER_MODE_GAME_OVER;
        engine.heroController.reload(); // To properly show / hide "resurrect" button

        App.self.tracker.trackEvent(EventsConfig.EV_GAME_GAME_OVER, state.levelName);

        App.self.tracker.trackEvent(
                EventsConfig.EV_GAME_GAME_OVER_REWARDED_STATE,
                App.self.mediadtor.isRewardedVideoLoaded()
                        ? EventsConfig.PAR_REWARDED_STATE_AVAILABLE
                        : EventsConfig.PAR_REWARDED_STATE_NOT_AVAILABLE);
    }

    private void showEndLevelScreen() {
        if (skipEndLevelScreenOnce || engine.inWallpaperMode) {
            skipEndLevelScreenOnce = false;
            loadLevel(LOAD_LEVEL_NEXT);
            return;
        }

        App.self.tracker.trackEvent(EventsConfig.EV_GAME_LEVEL_FINISHED, state.levelName);

        loadLevel(LOAD_LEVEL_JUST_NEXT_NAME);
        renderMode = RENDER_MODE_END_LEVEL;

        state.overallMonsters += state.killedMonsters;
        state.overallSecrets += state.foundSecrets;
        state.overallSeconds += state.timeInTicks / Engine.FRAMES_PER_SECOND;

        profile.autoSaveOnUpdate = false;
        profile.exp += state.levelExp;

        if (!profile.alreadyCompletedLevels.contains(state.levelName)) {
            profile.exp += GameConfig.EXP_END_LEVEL;
            profile.alreadyCompletedLevels.add(state.levelName);
        }

        profile.update(engine.activity);
        state.levelExp = 0;

        if (state.totalMonsters != 0 && state.killedMonsters == state.totalMonsters) {
            Achievements.updateStat(Achievements.STAT_P100_KILLS_ROW, profile, engine, state);
        } else {
            Achievements.resetStat(Achievements.STAT_P100_KILLS_ROW, profile, engine, state);
        }

        if (state.totalSecrets != 0 && state.foundSecrets == state.totalSecrets) {
            Achievements.updateStat(Achievements.STAT_P100_SECRETS_ROW, profile, engine, state);
        } else {
            Achievements.resetStat(Achievements.STAT_P100_SECRETS_ROW, profile, engine, state);
        }

        if (profile.isUnsavedUpdates) {
            profile.save(engine.activity);
        }

        endLevel.init(
                (state.totalMonsters == 0 ? -1 : (state.killedMonsters * 100 / state.totalMonsters)),
                (state.totalSecrets == 0 ? -1 : (state.foundSecrets * 100 / state.totalSecrets)),
                (state.timeInTicks / Engine.FRAMES_PER_SECOND));
    }

    public void nextLevel(@SuppressWarnings("SameParameterValue") boolean isTutorial) {
        skipEndLevelScreenOnce = isTutorial;
        nextLevelTime = engine.elapsedTime;

        soundManager.playSound(SoundManager.SOUND_LEVEL_END);

        if (!isTutorial) {
            soundManager.setPlaylist(SoundManager.LIST_ENDL);
        }
    }

    public void hitHero(int hits, Monster mon) {
        if (killedTime > 0) {
            return;
        }

        if (mon != null) {
            overlay.showHitSide(mon.x, mon.y);
        } else {
            overlay.showOverlay(Overlay.BLOOD);
        }

        if (!state.godMode && nextLevelTime == 0) {
            if (state.heroArmor > 0) {
                state.heroArmor = Math.max(
                        0,
                        state.heroArmor - Math.max(1, (int)((double)hits * GameConfig.ARMOR_HIT_TAKER)));

                state.heroHealth -= Math.max(1, (int)((double)hits * GameConfig.ARMOR_HEALTH_SAVER));
            } else {
                state.heroHealth -= hits;
            }
        }

        if (state.heroHealth <= 0) {
            state.heroHealth = 0;
            killedTime = engine.elapsedTime;

            if (mon != null) {
                killedAngle = GameMath.getAngle(mon.x - state.heroX, mon.y - state.heroY) * GameMath.RAD2G_F;

                //noinspection MagicNumber
                killedHeroAngle = ((Math.abs(360.0f + state.heroA - killedAngle) < Math.abs(state.heroA - killedAngle))
                        ? (360.0f + state.heroA)
                        : state.heroA);
            } else {
                killedAngle = state.heroA;
                killedHeroAngle = state.heroA;
            }

            soundManager.playSound(SoundManager.SOUND_DEATH_HERO);
            engine.soundManager.setPlaylist(SoundManager.LIST_GAMEOVER);
        }
    }

    private boolean processUse(int x, int y) {
        if (level.doorsMap[y][x] != null) {
            Door door = level.doorsMap[y][x];

            if (door.openPos >= Door.OPEN_POS_PASSABLE) {
                return false;
            }

            if (door.sticked) {
                if (door.requiredKey == 0) {
                    overlay.showLabel(Labels.LABEL_CANT_OPEN);
                    soundManager.playSound(SoundManager.SOUND_NO_WAY);
                    return true;
                }

                if ((state.heroKeysMask & door.requiredKey) == 0) {
                    if (door.requiredKey == 4) {
                        overlay.showLabel(Labels.LABEL_NEED_GREEN_KEY);
                    } else if (door.requiredKey == 2) {
                        overlay.showLabel(Labels.LABEL_NEED_RED_KEY);
                    } else {
                        overlay.showLabel(Labels.LABEL_NEED_BLUE_KEY);
                    }

                    soundManager.playSound(SoundManager.SOUND_NO_WAY);
                    return true;
                }

                door.unstick();
            }

            if (door.open()) {
                if ((state.passableMap[door.y][door.x] & Level.PASSABLE_IS_DOOR_OPENED_BY_HERO) == 0) {
                    state.levelExp += GameConfig.EXP_OPEN_DOOR;
                    Achievements.updateStat(Achievements.STAT_DOORS_OPENED, profile, engine, state);
                }

                state.passableMap[door.y][door.x] |= Level.PASSABLE_IS_DOOR_OPENED_BY_HERO;

                if (door.mark != null) {
                    processOneMark(door.mark);
                }

                return true;
            }

            return false;
        }

        // Если на месте метки лежит объект, не процессить метку (позволяет избежать некоторых странных багов)
        if ((state.passableMap[y][x] & Level.PASSABLE_IS_OBJECT) != 0) {
            return false;
        }

        //noinspection SimplifiableIfStatement
        if (level.marksMap[y][x] != null) {
            return processOneMark(level.marksMap[y][x]);
        }

        return false;
    }

    private boolean processUse(float x, float y, @SuppressWarnings("SameParameterValue") float wallDist) {
        if (Math.abs(x - prevUseX) < USE_IGNORE_THRESHOLD && Math.abs(y - prevUseY) < USE_IGNORE_THRESHOLD) {
            return false;
        }

        int fx = Math.max(0, (int)(x - wallDist));
        int tx = Math.min(state.levelWidth - 1, (int)(x + wallDist));
        int fy = Math.max(0, (int)(y - wallDist));
        int ty = Math.min(state.levelHeight - 1, (int)(y + wallDist));

        for (int i = fx; i <= tx; i++) {
            for (int j = fy; j <= ty; j++) {
                if (processUse(i, j)) {
                    prevUseX = x;
                    prevUseY = y;
                    return true;
                }
            }
        }

        prevUseX = -1.0f;
        prevUseY = -1.0f;
        return false;
    }

    @SuppressWarnings({ "UnusedReturnValue", "MagicNumber" })
    public boolean updateHeroPosition(float dx, float dy, float accel) {
        if (accel < -0.9f) {
            accel = -0.9f;
        } else if (accel > 0.9f) {
            accel = 0.9f;
        }

        float addX = accel * dx;
        float newX = state.heroX;

        if (Math.abs(addX) > 0.01f) {
            level.fillInitialInWallMap(state.heroX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);
            newX += addX;

            if (!level.isPassable(newX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO)) {
                if (processUse(newX, state.heroY, WALK_WALL_DIST)) {
                    hasMoved = true;
                    return false;
                }

                newX = (addX > 0
                        ? (float)Math.ceil(state.heroX) - WALK_WALL_DIST - 0.005f
                        : (float)Math.floor(state.heroX) + WALK_WALL_DIST + 0.005f);
            }
        }

        float addY = accel * dy;
        float newY = state.heroY;

        if (Math.abs(addY) > 0.01f) {
            level.fillInitialInWallMap(newX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);
            newY += addY;

            if (!level.isPassable(newX, newY, WALK_WALL_DIST, Level.PASSABLE_MASK_HERO)) {
                if (processUse(newX, newY, WALK_WALL_DIST)) {
                    hasMoved = true;
                    return false;
                }

                newY = (addY > 0
                        ? (float)Math.ceil(state.heroY) - WALK_WALL_DIST - 0.005f
                        : (float)Math.floor(state.heroY) + WALK_WALL_DIST + 0.005f);
            }
        }

        boolean positionUpdated = (Math.abs(state.heroX - newX) > GameMath.EPSILON) || (Math.abs(state.heroY - newY)
                > GameMath.EPSILON);

        hasMoved |= positionUpdated;
        state.heroX = newX;
        state.heroY = newY;

        if (Math.abs(newX - prevUseX) >= USE_IGNORE_THRESHOLD || Math.abs(newY - prevUseY) >= USE_IGNORE_THRESHOLD) {
            prevUseX = -1.0f;
            prevUseY = -1.0f;
        }

        return positionUpdated;
    }

    public void update() {
        if (isRewardedVideoWatched) {
            isRewardedVideoWatched = false;
            isGameOverFlag = false;
            killedTime = 0L;

            state.heroHealth = GameConfig.HEALTH_MAX;
            state.heroArmor = Math.min(GameConfig.ARMOR_MAX, state.heroArmor + GameConfig.ARMOR_ADD_GREEN);

            if (state.heroHasWeapon[Weapons.WEAPON_PISTOL] || state.heroHasWeapon[Weapons.WEAPON_DBLPISTOL]) {
                state.heroAmmo[Weapons.AMMO_CLIP] = Math.min(
                        GameConfig.AMMO_MAX_CLIP,
                        state.heroAmmo[Weapons.AMMO_CLIP] + GameConfig.AMMO_ADD_CBOX);
            }

            if (state.heroHasWeapon[Weapons.WEAPON_AK47]
                    || state.heroHasWeapon[Weapons.WEAPON_TMP]
                    || state.heroHasWeapon[Weapons.WEAPON_WINCHESTER]) {

                state.heroAmmo[Weapons.AMMO_SHELL] = Math.min(
                        GameConfig.AMMO_MAX_SHELL,
                        state.heroAmmo[Weapons.AMMO_SHELL] + GameConfig.AMMO_ADD_SBOX);
            }

            state.overallResurrects++;
        }

        if (engine.gameViewActive && !TextUtils.isEmpty(unprocessedGameCode)) {
            processGameCode(unprocessedGameCode);
            unprocessedGameCode = "";
        }

        if (engine.gameViewActive) {
            state.showEpisodeSelector = false;
        }

        if (level.buildPathToWavePending) {
            level.buildPathToWavePending = false;
            level.buildPathToWaveMap();
            autoMap.updatePathTo();
        }

        switch (renderMode) {
            case RENDER_MODE_END_LEVEL:
                endLevel.update();
                return;

            case RENDER_MODE_GAME_OVER:
                gameOver.update();
                return;
        }

        state.timeInTicks++;

        if (playStartLevelSound) {
            soundManager.playSound(SoundManager.SOUND_LEVEL_START);
            playStartLevelSound = false;
        }

        if (firstTouchTime < 0 && engine.interacted) {
            firstTouchTime = engine.elapsedTime;
        }

        hasMoved = false;

        for (Door door = state.doors.first(); door != null; door = door.next) {
            door.tryClose();
        }

        for (Monster mon = state.monsters.first(); mon != null; mon = mon.next) {
            mon.update();
        }

        for (Bullet bullet = state.bullets.first(); bullet != null; ) {
            Bullet nextBullet = bullet.next;
            bullet.update();

            if (bullet.bulletState == Bullet.STATE_RELEASE) {
                state.bullets.release(bullet);
            }

            bullet = nextBullet;
        }

        for (Explosion explosion = state.explosions.first(); explosion != null; ) {
            Explosion nextExplosion = explosion.next;

            if (!explosion.update()) {
                state.explosions.release(explosion);
            }

            explosion = nextExplosion;
        }

        for (Timeout timeout = state.timeouts.first(); timeout != null; ) {
            Timeout nextTimeout = timeout.next;

            if (timeout.delay <= 0) {
                level.executeActions(timeout.markId);
                state.timeouts.release(timeout);
            } else {
                timeout.delay--;
            }

            timeout = nextTimeout;
        }

        for (LookPoint lookPoint = state.lookPoints.first(); lookPoint != null; ) {
            LookPoint nextLookPoint = lookPoint.next;

            if (levelRenderer.awTouchedCellsMap[lookPoint.y][lookPoint.x]) {
                processOneMarkById(lookPoint.markId);
                state.lookPoints.release(lookPoint);
            }

            lookPoint = nextLookPoint;
        }

        weapons.update(nextLevelTime <= 0 && killedTime <= 0);

        if (actionNextWeapon && weapons.switchToNextWeapon()) {
            actionNextWeapon = false;
        }

        for (int i = 0, len = actionQuickWeapons.length; i < len; i++) {
            if (actionQuickWeapons[i]) {
                weapons.switchWeapon(state.lastWeapons[i]);
                actionQuickWeapons[i] = false;
            }
        }

        if (actionFire != 0) {
            weapons.fire();
        }

        if (actionGameMenu) {
            engine.changeView(Engine.VIEW_TYPE_GAME_MENU);
            actionGameMenu = false;

            if (firstTouchTime < 0) {
                firstTouchTime = engine.elapsedTime;
            }
        }

        level.clearPassable(state.heroX, state.heroY, WALK_WALL_DIST, Level.PASSABLE_IS_HERO);
        heroController.updateHero();

        // [[Game:01]] see [[Level:01]]
        // если проставлять passable и учитывать WALK_WALL_DIST, то это приводит к проблемам с ударами
        // у врагов ближнего боя. но снимается маска с учётом WALK_WALL_DIST (парой строчек выше),
        // чтобы не было траблов со старыми сохранками
        level.setPassable(state.heroX, state.heroY, 0.0f /* WALK_WALL_DIST */, Level.PASSABLE_IS_HERO);

        if (((int)state.heroX != heroCellX) || ((int)state.heroY != heroCellY)) {
            heroCellX = (int)state.heroX;
            heroCellY = (int)state.heroY;

            pickObjects(); // сначала - взять объект
            processMarks(); // потом - запроцессить скрипты (это лечит PathTo после ключа)
            autoMap.updatePathTo();
        }
    }

    private boolean processOneMark(Mark mark) {
        return (!mark.unmarked && mark.enabled && processOneMarkById(mark.id));
    }

    private boolean processOneMarkById(int markId) {
        int soundIdx = level.executeActions(markId);

        if (soundIdx >= 0) {
            // if this is *not* pop level switch, play sound
            if (nextLevelTime == 0) {
                soundManager.playSound(soundIdx);
                overlay.showOverlay(Overlay.MARK);
            }

            return true;
        }

        return false;
    }

    private void processMarks() {
        if ((level.marksMap[(int)state.heroY][(int)state.heroX] != null)
                && (level.doorsMap[(int)state.heroY][(int)state.heroX] == null)) {

            processOneMark(level.marksMap[(int)state.heroY][(int)state.heroX]);
        }
    }

    private void pickObjects() {
        int cy = (int)state.heroY;
        int cx = (int)state.heroX;

        if ((state.passableMap[cy][cx] & Level.PASSABLE_IS_OBJECT) == 0) {
            return;
        }

        ObjectContainer container = state.objectsMap.get(cy).get(cx);
        boolean soundWasNotPlayed = true;

        for (int i = 0; i < container.count; ) {
            int tex = container.get(i);

            if (!shouldPickOneObject(tex)) {
                i++;
                continue;
            }

            if (soundWasNotPlayed) {
                soundWasNotPlayed = false;

                playOneObjectSound(tex);
                overlay.showOverlay(Overlay.ITEM);
            }

            pickOneObject(tex);
            container.removeAt(i);
        }

        if (container.count == 0) {
            state.passableMap[cy][cx] &= ~Level.PASSABLE_MASK_OBJECT;
            levelRenderer.modLightMap(cx, cy, -LevelRenderer.LIGHT_OBJECT);
        }
    }

    private boolean shouldPickOneObject(int tex) {
        switch (tex) {
            case TextureLoader.OBJ_ARMOR_GREEN:
            case TextureLoader.OBJ_ARMOR_RED:
                return (state.heroArmor < maxArmor);

            case TextureLoader.OBJ_STIM:
            case TextureLoader.OBJ_MEDI:
                return (state.heroHealth < maxHealth);

            case TextureLoader.OBJ_CLIP:
            case TextureLoader.OBJ_CBOX:
                return (state.heroAmmo[Weapons.AMMO_CLIP] < maxAmmoClip);

            case TextureLoader.OBJ_SHELL:
            case TextureLoader.OBJ_SBOX:
                return (state.heroAmmo[Weapons.AMMO_SHELL] < maxAmmoShell);

            case TextureLoader.OBJ_GRENADE:
            case TextureLoader.OBJ_GBOX:
                return (state.heroAmmo[Weapons.AMMO_GRENADE] < maxAmmoGrenade);

            case TextureLoader.OBJ_BPACK:
                return (state.heroHealth < maxHealth
                        || state.heroAmmo[Weapons.AMMO_CLIP] < maxAmmoClip
                        || state.heroAmmo[Weapons.AMMO_SHELL] < maxAmmoShell);

            case TextureLoader.OBJ_DBLPIST:
                return (shouldPickWeapon(Weapons.WEAPON_DBLPISTOL));

            case TextureLoader.OBJ_AK47:
                return (shouldPickWeapon(Weapons.WEAPON_AK47));

            case TextureLoader.OBJ_TMP:
                return (shouldPickWeapon(Weapons.WEAPON_TMP));

            case TextureLoader.OBJ_WINCHESTER:
                return (shouldPickWeapon(Weapons.WEAPON_WINCHESTER));
        }

        return true;
    }

    private void playOneObjectSound(int tex) {
        switch (tex) {
            case TextureLoader.OBJ_CLIP:
            case TextureLoader.OBJ_CBOX:
            case TextureLoader.OBJ_SHELL:
            case TextureLoader.OBJ_SBOX:
            case TextureLoader.OBJ_GRENADE:
            case TextureLoader.OBJ_GBOX:
                soundManager.playSound(SoundManager.SOUND_PICK_AMMO);
                break;

            case TextureLoader.OBJ_BPACK:
            case TextureLoader.OBJ_DBLPIST:
            case TextureLoader.OBJ_AK47:
            case TextureLoader.OBJ_TMP:
            case TextureLoader.OBJ_WINCHESTER:
                soundManager.playSound(SoundManager.SOUND_PICK_WEAPON);
                break;

            default:
                soundManager.playSound(SoundManager.SOUND_PICK_ITEM);
                break;
        }
    }

    private void pickOneObject(int tex) {
        switch (tex) {
            case TextureLoader.OBJ_ARMOR_GREEN:
                state.heroArmor = Math.min(state.heroArmor + GameConfig.ARMOR_ADD_GREEN, maxArmor);
                break;

            case TextureLoader.OBJ_ARMOR_RED:
                state.heroArmor = Math.min(state.heroArmor + GameConfig.ARMOR_ADD_RED, maxArmor);
                break;

            case TextureLoader.OBJ_KEY_BLUE:
                state.heroKeysMask |= 1;
                level.requestBuildPathToWave();
                break;

            case TextureLoader.OBJ_KEY_RED:
                state.heroKeysMask |= 2;
                level.requestBuildPathToWave();
                break;

            case TextureLoader.OBJ_KEY_GREEN:
                state.heroKeysMask |= 4;
                level.requestBuildPathToWave();
                break;

            case TextureLoader.OBJ_OPENMAP:
                autoMap.openAllMap();
                break;

            case TextureLoader.OBJ_STIM:
                state.heroHealth = Math.min(state.heroHealth + GameConfig.HEALTH_ADD_STIM, maxHealth);
                break;

            case TextureLoader.OBJ_MEDI:
                state.heroHealth = Math.min(state.heroHealth + GameConfig.HEALTH_ADD_MEDI, maxHealth);
                break;

            case TextureLoader.OBJ_CLIP:
                pickAmmo(Weapons.AMMO_CLIP, GameConfig.AMMO_ADD_CLIP);
                break;

            case TextureLoader.OBJ_CBOX:
                pickAmmo(Weapons.AMMO_CLIP, GameConfig.AMMO_ADD_CBOX);
                break;

            case TextureLoader.OBJ_SHELL:
                pickAmmo(Weapons.AMMO_SHELL, GameConfig.AMMO_ADD_SHELL);
                break;

            case TextureLoader.OBJ_SBOX:
                pickAmmo(Weapons.AMMO_SHELL, GameConfig.AMMO_ADD_SBOX);
                break;

            case TextureLoader.OBJ_GRENADE:
                pickAmmo(Weapons.AMMO_GRENADE, GameConfig.AMMO_ADD_GRENADE);
                break;

            case TextureLoader.OBJ_GBOX:
                pickAmmo(Weapons.AMMO_GRENADE, GameConfig.AMMO_ADD_GBOX);
                break;

            case TextureLoader.OBJ_BPACK:
                state.heroHealth = Math.min(state.heroHealth + GameConfig.HEALTH_ADD_STIM, maxHealth);

                state.heroAmmo[Weapons.AMMO_CLIP] = Math.min(
                        state.heroAmmo[Weapons.AMMO_CLIP] + GameConfig.AMMO_ADD_CLIP,
                        maxAmmoClip);

                state.heroAmmo[Weapons.AMMO_SHELL] = Math.min(
                        state.heroAmmo[Weapons.AMMO_SHELL] + GameConfig.AMMO_ADD_SHELL,
                        maxAmmoShell);

                weapons.selectBestWeapon(-1);
                break;

            case TextureLoader.OBJ_DBLPIST:
                pickWeapon(Weapons.WEAPON_DBLPISTOL, GameConfig.AMMO_ADD_DBLPIST);
                break;

            case TextureLoader.OBJ_AK47:
                pickWeapon(Weapons.WEAPON_AK47, GameConfig.AMMO_ADD_AK47);
                break;

            case TextureLoader.OBJ_TMP:
                pickWeapon(Weapons.WEAPON_TMP, GameConfig.AMMO_ADD_TMP);
                break;

            case TextureLoader.OBJ_WINCHESTER:
                pickWeapon(Weapons.WEAPON_WINCHESTER, GameConfig.AMMO_ADD_WINCHESTER);
                break;
        }

        state.levelExp += GameConfig.EXP_PICK_OBJECT;
    }

    private boolean shouldPickWeapon(int weapon) {
        int ammoIdx = Weapons.WEAPONS[weapon].ammoIdx;

        int maxAmmo = (ammoIdx == Weapons.AMMO_CLIP
                ? maxAmmoClip
                : (ammoIdx == Weapons.AMMO_SHELL ? maxAmmoShell : maxAmmoGrenade));

        return (!state.heroHasWeapon[weapon] || state.heroAmmo[ammoIdx] < maxAmmo);
    }

    private void pickAmmo(int ammoIdx, int ammoAdd) {
        int maxAmmo = (ammoIdx == Weapons.AMMO_CLIP
                ? maxAmmoClip
                : (ammoIdx == Weapons.AMMO_SHELL ? maxAmmoShell : maxAmmoGrenade));

        if (ammoIdx == Weapons.AMMO_GRENADE) {
            state.heroHasWeapon[Weapons.WEAPON_GRENADE] = true;
        }

        state.heroAmmo[ammoIdx] = Math.min(state.heroAmmo[ammoIdx] + ammoAdd, maxAmmo);
        weapons.selectBestWeapon(ammoIdx);
    }

    private void pickWeapon(int weaponIdx, int ammoAdd) {
        int ammoIdx = Weapons.WEAPONS[weaponIdx].ammoIdx;

        int maxAmmo = (ammoIdx == Weapons.AMMO_CLIP
                ? maxAmmoClip
                : (ammoIdx == Weapons.AMMO_SHELL ? maxAmmoShell : maxAmmoGrenade));

        state.heroHasWeapon[weaponIdx] = true;
        state.heroAmmo[ammoIdx] = Math.min(state.heroAmmo[ammoIdx] + ammoAdd, maxAmmo);

        if (state.heroWeapon < weaponIdx) {
            weapons.switchWeapon(weaponIdx);
        }
    }

    @SuppressWarnings("MagicNumber")
    private void renderCrosshair() {
        renderer.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 0.5f);

        renderer.setCoordsQuadRectFlat(-CROSSHAIR_SIZE, 0.03f, CROSSHAIR_SIZE, 0.08f); // up
        renderer.batchQuad();

        renderer.setCoordsQuadRectFlat(CROSSHAIR_SIZE, -0.03f, -CROSSHAIR_SIZE, -0.08f); // down
        renderer.batchQuad();

        renderer.setCoordsQuadRectFlat(0.03f, -CROSSHAIR_SIZE, 0.08f, CROSSHAIR_SIZE); // right
        renderer.batchQuad();

        renderer.setCoordsQuadRectFlat(-0.03f, CROSSHAIR_SIZE, -0.08f, -CROSSHAIR_SIZE); // left
        renderer.batchQuad();

        renderer.useOrtho(-engine.ratio, engine.ratio, -1.0f, 1.0f, 0.0f, 1.0f);
        renderer.renderBatch(Renderer.FLAG_BLEND);
    }

    @SuppressWarnings("MagicNumber")
    private void renderSky() {
        renderer.useOrtho(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);

        if (GameConfig.COLOR_SKY > 0) {
            float r = (float)((GameConfig.COLOR_SKY >> 16) & 0xff) / 255.0f;
            float g = (float)((GameConfig.COLOR_SKY >> 8) & 0xff) / 255.0f;
            float b = (float)(GameConfig.COLOR_SKY & 0xff) / 255.0f;

            renderer.startBatch();
            renderer.setColorQuadRGBA(r, g, b, 1.0f);
            renderer.setCoordsQuadRectFlat(0.0f, 1.0f, 1.0f, 0.0f);
            renderer.batchQuad();
            renderer.renderBatch(0);
        }

        float ox = (float)Math.sin((state.heroA % 30.0f) * GameMath.G2RAD_F);
        float oy = (float)Math.sin(state.heroVertA * GameMath.G2RAD_F);

        renderer.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
        renderer.setTexRect(0, 0, 4 << 16, 1 << 16);
        renderer.setCoordsQuadRectFlat(ox - 1.0f, 1.1f - oy, ox + 1.0f, 0.15f - oy);
        renderer.batchQuad();
        renderer.renderBatch(Renderer.FLAG_TEX_REPEAT, Renderer.TEXTURE_SKY);

        if (GameConfig.COLOR_SKY > 0) {
            float r = (float)((GameConfig.COLOR_SKY >> 16) & 0xff) / 255.0f;
            float g = (float)((GameConfig.COLOR_SKY >> 8) & 0xff) / 255.0f;
            float b = (float)(GameConfig.COLOR_SKY & 0xff) / 255.0f;

            renderer.startBatch();
            renderer.setColorQuadRGBA(r, g, b, 1.0f);
            renderer.setCoordsQuadRectFlat(ox - 1.0f, 1.09f - oy, ox + 1.0f, 1.11f - oy);
            renderer.batchQuad();
            renderer.renderBatch(0);
        }
    }

    @SuppressWarnings("MagicNumber")
    protected void render() {
        long walkTime = 0;

        if (hasMoved) {
            if (prevMovedTime != 0) {
                walkTime = engine.elapsedTime - prevMovedTime;
            } else {
                prevMovedTime = engine.elapsedTime;
            }
        } else {
            prevMovedTime = 0;
        }

        float offY = LevelRenderer.HALF_WALL / 8.0f
                + (float)Math.sin((float)walkTime / 100.0f) * LevelRenderer.HALF_WALL / 16.0f;

        float rotX = state.heroVertA;

        if (killedTime > 0) {
            float killedRatio = Math.min(1.0f, (float)(engine.elapsedTime - killedTime) / 1000.0f);

            offY += killedRatio * (LevelRenderer.HALF_WALL * 0.5f - offY);
            rotX += killedRatio * (-45.0f - rotX);
            state.setHeroA(killedHeroAngle + (killedAngle - killedHeroAngle) * killedRatio);
        }

        renderSky();
        levelRenderer.render(engine.elapsedTime, -offY, -90.0f - rotX);

        if (config.showCrosshair && !engine.inWallpaperMode) {
            renderCrosshair();
        }

        weapons.render(walkTime);
        autoMap.render();
        overlay.renderOverlay();

        if (!engine.inWallpaperMode) {
            overlay.renderHitSide();
            engine.stats.render();

            if (renderMode == RENDER_MODE_GAME) {
                heroController.render(
                        true,
                        (firstTouchTime >= 0 ? firstTouchTime : engine.elapsedTime));
            }
        }

        if (nextLevelTime > 0) {
            overlay.renderEndLevelLayer((float)(engine.elapsedTime - nextLevelTime) / 500.0f);
        }

        if (renderMode == RENDER_MODE_END_LEVEL) {
            endLevel.render();
            heroController.render(false, 0L);
        } else if (renderMode == RENDER_MODE_GAME_OVER) {
            gameOver.render();
            heroController.render(false, 0L);
        }

        if (!engine.inWallpaperMode) {
            overlay.renderLabels();
        }

        if (config.gamma > 0.01f) {
            overlay.renderGammaLayer();
        }

        if (engine.showFps) {
            engine.renderFps();
        }

        if (renderMode == RENDER_MODE_GAME) {
            if (nextLevelTime > 0) {
                if (engine.elapsedTime - nextLevelTime > 1000) {
                    if (isGameOverFlag) {
                        showGameOverScreen();
                    } else {
                        showEndLevelScreen();
                    }
                }
            } else if ((killedTime > 0) && (engine.elapsedTime - killedTime > 3500)) {
                isGameOverFlag = true;
                nextLevelTime = engine.elapsedTime;
            }
        }
    }
}
