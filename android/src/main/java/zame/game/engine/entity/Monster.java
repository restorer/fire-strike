package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.level.Level;
import zame.game.engine.level.LevelConfig;
import zame.game.engine.level.LevelRenderer;
import zame.game.engine.state.Profile;
import zame.game.engine.state.State;
import zame.game.engine.util.GameMath;
import zame.game.engine.visual.Weapons;
import zame.game.feature.achievements.Achievements;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.GameConfig;

public class Monster extends DataListItem<Monster> implements EngineObject, DataItem {
    private static final int FIELD_CELL_X = 1;
    private static final int FIELD_CELL_Y = 2;
    private static final int FIELD_X = 3;
    private static final int FIELD_Y = 4;
    private static final int FIELD_TEXTURE = 5;
    private static final int FIELD_DIR = 6;
    // private static final int FIELD_MAX_STEP = 7;
    private static final int FIELD_HEALTH = 8;
    private static final int FIELD_HITS = 9;
    private static final int FIELD_ATTACK_DIST = 10;
    private static final int FIELD_AMMO_IDX = 11;
    private static final int FIELD_STEP = 12;
    private static final int FIELD_PREV_X = 13;
    private static final int FIELD_PREV_Y = 14;
    private static final int FIELD_STUN_TICKS = 15;
    private static final int FIELD_ATTACK_TICKS = 16;
    private static final int FIELD_AROUND_REQ_DIR = 17;
    private static final int FIELD_INVERSE_ROTATION = 18;
    private static final int FIELD_PREV_AROUND_X = 19;
    private static final int FIELD_PREV_AROUND_Y = 20;
    private static final int FIELD_SHOOT_ANGLE = 21;
    private static final int FIELD_CHASE_MODE = 22;
    private static final int FIELD_WAIT_FOR_DOOR = 23;
    private static final int FIELD_IN_SHOOT_MODE = 24;
    private static final int FIELD_UID = 25;
    private static final int FIELD_SHOULD_SHOOT_IN_HERO = 26;
    private static final int FIELD_ON_KILL_ACTION = 27;

    private static final int MAX_STEP = 50;
    private static final int ATTACK_ANIM_TIME = 15;

    private static final int AIM_MIN_TIME = 5; // min aim - 0.125s
    private static final int AIM_MAX_TIME = 20; // max aim - 0.5s
    private static final int ATTACK_WAIT_TIME = 40 - AIM_MIN_TIME; // spd ~= 1 hit per second

    private static final float VISIBLE_DIST = 32.0f;
    private static final float HEAR_DIST = 5.0f;

    private Engine engine;
    private State state;
    private Level level;
    private Game game;
    private LevelRenderer levelRenderer;
    private SoundManager soundManager;
    private Profile profile;

    public float x;
    public float y;
    public int texture;
    public int dir; // 0 - right, 1 - up, 2 - left, 3 - down
    private int step;
    public int health;
    public boolean chaseMode;
    public int shootAngle;

    public int uid; // required for save/load for bullets
    public int cellX;
    public int cellY;
    public int prevX = -1;
    public int prevY = -1;
    public int stunTicks; // hero hits monster
    public int attackTicks; // attack animation time (not used for anything else)
    public long dieTime;
    public boolean isInAttackState;
    public boolean isAimedOnHero;
    public int onKillActionId = -1;

