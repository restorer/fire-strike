package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.level.Level;
import zame.game.engine.level.LevelRenderer;
import zame.game.engine.state.State;
import zame.game.engine.util.GameMath;

public class Bullet extends DataListItem<Bullet> implements EngineObject, DataItem {
    private static final float SHOOT_RADIUS = 0.2f;
    private static final float BULLET_RADIUS = 0.1f;
    private static final float SHOOT_RADIUS_SQ = SHOOT_RADIUS * SHOOT_RADIUS; // actual = BULLET_RADIUS + SHOOT_RADIUS
    private static final float HIT_MIN_FLY_DIST_SQ = 0.25f * 0.25f; // must be > than SHOOT_RADIUS_SQ
    private static final float INITIAL_ACCEL = 0.3f; // must be > than SHOOT_RADIUS

    public static class BulletParams {
        float speed;
        public float maxDist;
        int baseTexture;
        boolean explosion;
        boolean boomAfterMaxDist;

        BulletParams(float speed, float maxDist, int baseTexture, boolean explosion, boolean boomAfterMaxDist) {
            this.speed = speed;
            this.maxDist = maxDist;
            this.baseTexture = baseTexture;
            this.explosion = explosion;
            this.boomAfterMaxDist = boomAfterMaxDist;
        }
    }

    public static class HitParams {
        public static final int HIT_OUT = 1; // out of the level, must be > than 0
        public static final int HIT_WALL = 2; // must be > than HIT_OUT
        static final int HIT_EXPLOSIVE = 3; // can be only when explosion = true, must be > than HIT_WALL
        public static final int HIT_MONSTER = 4; // must be > than HIT_EXPLOSIVE
        public static final int HIT_HERO = 5; // must be > than HIT_MONSTER

        int hit;
        float x;
        float y;
        float tx;
        float ty;
        Monster target;

        HitParams(int hit) {
            this.hit = hit;
        }
    }

    private static final int FIELD_X = 1;
    private static final int FIELD_Y = 2;
    private static final int FIELD_AR = 3;
    private static final int FIELD_MON_UID = 4;
    private static final int FIELD_AMMO_IDX = 5;
    private static final int FIELD_HITS = 6;
    private static final int FIELD_STUN = 7;
    private static final int FIELD_BULLET_STATE = 8;
    private static final int FIELD_DIST = 9;
    private static final int FIELD_SX = 10;
    private static final int FIELD_SY = 11;

    // Пусть игрок стоит на (0.0, 0.0). Самое близкое как может стоять враг - (1.5, 0.5)
    // или (0.5, 1.5), т.к. есть клеточки. Получается, что если расстояние для ближнего удара
    // будет меньше чем sqrt(0.5 * 0.5 + 1.5 * 1.5) ~= 1.59, то в такой конфигурации будет невозможно
    // нанести удар.
    //
    // А если у игрока делать setPassable() с учётом WALK_WALL_DIST (которое сейчас 0.2),
    // то расстояние увеличивается до sqrt(0.5 * 0.5 + (2.5 - 0.8) * (2.5 - 0.8)) ~= 1.78
    static final float MONSTER_MEELE_MAX_DIST = 1.59f;

    private static final BulletParams[] BULLET_PARAMS = {
            // AMMO_CLIP
            new BulletParams(50.0f, 100.0f, -1, false, false),
            // AMMO_SHELL
            new BulletParams(50.0f, 100.0f, -1, false, false),
            // AMMO_GRENADE
            new BulletParams(0.5f, 8.0f, 0, true, true), };

    private static final BulletParams[] NEAR_PARAMS = {
            // -1 (hero punch)
            new BulletParams(1.4f, 1.4f, -1, false, false),
            // -2 (monster punch)
            new BulletParams(MONSTER_MEELE_MAX_DIST, MONSTER_MEELE_MAX_DIST, -1, false, false), };

    private static final int STATE_INITIAL = 0;
    private static final int STATE_FLY = 1;
    public static final int STATE_RELEASE = 2;

    private Engine engine;
    private State state;
    private Game game;
    private Level level;
    private LevelRenderer levelRenderer;
    private float xoff;
    private float yoff;
    private final HitParams tmpHitOut = new HitParams(HitParams.HIT_OUT);
    private final HitParams tmpHitA = new HitParams(0);
    private final HitParams tmpHitB = new HitParams(0);

    public float sx;
    public float sy;
    public float x;
    public float y;
    private float dx;
    private float dy;

