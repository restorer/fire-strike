package zame.game.engine.level;

import zame.game.core.serializer.DataList;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.entity.AutoWall;
import zame.game.engine.entity.Bullet;
import zame.game.engine.entity.BulletTrace;
import zame.game.engine.entity.DebugTraceInfo;
import zame.game.engine.entity.Door;
import zame.game.engine.entity.Explosion;
import zame.game.engine.entity.Monster;
import zame.game.engine.entity.ObjectContainer;
import zame.game.engine.entity.TouchedCell;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.state.State;
import zame.game.engine.util.GameMath;

public class LevelRenderer implements EngineObject {
    private static final float FRUSTUM_SIZE = 0.1f * (float)Math.tan(Math.toRadians(50.0) / 2);

    private static final int LIGHTTAB_MAX = 1000;
    private static final int LIGHTTAB_LAST = LIGHTTAB_MAX - 1;
    private static final float LIGHTTAB_VAL_MIN = 0.25f;
    private static final float LIGHTTAB_VAL_MAX = 0.75f;
    private static final float LIGHTTAB_VAL_MID = (LIGHTTAB_VAL_MIN + LIGHTTAB_VAL_MAX) * 0.5f;
    private static final float LIGHTTAB_VAL_SINMULT = (LIGHTTAB_VAL_MAX - LIGHTTAB_VAL_MIN) * 0.5f;
    private static final float LIGHTTAB_DIST_MULT = LIGHTTAB_MAX / 10.0f;

    public static final int AUTO_WALL_TYPE_WALL = 0;
    public static final int AUTO_WALL_TYPE_TRANSP = 1;
    public static final int AUTO_WALL_TYPE_DOOR = 2;

    public static final int AUTO_WALL_MASK_HORIZONTAL = 1;
    public static final int AUTO_WALL_MASK_VERTICAL = 2;
    public static final int AUTO_WALL_MASK_DOOR = 4;

    public static final int MAX_AUTO_WALLS = Level.MAX_WIDTH * Level.MAX_HEIGHT * 2;
    public static final int MAX_AW_CELLS = Level.MAX_WIDTH * Level.MAX_HEIGHT;
    public static final float HALF_WALL = 0.4f;
    public static final float LIGHT_OBJECT = 1.0f;

    private static final float HALF_WALL_PLUS_EXTRUDE = HALF_WALL + 0.05f;
    private static final float HALF_DOOR_WIDTH = 0.025f;
    private static final float LIGHT_DOOR = 0.5f;
    private static final float LIGHT_LAMP = 1.0f;
    private static final float LIGHT_ARROW = 1.0f;
    private static final float MONSTER_SIZE_MULT = 0.75f;
    private static final float EXTRUDE_LIGHT_MULT = 0.5f;

    private static final float BTRACE_MIN_SCALE = 0.005f;
    private static final float BTRACE_MAX_SCALE = 0.05f;
    private static final float BTRACE_MAX_DIST = 10.0f;

    public PortalTracer tracer = new PortalTracer();

    public boolean debugOnAutomap;
    public boolean[][] awTouchedCellsMap = new boolean[Level.MAX_HEIGHT][Level.MAX_WIDTH];
    public DataList<DebugTraceInfo> debugTraceInfos = new DataList<>(DebugTraceInfo.class, Level.MAX_BULLETS * 3);

    private final float[] lightTab = new float[LIGHTTAB_MAX];
    private Engine engine;
    private State state;
    private Renderer renderer;
    private Level level;
    private final float[][] lightMap = new float[Level.MAX_HEIGHT * 2 + 1][Level.MAX_WIDTH * 2 + 1];
    public float currentHeroX;
    public float currentHeroY;
    public float currentHeroA;
    private float sightDx;
    private float sightDy;
    private float billboardDx;
    private float billboardDy;
    public DataList<BulletTrace> bulletTraces = new DataList<>(BulletTrace.class, Level.MAX_BULLETS);

    public LevelRenderer() {
        //
        //                  ^
        //                  |
        // LIGHTTAB_VAL_MAX | ###
        //                  |    ##
        //                  |      #
        //                  |       ##
        // LIGHTTAB_VAL_MIN |         ###
        //                  |
        // -----------------+-------------------->
        //                  | 0  LIGHTTAB_LAST
        //

        for (int i = 0; i < LIGHTTAB_MAX; i++) {
            float v = (float)Math.sin((float)i / (float)LIGHTTAB_MAX * GameMath.PI_F + GameMath.PI_D2F);
            lightTab[i] = LIGHTTAB_VAL_MID + LIGHTTAB_VAL_SINMULT * v;
        }
    }

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.state = engine.state;
        this.renderer = engine.renderer;
        this.level = engine.level;

