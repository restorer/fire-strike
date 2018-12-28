package zame.game.engine.state;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import zame.game.App;
import zame.game.R;
import zame.game.core.serializer.DataList;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;
import zame.game.core.util.Common;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.entity.Action;
import zame.game.engine.entity.AutoWall;
import zame.game.engine.entity.Bullet;
import zame.game.engine.entity.Door;
import zame.game.engine.entity.Explosion;
import zame.game.engine.entity.LookPoint;
import zame.game.engine.entity.Mark;
import zame.game.engine.entity.Monster;
import zame.game.engine.entity.ObjectContainer;
import zame.game.engine.entity.OnChangeWeaponAction;
import zame.game.engine.entity.Timeout;
import zame.game.engine.entity.TouchedCell;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.level.Level;
import zame.game.engine.level.LevelConfig;
import zame.game.engine.level.LevelRenderer;
import zame.game.engine.util.GameMath;
import zame.game.engine.visual.Weapons;
import zame.game.feature.achievements.Achievements;
import zame.game.flavour.config.GameConfig;

public class State extends BaseState implements EngineObject {
    public static final String LEVEL_INITIAL = "e00m00";

    private static final int FIELD_BUILD = 1;
    private static final int FIELD_LEVEL_NAME = 2;
    private static final int FIELD_HERO_X = 3;
    private static final int FIELD_HERO_Y = 4;
    private static final int FIELD_HERO_A = 5;
    private static final int FIELD_HERO_KEYS_MASK = 6;
    private static final int FIELD_HERO_WEAPON = 7;
    private static final int FIELD_HERO_HEALTH = 8;
    private static final int FIELD_HERO_ARMOR = 9;
    private static final int FIELD_HERO_HAS_WEAPON = 10;
    private static final int FIELD_HERO_AMMO = 11;
    private static final int FIELD_OVERALL_DEATHS = 12;
    private static final int FIELD_TOTAL_MONSTERS = 13;
    private static final int FIELD_TOTAL_SECRETS = 14;
    private static final int FIELD_OVERALL_RESURRECTS = 15;
    private static final int FIELD_KILLED_MONSTERS = 16;
    private static final int FIELD_FOUND_SECRETS = 17;
    private static final int FIELD_FOUND_SECRETS_MASK = 18;
    private static final int FIELD_LEVEL_WIDTH = 19;
    private static final int FIELD_LEVEL_HEIGHT = 20;
    private static final int FIELD_WALLS_MAP = 21;
    private static final int FIELD_TEX_MAP = 22;
    private static final int FIELD_OBJECTS_MAP = 23;
    private static final int FIELD_PASSABLE_MAP = 24;
    private static final int FIELD_DOORS = 25;
    private static final int FIELD_MONSTERS = 26;
    private static final int FIELD_MARKS = 27;
    private static final int FIELD_ACTIONS = 28;
    private static final int FIELD_DRAWN_AUTO_WALLS = 29;
    private static final int FIELD_AUTO_WALLS = 30;
    private static final int FIELD_TEMP_ELAPSED_TIME = 31;
    private static final int FIELD_TEMP_LAST_TIME = 32;
    private static final int FIELD_GOD_MODE = 33;
    private static final int FIELD_SHOW_EPISODE_SELECTOR = 34;
    private static final int FIELD_HIGHLIGHTED_CONTROL_TYPE_MASK = 35;
    private static final int FIELD_SHOWN_MESSAGE_ID = 36;
    private static final int FIELD_HERO_VERT_A = 37;
    // private static final int FIELD_AUTO_SELECT_WEAPON = 38;
    private static final int FIELD_AW_TOUCHED_CELLS = 39;
    private static final int FIELD_TIME_IN_TICKS = 40;
    // private static final int FIELD_OVERALL_ITEMS = 41;
    private static final int FIELD_OVERALL_MONSTERS = 42;
    private static final int FIELD_OVERALL_SECRETS = 43;
    private static final int FIELD_OVERALL_SECONDS = 44;
    private static final int FIELD_WP_PATH_IDX = 45;
    private static final int FIELD_BULLETS = 46;
    private static final int FIELD_EXPLOSIONS = 47;
    private static final int FIELD_FLOOR_MAP_1 = 48;
    private static final int FIELD_CEIL_MAP_1 = 49;
    private static final int FIELD_MUST_RELOAD = 50;
    private static final int FIELD_MUST_LOAD_AUTOSAVE = 51;
    private static final int FIELD_LEVEL_EXP = 52;
    private static final int FIELD_FLOOR_MAP_2 = 53;
    private static final int FIELD_FLOOR_MAP_3 = 54;
    private static final int FIELD_FLOOR_MAP_4 = 55;
    private static final int FIELD_CEIL_MAP_2 = 56;
    private static final int FIELD_CEIL_MAP_3 = 57;
    private static final int FIELD_CEIL_MAP_4 = 58;
    private static final int FIELD_ARROWS_MAP = 59;
    private static final int FIELD_SAVED_OR_NEW = 60;
    private static final int FIELD_DISABLED_CONTROLS_MASK = 61;
    private static final int FIELD_STATS = 62;
    private static final int FIELD_EXPLOSIVES_MAP = 63;
    private static final int FIELD_TIMEOUTS = 64;
    private static final int FIELD_CONTROLS_HELP_MASK = 65;
    private static final int FIELD_LOOK_POINTS = 66;
    private static final int FIELD_LAST_WEAPONS = 67;
    private static final int FIELD_LAST_WEAPON_IDX = 68;
    private static final int FIELD_ON_CHANGE_WEAPON_ACTIONS = 69;
    private static final int FIELD_PATH_TO_X = 70;
    private static final int FIELD_PATH_TO_Y = 71;
    private static final int FIELD_CHEATS_DISABLED = 72;