    public float dist;
    public int monUid;
    public Monster mon;
    public int bulletState;
    public BulletParams params;

    private int angle;
    private float ar;
    private int ammoIdx; // -1 for hero hit
    private int hits;
    private int stun;

    public static boolean shootOrPunch(
            State state,
            float x,
            float y,
            float ar,
            Monster mon,
            int ammoIdx,
            int hits,
            int stun) {

        Bullet bullet = state.bullets.take();

        if (bullet == null) {
            // remove very old bullet and re-take new
            state.bullets.release(state.bullets.first());
            bullet = state.bullets.take();
        }

        bullet.init(x, y, ar, mon, ammoIdx, hits, stun);
        boolean hit = bullet.update();

        if (bullet.bulletState == STATE_RELEASE) {
            state.bullets.release(bullet);
        }

        return hit;
    }

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.state = engine.state;
        this.game = engine.game;
        this.level = engine.level;
        this.levelRenderer = engine.levelRenderer;
    }

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_X, x);
        writer.write(FIELD_Y, y);
        writer.write(FIELD_AR, ar);
        writer.write(FIELD_MON_UID, monUid);
        writer.write(FIELD_AMMO_IDX, ammoIdx);
        writer.write(FIELD_HITS, hits);
        writer.write(FIELD_STUN, stun);
        writer.write(FIELD_BULLET_STATE, bulletState);
        writer.write(FIELD_DIST, dist);
        writer.write(FIELD_SX, sx);
        writer.write(FIELD_SY, sy);
    }

    @Override
    public void readFrom(DataReader reader) {
        x = reader.readFloat(FIELD_X);
        y = reader.readFloat(FIELD_Y);
        ar = reader.readFloat(FIELD_AR);
        monUid = reader.readInt(FIELD_MON_UID);
        ammoIdx = reader.readInt(FIELD_AMMO_IDX);
        hits = reader.readInt(FIELD_HITS);
        stun = reader.readInt(FIELD_STUN);
        bulletState = reader.readInt(FIELD_BULLET_STATE);
        dist = reader.readFloat(FIELD_DIST);
        sx = reader.readFloat(FIELD_SX);
        sy = reader.readFloat(FIELD_SY);

        updateVars();
    }

    public void init(float x, float y, float ar, Monster mon, int ammoIdx, int hits, int stun) {
        this.sx = x;
        this.sy = y;
        this.x = x;
        this.y = y;
        this.ar = ar;
        this.mon = mon;
        this.monUid = (mon == null ? -1 : mon.uid);
        this.ammoIdx = ammoIdx;
        this.hits = hits;
        this.stun = stun;

        dist = 0.0f;
        bulletState = STATE_INITIAL;

        updateVars();
    }

    private void updateVars() {
        //noinspection MagicNumber
        angle = ((int)(ar * GameMath.RAD2G_F) + 360) % 360;

        dx = (float)Math.cos(ar);
        dy = -(float)Math.sin(ar);
        params = (ammoIdx < 0 ? NEAR_PARAMS[monUid < 0 ? 0 : 1] : BULLET_PARAMS[ammoIdx]);
        xoff = -dy * BULLET_RADIUS;
        yoff = dx * BULLET_RADIUS;
    }

    @SuppressWarnings("MagicNumber")
    public boolean update() {
        if (bulletState > STATE_FLY) {
            return false;
        }

        level.shootSeq++;

        float accel = (bulletState == STATE_INITIAL ? Math.max(INITIAL_ACCEL, params.speed) : params.speed);
        float nx = x + dx * accel;
        float ny = y + dy * accel;
        float cx = nx + dx * BULLET_RADIUS;
        float cy = ny + dy * BULLET_RADIUS;

        HitParams hitParamsLPtr;
        HitParams hitParamsRPtr;

        hitParamsLPtr = traceBullet(x + xoff, y + yoff, cx + xoff, cy + yoff);

        if (hitParamsLPtr != null && hit(hitParamsLPtr)) {
            daBoom();
            return true;
        }

        hitParamsRPtr = traceBullet(x - xoff, y - yoff, cx - xoff, cy - yoff);

        if (hitParamsRPtr != null && hit(hitParamsRPtr)) {
            daBoom();
            return true;
        }

        HitParams hitParamsPtr = traceBullet(x, y, cx, cy);

        if (hitParamsPtr != null && hit(hitParamsPtr)) {
            daBoom();
            return true;
        }

        if (hitParamsPtr == null || hitParamsLPtr == null || hitParamsRPtr == null) {
            dist += accel;

            if (dist < params.maxDist - BULLET_RADIUS) {
                x = nx;
                y = ny;
                bulletState = STATE_FLY;
            } else if (params.boomAfterMaxDist) {
                daBoom();
            } else {
                bulletState = STATE_RELEASE;
            }
        } else if (hitParamsPtr.hit != HitParams.HIT_OUT) {
            x = (float)Math.floor(hitParamsPtr.x - dx * 0.5f) + 0.5f;
            y = (float)Math.floor(hitParamsPtr.y - dy * 0.5f) + 0.5f;
            daBoom();
        } else {
            bulletState = STATE_RELEASE;
        }

        return false;
    }

    public int getTexture() {
        if (bulletState == STATE_RELEASE || params.baseTexture < 0) {
            return -1;
        }

        //noinspection MagicNumber
        return params.baseTexture + ((((int)state.heroA + 360 + 45 - angle) % 360) / 90);
    }

    private void daBoom() {
        if (params.explosion && ammoIdx >= 0) { // "ammoIdx >= 0" just for case
            Explosion.boom(state, x, y, mon, ammoIdx, hits, stun);
        }

        bulletState = STATE_RELEASE;
    }

    // is bullet hits an explosive, monster or hero.
    // this function doesn't handle hit into walls (this is done inside "update" function)
    private boolean hit(HitParams hitParams) {
        if (hitParams.hit < HitParams.HIT_EXPLOSIVE) {
            return false;
        }

        if (!params.explosion) {
            float sdx = hitParams.tx - sx;
            float sdy = hitParams.ty - sy;
            float dist = (float)Math.sqrt(sdx * sdx + sdy * sdy);

            if (dist > params.maxDist) {
                return false;
            }

            int resHits = engine.getRealHits(hits, dist);

            if (hitParams.hit == HitParams.HIT_MONSTER && hitParams.target != null) {
                hitParams.target.hit(resHits, stun);
            } else if (hitParams.hit == HitParams.HIT_HERO) {
                game.hitHero(resHits, mon);
            }
        }

        // Это нужно не сколько для того, чтоб красиво отрисовывались взрывы когда игрок кидает гранату,
        // сколько для того, чтоб игрок видел взрыв когда **в него** кидают гранату.
        x = hitParams.tx - dx * GameMath.SIGHT_OFFSET;
        y = hitParams.ty - dy * GameMath.SIGHT_OFFSET;

        // Не рисовать выстрелы во врагов, т.к. когда враг падает, а выстрел остаётся не месте - выглядит странно
        //
        // if (mon == null && !params.explosion) {
        //     BulletTrace.append(engine.random, levelRenderer, x, y, -0.1f);
        // }

        return true;
    }

    @SuppressWarnings("MagicNumber")
    private boolean checkHitRadius(float hx, float hy, float tx, float ty) {
        float cdx = tx - sx;
        float cdy = ty - sy;

        if ((cdx * cdx + cdy * cdy) < HIT_MIN_FLY_DIST_SQ) {
            return false;
        }

        float hdx = hx - tx;
        float hdy = hy - ty;
        float a = dx * dx + dy * dy;
        float b = 2.0f * (hdx * dx + hdy * dy);
        float c = hdx * hdx + hdy * hdy - SHOOT_RADIUS_SQ;

        // startBatch of bullet vector is out of radius
        //noinspection SimplifiableIfStatement
        if (b >= 0.0f && c >= 0.0f) {
            return false;
        }

        return ((b * b - 4.0f * a * c) >= 0.0f);
    }

    // modified Level_CheckLine from wolf3d for iphone by Carmack
    @SuppressWarnings({ "MagicNumber", "ConstantConditions" })
    private HitParams traceBullet(float x1, float y1, float x2, float y2) {
        int cx1 = (int)x1;
        int cy1 = (int)y1;
        int maxX = state.levelWidth - 1;
        int maxY = state.levelHeight - 1;

        if (cx1 < 0 || cx1 > maxX || cy1 < 0 || cy1 > maxY) {
            return tmpHitOut;
        }

        int cx2 = (int)x2;
        int cy2 = (int)y2;
        float maxXf = (float)maxX;
        float maxYf = (float)maxY;
        int[][] localPassableMap = state.passableMap;
        Monster[][] localMonstersMap = level.monstersMap;
        Monster[][] localMonstersPrevMap = level.monstersPrevMap;
        int[][] localShootSeqMap = level.shootSeqMap;
        int localShootSeq = level.shootSeq;

        tmpHitA.hit = 0;
        tmpHitB.hit = 0;

        if (cx1 != cx2) {
            int stepX;
            float partial;

            if (cx2 > cx1) {
                partial = 1.0f - (x1 - (float)((int)x1));
                stepX = 1;
            } else {
                partial = x1 - (float)((int)x1);
                stepX = -1;
            }

            float dx = ((x2 >= x1) ? (x2 - x1) : (x1 - x2));
            float stepY = (y2 - y1) / dx;
            float y = y1 + (stepY * partial);

            cx1 += stepX;
            cx2 += stepX;

            do {
                if (cx1 < 0 || cx1 > maxX || y < 0.0f || y > maxYf) {
                    tmpHitA.hit = HitParams.HIT_OUT;
                    break;
                }

                int iy = (int)y;
                int pass = localPassableMap[iy][cx1];

                if ((pass & Level.PASSABLE_MASK_BULLET) != 0) {
                    tmpHitA.x = (stepX > 0 ? cx1 : (float)cx1 + GameMath.ONE_MINUS_LITTLE);
                    tmpHitA.y = y;

                    if ((pass & Level.PASSABLE_IS_MONSTER) != 0) {
                        Monster target = localMonstersMap[iy][cx1];

                        if (target == null) {
                            target = localMonstersPrevMap[iy][cx1];
                        }

                        if (target != null && checkHitRadius(tmpHitA.x, tmpHitA.y, target.x, target.y)) {
                            tmpHitA.target = target;
                            tmpHitA.hit = HitParams.HIT_MONSTER;
                            tmpHitA.tx = target.x;
                            tmpHitA.ty = target.y;
                            break;
                        }
                    } else if ((pass & Level.PASSABLE_IS_HERO) != 0) {
                        if (checkHitRadius(tmpHitA.x, tmpHitA.y, state.heroX, state.heroY)) {
                            tmpHitA.hit = HitParams.HIT_HERO;
                            tmpHitA.tx = state.heroX;
                            tmpHitA.ty = state.heroY;
                            break;
                        }
                    } else if ((pass & Level.PASSABLE_IS_EXPLOSIVE) != 0
                            || (pass & Level.PASSABLE_IS_DECOR_ITEM) != 0) {

                        float itemX = (float)cx1 + 0.5f;
                        float itemY = (float)iy + 0.5f;

                        if (checkHitRadius(tmpHitA.x, tmpHitA.y, itemX, itemY)) {
                            if (params.explosion) {
                                tmpHitA.hit = HitParams.HIT_EXPLOSIVE;
                                tmpHitA.tx = itemX;
                                tmpHitA.ty = itemY;
                                break;
                            } else if ((pass & Level.PASSABLE_IS_EXPLOSIVE) != 0
                                    && localShootSeqMap[iy][cx1] != localShootSeq) {

                                localShootSeqMap[iy][cx1] = localShootSeq;
                                Explosion.tryBoom(state, cx1, iy, hits);

                                if (mon == null) {
                                    BulletTrace.append(engine.random, levelRenderer, itemX, itemY, 0.25f);
                                }

                                // no break
                            }
                        }
                    } else {
                        tmpHitA.hit = HitParams.HIT_WALL;
                        break;
                    }
                }

                y += stepY;
                cx1 += stepX;
            } while (cx1 != cx2);
        }

        if (cy1 != cy2) {
            int stepY;
            float partial;

            if (cy2 > cy1) {
                partial = 1.0f - (y1 - (float)((int)y1));
                stepY = 1;
            } else {
                partial = y1 - (float)((int)y1);
                stepY = -1;
            }

            float dy = ((y2 >= y1) ? (y2 - y1) : (y1 - y2));
            float stepX = (x2 - x1) / dy;
            float x = x1 + (stepX * partial);

            cy1 += stepY;
            cy2 += stepY;

            do {
                if (cy1 < 0 || cy1 > maxY || x < 0.0f || x > maxXf) {
                    tmpHitB.hit = HitParams.HIT_OUT;
                    break;
                }

                int ix = (int)x;
                int pass = localPassableMap[cy1][ix];

                if ((pass & Level.PASSABLE_MASK_BULLET) != 0) {
                    tmpHitB.x = x;
                    tmpHitB.y = (stepY > 0 ? cy1 : (float)cy1 + GameMath.ONE_MINUS_LITTLE);

                    if ((pass & Level.PASSABLE_IS_MONSTER) != 0) {
                        Monster target = localMonstersMap[cy1][ix];

                        if (target == null) {
                            target = localMonstersPrevMap[cy1][ix];
                        }

                        if (target != null && checkHitRadius(tmpHitB.x, tmpHitB.y, target.x, target.y)) {
                            tmpHitB.target = target;
                            tmpHitB.hit = HitParams.HIT_MONSTER;
                            tmpHitB.tx = target.x;
                            tmpHitB.ty = target.y;
                            break;
                        }
                    } else if ((pass & Level.PASSABLE_IS_HERO) != 0) {
                        if (checkHitRadius(tmpHitB.x, tmpHitB.y, state.heroX, state.heroY)) {
                            tmpHitB.hit = HitParams.HIT_HERO;
                            tmpHitB.tx = state.heroX;
                            tmpHitB.ty = state.heroY;
                            break;
                        }
                    } else if ((pass & Level.PASSABLE_IS_EXPLOSIVE) != 0
                            || (pass & Level.PASSABLE_IS_DECOR_ITEM) != 0) {

                        float itemX = (float)ix + 0.5f;
                        float itemY = (float)cy1 + 0.5f;

                        if (checkHitRadius(tmpHitB.x, tmpHitB.y, itemX, itemY)) {
                            if (params.explosion) {
                                tmpHitB.hit = HitParams.HIT_EXPLOSIVE;
                                tmpHitB.tx = itemX;
                                tmpHitB.ty = itemY;
                                break;
                            } else if ((pass & Level.PASSABLE_IS_EXPLOSIVE) != 0
                                    && localShootSeqMap[cy1][ix] != localShootSeq) {

                                localShootSeqMap[cy1][ix] = localShootSeq;
                                Explosion.tryBoom(state, ix, cy1, hits);

                                if (mon == null) {
                                    BulletTrace.append(engine.random, levelRenderer, itemX, itemY, 0.25f);
                                }

                                // no break
                            }
                        }
                    } else {
                        tmpHitB.hit = HitParams.HIT_WALL;
                        break;
                    }
                }

                x += stepX;
                cy1 += stepY;
            } while (cy1 != cy2);
        }

        if (tmpHitA.hit == HitParams.HIT_OUT && tmpHitB.hit == HitParams.HIT_OUT) {
            return tmpHitOut;
        } else if (tmpHitA.hit < HitParams.HIT_WALL && tmpHitB.hit < HitParams.HIT_WALL) {
            if (levelRenderer.debugOnAutomap) {
                DebugTraceInfo.addInfo(levelRenderer, x1, y1, x2, y2, 0);
            }

            return null;
        }

        if (tmpHitA.hit > HitParams.HIT_OUT && tmpHitB.hit < HitParams.HIT_WALL) {
            if (levelRenderer.debugOnAutomap) {
                DebugTraceInfo.addInfo(levelRenderer, x1, y1, tmpHitA.x, tmpHitA.y, tmpHitA.hit);
            }

            return tmpHitA;
        } else if (tmpHitB.hit > HitParams.HIT_OUT && tmpHitA.hit < HitParams.HIT_WALL) {
            if (levelRenderer.debugOnAutomap) {
                DebugTraceInfo.addInfo(levelRenderer, x1, y1, tmpHitB.x, tmpHitB.y, tmpHitB.hit);
            }

            return tmpHitB;
        } else {
            float h1dx = tmpHitA.x - x1;
            float h1dy = tmpHitA.y - y1;
            float h2dx = tmpHitB.x - x1;
            float h2dy = tmpHitB.y - y1;

            if ((h1dx * h1dx + h1dy * h1dy) < (h2dx * h2dx + h2dy * h2dy)) {
                if (levelRenderer.debugOnAutomap) {
                    DebugTraceInfo.addInfo(levelRenderer, x1, y1, tmpHitA.x, tmpHitA.y, tmpHitA.hit);
                }

                return tmpHitA;
            } else {
                if (levelRenderer.debugOnAutomap) {
                    DebugTraceInfo.addInfo(levelRenderer, x1, y1, tmpHitB.x, tmpHitB.y, tmpHitB.hit);
                }

                return tmpHitB;
            }
        }
    }
}