        tracer.onCreate(engine);
    }

    public void updateAfterLevelLoadedOrCreated() {
        for (AutoWall aw = state.autoWalls.first(); aw != null; aw = aw.next) {
            if (aw.doorUid >= 0) {
                for (Door door = state.doors.first(); door != null; door = door.next) {
                    if (door.uid == aw.doorUid) {
                        aw.door = door;
                        break;
                    }
                }

                if (aw.door == null) {
                    aw.doorUid = -1;
                }
            }
        }

        for (Bullet bullet = state.bullets.first(); bullet != null; bullet = bullet.next) {
            if (bullet.monUid >= 0) {
                for (Monster mon = state.monsters.first(); mon != null; mon = mon.next) {
                    if (mon.uid == bullet.monUid) {
                        bullet.mon = mon;
                        break;
                    }
                }
            }
        }

        for (Explosion explosion = state.explosions.first(); explosion != null; explosion = explosion.next) {
            if (explosion.monUid >= 0) {
                for (Monster mon = state.monsters.first(); mon != null; mon = mon.next) {
                    if (mon.uid == explosion.monUid) {
                        explosion.mon = mon;
                        break;
                    }
                }
            }
        }

        for (int i = 0, levelHeight = state.levelHeight; i < levelHeight; i++) {
            for (int j = 0, levelWidth = state.levelWidth; j < levelWidth; j++) {
                awTouchedCellsMap[i][j] = false;
            }
        }

        for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = tc.next) {
            awTouchedCellsMap[tc.y][tc.x] = true;
        }

        for (int ly = 0, maxLy = state.levelHeight * 2 + 1; ly < maxLy; ly++) {
            for (int lx = 0, maxLx = state.levelWidth * 2 + 1; lx < maxLx; lx++) {
                lightMap[ly][lx] = 0.0f;
            }
        }

        for (int y = 0, levelHeight = state.levelHeight; y < levelHeight; y++) {
            for (int x = 0, levelWidth = state.levelWidth; x < levelWidth; x++) {
                modLightMap(x, y, getLightMapValue(x, y));
            }
        }
    }

    float getLightMapValue(int x, int y) {
        float value = 0.0f;
        int pass = state.passableMap[y][x];

        if (level.doorsMap[y][x] != null) {
            value += LIGHT_DOOR;
        } else if ((pass & Level.PASSABLE_IS_OBJECT) != 0) {
            value += LIGHT_OBJECT;
        } else if ((pass & Level.PASSABLE_IS_DECOR_LAMP) != 0) {
            int tex = state.texMap[y][x];

            for (int i = 0, len = TextureLoader.DLAMP_LIGHTS.length; i < len; i++) {
                if (TextureLoader.DLAMP_LIGHTS[i] == tex) {
                    value += LIGHT_LAMP;
                    break;
                }
            }
        } else if ((pass & Level.PASSABLE_IS_WALL) != 0) {
            int tex = state.wallsMap[y][x];

            for (int i = 0, len = TextureLoader.WALL_LIGHTS.length; i < len; i++) {
                if (TextureLoader.WALL_LIGHTS[i] == tex) {
                    value += LIGHT_LAMP;
                    break;
                }
            }
        } else if ((pass & Level.PASSABLE_IS_DECOR_ITEM) != 0) {
            int tex = state.texMap[y][x];

            for (int i = 0, len = TextureLoader.DITEM_LIGHTS.length; i < len; i++) {
                if (TextureLoader.DITEM_LIGHTS[i] == tex) {
                    value += LIGHT_LAMP;
                    break;
                }
            }
        }

        int ceil1 = state.ceilMap1[y][x];
        int ceil2 = state.ceilMap2[y][x];
        int ceil3 = state.ceilMap3[y][x];
        int ceil4 = state.ceilMap4[y][x];

        for (int i = 0, len = TextureLoader.CEIL_LIGHTS.length; i < len; i++) {
            int cl = TextureLoader.CEIL_LIGHTS[i];

            if (ceil1 == cl || ceil2 == cl || ceil3 == cl || ceil4 == cl) {
                value += LIGHT_LAMP;
                break;
            }
        }

        if (state.arrowsMap[y][x] != 0) {
            value += LIGHT_ARROW;
        }

        return value;
    }

    public void modLightMap(int cx, int cy, float val) {
        int lx = cx * 2;
        int ly = cy * 2;

        lightMap[ly][lx] += val;
        lightMap[ly][lx + 1] += val;
        lightMap[ly][lx + 2] += val;

        ly++;
        lightMap[ly][lx] += val;
        lightMap[ly][lx + 1] += val;
        lightMap[ly][lx + 2] += val;

        ly++;
        lightMap[ly][lx] += val;
        lightMap[ly][lx + 1] += val;
        lightMap[ly][lx + 2] += val;
    }

    @SuppressWarnings({ "MagicNumber", "ManualMinMaxCalculation" })
    private float getLightness(float x, float y) {
        float dx = x - currentHeroX;
        float dy = y - currentHeroY;
        int d = (int)((float)Math.sqrt(dx * dx + dy * dy) * LIGHTTAB_DIST_MULT);

        try {
            return lightTab[d < 0 ? 0 : (d > LIGHTTAB_LAST ? LIGHTTAB_LAST : d)]
                    + lightMap[(int)((y + 0.25f) * 2.0f)][(int)((x + 0.25f) * 2.0f)];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0.0f;
        }
    }

    @SuppressWarnings("MagicNumber")
    private void setWallLighting(float fromX, float fromY, float toX, float toY, boolean vert) {
        int ang = ((int)currentHeroA + (vert ? 0 : 270)) % 360;

        if (ang > 90) {
            if (ang < 180) {
                ang = 180 - ang;
            } else if (ang < 270) {
                ang = ang - 180;
            } else {
                ang = 360 - ang;
            }
        }

        float l = 1.0f - 0.5f * (float)ang / 90.0f;
        renderer.setColorQuadLight(getLightness(fromX, fromY) * l, getLightness(toX, toY) * l);
    }

    // This method:
    // did *not* check for available space (MAX_AUTO_WALLS),
    // did *not* check if wall already exists,
    // did *not* append wall mask,
    // did *not* add doors
    public void appendAutoWall(int fromX, int fromY, int toX, int toY, int type) {
        AutoWall foundAw = null;
        boolean vert = (fromX == toX);

        for (AutoWall aw = state.autoWalls.first(); aw != null; aw = aw.next) {
            if (aw.door != null || aw.vert != vert || aw.type != type) {
                continue;
            }

            if ((int)aw.fromX == fromX && (int)aw.fromY == fromY) {
                aw.fromX = (float)toX;
                aw.fromY = (float)toY;
                foundAw = aw;
                break;
            } else if ((int)aw.toX == fromX && (int)aw.toY == fromY) {
                aw.toX = (float)toX;
                aw.toY = (float)toY;
                foundAw = aw;
                break;
            } else if ((int)aw.fromX == toX && (int)aw.fromY == toY) {
                aw.fromX = (float)fromX;
                aw.fromY = (float)fromY;
                foundAw = aw;
                break;
            } else if ((int)aw.toX == toX && (int)aw.toY == toY) {
                aw.toX = (float)fromX;
                aw.toY = (float)fromY;
                foundAw = aw;
                break;
            }
        }

        if (foundAw == null) {
            AutoWall aw = state.autoWalls.take();

            aw.fromX = (float)fromX;
            aw.fromY = (float)fromY;
            aw.toX = (float)toX;
            aw.toY = (float)toY;
            aw.vert = vert;
            aw.type = type;
            aw.doorUid = -1;
            aw.door = null;

            return;
        }

        for (; ; ) {
            AutoWall nextFoundAw = null;

            for (AutoWall aw = state.autoWalls.first(); aw != null; aw = aw.next) {
                if ((aw == foundAw) || (aw.door != null) || (aw.vert != foundAw.vert) || (aw.type != foundAw.type)) {
                    continue;
                }

                if ((int)aw.fromX == (int)foundAw.fromX && (int)aw.fromY == (int)foundAw.fromY) {
                    aw.fromX = foundAw.toX;
                    aw.fromY = foundAw.toY;
                    nextFoundAw = aw;
                    break;
                } else if ((int)aw.toX == (int)foundAw.fromX && (int)aw.toY == (int)foundAw.fromY) {
                    aw.toX = foundAw.toX;
                    aw.toY = foundAw.toY;
                    nextFoundAw = aw;
                    break;
                } else if ((int)aw.fromX == (int)foundAw.toX && (int)aw.fromY == (int)foundAw.toY) {
                    aw.fromX = foundAw.fromX;
                    aw.fromY = foundAw.fromY;
                    nextFoundAw = aw;
                    break;
                } else if ((int)aw.toX == (int)foundAw.toX && (int)aw.toY == (int)foundAw.toY) {
                    aw.toX = foundAw.fromX;
                    aw.toY = foundAw.fromY;
                    nextFoundAw = aw;
                    break;
                }
            }

            if (nextFoundAw == null) {
                break;
            }

            state.autoWalls.release(foundAw);
            foundAw = nextFoundAw;
        }
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    private void renderWalls() {
        renderer.startBatch();
        renderer.setCoordsQuadBillboardZ(-HALF_WALL, HALF_WALL);

        PortalTracer.Wall[] localWalls = tracer.walls;
        Door[][] localDoorsMap = level.doorsMap;
        int[][] localDrawnAutoWalls = state.drawnAutoWalls;

        for (int i = 0, len = tracer.wallsCount; i < len; i++) {
            int autoWallMask;
            Door door;
            PortalTracer.Wall wall = localWalls[i];

            if (wall.fromX == wall.toX) {
                door = (wall.fromY < wall.toY
                        ? localDoorsMap[wall.fromY][wall.fromX - 1]
                        : localDoorsMap[wall.toY][wall.fromX]);

                autoWallMask = AUTO_WALL_MASK_VERTICAL;
            } else {
                door = (wall.fromX < wall.toX
                        ? localDoorsMap[wall.fromY][wall.fromX]
                        : localDoorsMap[wall.fromY - 1][wall.toX]);

                autoWallMask = AUTO_WALL_MASK_HORIZONTAL;
            }

            // by the way, mx and my *not* always equal to wall.cellX and wall.cellY
            int mx = (wall.fromX < wall.toX ? wall.fromX : wall.toX);
            int my = (wall.fromY < wall.toY ? wall.fromY : wall.toY);

            if (((localDrawnAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
                localDrawnAutoWalls[my][mx] |= autoWallMask;
                appendAutoWall(wall.fromX, wall.fromY, wall.toX, wall.toY, AUTO_WALL_TYPE_WALL);
            }

            renderer.setCoordsQuadBillboard((float)wall.fromX, (float)wall.fromY, (float)wall.toX, (float)wall.toY);

            setWallLighting(
                    (float)wall.fromX,
                    (float)wall.fromY,
                    (float)wall.toX,
                    (float)wall.toY,
                    (wall.fromX == wall.toX));

            if (door != null) {
                renderer.batchTexQuad(door.texture + TextureLoader.BASE_DOORS_S);
            } else if (wall.flipTexture) {
                renderer.batchTexQuadFlipped(wall.texture);
            } else {
                renderer.batchTexQuad(wall.texture);
            }
        }

        TouchedCell[] localTouchedCells = tracer.touchedCells;

        for (int i = 0, len = tracer.touchedCellsCountPriorToPostProcess; i < len; i++) {
            TouchedCell tc = localTouchedCells[i];

            if (!awTouchedCellsMap[tc.y][tc.x] && state.awTouchedCells.canTake()) {
                awTouchedCellsMap[tc.y][tc.x] = true;
                state.awTouchedCells.take().copyFrom(tc);
            }
        }

        renderer.renderBatch(
                Renderer.FLAG_CULL
                        | Renderer.FLAG_DEPTH
                        | Renderer.FLAG_ALPHA
                        | Renderer.FLAG_SMOOTH,
                Renderer.TEXTURE_MAIN);
    }

    @SuppressWarnings("MagicNumber")
    private void renderDoors() {
        renderer.startBatch();
        renderer.setCoordsQuadBillboardZ(-HALF_WALL, HALF_WALL);

        TouchedCell[] localTouchedCells = tracer.touchedCells;
        Door[][] localDoorsMap = level.doorsMap;
        int[][] localDrawnAutoWalls = state.drawnAutoWalls;

        for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
            TouchedCell tc = localTouchedCells[i];
            Door door = localDoorsMap[tc.y][tc.x];

            if (door == null) {
                continue;
            }

            float fromX;
            float fromY;
            float toX;
            float toY;

            float fromX1;
            float fromY1;
            float toX1;
            float toY1;

            float fromX2;
            float fromY2;
            float toX2;
            float toY2;

            if (door.vert) {
                fromX = (float)door.x + 0.5f;
                toX = fromX;
                fromY = (float)door.y;
                toY = fromY + 1.0f;
            } else {
                fromX = (float)door.x;
                toX = fromX + 1.0f;
                fromY = (float)door.y + 0.5f;
                toY = fromY;
            }

            if ((localDrawnAutoWalls[door.y][door.x] & AUTO_WALL_MASK_DOOR) == 0 && state.autoWalls.canTake()) {
                localDrawnAutoWalls[door.y][door.x] |= AUTO_WALL_MASK_DOOR;
                AutoWall aw = state.autoWalls.take();

                aw.fromX = fromX;
                aw.fromY = fromY;
                aw.toX = toX;
                aw.toY = toY;
                aw.vert = door.vert;
                aw.type = AUTO_WALL_TYPE_DOOR;
                aw.doorUid = door.uid;
                aw.door = door;
            }

            if (door.vert) {
                fromY += door.openPos;
                toY += door.openPos;

                fromX1 = fromX - HALF_DOOR_WIDTH;
                fromY1 = fromY;
                toX1 = fromX1;
                toY1 = toY;

                fromX2 = fromX + HALF_DOOR_WIDTH;
                fromY2 = fromY;
                toX2 = fromX2;
                toY2 = toY;
            } else {
                fromX += door.openPos;
                toX += door.openPos;

                fromX1 = fromX;
                fromY1 = fromY - HALF_DOOR_WIDTH;
                toX1 = toX;
                toY1 = fromY1;

                fromX2 = fromX;
                fromY2 = fromY + HALF_DOOR_WIDTH;
                toX2 = toX;
                toY2 = fromY2;
            }

            setWallLighting(fromX, fromY, toX, toY, door.vert);

            renderer.setCoordsQuadBillboard(fromX1, fromY1, toX1, toY1);
            renderer.batchTexQuad(door.texture + TextureLoader.BASE_DOORS_F);

            renderer.setCoordsQuadBillboard(fromX2, fromY2, toX2, toY2);
            renderer.batchTexQuad(door.texture + TextureLoader.BASE_DOORS_F);

            setWallLighting(fromX1, fromY1, fromX2, fromY2, !door.vert);

            renderer.r1 *= EXTRUDE_LIGHT_MULT;
            renderer.g1 *= EXTRUDE_LIGHT_MULT;
            renderer.b1 *= EXTRUDE_LIGHT_MULT;

            renderer.r2 *= EXTRUDE_LIGHT_MULT;
            renderer.g2 *= EXTRUDE_LIGHT_MULT;
            renderer.b2 *= EXTRUDE_LIGHT_MULT;

            renderer.r3 *= EXTRUDE_LIGHT_MULT;
            renderer.g3 *= EXTRUDE_LIGHT_MULT;
            renderer.b3 *= EXTRUDE_LIGHT_MULT;

            renderer.r4 *= EXTRUDE_LIGHT_MULT;
            renderer.g4 *= EXTRUDE_LIGHT_MULT;
            renderer.b4 *= EXTRUDE_LIGHT_MULT;

            renderer.setCoordsQuadBillboard(fromX1, fromY1, fromX2, fromY2);
            renderer.batchTexQuadExtruded(door.texture + TextureLoader.BASE_DOORS_S);
        }

        renderer.renderBatch(Renderer.FLAG_DEPTH | Renderer.FLAG_SMOOTH, Renderer.TEXTURE_MAIN);
    }

    // render objects, decorations and transparents
    @SuppressWarnings({ "MagicNumber", "ManualMinMaxCalculation" })
    private void batchObjects() {
        TouchedCell[] localTouchedCells = tracer.touchedCells;
        int[][] localPassableMap = state.passableMap;
        int[][] localTexMap = state.texMap;
        int[][] localDrawnAutoWalls = state.drawnAutoWalls;

        renderer.setCoordsQuadBillboardZ(-HALF_WALL, HALF_WALL);

        float objOffX = -sightDx * GameMath.SIGHT_OFFSET;
        float objOffY = -sightDy * GameMath.SIGHT_OFFSET;

        for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
            TouchedCell tc = localTouchedCells[i];
            int pass = localPassableMap[tc.y][tc.x];
            int tex = localTexMap[tc.y][tc.x];

            if ((pass & Level.PASSABLE_IS_OBJECT) != 0) {
                ObjectContainer container = state.objectsMap.get(tc.y).get(tc.x);
                int objectsCount = container.count;

                if (objectsCount == 0) {
                    // should not happen
                    continue;
                }

                float mx = (float)tc.x + 0.5f;
                float my = (float)tc.y + 0.5f;

                renderer.setColorQuadLight(getLightness(mx, my));

                if (objectsCount == 1) {
                    renderer.setCoordsQuadBillboard(
                            mx - billboardDx + objOffX,
                            my - billboardDy + objOffY,
                            mx + billboardDx + objOffX,
                            my + billboardDy + objOffY);

                    renderer.batchTexQuad(container.get(0));
                } else {
                    for (int objIndex = 0; objIndex < objectsCount; objIndex++) {
                        float oa = (float)objIndex * GameMath.PI_M2F / (float)objectsCount;

                        float omx = mx + (float)Math.cos(oa) * 0.25f;
                        float omy = my + (float)Math.sin(oa) * 0.25f;

                        renderer.setCoordsQuadBillboard(
                                omx - billboardDx,
                                omy - billboardDy,
                                omx + billboardDx,
                                omy + billboardDy);

                        renderer.batchTexQuad(container.get(objIndex));
                    }
                }

                continue;
            }

            if ((pass & Level.PASSABLE_MASK_DECORATION) != 0) {
                float mx = (float)tc.x + 0.5f;
                float my = (float)tc.y + 0.5f;

                renderer.setCoordsQuadBillboard(
                        mx - billboardDx,
                        my - billboardDy,
                        mx + billboardDx,
                        my + billboardDy);

                renderer.setColorQuadLight(getLightness(mx, my));
                renderer.batchTexQuad(tex);
                continue;
            }

            if ((pass & Level.PASSABLE_IS_TRANSP_WINDOW) != 0) {
                float fromX;
                float fromY;
                float toX;
                float toY;

                boolean vert = ((pass & Level.PASSABLE_IS_TRANSP_WINDOW_VERT) != 0);

                if (vert) {
                    fromX = (float)tc.x + 0.5f;
                    toX = fromX;
                    fromY = (float)tc.y;
                    toY = fromY + 1.0f;
                } else {
                    fromX = (float)tc.x;
                    toX = fromX + 1.0f;
                    fromY = (float)tc.y + 0.5f;
                    toY = fromY;
                }

                if ((localDrawnAutoWalls[tc.y][tc.x] & AUTO_WALL_MASK_DOOR) == 0 && state.autoWalls.canTake()) {
                    localDrawnAutoWalls[tc.y][tc.x] |= AUTO_WALL_MASK_DOOR;
                    AutoWall aw = state.autoWalls.take();

                    aw.fromX = fromX;
                    aw.fromY = fromY;
                    aw.toX = toX;
                    aw.toY = toY;
                    aw.vert = vert;
                    aw.type = AUTO_WALL_TYPE_TRANSP;
                    aw.doorUid = -1;
                    aw.door = null;
                }

                renderer.setCoordsQuadBillboard(fromX, fromY, toX, toY);
                setWallLighting(fromX, fromY, toX, toY, vert);
                renderer.batchTexQuad(tex);

                continue;
            }

            if ((pass & Level.PASSABLE_IS_TRANSP) != 0
                    && (pass & Level.PASSABLE_IS_NOTRANS) == 0
                    && tex != 0) {

                for (int s = 0; s < 4; s++) {
                    if ((localPassableMap[tc.y + PortalTracer.Y_CELL_ADD[s]][tc.x + PortalTracer.X_CELL_ADD[s]]
                            & Level.PASSABLE_MASK_WALL_N_TRANSP) == 0) {

                        int fromX = tc.x + PortalTracer.X_ADD[s];
                        int fromY = tc.y + PortalTracer.Y_ADD[s];
                        int toX = tc.x + PortalTracer.X_ADD[(s + 1) % 4];
                        int toY = tc.y + PortalTracer.Y_ADD[(s + 1) % 4];

                        renderer.setCoordsQuadBillboard((float)fromX, (float)fromY, (float)toX, (float)toY);

                        int mx = (fromX < toX ? fromX : toX);
                        int my = (fromY < toY ? fromY : toY);
                        int autoWallMask = ((s == 1 || s == 3) ? AUTO_WALL_MASK_VERTICAL : AUTO_WALL_MASK_HORIZONTAL);

                        if (((localDrawnAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
                            localDrawnAutoWalls[my][mx] |= autoWallMask;
                            appendAutoWall(fromX, fromY, toX, toY, AUTO_WALL_TYPE_TRANSP);
                        }

                        setWallLighting((float)fromX, (float)fromY, (float)toX, (float)toY, (s == 1 || s == 3));

                        if (s == 0 || s == 3) {
                            renderer.batchTexQuadFlipped(tex);
                        } else {
                            renderer.batchTexQuad(tex);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("MagicNumber")
    private void renderMonsters(long elapsedTime, int texIdx, boolean deadCorpses) {
        renderer.startBatch();
        renderer.setCoordsQuadBillboardZ(-HALF_WALL, HALF_WALL * MONSTER_SIZE_MULT);

        int fromTex = texIdx * TextureLoader.MONSTERS_IN_TEXTURE * TextureLoader.COUNT_MONSTER;
        int toTex = fromTex + TextureLoader.MONSTERS_IN_TEXTURE * TextureLoader.COUNT_MONSTER - 1;

        boolean[][] tcMap = tracer.touchedCellsMap;

        float offX = deadCorpses ? sightDx * GameMath.SIGHT_OFFSET : 0.0f;
        float offY = deadCorpses ? sightDy * GameMath.SIGHT_OFFSET : 0.0f;

        for (Monster mon = state.monsters.first(); mon != null; mon = mon.next) {
            if ((deadCorpses && mon.health > 0)
                    || (!deadCorpses && mon.health <= 0)
                    || mon.texture < fromTex
                    || mon.texture > toTex) {

                continue;
            }

            if (!tcMap[mon.cellY][mon.cellX]
                    && !(mon.prevY >= 0 && mon.prevX >= 0 && tcMap[mon.prevY][mon.prevX])) {

                continue;
            }

            int tex = mon.texture - fromTex;

            if (mon.health > 0) {
                if ((mon.stunTicks <= 0) && (mon.attackTicks > 0)) {
                    tex += 15;
                } else {
                    if (mon.isAimedOnHero) {
                        tex += 2;
                    } else if (mon.chaseMode) {
                        tex += ((((int)currentHeroA + 360 + 45 - mon.dir * 90) % 360) / 90);
                    }

                    if (mon.stunTicks > 0) {
                        tex += 8;
                    } else if (!mon.isInAttackState && ((elapsedTime % 800) > 400)) {
                        tex += 4;
                    }
                }
            } else {
                if (mon.dieTime == 0) {
                    mon.dieTime = elapsedTime;
                }

                tex += 12 + (mon.dieTime < 0 ? 2 : Math.min(2, (elapsedTime - mon.dieTime) / 150));
            }

            renderer.setCoordsQuadBillboard(
                    mon.x - billboardDx * MONSTER_SIZE_MULT + offX,
                    mon.y - billboardDy * MONSTER_SIZE_MULT + offY,
                    mon.x + billboardDx * MONSTER_SIZE_MULT + offX,
                    mon.y + billboardDy * MONSTER_SIZE_MULT + offY);

            renderer.setColorQuadLight(getLightness(mon.x, mon.y));
            renderer.batchTexQuadMon(tex);
        }

        renderer.renderBatch(
                Renderer.FLAG_DEPTH | Renderer.FLAG_ALPHA | Renderer.FLAG_SMOOTH,
                Renderer.TEXTURE_MONSTERS + texIdx);
    }

    private void renderArrows() {
        TouchedCell[] localTouchedCells = tracer.touchedCells;
        int[][] localWallsMap = state.wallsMap;
        int[][] localArrowsMap = state.arrowsMap;

        renderer.startBatch();
        renderer.setCoordsQuadZ(-HALF_WALL + GameMath.SIGHT_OFFSET);

        for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
            TouchedCell tc = localTouchedCells[i];
            int arrowTex = localArrowsMap[tc.y][tc.x];

            if ((localWallsMap[tc.y][tc.x] > 0) || (arrowTex == 0)) {
                continue;
            }

            float fx = (float)tc.x;
            float tx = (float)(tc.x + 1);
            float fy = (float)tc.y;
            float ty = (float)(tc.y + 1);

            renderer.setColorQuadLight(
                    getLightness(fx, ty),
                    getLightness(fx, fy),
                    getLightness(tx, fy),
                    getLightness(tx, ty));

            renderer.setCoordsQuadRect(fx, -ty, tx, -fy);
            renderer.batchTexQuad(arrowTex);
        }

        renderer.renderBatch(Renderer.FLAG_DEPTH | Renderer.FLAG_ALPHA | Renderer.FLAG_SMOOTH, Renderer.TEXTURE_MAIN);
    }

    @SuppressWarnings({ "MagicNumber" })
    private void renderFloorAndCeil() {
        TouchedCell[] localTouchedCells = tracer.touchedCells;
        int[][] localWallsMap = state.wallsMap;
        int[][] localFloorMap1 = state.floorMap1;
        int[][] localFloorMap2 = state.floorMap2;
        int[][] localFloorMap3 = state.floorMap3;
        int[][] localFloorMap4 = state.floorMap4;
        int[][] localCeilMap1 = state.ceilMap1;
        int[][] localCeilMap2 = state.ceilMap2;
        int[][] localCeilMap3 = state.ceilMap3;
        int[][] localCeilMap4 = state.ceilMap4;

        renderer.startBatch();

        for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
            TouchedCell tc = localTouchedCells[i];

            int floorTex1 = localFloorMap1[tc.y][tc.x];
            int floorTex2 = localFloorMap2[tc.y][tc.x];
            int floorTex3 = localFloorMap3[tc.y][tc.x];
            int floorTex4 = localFloorMap4[tc.y][tc.x];

            int ceilTex1 = localCeilMap1[tc.y][tc.x];
            int ceilTex2 = localCeilMap2[tc.y][tc.x];
            int ceilTex3 = localCeilMap3[tc.y][tc.x];
            int ceilTex4 = localCeilMap4[tc.y][tc.x];

            float fx = (float)tc.x;
            float tx = (float)(tc.x + 1);
            float fy = (float)tc.y;
            float ty = (float)(tc.y + 1);
            float mx = fx + 0.5f;
            float my = fy + 0.5f;

            float lff = getLightness(fx, fy);
            float lft = getLightness(fx, ty);
            float ltf = getLightness(tx, fy);
            float ltt = getLightness(tx, ty);
            float lfm = getLightness(fx, my);
            float lmf = getLightness(mx, fy);
            float lmm = getLightness(mx, my);
            float lmt = getLightness(mx, ty);
            float ltm = getLightness(tx, my);

            fy = -fy;
            ty = -ty;
            my = -my;

            if (localWallsMap[tc.y][tc.x] <= 0) {
                // render floor
                renderer.setCoordsQuadZ(-HALF_WALL);

                if (floorTex1 == floorTex2 && floorTex1 == floorTex3 && floorTex1 == floorTex4) {
                    if (floorTex1 != 0) {
                        renderer.setColorQuadLight(lft, lff, ltf, ltt);
                        renderer.setCoordsQuadRect(fx, ty, tx, fy);
                        renderer.batchTexQuad(floorTex1);
                    }
                } else {
                    if (floorTex1 != 0) {
                        renderer.setColorQuadLight(lfm, lff, lmf, lmm);
                        renderer.setCoordsQuadRect(fx, my, mx, fy);
                        renderer.batchTexQuadPieceTL(floorTex1);
                    }

                    if (floorTex2 != 0) {
                        renderer.setColorQuadLight(lmm, lmf, ltf, ltm);
                        renderer.setCoordsQuadRect(mx, my, tx, fy);
                        renderer.batchTexQuadPieceTR(floorTex2);
                    }

                    if (floorTex3 != 0) {
                        renderer.setColorQuadLight(lft, lfm, lmm, lmt);
                        renderer.setCoordsQuadRect(fx, ty, mx, my);
                        renderer.batchTexQuadPieceBL(floorTex3);
                    }

                    if (floorTex4 != 0) {
                        renderer.setColorQuadLight(lmt, lmm, ltm, ltt);
                        renderer.setCoordsQuadRect(mx, ty, tx, my);
                        renderer.batchTexQuadPieceBR(floorTex4);
                    }
                }

                // render ceil
                renderer.setCoordsQuadZ(HALF_WALL);

                if (ceilTex1 == ceilTex2 && ceilTex1 == ceilTex3 && ceilTex1 == ceilTex4) {
                    if (ceilTex1 != 0) {
                        renderer.setColorQuadLight(ltt, ltf, lff, lft);
                        renderer.setCoordsQuadRect(tx, ty, fx, fy);
                        renderer.batchTexQuadFlipped(ceilTex1);
                    }
                } else {
                    if (ceilTex1 != 0) {
                        renderer.setColorQuadLight(lmm, lmf, lff, lmt);
                        renderer.setCoordsQuadRect(mx, my, fx, fy);
                        renderer.batchTexQuadFlippedPartTL(ceilTex1);
                    }

                    if (ceilTex2 != 0) {
                        renderer.setColorQuadLight(ltm, ltf, lmf, lmm);
                        renderer.setCoordsQuadRect(tx, my, mx, fy);
                        renderer.batchTexQuadFlippedPartTR(ceilTex2);
                    }

                    if (ceilTex3 != 0) {
                        renderer.setColorQuadLight(lmt, lmm, lfm, lft);
                        renderer.setCoordsQuadRect(mx, ty, fx, my);
                        renderer.batchTexQuadFlippedPartBL(ceilTex3);
                    }

                    if (ceilTex4 != 0) {
                        renderer.setColorQuadLight(ltt, ltm, lmm, lmt);
                        renderer.setCoordsQuadRect(tx, ty, mx, my);
                        renderer.batchTexQuadFlippedPartBR(ceilTex4);
                    }
                }
            }

            // render extrude

            // 2l | 1  2
            // 4l | 3  4
            // ---+------
            //    | 1d 2d

            renderer.z1 = HALF_WALL_PLUS_EXTRUDE;
            renderer.z2 = HALF_WALL_PLUS_EXTRUDE;
            renderer.z3 = HALF_WALL;
            renderer.z4 = HALF_WALL;

            lff *= EXTRUDE_LIGHT_MULT;
            lft *= EXTRUDE_LIGHT_MULT;
            // "ltf" is not used below
            ltt *= EXTRUDE_LIGHT_MULT;
            lfm *= EXTRUDE_LIGHT_MULT;
            lmf *= EXTRUDE_LIGHT_MULT;
            lmm *= EXTRUDE_LIGHT_MULT;
            lmt *= EXTRUDE_LIGHT_MULT;
            ltm *= EXTRUDE_LIGHT_MULT;

            if (tc.x > 0) {
                int ceilTex2l = localCeilMap2[tc.y][tc.x - 1];
                int ceilTex4l = localCeilMap4[tc.y][tc.x - 1];

                if ((ceilTex2l == 0) == (ceilTex1 != 0)) {
                    renderer.setColorQuadLight(lff, lfm, lfm, lff);
                    renderer.setCoordsQuadRect(fx, fy, fx, my);

                    if (ceilTex1 != 0) {
                        renderer.batchTexQuadPieceTL(ceilTex1);
                    } else {
                        renderer.batchTexQuadPieceTR(ceilTex2l);
                    }
                }

                if ((ceilTex4l == 0) == (ceilTex3 != 0)) {
                    renderer.setColorQuadLight(lfm, lft, lft, lfm);
                    renderer.setCoordsQuadRect(fx, my, fx, ty);

                    if (ceilTex3 != 0) {
                        renderer.batchTexQuadPieceBL(ceilTex3);
                    } else {
                        renderer.batchTexQuadPieceBR(ceilTex4l);
                    }
                }
            }

            if (tc.y < (state.levelHeight - 1)) {
                int ceilTex1d = localCeilMap1[tc.y + 1][tc.x];
                int ceilTex2d = localCeilMap2[tc.y + 1][tc.x];

                if ((ceilTex1d == 0) == (ceilTex3 != 0)) {
                    renderer.setColorQuadLight(lft, lmt, lmt, lft);
                    renderer.setCoordsQuadRectFlip(fx, ty, mx, ty);

                    if (ceilTex3 != 0) {
                        renderer.batchTexQuadPieceBL(ceilTex3);
                    } else {
                        renderer.batchTexQuadPieceBL(ceilTex1d);
                    }
                }

                if ((ceilTex2d == 0) == (ceilTex4 != 0)) {
                    renderer.setColorQuadLight(lmt, ltt, ltt, lmt);
                    renderer.setCoordsQuadRectFlip(mx, ty, tx, ty);

                    if (ceilTex4 != 0) {
                        renderer.batchTexQuadPieceBR(ceilTex4);
                    } else {
                        renderer.batchTexQuadPieceTR(ceilTex2d);
                    }
                }
            }

            if ((ceilTex1 == 0) == (ceilTex2 != 0)) {
                renderer.setColorQuadLight(lmf, lmm, lmm, lmf);
                renderer.setCoordsQuadRect(mx, fy, mx, my);

                if (ceilTex2 != 0) {
                    renderer.batchTexQuadPieceTR(ceilTex2);
                } else {
                    renderer.batchTexQuadPieceTL(ceilTex1);
                }
            }

            if ((ceilTex3 == 0) == (ceilTex4 != 0)) {
                renderer.setColorQuadLight(lmm, lmt, lmt, lmm);
                renderer.setCoordsQuadRect(mx, my, mx, ty);

                if (ceilTex4 != 0) {
                    renderer.batchTexQuadPieceBR(ceilTex4);
                } else {
                    renderer.batchTexQuadPieceBL(ceilTex3);
                }
            }

            if ((ceilTex1 == 0) == (ceilTex3 != 0)) {
                renderer.setColorQuadLight(lfm, lmm, lmm, lfm);
                renderer.setCoordsQuadRectFlip(fx, my, mx, my);

                if (ceilTex3 != 0) {
                    renderer.batchTexQuadPieceBL(ceilTex3);
                } else {
                    renderer.batchTexQuadPieceTL(ceilTex1);
                }
            }

            if ((ceilTex2 == 0) == (ceilTex4 != 0)) {
                renderer.setColorQuadLight(lmm, ltm, ltm, lmm);
                renderer.setCoordsQuadRectFlip(mx, my, tx, my);

                if (ceilTex4 != 0) {
                    renderer.batchTexQuadPieceBR(ceilTex4);
                } else {
                    renderer.batchTexQuadPieceTR(ceilTex2);
                }
            }
        }

        renderer.renderBatch(Renderer.FLAG_DEPTH | Renderer.FLAG_SMOOTH, Renderer.TEXTURE_MAIN);
    }

    @SuppressWarnings("MagicNumber")
    private void batchBullets() {
        boolean[][] localTouchedCellsMap = tracer.touchedCellsMap;

        for (Bullet bullet = state.bullets.first(); bullet != null; bullet = bullet.next) {
            int tex = bullet.getTexture();

            if (tex < 0 || !localTouchedCellsMap[(int)bullet.y][(int)bullet.x]) {
                continue;
            }

            float offZ = HALF_WALL * 0.5f * (float)Math.sin(bullet.dist / bullet.params.maxDist * Math.PI * 1.5);

            renderer.setCoordsQuadBillboard(
                    bullet.x - billboardDx,
                    bullet.y - billboardDy,
                    bullet.x + billboardDx,
                    bullet.y + billboardDy,
                    -HALF_WALL + offZ,
                    HALF_WALL + offZ);

            renderer.setColorQuadLight(getLightness(bullet.x, bullet.y));
            renderer.batchTexQuad(TextureLoader.BASE_BULLETS + tex);
        }
    }

    private void batchExplosions() {
        boolean[][] localTouchedCellsMap = tracer.touchedCellsMap;
        renderer.setCoordsQuadBillboardZ(-HALF_WALL, HALF_WALL);

        for (Explosion explosion = state.explosions.first(); explosion != null; explosion = explosion.next) {
            int tex = explosion.getTexture();

            if (tex < 0 || !localTouchedCellsMap[(int)explosion.y][(int)explosion.x]) {
                continue;
            }

            renderer.setCoordsQuadBillboard(
                    explosion.x - billboardDx,
                    explosion.y - billboardDy,
                    explosion.x + billboardDx,
                    explosion.y + billboardDy);

            renderer.setColorQuadLight(getLightness(explosion.x, explosion.y));
            renderer.batchTexQuad(TextureLoader.BASE_EXPLOSIONS + tex);
        }
    }

    @SuppressWarnings("MagicNumber")
    private void renderBulletTraces() {
        renderer.startBatch();
        renderer.setColorQuadRGB(1.0f, 1.0f, 1.0f);

        for (BulletTrace bulletTrace = bulletTraces.first(); bulletTrace != null; ) {
            BulletTrace nextBulletTrace = bulletTrace.next;

            float dx = bulletTrace.x - currentHeroX;
            float dy = bulletTrace.y - currentHeroY;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            float scale = BTRACE_MIN_SCALE + (BTRACE_MAX_SCALE - BTRACE_MIN_SCALE) * (dist / BTRACE_MAX_DIST);

            float x = bulletTrace.x + billboardDx * bulletTrace.off - sightDx * GameMath.SIGHT_OFFSET;
            float y = bulletTrace.y + billboardDy * bulletTrace.off - sightDy * GameMath.SIGHT_OFFSET;
            float z = bulletTrace.z * -HALF_WALL;

            renderer.setCoordsQuadBillboard(
                    x - billboardDx * scale,
                    y - billboardDy * scale,
                    x + billboardDx * scale,
                    y + billboardDy * scale,
                    z - HALF_WALL * scale * 1.25f,
                    z + HALF_WALL * scale * 1.25f);

            renderer.setColorQuadA((float)bulletTrace.ticks / (float)BulletTrace.MAX_TICKS);
            renderer.batchQuad();

            bulletTrace.ticks--;

            if (bulletTrace.ticks <= 0) {
                bulletTraces.release(bulletTrace);
            }

            bulletTrace = nextBulletTrace;
        }

        renderer.renderBatch(Renderer.FLAG_DEPTH | Renderer.FLAG_BLEND);
    }

    @SuppressWarnings("MagicNumber")
    public void render(long elapsedTime, float ypos, float xrot) {
        currentHeroX = state.heroX;
        currentHeroY = state.heroY;
        currentHeroA = state.heroA;

        sightDx = engine.heroCs; // == cos(-heroAr);
        sightDy = -engine.heroSn; // == sin(-heroAr);

        billboardDx = sightDy * -0.5f;
        billboardDy = sightDx * 0.5f;

        for (Door door = state.doors.first(); door != null; door = door.next) {
            door.updateBeforeRender();
        }

        tracer.trace(currentHeroX, currentHeroY, engine.heroAr, 44.0f * GameMath.G2RAD_F);

        renderer.useFrustum(
                -FRUSTUM_SIZE,
                FRUSTUM_SIZE,
                -FRUSTUM_SIZE / engine.ratio,
                FRUSTUM_SIZE / engine.ratio,
                0.1f,
                100.0f);

        renderer.gl.glTranslatef(0.0f, ypos, -0.1f);
        renderer.gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
        renderer.gl.glRotatef(90.0f - currentHeroA, 0.0f, 0.0f, 1.0f);
        renderer.gl.glTranslatef(-currentHeroX, currentHeroY, 0.0f);

        renderer.setColorQuadA(1.0f);

        renderArrows();
        renderFloorAndCeil();
        renderWalls();
        renderDoors();

        renderMonsters(elapsedTime, 0, false);
        renderMonsters(elapsedTime, 1, false);

        renderer.startBatch();

        batchExplosions();
        batchBullets();
        batchObjects();

        renderer.renderBatch(
                Renderer.FLAG_DEPTH | Renderer.FLAG_ALPHA | Renderer.FLAG_SMOOTH,
                Renderer.TEXTURE_MAIN);

        renderMonsters(elapsedTime, 0, true);
        renderMonsters(elapsedTime, 1, true);

        renderBulletTraces();
    }
}