    private Engine engine;

    public String levelName = "";
    public boolean showEpisodeSelector;
    public float heroA;
    public int heroWeapon;
    public int[] lastWeapons = { -1, -1, -1 };
    public int lastWeaponIdx;

    public int overallMonsters;
    public int overallSecrets;
    public int overallSeconds;
    public int overallDeaths;
    public int overallResurrects;
    public int heroKeysMask;
    public int heroHealth;
    public int heroArmor;
    public int[] heroAmmo = new int[Weapons.AMMO_LAST];

    public boolean mustReload;
    public boolean mustLoadAutosave;
    public float heroX;
    public float heroY;
    public boolean[] heroHasWeapon = new boolean[Weapons.WEAPON_LAST];

    public int totalMonsters;
    public int totalSecrets;
    public int killedMonsters;
    public int foundSecrets;
    public int foundSecretsMask;
    public int timeInTicks; // divide by 40 to get seconds
    public int levelExp;

    private boolean savedOrNew;

    public int levelWidth;
    public int levelHeight;

    public int[][] wallsMap;
    public int[][] texMap;
    public List<List<ObjectContainer>> objectsMap = new ArrayList<>();
    public int[][] passableMap;
    public int[][] floorMap1;
    public int[][] floorMap2;
    public int[][] floorMap3;
    public int[][] floorMap4;
    public int[][] ceilMap1;
    public int[][] ceilMap2;
    public int[][] ceilMap3;
    public int[][] ceilMap4;
    public int[][] arrowsMap;
    public int[][] explosivesMap;

    public DataList<Door> doors = new DataList<>(Door.class, Level.MAX_DOORS);
    public DataList<Monster> monsters = new DataList<>(Monster.class, Level.MAX_MONSTERS);

    public DataList<Mark> marks = new DataList<>(Mark.class, Level.MAX_MARKS);
    public DataList<Timeout> timeouts = new DataList<>(Timeout.class, Level.MAX_TIMEOUTS);
    public DataList<LookPoint> lookPoints = new DataList<>(LookPoint.class, Level.MAX_LOOK_POINTS);

    public DataList<OnChangeWeaponAction> onChangeWeaponActions = new DataList<>(OnChangeWeaponAction.class,
            Level.MAX_ON_CHANGE_WEAPON_ACTIONS);

    public List<List<Action>> actions = new ArrayList<>();

    public int[][] drawnAutoWalls;
    public DataList<AutoWall> autoWalls = new DataList<>(AutoWall.class, LevelRenderer.MAX_AUTO_WALLS);
    public DataList<TouchedCell> awTouchedCells = new DataList<>(TouchedCell.class, LevelRenderer.MAX_AW_CELLS);

    public long tempElapsedTime;
    public long tempLastTime;
    public boolean godMode;
    public int shownMessageId;
    public int disabledControlsMask;
    public int controlsHelpMask;
    public boolean cheatsDisabled;

