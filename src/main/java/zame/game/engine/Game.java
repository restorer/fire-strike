package zame.game.engine;

import android.text.TextUtils;
import javax.microedition.khronos.opengles.GL10;
import zame.game.App;
import zame.game.Common;
import zame.game.R;
import zame.game.engine.hud.AutoMap;
import zame.game.managers.SoundManager;
import zame.game.store.Achievements;
import zame.game.store.Profile;

public class Game implements EngineObject {
    public static final int RENDER_MODE_GAME = 1;
    public static final int RENDER_MODE_END_LEVEL = 2;
    public static final int RENDER_MODE_GAME_OVER = 4;
    public static final int RENDER_MODE_ALL = RENDER_MODE_GAME | RENDER_MODE_END_LEVEL | RENDER_MODE_GAME_OVER;

    static final float WALK_WALL_DIST = 0.2f;
    private static final float USE_IGNORE_THRESHOLD = 0.5f;
    private static final int LOAD_LEVEL_JUST_NEXT_NAME = 4;
    private static final float CROSSHAIR_SIZE = 0.005f;

    private static final String P = "p"; // Please
    private static final String G = "g"; // Give
    private static final String T = "t"; // The
    private static final String D = "d"; // Debug
    private static final String PGT = P + G + T; // Please Give The
    private static final String PD = P + D; // Please Debug

    static final int LOAD_LEVEL_NORMAL = 1;
    @SuppressWarnings("WeakerAccess") static final int LOAD_LEVEL_NEXT = 2;
    static final int LOAD_LEVEL_RELOAD = 3;

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
    private long firstTouchTime;
    private boolean hasMoved;

    long killedTime;

    public int actionFire;
    public boolean actionGameMenu;
    public boolean actionNextWeapon;
    public boolean[] actionQuickWeapons = { false, false, false };
    public boolean actionUpgradeButton;
    public String savedGameParam = "";
    public String unprocessedGameCode = "";
    public int renderMode;
    public boolean isRewardedVideoWatched;