    private int hits;
    private int ammoIdx;
    private float attackDist;
    private int attackSoundIdx;
    private int deathSoundIdx;
    private int readySoundIdx;
    private int aroundReqDir;
    private boolean inverseRotation;
    private int prevAroundX;
    private int prevAroundY;
    private boolean waitForDoor;
    private boolean inShootMode;
    private boolean shouldShootInHero;
    private boolean mustEnableChaseMode;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.state = engine.state;
        this.level = engine.level;
        this.game = engine.game;
        this.levelRenderer = engine.levelRenderer;
        this.soundManager = engine.soundManager;
        this.profile = engine.profile;
    }

    @SuppressWarnings("MagicNumber")
    public void configure(int uid, int cellX, int cellY, int monIndex, LevelConfig.MonsterConfig monConf) {
        this.uid = uid;
        this.cellX = cellX;
        this.cellY = cellY;

        step = 0;
        stunTicks = 0;
        attackTicks = 0;
        dieTime = 0;
        aroundReqDir = -1;
        inverseRotation = false;
        prevAroundX = -1;
        prevAroundY = -1;
        shouldShootInHero = false;
        chaseMode = false;
        waitForDoor = false;
        inShootMode = false;
        mustEnableChaseMode = false;
        onKillActionId = -1;

        dir = 0;
        prevX = -1;
        prevY = -1;
        x = (float)cellX + 0.5f;
        y = (float)cellY + 0.5f;
        texture = TextureLoader.COUNT_MONSTER * monIndex;

        health = monConf.health;
        hits = monConf.hits;
        setAttackDist(monConf.hitType != LevelConfig.HIT_TYPE_MEELE);

        if (monConf.hitType == LevelConfig.HIT_TYPE_CLIP) {
            ammoIdx = Weapons.AMMO_CLIP;
        } else if (monConf.hitType == LevelConfig.HIT_TYPE_SHELL) {
            ammoIdx = Weapons.AMMO_SHELL;
        } else if (monConf.hitType == LevelConfig.HIT_TYPE_GRENADE) {
            ammoIdx = Weapons.AMMO_GRENADE;
        } else { // HIT_TYPE_MEELE
            ammoIdx = -1;
        }
    }

    public void postConfigure() {
        int monIndex = texture / TextureLoader.COUNT_MONSTER;

        if (monIndex < 0 || monIndex >= GameConfig.MAX_MONSTER_TYPES) {
            monIndex = 0;
        }

        attackSoundIdx = SoundManager.SOUNDLIST_ATTACK_MON[monIndex];
        deathSoundIdx = SoundManager.SOUNDLIST_DEATH_MON[monIndex];
        readySoundIdx = SoundManager.SOUNDLIST_READY_MON[monIndex];
    }

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_CELL_X, cellX);
        writer.write(FIELD_CELL_Y, cellY);
        writer.write(FIELD_X, x);
        writer.write(FIELD_Y, y);
        writer.write(FIELD_TEXTURE, texture);
        writer.write(FIELD_DIR, dir);
        writer.write(FIELD_HEALTH, health);
        writer.write(FIELD_HITS, hits);
        writer.write(FIELD_ATTACK_DIST, attackDist);
        writer.write(FIELD_AMMO_IDX, ammoIdx);
        writer.write(FIELD_STEP, step);
        writer.write(FIELD_PREV_X, prevX);
        writer.write(FIELD_PREV_Y, prevY);
        writer.write(FIELD_STUN_TICKS, stunTicks);
        writer.write(FIELD_ATTACK_TICKS, attackTicks);
        writer.write(FIELD_AROUND_REQ_DIR, aroundReqDir);
        writer.write(FIELD_INVERSE_ROTATION, inverseRotation);
        writer.write(FIELD_PREV_AROUND_X, prevAroundX);
        writer.write(FIELD_PREV_AROUND_Y, prevAroundY);
        writer.write(FIELD_SHOOT_ANGLE, shootAngle);
        writer.write(FIELD_CHASE_MODE, chaseMode);
        writer.write(FIELD_WAIT_FOR_DOOR, waitForDoor);
        writer.write(FIELD_IN_SHOOT_MODE, inShootMode);
        writer.write(FIELD_UID, uid);
        writer.write(FIELD_SHOULD_SHOOT_IN_HERO, shouldShootInHero);
        writer.write(FIELD_ON_KILL_ACTION, onKillActionId);
    }

    @Override
    public void readFrom(DataReader reader) {
        cellX = reader.readInt(FIELD_CELL_X);
        cellY = reader.readInt(FIELD_CELL_Y);
        x = reader.readFloat(FIELD_X);
        y = reader.readFloat(FIELD_Y);
        texture = reader.readInt(FIELD_TEXTURE);
        dir = reader.readInt(FIELD_DIR);
        health = reader.readInt(FIELD_HEALTH);
        hits = reader.readInt(FIELD_HITS);
        attackDist = reader.readFloat(FIELD_ATTACK_DIST);
        ammoIdx = reader.readInt(FIELD_AMMO_IDX);
        step = reader.readInt(FIELD_STEP);
        prevX = reader.readInt(FIELD_PREV_X);
        prevY = reader.readInt(FIELD_PREV_Y);
        stunTicks = reader.readInt(FIELD_STUN_TICKS);
        attackTicks = reader.readInt(FIELD_ATTACK_TICKS);
        aroundReqDir = reader.readInt(FIELD_AROUND_REQ_DIR);
        inverseRotation = reader.readBoolean(FIELD_INVERSE_ROTATION);
        prevAroundX = reader.readInt(FIELD_PREV_AROUND_X);
        prevAroundY = reader.readInt(FIELD_PREV_AROUND_Y);
        shootAngle = reader.readInt(FIELD_SHOOT_ANGLE);
        chaseMode = reader.readBoolean(FIELD_CHASE_MODE);
        waitForDoor = reader.readBoolean(FIELD_WAIT_FOR_DOOR);
        inShootMode = reader.readBoolean(FIELD_IN_SHOOT_MODE);
        uid = reader.readInt(FIELD_UID);
        shouldShootInHero = reader.readBoolean(FIELD_SHOULD_SHOOT_IN_HERO);
        onKillActionId = reader.readInt(FIELD_ON_KILL_ACTION, -1);

        isInAttackState = false;
        isAimedOnHero = false;
        dieTime = (health <= 0 ? -1 : 0);
        mustEnableChaseMode = false;
    }

    private void setAttackDist(boolean longAttackDist) {
        // for meele attach it probably should be Bullet.MONSTER_MEELE_MAX_DIST + Bullet.SHOOT_RADIUS
        // (because bullet looks forward for Bullet.SHOOT_RADIUS), but if I do this,
        // monster punches doesn't hit hero

        //noinspection MagicNumber
        attackDist = (longAttackDist ? 10.0f : Bullet.MONSTER_MEELE_MAX_DIST);
    }

    private void enableChaseMode() {
        if (!chaseMode) {
            chaseMode = true;

            if (!engine.level.isInitialUpdate) {
                soundManager.playSound(readySoundIdx);
            }
        }

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx != 0 || dy != 0) {
                    Monster mon = level.monstersMap[cellY + dy][cellX + dx];

                    if (mon != null) {
                        mon.mustEnableChaseMode = true;
                    }
                }
            }
        }
    }

    public void hit(int amt, int stun) {
        stunTicks += stun; // Добавлять STUN, а не реплейсить
        aroundReqDir = -1;
        enableChaseMode();

        if (amt <= 0) {
            return;
        }

        health -= Math.max(1, (int)((float)amt * game.healthHitMonsterMult));

        if (health <= 0) {
            engine.soundManager.playSound(deathSoundIdx);
            state.levelExp += GameConfig.EXP_KILL_MONSTER;

            state.passableMap[cellY][cellX] &= ~Level.PASSABLE_IS_MONSTER;
            state.passableMap[cellY][cellX] |= Level.PASSABLE_IS_DEAD_CORPSE;
            level.monstersMap[cellY][cellX] = null;

            if (prevX >= 0 && prevY >= 0 && prevX < state.levelWidth && prevY < state.levelHeight) {
                level.monstersPrevMap[prevY][prevX] = null;
            }

            if (ammoIdx >= 0) {
                int ammoType = Weapons.AMMO_OBJ_TEX_MAP[ammoIdx];

                if (!ObjectContainer.dropAt(ammoType, state, levelRenderer, cellX, cellY)) {
                    outer:

                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dy != 0
                                    && dx != 0
                                    && ObjectContainer.dropAt(ammoType, state, levelRenderer, cellX + dx, cellY + dy)) {

                                break outer;
                            }
                        }
                    }
                }
            }

            state.killedMonsters++;
            Achievements.updateStat(Achievements.STAT_MONSTERS_KILLED, profile, engine, state);

            if (onKillActionId >= 0) {
                level.executeActions(onKillActionId);
            }
        }
    }

    @SuppressWarnings({ "MagicNumber", "ConstantConditions" })
    public void update() {
        if (health <= 0) {
            return;
        }

        if (mustEnableChaseMode && !chaseMode) {
            enableChaseMode();
        }

        float dx = 1.0f;
        float dy = 1.0f;
        float dist = 1.0f;

        if (shouldShootInHero || step == 0) {
            x = (float)cellX + 0.5f;
            y = (float)cellY + 0.5f;
            dx = state.heroX - x;
            dy = state.heroY - y;
            dist = (float)Math.sqrt(dx * dx + dy * dy);
        }

        if (shouldShootInHero) {
            shouldShootInHero = false;

            if (game.killedTime == 0) {
                soundManager.playSound(attackSoundIdx);

                Bullet.shootOrPunch(
                        state,
                        x,
                        y,
                        GameMath.getAngle(dx, dy, dist),
                        this,
                        ammoIdx,
                        Math.max(1, (int)((float)hits * game.healthHitHeroMult)),
                        0);
            }
        }

        if (step == 0) {
            boolean tryAround = false;
            boolean isHeroVisibleForShoot = false;

            isInAttackState = false;
            isAimedOnHero = false;

            if (prevX >= 0 && prevY >= 0 && prevX < state.levelWidth && prevY < state.levelHeight) {
                level.monstersPrevMap[prevY][prevX] = null;
            }

            prevX = cellX;
            prevY = cellY;
            level.monstersPrevMap[prevY][prevX] = this;

            if (dist <= VISIBLE_DIST) {
                if (!chaseMode && (dist <= HEAR_DIST
                        || traceLine(x, y, state.heroX, state.heroY, Level.PASSABLE_MASK_CHASE_WM))) {

                    enableChaseMode();
                }

                // if (chaseMode && ...) - don't additionally traceLine if hero is invisible
                if (chaseMode && traceLine(x, y, state.heroX, state.heroY, Level.PASSABLE_MASK_SHOOT_WM)) {
                    isHeroVisibleForShoot = true;
                }
            }

            state.passableMap[cellY][cellX] &= ~Level.PASSABLE_IS_MONSTER;
            level.monstersMap[cellY][cellX] = null;

            if (chaseMode) {
                if (aroundReqDir >= 0) {
                    if (!waitForDoor) {
                        dir = (dir + (inverseRotation ? 3 : 1)) % 4;
                    }
                } else if (dist <= VISIBLE_DIST) {
                    if (Math.abs(dy) <= 1.0f) {
                        dir = (dx < 0 ? 2 : 0);
                    } else {
                        dir = (dy < 0 ? 1 : 3);
                    }

                    tryAround = true;
                }

                if (isHeroVisibleForShoot && dist <= attackDist) {
                    int angleToHero = (int)(GameMath.getAngle(dx, dy, dist) * GameMath.RAD2G_F);
                    int angleDiff = angleToHero - shootAngle;

                    if (angleDiff > 180) {
                        angleDiff -= 360;
                    } else if (angleDiff < -180) {
                        angleDiff += 360;
                    }

                    angleDiff = (angleDiff < 0 ? -angleDiff : angleDiff);
                    shootAngle = angleToHero;

                    if (!inShootMode || angleDiff > Math.max(1, 15 - (int)(dist * 2.0f))) {
                        inShootMode = true;
                        step = engine.random.nextInt(AIM_MAX_TIME - AIM_MIN_TIME) + AIM_MIN_TIME;
                    } else if (inShootMode) {
                        isAimedOnHero = true;
                        shouldShootInHero = true;
                        attackTicks = ATTACK_ANIM_TIME;
                        step = ATTACK_WAIT_TIME;
                    }

                    isInAttackState = true;
                    dir = ((shootAngle + 45) % 360) / 90;
                    aroundReqDir = -1;
                } else {
                    inShootMode = false;
                    waitForDoor = false;

                    for (int i = 0; i < 4; i++) {
                        switch (dir) {
                            case 0:
                                cellX++;
                                break;

                            case 1:
                                cellY--;
                                break;

                            case 2:
                                cellX--;
                                break;

                            case 3:
                                cellY++;
                                break;
                        }

                        if ((state.passableMap[cellY][cellX] & Level.PASSABLE_MASK_MONSTER) == 0) {
                            if (dir == aroundReqDir) {
                                aroundReqDir = -1;
                            }

                            step = MAX_STEP;
                            break;
                        }

                        if (chaseMode
                                && ((state.passableMap[cellY][cellX] & Level.PASSABLE_IS_DOOR) != 0)
                                && ((state.passableMap[cellY][cellX] & Level.PASSABLE_IS_DOOR_OPENED_BY_HERO) != 0)) {

                            Door door = level.doorsMap[cellY][cellX];

                            if (!door.sticked) {
                                door.open();

                                waitForDoor = true;
                                cellX = prevX;
                                cellY = prevY;
                                step = 10;
                                break;
                            }
                        }

                        cellX = prevX;
                        cellY = prevY;

                        if (tryAround) {
                            if ((prevAroundX == cellX) && (prevAroundY == cellY)) {
                                inverseRotation = !inverseRotation;
                            }

                            aroundReqDir = dir;
                            prevAroundX = cellX;
                            prevAroundY = cellY;
                            tryAround = false;
                        }

                        dir = (dir + (inverseRotation ? 1 : 3)) % 4;
                    }

                    if (step == 0) {
                        step = MAX_STEP / 2;
                    }

                    shootAngle = dir * 90;
                }
            }

            state.passableMap[cellY][cellX] |= Level.PASSABLE_IS_MONSTER;
            level.monstersMap[cellY][cellX] = this;
        }

        x = (float)cellX + ((float)(prevX - cellX) * (float)step / (float)MAX_STEP) + 0.5f;
        y = (float)cellY + ((float)(prevY - cellY) * (float)step / (float)MAX_STEP) + 0.5f;

        if (attackTicks > 0) {
            attackTicks--;
        }

        if (stunTicks > 0) {
            stunTicks--;
        } else if (step > 0) {
            step--;
        }
    }

    private boolean traceLine(float x1, float y1, float x2, float y2, int mask) {
        return GameMath.traceLine(state.levelWidth, state.levelHeight, state.passableMap, x1, y1, x2, y2, mask);
    }
}