    private int highlightedControlTypeMask;
    private int wpPathIdx;

    public float heroVertA;

    public DataList<Bullet> bullets = new DataList<>(Bullet.class, Level.MAX_BULLETS);
    public DataList<Explosion> explosions = new DataList<>(Explosion.class, Level.MAX_EXPLOSIONS);

    public int[] stats = new int[Achievements.STAT_LAST];

    public int pathToX;
    public int pathToY;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;

        for (int i = 0; i < doors.buffer.length; i++) {
            doors.buffer[i].onCreate(engine);
        }

        for (int i = 0; i < monsters.buffer.length; i++) {
            monsters.buffer[i].onCreate(engine);
        }

        for (int i = 0; i < bullets.buffer.length; i++) {
            bullets.buffer[i].onCreate(engine);
        }

        for (int i = 0; i < explosions.buffer.length; i++) {
            explosions.buffer[i].onCreate(engine);
        }
    }

    public void setHeroA(float angle) {
        //noinspection MagicNumber
        heroA = (360.0f + (angle % 360.0f)) % 360.0f;

        engine.heroAr = heroA * GameMath.G2RAD_F;
        engine.heroCs = (float)Math.cos(engine.heroAr);
        engine.heroSn = (float)Math.sin(engine.heroAr);
    }

    @SuppressWarnings("MagicNumber")
    public void setHeroVertA(float angle) {
        if (angle < -15.0f) {
            angle = -15.0f;
        } else if (angle > 15.0f) {
            angle = 15.0f;
        }

        heroVertA = angle;
    }

    public void reload() {
        levelName = LEVEL_INITIAL;
        showEpisodeSelector = true;
        mustReload = false;
        mustLoadAutosave = false;
        savedOrNew = true;
        levelWidth = 1;
        levelHeight = 1;
        heroWeapon = Weapons.WEAPON_KNIFE;
        heroHealth = GameConfig.HEALTH_MAX;
        heroArmor = 0;
        godMode = false;
        overallMonsters = 0;
        overallSecrets = 0;
        overallSeconds = 0;
        overallDeaths = 0;
        overallResurrects = 0;
        lastWeaponIdx = 0;

        for (int i = 0; i < Weapons.WEAPON_LAST; i++) {
            heroHasWeapon[i] = false;
        }

        for (int i = 0; i < Weapons.AMMO_LAST; i++) {
            heroAmmo[i] = 0;
        }

        for (int i = 0; i < Achievements.STAT_LAST; i++) {
            stats[i] = 0;
        }

        heroHasWeapon[Weapons.WEAPON_KNIFE] = true;

        // heroHasWeapon[Weapons.WEAPON_PISTOL] = true;
        // heroAmmo[Weapons.AMMO_CLIP] = GameParams.AMMO_ENSURED_CLIP;

        for (int i = 0, len = lastWeapons.length; i < len; i++) {
            lastWeapons[i] = -1;
        }

        lastWeapons[0] = heroWeapon;
        setStartValues();
    }

    public void setStartValues() {
        heroKeysMask = 0;
        totalMonsters = 0;
        totalSecrets = 0;
        killedMonsters = 0;
        foundSecrets = 0;
        foundSecretsMask = 0;
        levelExp = 0;
        highlightedControlTypeMask = 0;
        shownMessageId = -1;
        disabledControlsMask = 0;
        controlsHelpMask = 0;
        cheatsDisabled = false;
        heroVertA = 0.0f;
        timeInTicks = 0;
        wpPathIdx = 0;
        pathToX = -1;
        pathToY = -1;

        engine.level.requestBuildPathToWave();

        objectsMap.clear();
        doors.clear();
        monsters.clear();
        marks.clear();
        timeouts.clear();
        lookPoints.clear();
        onChangeWeaponActions.clear();
        actions.clear();
        autoWalls.clear();
        awTouchedCells.clear();
        bullets.clear();
        explosions.clear();

        for (int i = 0; i < levelHeight; i++) {
            List<ObjectContainer> line = new ArrayList<>();
            objectsMap.add(line);

            for (int j = 0; j < levelWidth; j++) {
                line.add(new ObjectContainer());
            }
        }

        for (int i = 0; i < Level.MAX_ACTIONS; i++) {
            actions.add(new ArrayList<Action>());
        }

        drawnAutoWalls = new int[levelHeight][levelWidth];

        for (int i = 0; i < levelHeight; i++) {
            for (int j = 0; j < levelWidth; j++) {
                drawnAutoWalls[i][j] = 0;
            }
        }
    }

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        for (int i = 0; i < levelHeight; i++) {
            for (int j = 0; j < levelWidth; j++) {
                wallsMap[i][j] = TextureLoader.packTexId(wallsMap[i][j]);
                texMap[i][j] = TextureLoader.packTexId(texMap[i][j]);

                for (int k = 0; k < ObjectContainer.MAX_OBJECTS; k++) {
                    objectsMap.get(i).get(j).set(k, TextureLoader.packTexId(objectsMap.get(i).get(j).get(k)));
                }

                arrowsMap[i][j] = TextureLoader.packTexId(arrowsMap[i][j]);

                floorMap1[i][j] = TextureLoader.packTexId(floorMap1[i][j]);
                floorMap2[i][j] = TextureLoader.packTexId(floorMap2[i][j]);
                floorMap3[i][j] = TextureLoader.packTexId(floorMap3[i][j]);
                floorMap4[i][j] = TextureLoader.packTexId(floorMap4[i][j]);

                ceilMap1[i][j] = TextureLoader.packTexId(ceilMap1[i][j]);
                ceilMap2[i][j] = TextureLoader.packTexId(ceilMap2[i][j]);
                ceilMap3[i][j] = TextureLoader.packTexId(ceilMap3[i][j]);
                ceilMap4[i][j] = TextureLoader.packTexId(ceilMap4[i][j]);
            }
        }

        writer.write(FIELD_BUILD, App.self.getVersionName());
        writer.write(FIELD_LEVEL_NAME, levelName);
        writer.write(FIELD_HERO_X, heroX);
        writer.write(FIELD_HERO_Y, heroY);
        writer.write(FIELD_HERO_A, heroA);
        writer.write(FIELD_HERO_KEYS_MASK, heroKeysMask);
        writer.write(FIELD_HERO_WEAPON, heroWeapon);
        writer.write(FIELD_HERO_HEALTH, heroHealth);
        writer.write(FIELD_HERO_ARMOR, heroArmor);
        writer.write(FIELD_HERO_HAS_WEAPON, heroHasWeapon);
        writer.write(FIELD_HERO_AMMO, heroAmmo);
        writer.write(FIELD_TOTAL_MONSTERS, totalMonsters);
        writer.write(FIELD_TOTAL_SECRETS, totalSecrets);
        writer.write(FIELD_KILLED_MONSTERS, killedMonsters);
        writer.write(FIELD_FOUND_SECRETS, foundSecrets);
        writer.write(FIELD_FOUND_SECRETS_MASK, foundSecretsMask);
        writer.write(FIELD_LEVEL_WIDTH, levelWidth);
        writer.write(FIELD_LEVEL_HEIGHT, levelHeight);
        writer.write(FIELD_WALLS_MAP, wallsMap);
        writer.write(FIELD_TEX_MAP, texMap);
        writer.writeList2d(FIELD_OBJECTS_MAP, objectsMap);
        writer.write(FIELD_PASSABLE_MAP, passableMap);
        writer.write(FIELD_DOORS, doors);
        writer.write(FIELD_MONSTERS, monsters);
        writer.write(FIELD_MARKS, marks);
        writer.writeList2d(FIELD_ACTIONS, actions);
        writer.write(FIELD_DRAWN_AUTO_WALLS, drawnAutoWalls);
        writer.write(FIELD_AUTO_WALLS, autoWalls);
        writer.write(FIELD_TEMP_ELAPSED_TIME, tempElapsedTime);
        writer.write(FIELD_TEMP_LAST_TIME, tempLastTime);
        writer.write(FIELD_GOD_MODE, godMode);
        writer.write(FIELD_SHOW_EPISODE_SELECTOR, showEpisodeSelector);
        writer.write(FIELD_HIGHLIGHTED_CONTROL_TYPE_MASK, highlightedControlTypeMask);
        writer.write(FIELD_SHOWN_MESSAGE_ID, shownMessageId);
        writer.write(FIELD_HERO_VERT_A, heroVertA);
        writer.write(FIELD_AW_TOUCHED_CELLS, awTouchedCells);
        writer.write(FIELD_TIME_IN_TICKS, timeInTicks);
        writer.write(FIELD_OVERALL_MONSTERS, overallMonsters);
        writer.write(FIELD_OVERALL_SECRETS, overallSecrets);
        writer.write(FIELD_OVERALL_SECONDS, overallSeconds);
        writer.write(FIELD_OVERALL_DEATHS, overallDeaths);
        writer.write(FIELD_OVERALL_RESURRECTS, overallResurrects);
        writer.write(FIELD_WP_PATH_IDX, wpPathIdx);
        writer.write(FIELD_BULLETS, bullets);
        writer.write(FIELD_EXPLOSIONS, explosions);
        writer.write(FIELD_FLOOR_MAP_1, floorMap1);
        writer.write(FIELD_FLOOR_MAP_2, floorMap2);
        writer.write(FIELD_FLOOR_MAP_3, floorMap3);
        writer.write(FIELD_FLOOR_MAP_4, floorMap4);
        writer.write(FIELD_CEIL_MAP_1, ceilMap1);
        writer.write(FIELD_CEIL_MAP_2, ceilMap2);
        writer.write(FIELD_CEIL_MAP_3, ceilMap3);
        writer.write(FIELD_CEIL_MAP_4, ceilMap4);
        writer.write(FIELD_ARROWS_MAP, arrowsMap);
        writer.write(FIELD_MUST_RELOAD, mustReload);
        writer.write(FIELD_MUST_LOAD_AUTOSAVE, mustLoadAutosave);
        writer.write(FIELD_LEVEL_EXP, levelExp);
        writer.write(FIELD_SAVED_OR_NEW, savedOrNew);
        writer.write(FIELD_DISABLED_CONTROLS_MASK, disabledControlsMask);
        writer.write(FIELD_STATS, stats);
        writer.write(FIELD_EXPLOSIVES_MAP, explosivesMap);
        writer.write(FIELD_TIMEOUTS, timeouts);
        writer.write(FIELD_CONTROLS_HELP_MASK, controlsHelpMask);
        writer.write(FIELD_LOOK_POINTS, lookPoints);
        writer.write(FIELD_ON_CHANGE_WEAPON_ACTIONS, onChangeWeaponActions);
        writer.write(FIELD_LAST_WEAPONS, lastWeapons);
        writer.write(FIELD_LAST_WEAPON_IDX, lastWeaponIdx);
        writer.write(FIELD_PATH_TO_X, pathToX);
        writer.write(FIELD_PATH_TO_Y, pathToY);
        writer.write(FIELD_CHEATS_DISABLED, cheatsDisabled);

        for (int i = 0; i < levelHeight; i++) {
            for (int j = 0; j < levelWidth; j++) {
                wallsMap[i][j] = TextureLoader.unpackTexId(wallsMap[i][j]);
                texMap[i][j] = TextureLoader.unpackTexId(texMap[i][j]);

                for (int k = 0; k < ObjectContainer.MAX_OBJECTS; k++) {
                    objectsMap.get(i).get(j).set(k, TextureLoader.unpackTexId(objectsMap.get(i).get(j).get(k)));
                }

                arrowsMap[i][j] = TextureLoader.unpackTexId(arrowsMap[i][j]);

                floorMap1[i][j] = TextureLoader.unpackTexId(floorMap1[i][j]);
                floorMap2[i][j] = TextureLoader.unpackTexId(floorMap2[i][j]);
                floorMap3[i][j] = TextureLoader.unpackTexId(floorMap3[i][j]);
                floorMap4[i][j] = TextureLoader.unpackTexId(floorMap4[i][j]);

                ceilMap1[i][j] = TextureLoader.unpackTexId(ceilMap1[i][j]);
                ceilMap2[i][j] = TextureLoader.unpackTexId(ceilMap2[i][j]);
                ceilMap3[i][j] = TextureLoader.unpackTexId(ceilMap3[i][j]);
                ceilMap4[i][j] = TextureLoader.unpackTexId(ceilMap4[i][j]);
            }
        }
    }

    @Override
    public void readFrom(DataReader reader) {
        levelName = reader.readString(FIELD_LEVEL_NAME, LEVEL_INITIAL);
        levelWidth = reader.readInt(FIELD_LEVEL_WIDTH);
        levelHeight = reader.readInt(FIELD_LEVEL_HEIGHT);
        heroX = reader.readFloat(FIELD_HERO_X);
        heroY = reader.readFloat(FIELD_HERO_Y);
        setHeroA(reader.readFloat(FIELD_HERO_A));
        heroKeysMask = reader.readInt(FIELD_HERO_KEYS_MASK);
        heroWeapon = reader.readInt(FIELD_HERO_WEAPON);
        heroHealth = reader.readInt(FIELD_HERO_HEALTH);
        heroArmor = reader.readInt(FIELD_HERO_ARMOR);
        heroHasWeapon = reader.readBooleanArray(FIELD_HERO_HAS_WEAPON, Weapons.WEAPON_LAST);
        heroAmmo = reader.readIntArray(FIELD_HERO_AMMO, Weapons.AMMO_LAST);
        totalMonsters = reader.readInt(FIELD_TOTAL_MONSTERS);
        totalSecrets = reader.readInt(FIELD_TOTAL_SECRETS);
        killedMonsters = reader.readInt(FIELD_KILLED_MONSTERS);
        foundSecrets = reader.readInt(FIELD_FOUND_SECRETS);
        foundSecretsMask = reader.readInt(FIELD_FOUND_SECRETS_MASK);
        wallsMap = reader.readInt2dArray(FIELD_WALLS_MAP, levelHeight, levelWidth);
        texMap = reader.readInt2dArray(FIELD_TEX_MAP, levelHeight, levelWidth);
        reader.readList2d(FIELD_OBJECTS_MAP, objectsMap, ObjectContainer.class);
        passableMap = reader.readInt2dArray(FIELD_PASSABLE_MAP, levelHeight, levelWidth);
        reader.readDataList(FIELD_DOORS, doors);
        reader.readDataList(FIELD_MONSTERS, monsters);
        reader.readDataList(FIELD_MARKS, marks);
        reader.readList2d(FIELD_ACTIONS, actions, Action.class);
        drawnAutoWalls = reader.readInt2dArray(FIELD_DRAWN_AUTO_WALLS, levelHeight, levelWidth);
        reader.readDataList(FIELD_AUTO_WALLS, autoWalls);
        tempElapsedTime = reader.readLong(FIELD_TEMP_ELAPSED_TIME);
        tempLastTime = reader.readLong(FIELD_TEMP_LAST_TIME);
        godMode = reader.readBoolean(FIELD_GOD_MODE);
        showEpisodeSelector = reader.readBoolean(FIELD_SHOW_EPISODE_SELECTOR, true);
        highlightedControlTypeMask = reader.readInt(FIELD_HIGHLIGHTED_CONTROL_TYPE_MASK);
        shownMessageId = reader.readInt(FIELD_SHOWN_MESSAGE_ID);
        heroVertA = reader.readFloat(FIELD_HERO_VERT_A);
        reader.readDataList(FIELD_AW_TOUCHED_CELLS, awTouchedCells);
        timeInTicks = reader.readInt(FIELD_TIME_IN_TICKS);
        overallMonsters = reader.readInt(FIELD_OVERALL_MONSTERS);
        overallSecrets = reader.readInt(FIELD_OVERALL_SECRETS);
        overallSeconds = reader.readInt(FIELD_OVERALL_SECONDS);
        overallDeaths = reader.readInt(FIELD_OVERALL_DEATHS);
        overallResurrects = reader.readInt(FIELD_OVERALL_RESURRECTS);
        wpPathIdx = reader.readInt(FIELD_WP_PATH_IDX);
        reader.readDataList(FIELD_BULLETS, bullets);
        reader.readDataList(FIELD_EXPLOSIONS, explosions);
        floorMap1 = reader.readInt2dArray(FIELD_FLOOR_MAP_1, levelHeight, levelWidth);
        floorMap2 = reader.readInt2dArray(FIELD_FLOOR_MAP_2, levelHeight, levelWidth);
        floorMap3 = reader.readInt2dArray(FIELD_FLOOR_MAP_3, levelHeight, levelWidth);
        floorMap4 = reader.readInt2dArray(FIELD_FLOOR_MAP_4, levelHeight, levelWidth);
        ceilMap1 = reader.readInt2dArray(FIELD_CEIL_MAP_1, levelHeight, levelWidth);
        ceilMap2 = reader.readInt2dArray(FIELD_CEIL_MAP_2, levelHeight, levelWidth);
        ceilMap3 = reader.readInt2dArray(FIELD_CEIL_MAP_3, levelHeight, levelWidth);
        ceilMap4 = reader.readInt2dArray(FIELD_CEIL_MAP_4, levelHeight, levelWidth);
        arrowsMap = reader.readInt2dArray(FIELD_ARROWS_MAP, levelHeight, levelWidth);
        mustReload = reader.readBoolean(FIELD_MUST_RELOAD);
        mustLoadAutosave = reader.readBoolean(FIELD_MUST_LOAD_AUTOSAVE);
        levelExp = reader.readInt(FIELD_LEVEL_EXP);
        savedOrNew = reader.readBoolean(FIELD_SAVED_OR_NEW);
        disabledControlsMask = reader.readInt(FIELD_DISABLED_CONTROLS_MASK);
        stats = reader.readIntArray(FIELD_STATS, Achievements.STAT_LAST);
        explosivesMap = reader.readInt2dArray(FIELD_EXPLOSIVES_MAP, levelHeight, levelWidth);
        reader.readDataList(FIELD_TIMEOUTS, timeouts);
        controlsHelpMask = reader.readInt(FIELD_CONTROLS_HELP_MASK);
        reader.readDataList(FIELD_LOOK_POINTS, lookPoints);
        reader.readDataList(FIELD_ON_CHANGE_WEAPON_ACTIONS, onChangeWeaponActions);
        lastWeapons = reader.readIntArray(FIELD_LAST_WEAPONS, 3);
        lastWeaponIdx = reader.readInt(FIELD_LAST_WEAPON_IDX);
        pathToX = reader.readInt(FIELD_PATH_TO_X);
        pathToY = reader.readInt(FIELD_PATH_TO_Y);
        cheatsDisabled = reader.readBoolean(FIELD_CHEATS_DISABLED);

        engine.level.requestBuildPathToWave();

        for (int i = 0; i < levelHeight; i++) {
            for (int j = 0; j < levelWidth; j++) {
                wallsMap[i][j] = TextureLoader.unpackTexId(wallsMap[i][j]);
                texMap[i][j] = TextureLoader.unpackTexId(texMap[i][j]);

                for (int k = 0; k < ObjectContainer.MAX_OBJECTS; k++) {
                    objectsMap.get(i).get(j).set(k, TextureLoader.unpackTexId(objectsMap.get(i).get(j).get(k)));
                }

                arrowsMap[i][j] = TextureLoader.unpackTexId(arrowsMap[i][j]);

                floorMap1[i][j] = TextureLoader.unpackTexId(floorMap1[i][j]);
                floorMap2[i][j] = TextureLoader.unpackTexId(floorMap2[i][j]);
                floorMap3[i][j] = TextureLoader.unpackTexId(floorMap3[i][j]);
                floorMap4[i][j] = TextureLoader.unpackTexId(floorMap4[i][j]);

                ceilMap1[i][j] = TextureLoader.unpackTexId(ceilMap1[i][j]);
                ceilMap2[i][j] = TextureLoader.unpackTexId(ceilMap2[i][j]);
                ceilMap3[i][j] = TextureLoader.unpackTexId(ceilMap3[i][j]);
                ceilMap4[i][j] = TextureLoader.unpackTexId(ceilMap4[i][j]);

                if (((passableMap[i][j] & Level.PASSABLE_IS_EXPLOSIVE) != 0) && (explosivesMap[i][j] == 0)) {
                    explosivesMap[i][j] = GameConfig.HEALTH_BARREL;
                }
            }
        }

        engine.level.conf = LevelConfig.read(App.self.getAssets(), levelName);
    }

    @SuppressWarnings("RedundantMethodOverride")
    @Override
    protected int getVersion() {
        return 1;
    }

    @SuppressWarnings("RedundantMethodOverride")
    @Override
    protected void versionUpgrade(int version) {
    }

    @Override
    public boolean save(String name) {
        if (levelExp != 0) {
            engine.profile.exp += levelExp;
            engine.profile.save(engine.activity);
            levelExp = 0;
        }

        if (!super.save(engine.getSavePathBySaveName(name))) {
            Common.showToast(engine.activity, R.string.engine_state_cant_save);
            return false;
        }

        return true;
    }

    @Override
    public int load(String name) {
        int result = super.load(engine.getSavePathBySaveName(name));

        if (result == LOAD_RESULT_ERROR) {
            Common.showToast(engine.activity, R.string.engine_state_cant_load);
        }

        return result;
    }
}