    @Override
    public void setEngine(Engine engine) {
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

    private void setStartValues() {
        nextLevelTime = 0;
        killedTime = 0;
        isGameOverFlag = false;
        playStartLevelSound = false;
        skipEndLevelScreenOnce = false;
        firstTouchTime = -1;
    }

    public void init() {
        setStartValues();
        renderMode = RENDER_MODE_GAME;
        soundManager.setPlaylist(SoundManager.LIST_MAIN);

        labels.init();
        overlay.init();
        state.init();
        level.init();
        weapons.init();

        if (TextUtils.isEmpty(savedGameParam)) {
            loadLevel(LOAD_LEVEL_NORMAL);
            playStartLevelSound = true;

            if (State.LEVEL_INITIAL.equals(state.levelName)) {
                App.self.trackerInst.send("JustStarted");
            }
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
        maxHealth = GameParams.HEALTH_MAX;
        maxArmor = GameParams.ARMOR_MAX;
        maxAmmoClip = GameParams.AMMO_MAX_CLIP;
        maxAmmoShell = GameParams.AMMO_MAX_SHELL;
        maxAmmoGrenade = GameParams.AMMO_MAX_GRENADE;
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
                    Common.showToast(R.string.msg_cheats_disabled);
                }
            } else if ((PGT + "h").equals(code)) {
                if (!state.cheatsDisabled) {
                    // Please Give The Health
                    state.heroHealth = maxHealth;
                    state.heroArmor = maxArmor;
                } else {
                    Common.showToast(R.string.msg_cheats_disabled);
                }
            } else if ((PGT + "k").equals(code)) {
                // Please Give The Keys
                state.heroKeysMask = 7;
            } else if ((P + "lnl").equals(code)) {
                // Please Load Next Level
                loadLevel(LOAD_LEVEL_NEXT);
            } else if ((P + G + "gm").equals(code)) {
                if (!state.cheatsDisabled) {
                    // Please Give God Mode
                    state.godMode = !state.godMode;
                } else {
                    Common.showToast(R.string.msg_cheats_disabled);
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
                        Common.showToast(R.string.msg_dbg_loaded);
                    } else {
                        Common.showToast(R.string.msg_cant_load_state);
                    }
                }
            } else if (code.startsWith(PD + "s")) {
                // Please Debug Save ...
                String saveName = code.substring(3);

                if (saveName.matches("^[0-9]+$")) {
                    state.save(Engine.DGB_SAVE_PREFIX + saveName);
                    Common.showToast(R.string.msg_dbg_saved);
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

    void loadLevel(int loadLevelType) {
        if (loadLevelType == LOAD_LEVEL_NEXT || loadLevelType == LOAD_LEVEL_JUST_NEXT_NAME) {
            String nextLevelName = profile.getLevel(state.levelName).getNextLevelName();

            if (!level.exists(nextLevelName)) {
                return;
            }

            state.levelName = nextLevelName;
            state.showEpisodeSelector = true;
        }

        if (loadLevelType != LOAD_LEVEL_JUST_NEXT_NAME) {
            setStartValues();
            state.mustReload = false;

            if (state.mustLoadAutosave) {
                if (state.load(engine.autosaveName) == State.LOAD_RESULT_SUCCESS) {
                    state.showEpisodeSelector = false;
                    engine.updateAfterLevelLoadedOrCreated();
                } else {
                    level.load(state.levelName);
                    state.heroWeapon = weapons.getBestWeapon();
                }

                updatePurchases();
                renderMode = RENDER_MODE_GAME;
            } else {
                level.load(state.levelName);
                state.heroWeapon = weapons.getBestWeapon();
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
        renderMode = RENDER_MODE_GAME_OVER;
        App.self.trackerInst.send("GameOver", state.levelName);
    }

    private void showEndLevelScreen() {
        if (skipEndLevelScreenOnce || engine.inWallpaperMode) {
            skipEndLevelScreenOnce = false;
            loadLevel(LOAD_LEVEL_NEXT);
            return;
        }

        App.self.trackerInst.send("LevelCompleted", state.levelName);

        loadLevel(LOAD_LEVEL_JUST_NEXT_NAME);
        renderMode = RENDER_MODE_END_LEVEL;

        if (state.pickedItems > state.totalItems) {
            state.pickedItems = state.totalItems;
            Common.log("Game.showEndLevelScreen: state.pickedItems > state.totalItems");
        }

        state.overallItems += state.pickedItems;
        state.overallMonsters += state.killedMonsters;
        state.overallSecrets += state.foundSecrets;
        state.overallSeconds += state.timeInTicks / Engine.FRAMES_PER_SECOND;

        profile.autoSaveOnUpdate = false;
        profile.exp += state.levelExp;

        if (!profile.alreadyCompletedLevels.contains(state.levelName)) {
            profile.exp += GameParams.EXP_END_LEVEL;
            profile.alreadyCompletedLevels.add(state.levelName);
        }

        profile.update();
        state.levelExp = 0;

        if (state.totalMonsters != 0 && state.killedMonsters == state.totalMonsters) {
            Achievements.updateStat(Achievements.STAT_P100_KILLS_ROW, profile, engine, state);
        } else {
            Achievements.resetStat(Achievements.STAT_P100_KILLS_ROW, profile, engine, state);
        }

        if (state.totalItems != 0 && state.pickedItems == state.totalItems) {
            Achievements.updateStat(Achievements.STAT_P100_ITEMS_ROW, profile, engine, state);
        } else {
            Achievements.resetStat(Achievements.STAT_P100_ITEMS_ROW, profile, engine, state);
        }

        if (state.totalSecrets != 0 && state.foundSecrets == state.totalSecrets) {
            Achievements.updateStat(Achievements.STAT_P100_SECRETS_ROW, profile, engine, state);
        } else {
            Achievements.resetStat(Achievements.STAT_P100_SECRETS_ROW, profile, engine, state);
        }

        if (profile.isUnsavedUpdates) {
            profile.save();
        }

        endLevel.init((state.totalMonsters == 0 ? -1 : (state.killedMonsters * 100 / state.totalMonsters)),
                (state.totalItems == 0 ? -1 : (state.pickedItems * 100 / state.totalItems)),
                (state.totalSecrets == 0 ? -1 : (state.foundSecrets * 100 / state.totalSecrets)),
                (state.timeInTicks / Engine.FRAMES_PER_SECOND));
    }

    void nextLevel(@SuppressWarnings("SameParameterValue") boolean isTutorial) {
        skipEndLevelScreenOnce = isTutorial;
        nextLevelTime = engine.elapsedTime;

        soundManager.playSound(SoundManager.SOUND_LEVEL_END);

        if (!isTutorial) {
            soundManager.setPlaylist(SoundManager.LIST_ENDL);
        }
    }

    void hitHero(int hits, Monster mon) {
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
                state.heroArmor = Math.max(0,
                        state.heroArmor - Math.max(1, (int)((double)hits * GameParams.ARMOR_HIT_TAKER)));

                state.heroHealth -= Math.max(1, (int)((double)hits * GameParams.ARMOR_HEALTH_SAVER));
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

                    // if (door.mark != null) {
                    //     processOneMarkById(100 + door.mark.id);
                    // }

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

                    // if (door.mark != null) {
                    //     processOneMarkById(100 + door.mark.id);
                    // }

                    soundManager.playSound(SoundManager.SOUND_NO_WAY);
                    return true;
                }

                door.unstick();
            }

            if (door.open()) {
                if ((state.passableMap[door.y][door.x] & Level.PASSABLE_IS_DOOR_OPENED_BY_HERO) == 0) {
                    state.levelExp += GameParams.EXP_OPEN_DOOR;
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

    private void processShoot() {
        // just for case
        if (weapons.hasNoAmmo(state.heroWeapon)) {
            weapons.selectBestWeapon();
        }

        Weapons.WeaponParams localParams = weapons.currentParams;

        //noinspection BooleanVariableAlwaysNegated
        boolean hitOrShoot = Bullet.shootOrPunch(state,
                state.heroX,
                state.heroY,
                engine.heroAr,
                null,
                localParams.ammoIdx,
                localParams.hits,
                localParams.hitTimeout);

        if (weapons.currentCycle[weapons.shootCycle] > -1000) {
            soundManager.playSound((localParams.noHitSoundIdx != 0 && !hitOrShoot)
                    ? localParams.noHitSoundIdx
                    : localParams.soundIdx);
        }

        if (localParams.ammoIdx >= 0) {
            state.heroAmmo[localParams.ammoIdx] -= localParams.needAmmo;

            if (state.heroAmmo[localParams.ammoIdx] < localParams.needAmmo) {
                if (state.heroAmmo[localParams.ammoIdx] < 0) {
                    state.heroAmmo[localParams.ammoIdx] = 0;
                }

                weapons.selectBestWeapon();
            }
        }
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

    void resume() {
        if (isRewardedVideoWatched) {
            isRewardedVideoWatched = false;
            isGameOverFlag = false;
            killedTime = 0L;

            state.heroHealth = GameParams.HEALTH_MAX;
            state.heroArmor = Math.min(GameParams.ARMOR_MAX, state.heroArmor + GameParams.ARMOR_ADD_GREEN);

            if (state.heroHasWeapon[Weapons.WEAPON_PISTOL] || state.heroHasWeapon[Weapons.WEAPON_DBLPISTOL]) {
                state.heroAmmo[Weapons.AMMO_CLIP] = Math.min(GameParams.AMMO_MAX_CLIP,
                        state.heroAmmo[Weapons.AMMO_CLIP] + GameParams.AMMO_ADD_CBOX);
            }

            if (state.heroHasWeapon[Weapons.WEAPON_AK47] || state.heroHasWeapon[Weapons.WEAPON_TMP] || state.heroHasWeapon[Weapons.WEAPON_WINCHESTER]) {
                state.heroAmmo[Weapons.AMMO_SHELL] = Math.min(GameParams.AMMO_MAX_SHELL,
                        state.heroAmmo[Weapons.AMMO_SHELL] + GameParams.AMMO_ADD_SBOX);
            }
        }

        if (engine.gameViewActive && !TextUtils.isEmpty(unprocessedGameCode)) {
            processGameCode(unprocessedGameCode);
            unprocessedGameCode = "";
        }

        if (engine.gameViewActive) {
            state.showEpisodeSelector = false;
        }
    }

    public void update() {
        if (level.buildPathToWavePending) {
            level.buildPathToWavePending = false;
            level.buildPathToWaveMap();
            autoMap.updatePathTo();
        }

        if (renderMode == RENDER_MODE_END_LEVEL) {
            endLevel.update();
            return;
        } else if (renderMode == RENDER_MODE_GAME_OVER) {
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

        for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
            door.tryClose();
        }

        for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
            mon.update();
        }

        for (Bullet bullet = state.bullets.first(); bullet != null; ) {
            Bullet nextBullet = (Bullet)bullet.next;
            bullet.update();

            if (bullet.bulletState == Bullet.STATE_RELEASE) {
                state.bullets.release(bullet);
            }

            //noinspection AssignmentToForLoopParameter
            bullet = nextBullet;
        }

        for (Explosion explosion = state.explosions.first(); explosion != null; ) {
            Explosion nextExplosion = (Explosion)explosion.next;

            if (!explosion.update()) {
                state.explosions.release(explosion);
            }

            //noinspection AssignmentToForLoopParameter
            explosion = nextExplosion;
        }

        for (Timeout timeout = state.timeouts.first(); timeout != null; ) {
            Timeout nextTimeout = (Timeout)timeout.next;

            if (timeout.delay <= 0) {
                level.executeActions(timeout.markId);
                state.timeouts.release(timeout);
            } else {
                timeout.delay--;
            }

            //noinspection AssignmentToForLoopParameter
            timeout = nextTimeout;
        }

        for (LookPoint lookPoint = state.lookPoints.first(); lookPoint != null; ) {
            LookPoint nextLookPoint = (LookPoint)lookPoint.next;

            if (levelRenderer.awTouchedCellsMap[lookPoint.y][lookPoint.x]) {
                processOneMarkById(lookPoint.markId);
                state.lookPoints.release(lookPoint);
            }

            //noinspection AssignmentToForLoopParameter
            lookPoint = nextLookPoint;
        }

        if ((nextLevelTime > 0) || (killedTime > 0)) {
            if (weapons.shootCycle > 0) {
                weapons.shootCycle = (weapons.shootCycle + 1) % weapons.currentCycle.length;
            }

            return;
        }

        if (weapons.currentCycle[weapons.shootCycle] < 0) {
            processShoot();
        }

        if (weapons.shootCycle > 0) {
            weapons.shootCycle = (weapons.shootCycle + 1) % weapons.currentCycle.length;
        }

        if (actionNextWeapon && weapons.shootCycle == 0 && weapons.changeWeaponDir == 0) {
            weapons.nextWeapon();
            actionNextWeapon = false;
        }

        for (int i = 0, len = actionQuickWeapons.length; i < len; i++) {
            if (actionQuickWeapons[i]) {
                if (state.heroWeapon != state.lastWeapons[i]) {
                    weapons.switchWeapon(state.lastWeapons[i]);
                }

                actionQuickWeapons[i] = false;
            }
        }

        if (actionFire != 0 && weapons.shootCycle == 0 && weapons.changeWeaponDir == 0) {
            weapons.shootCycle++;
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

            processMarks();
            pickObjects();
            autoMap.updatePathTo();
        }
    }

    private boolean processOneMark(Mark mark) {
        return (mark.enabled && processOneMarkById(mark.id));
    }

    private boolean processOneMarkById(int markId) {
        int soundIdx = level.executeActions(markId);

        if (soundIdx >= 0) {
            // if this is *not* end level switch, play sound
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

        if ((state.passableMap[cy][cx] & Level.PASSABLE_IS_OBJECT_DONT_COUNT) == 0) {
            // count every object (even if user didn't pick it) to allow 100% items at the end of level
            state.pickedItems++;
        }

        state.passableMap[cy][cx] |= Level.PASSABLE_IS_OBJECT_DONT_COUNT;

        // decide shall we pick object or not

        switch (state.objectsMap[cy][cx]) {
            case TextureLoader.OBJ_ARMOR_GREEN:
            case TextureLoader.OBJ_ARMOR_RED:
                if (state.heroArmor >= maxArmor) {
                    return;
                }
                break;

            case TextureLoader.OBJ_STIM:
            case TextureLoader.OBJ_MEDI:
                if (state.heroHealth >= maxHealth) {
                    return;
                }
                break;

            case TextureLoader.OBJ_CLIP:
            case TextureLoader.OBJ_CBOX:
                if (state.heroAmmo[Weapons.AMMO_CLIP] >= maxAmmoClip) {
                    return;
                }
                break;

            case TextureLoader.OBJ_SHELL:
            case TextureLoader.OBJ_SBOX:
                if (state.heroAmmo[Weapons.AMMO_SHELL] >= maxAmmoShell) {
                    return;
                }
                break;

            case TextureLoader.OBJ_GRENADE:
            case TextureLoader.OBJ_GBOX:
                if (state.heroAmmo[Weapons.AMMO_GRENADE] >= maxAmmoGrenade) {
                    return;
                }
                break;

            case TextureLoader.OBJ_BPACK:
                if (state.heroHealth >= maxHealth
                        && state.heroAmmo[Weapons.AMMO_CLIP] >= maxAmmoClip
                        && state.heroAmmo[Weapons.AMMO_SHELL] >= maxAmmoShell) {

                    return;
                }
                break;

            case TextureLoader.OBJ_DBLPIST:
                if (shouldNotPickWeapon(Weapons.WEAPON_DBLPISTOL)) {
                    return;
                }
                break;

            case TextureLoader.OBJ_AK47:
                if (shouldNotPickWeapon(Weapons.WEAPON_AK47)) {
                    return;
                }
                break;

            case TextureLoader.OBJ_TMP:
                if (shouldNotPickWeapon(Weapons.WEAPON_TMP)) {
                    return;
                }
                break;

            case TextureLoader.OBJ_WINCHESTER:
                if (shouldNotPickWeapon(Weapons.WEAPON_WINCHESTER)) {
                    return;
                }
                break;
        }

        // play sounds

        switch (state.objectsMap[cy][cx]) {
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

        // add healh/armor/wepons/bullets

        int bestWeapon = (state.autoSelectWeapon ? -1 : weapons.getBestWeapon());

        switch (state.objectsMap[cy][cx]) {
            case TextureLoader.OBJ_ARMOR_GREEN:
                state.heroArmor = Math.min(state.heroArmor + GameParams.ARMOR_ADD_GREEN, maxArmor);
                break;

            case TextureLoader.OBJ_ARMOR_RED:
                state.heroArmor = Math.min(state.heroArmor + GameParams.ARMOR_ADD_RED, maxArmor);
                break;

            case TextureLoader.OBJ_KEY_BLUE:
                state.heroKeysMask |= 1;
                break;

            case TextureLoader.OBJ_KEY_RED:
                state.heroKeysMask |= 2;
                break;

            case TextureLoader.OBJ_KEY_GREEN:
                state.heroKeysMask |= 4;
                break;

            case TextureLoader.OBJ_OPENMAP:
                autoMap.openAllMap();
                break;

            case TextureLoader.OBJ_STIM:
                state.heroHealth = Math.min(state.heroHealth + GameParams.HEALTH_ADD_STIM, maxHealth);
                break;

            case TextureLoader.OBJ_MEDI:
                state.heroHealth = Math.min(state.heroHealth + GameParams.HEALTH_ADD_MEDI, maxHealth);
                break;

            case TextureLoader.OBJ_CLIP:
                pickAmmo(Weapons.AMMO_CLIP, GameParams.AMMO_ADD_CLIP, bestWeapon);
                break;

            case TextureLoader.OBJ_CBOX:
                pickAmmo(Weapons.AMMO_CLIP, GameParams.AMMO_ADD_CBOX, bestWeapon);
                break;

            case TextureLoader.OBJ_SHELL:
                pickAmmo(Weapons.AMMO_SHELL, GameParams.AMMO_ADD_SHELL, bestWeapon);
                break;

            case TextureLoader.OBJ_SBOX:
                pickAmmo(Weapons.AMMO_SHELL, GameParams.AMMO_ADD_SBOX, bestWeapon);
                break;

            case TextureLoader.OBJ_GRENADE:
                pickAmmo(Weapons.AMMO_GRENADE, GameParams.AMMO_ADD_GRENADE, bestWeapon);
                break;

            case TextureLoader.OBJ_GBOX:
                pickAmmo(Weapons.AMMO_GRENADE, GameParams.AMMO_ADD_GBOX, bestWeapon);
                break;

            case TextureLoader.OBJ_BPACK:
                state.heroHealth = Math.min(state.heroHealth + GameParams.HEALTH_ADD_STIM, maxHealth);

                state.heroAmmo[Weapons.AMMO_CLIP] = Math.min(state.heroAmmo[Weapons.AMMO_CLIP]
                        + GameParams.AMMO_ADD_CLIP, maxAmmoClip);

                state.heroAmmo[Weapons.AMMO_SHELL] = Math.min(state.heroAmmo[Weapons.AMMO_SHELL]
                        + GameParams.AMMO_ADD_SHELL, maxAmmoShell);

                // do not check if AK47 existing (than, if it didn't exists, pistol or double pistol will be selected)
                // why AK47? because it is the first weapon which use AMMO_SHELL
                if (bestWeapon < Weapons.WEAPON_AK47) {
                    weapons.selectBestWeapon();
                    state.autoSelectWeapon = true;
                }
                break;

            case TextureLoader.OBJ_DBLPIST:
                pickWeapon(Weapons.WEAPON_DBLPISTOL, GameParams.AMMO_ADD_DBLPIST, bestWeapon);
                break;

            case TextureLoader.OBJ_AK47:
                pickWeapon(Weapons.WEAPON_AK47, GameParams.AMMO_ADD_AK47, bestWeapon);
                break;

            case TextureLoader.OBJ_TMP:
                pickWeapon(Weapons.WEAPON_TMP, GameParams.AMMO_ADD_TMP, bestWeapon);
                break;

            case TextureLoader.OBJ_WINCHESTER:
                pickWeapon(Weapons.WEAPON_WINCHESTER, GameParams.AMMO_ADD_WINCHESTER, bestWeapon);
                break;
        }

        state.levelExp += GameParams.EXP_PICK_OBJECT;

        // remove picked objects from map
        state.objectsMap[cy][cx] = 0;
        state.passableMap[cy][cx] &= ~Level.PASSABLE_MASK_OBJECT;
        levelRenderer.modLightMap(cx, cy, -LevelRenderer.LIGHT_OBJECT);

        overlay.showOverlay(Overlay.ITEM);
    }

    private boolean shouldNotPickWeapon(int weapon) {
        int ammoIdx = Weapons.WEAPONS[weapon].ammoIdx;

        int maxAmmo = (ammoIdx == Weapons.AMMO_CLIP
                ? maxAmmoClip
                : (ammoIdx == Weapons.AMMO_SHELL ? maxAmmoShell : maxAmmoGrenade));

        return (state.heroHasWeapon[weapon] && state.heroAmmo[ammoIdx] >= maxAmmo);
    }

    private void pickAmmo(int ammoIdx, int ammoAdd, int bestWeapon) {
        int maxAmmo = (ammoIdx == Weapons.AMMO_CLIP
                ? maxAmmoClip
                : (ammoIdx == Weapons.AMMO_SHELL ? maxAmmoShell : maxAmmoGrenade));

        int weapon = (ammoIdx == Weapons.AMMO_CLIP
                ? Weapons.WEAPON_PISTOL
                : (ammoIdx == Weapons.AMMO_SHELL ? Weapons.WEAPON_TMP : Weapons.WEAPON_GRENADE));

        if (ammoIdx == Weapons.AMMO_GRENADE) {
            state.heroHasWeapon[Weapons.WEAPON_GRENADE] = true;
        }

        state.heroAmmo[ammoIdx] = Math.min(state.heroAmmo[ammoIdx] + ammoAdd, maxAmmo);

        if (state.heroHasWeapon[weapon] && bestWeapon < weapon) {
            weapons.selectBestWeapon();
            state.autoSelectWeapon = true;
        }
    }

    private void pickWeapon(int weapon, int ammoAdd, int bestWeapon) {
        int ammoIdx = Weapons.WEAPONS[weapon].ammoIdx;

        int maxAmmo = (ammoIdx == Weapons.AMMO_CLIP
                ? maxAmmoClip
                : (ammoIdx == Weapons.AMMO_SHELL ? maxAmmoShell : maxAmmoGrenade));

        state.heroHasWeapon[weapon] = true;
        state.heroAmmo[ammoIdx] = Math.min(state.heroAmmo[ammoIdx] + ammoAdd, maxAmmo);

        if (bestWeapon < weapon) {
            weapons.selectBestWeapon();
            state.autoSelectWeapon = true;
        }
    }

    @SuppressWarnings("MagicNumber")
    private void drawCrosshair(GL10 gl) {
        renderer.initOrtho(gl, true, false, -engine.ratio, engine.ratio, -1.0f, 1.0f, 0.0f, 1.0f);
        renderer.init();

        renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 0.5f);

        renderer.setQuadOrthoCoords(-CROSSHAIR_SIZE, 0.03f, CROSSHAIR_SIZE, 0.08f); // up
        renderer.drawQuad();

        renderer.setQuadOrthoCoords(CROSSHAIR_SIZE, -0.03f, -CROSSHAIR_SIZE, -0.08f); // down
        renderer.drawQuad();

        renderer.setQuadOrthoCoords(0.03f, -CROSSHAIR_SIZE, 0.08f, CROSSHAIR_SIZE); // right
        renderer.drawQuad();

        renderer.setQuadOrthoCoords(-0.03f, CROSSHAIR_SIZE, -0.08f, -CROSSHAIR_SIZE); // left
        renderer.drawQuad();

        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        renderer.flush(gl, false);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
    }

    @SuppressWarnings("MagicNumber")
    private void drawSky(GL10 gl) {
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_BLEND);
        gl.glDisable(GL10.GL_ALPHA_TEST);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_FLAT);

        renderer.initOrtho(gl, true, false, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);

        if (GameParams.COLOR_SKY > 0) {
            float r = (float)((GameParams.COLOR_SKY >> 16) & 0xff) / 255.0f;
            float g = (float)((GameParams.COLOR_SKY >> 8) & 0xff) / 255.0f;
            float b = (float)(GameParams.COLOR_SKY & 0xff) / 255.0f;

            renderer.init();
            renderer.setQuadRGBA(r, g, b, 1.0f);
            renderer.setQuadOrthoCoords(0.0f, 1.0f, 1.0f, 0.0f);
            renderer.drawQuad();
            renderer.flush(gl, false);
        }

        float ox = (float)Math.sin((state.heroA % 30.0f) * GameMath.G2RAD_F);
        float oy = (float)Math.sin(state.heroVertA * GameMath.G2RAD_F);

        renderer.init();
        renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
        renderer.setQuadTexCoords(0, 0, 4 << 16, 1 << 16);
        renderer.setQuadOrthoCoords(ox - 1.0f, 1.1f - oy, ox + 1.0f, 0.15f - oy);
        renderer.drawQuad();
        renderer.bindTextureRep(gl, engine.textureLoader.textures[TextureLoader.TEXTURE_SKY]);
        renderer.flush(gl);

        if (GameParams.COLOR_SKY > 0) {
            float r = (float)((GameParams.COLOR_SKY >> 16) & 0xff) / 255.0f;
            float g = (float)((GameParams.COLOR_SKY >> 8) & 0xff) / 255.0f;
            float b = (float)(GameParams.COLOR_SKY & 0xff) / 255.0f;

            renderer.init();
            renderer.setQuadRGBA(r, g, b, 1.0f);
            renderer.setQuadOrthoCoords(ox - 1.0f, 1.09f - oy, ox + 1.0f, 1.11f - oy);
            renderer.drawQuad();
            renderer.flush(gl, false);
        }

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glEnable(GL10.GL_CULL_FACE);
    }

    @SuppressWarnings("MagicNumber")
    protected void render(GL10 gl) {
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

        float yoff = LevelRenderer.HALF_WALL / 8.0f
                + (float)Math.sin((float)walkTime / 100.0f) * LevelRenderer.HALF_WALL / 16.0f;

        float xrot = state.heroVertA;

        if (killedTime > 0) {
            float killedRatio = Math.min(1.0f, (float)(engine.elapsedTime - killedTime) / 1000.0f);

            yoff += killedRatio * (LevelRenderer.HALF_WALL * 0.5f - yoff);
            xrot += killedRatio * (-45.0f - xrot);
            state.setHeroA(killedHeroAngle + (killedAngle - killedHeroAngle) * killedRatio);
        }

        drawSky(gl);
        levelRenderer.render(gl, engine.elapsedTime, -yoff, -90.0f - xrot);

        if (config.showCrosshair && !engine.inWallpaperMode) {
            drawCrosshair(gl);
        }

        weapons.render(gl, walkTime);
        autoMap.render(gl);

        overlay.renderOverlay(gl);
        overlay.renderHitSide(gl);

        if (!engine.inWallpaperMode) {
            engine.stats.render(gl);

            if (renderMode == RENDER_MODE_GAME) {
                heroController.renderControls(gl,
                        true,
                        (firstTouchTime >= 0 ? firstTouchTime : engine.elapsedTime));
            }
        }

        if (nextLevelTime > 0) {
            overlay.renderEndLevelLayer(gl, (float)(engine.elapsedTime - nextLevelTime) / 500.0f);
        }

        if (renderMode == RENDER_MODE_END_LEVEL) {
            endLevel.render(gl);
            heroController.renderControls(gl, false, 0L);
        } else if (renderMode == RENDER_MODE_GAME_OVER) {
            gameOver.render(gl);
            heroController.renderControls(gl, false, 0L);
        }

        overlay.renderLabels(gl);

        if (config.gamma > 0.01f) {
            overlay.renderGammaLayer(gl);
        }

        if (engine.showFps) {
            engine.drawFps(gl);
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
