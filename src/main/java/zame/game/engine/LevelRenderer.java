package zame.game.engine;

import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.data.DataList;

public class LevelRenderer implements EngineObject {
    private static final int MAX_LIGHT_TAB = 1000;
    private static final int LAST_LIGHT_TAB = MAX_LIGHT_TAB - 1;
    private static final float LIGHT_TAB_MULT = MAX_LIGHT_TAB / 10.0f;

    public static final int AUTO_WALL_TYPE_WALL = 0;
    public static final int AUTO_WALL_TYPE_TRANSP = 1;
    public static final int AUTO_WALL_TYPE_DOOR = 2;

    public static final int AUTO_WALL_MASK_HORIZONTAL = 1;
    public static final int AUTO_WALL_MASK_VERTICAL = 2;
    public static final int AUTO_WALL_MASK_DOOR = 4;

    static final int MAX_AUTO_WALLS = Level.MAX_WIDTH * Level.MAX_HEIGHT * 2;
    static final int MAX_AW_CELLS = Level.MAX_WIDTH * Level.MAX_HEIGHT;
    static final float HALF_WALL = 0.4f;
    static final float LIGHT_OBJECT = 1.0f;

    private static final float HALF_WALL_PLUS_EXTRUDE = HALF_WALL + 0.05f;
    private static final float HALF_DOOR_WIDTH = 0.025f;
    private static final float LIGHT_DOOR = 0.5f;
    private static final float LIGHT_LAMP = 1.0f;
    private static final float LIGHT_ARROW = 1.0f;
    private static final float MONSTER_SIZE_MULT = 0.75f;
    private static final float EXTRUDE_LIGHT_MULT = 0.5f;

    public PortalTracer tracer = new PortalTracer();

    public boolean debugOnAutomap;
    public boolean[][] awTouchedCellsMap = new boolean[Level.MAX_HEIGHT][Level.MAX_WIDTH];
    public DataList<TraceInfo> tracesInfo = new DataList<>(TraceInfo.class, Level.MAX_BULLETS * 3);

    private Engine engine;
    private State state;
    private Renderer renderer;
    private Level level;
    private TextureLoader textureLoader;
    private float flatObjDx;
    private float flatObjDy;
    private float[] lightTab = new float[MAX_LIGHT_TAB];
    public float currentHeroX;
    public float currentHeroY;
    public float currentHeroA;
    private float currentHeroCs;
    private float currentHeroSn;
    private float[][] lightMap = new float[Level.MAX_HEIGHT * 2 + 1][Level.MAX_WIDTH * 2 + 1];

    LevelRenderer() {
        final float ambient = 0.5f;
        final float heroLightness = 0.2f;

        for (int i = 0; i < MAX_LIGHT_TAB; i++) {
            lightTab[i] = (float)Math.sin(((float)i / (float)MAX_LIGHT_TAB) * GameMath.PI_F + GameMath.PI_D2F)
                    * heroLightness + ambient;
        }
    }

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
        this.state = engine.state;
        this.renderer = engine.renderer;
        this.level = engine.level;
        this.textureLoader = engine.textureLoader;

        tracer.setEngine(engine);
    }

    void updateAfterLoadOrCreate() {
        for (AutoWall aw = state.autoWalls.first(); aw != null; aw = (AutoWall)aw.next) {
            if (aw.doorUid >= 0) {
                for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
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

        for (Bullet bullet = state.bullets.first(); bullet != null; bullet = (Bullet)bullet.next) {
            if (bullet.monUid >= 0) {
                for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
                    if (mon.uid == bullet.monUid) {
                        bullet.mon = mon;
                        break;
                    }
                }
            }
        }

        for (Explosion explosion = state.explosions.first(); explosion != null; explosion = (Explosion)explosion.next) {
            if (explosion.monUid >= 0) {
                for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
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

        for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
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

    void modLightMap(int cx, int cy, float val) {
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

    @SuppressWarnings("MagicNumber")
    private void updateDoors(long elapsedTime) {
        for (Door door = state.doors.first(); door != null; door = (Door)door.next) {
            if (door.dir != 0) {
                if (door.dir > 0) {
                    door.openPos = (float)(elapsedTime - door.lastTime) / 300.0f;
                } else {
                    door.openPos = Door.OPEN_POS_MAX - (float)(elapsedTime - door.lastTime) / 200.0f;
                }

                door.update(elapsedTime);
            }
        }
    }

    @SuppressWarnings("MagicNumber")
    private float getLightness(float x, float y) {
        int d = (int)(((x - currentHeroX) * currentHeroCs - (y - currentHeroY) * currentHeroSn - 0.5f)
                * LIGHT_TAB_MULT);

        try {
            return lightTab[d < 0 ? 0 : (d > LAST_LIGHT_TAB ? LAST_LIGHT_TAB : d)] + lightMap[(int)((y + 0.25f)
                    * 2.0f)][(int)((x + 0.25f) * 2.0f)];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0.0f;
        }
    }

    private void setObjLighting(float x, float y) {
        float l = getLightness(x, y);

        renderer.r1 = l;
        renderer.g1 = l;
        renderer.b1 = l;
        renderer.r2 = l;
        renderer.g2 = l;
        renderer.b2 = l;
        renderer.r3 = l;
        renderer.g3 = l;
        renderer.b3 = l;
        renderer.r4 = l;
        renderer.g4 = l;
        renderer.b4 = l;
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

        float l1 = getLightness(fromX, fromY) * l;
        renderer.r1 = l1;
        renderer.g1 = l1;
        renderer.b1 = l1;
        renderer.r2 = l1;
        renderer.g2 = l1;
        renderer.b2 = l1;

        float l2 = getLightness(toX, toY) * l;
        renderer.r3 = l2;
        renderer.g3 = l2;
        renderer.b3 = l2;
        renderer.r4 = l2;
        renderer.g4 = l2;
        renderer.b4 = l2;
    }

    // This method:
    // did *not* check for available space (MAX_AUTO_WALLS),
    // did *not* check if wall already exists,
    // did *not* append wall mask,
    // did *not* add doors
    public void appendAutoWall(int fromX, int fromY, int toX, int toY, int type) {
        AutoWall foundAw = null;
        boolean vert = (fromX == toX);

        for (AutoWall aw = state.autoWalls.first(); aw != null; aw = (AutoWall)aw.next) {
            if ((aw.door != null) || (aw.vert != vert) || (aw.type != type)) {
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

            for (AutoWall aw = state.autoWalls.first(); aw != null; aw = (AutoWall)aw.next) {
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

    private void renderLevel() {
        renderer.z1 = -HALF_WALL;
        renderer.z2 = HALF_WALL;
        renderer.z3 = HALF_WALL;
        renderer.z4 = -HALF_WALL;

        PortalTracer.Wall[] localWalls = tracer.walls;
        Door[][] localDoorsMap = level.doorsMap;
        int[][] localDrawedAutoWalls = state.drawnAutoWalls;

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

            if (((localDrawedAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
                localDrawedAutoWalls[my][mx] |= autoWallMask;
                appendAutoWall(wall.fromX, wall.fromY, wall.toX, wall.toY, AUTO_WALL_TYPE_WALL);
            }

            renderer.x1 = (float)wall.fromX;
            renderer.y1 = -(float)wall.fromY;
            renderer.x2 = (float)wall.fromX;
            renderer.y2 = -(float)wall.fromY;
            renderer.x3 = (float)wall.toX;
            renderer.y3 = -(float)wall.toY;
            renderer.x4 = (float)wall.toX;
            renderer.y4 = -(float)wall.toY;

            setWallLighting((float)wall.fromX,
                    (float)wall.fromY,
                    (float)wall.toX,
                    (float)wall.toY,
                    (wall.fromX == wall.toX));

            if (door != null) {
                renderer.drawQuad(door.texture + TextureLoader.BASE_DOORS_S);
            } else if (wall.flipTexture) {
                renderer.drawQuadFlipLR(wall.texture);
            } else {
                renderer.drawQuad(wall.texture);
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
    }

    @SuppressWarnings("MagicNumber")
    private void renderDoors() {
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

            if (((localDrawnAutoWalls[door.y][door.x] & AUTO_WALL_MASK_DOOR) == 0) && state.autoWalls.canTake()) {
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
                toX1 = fromX1;
                fromY1 = -fromY;
                toY1 = -toY;

                fromX2 = fromX + HALF_DOOR_WIDTH;
                toX2 = fromX2;
                fromY2 = -fromY;
                toY2 = -toY;
            } else {
                fromX += door.openPos;
                toX += door.openPos;

                fromX1 = fromX;
                toX1 = toX;
                fromY1 = -(fromY - HALF_DOOR_WIDTH);
                toY1 = fromY1;

                fromX2 = fromX;
                toX2 = toX;
                fromY2 = -(fromY + HALF_DOOR_WIDTH);
                toY2 = fromY2;
            }

            setWallLighting(fromX, fromY, toX, toY, door.vert);

            renderer.x1 = fromX1;
            renderer.y1 = fromY1;
            renderer.x2 = fromX1;
            renderer.y2 = fromY1;
            renderer.x3 = toX1;
            renderer.y3 = toY1;
            renderer.x4 = toX1;
            renderer.y4 = toY1;
            renderer.drawQuad(door.texture + TextureLoader.BASE_DOORS_F);

            renderer.x1 = fromX2;
            renderer.y1 = fromY2;
            renderer.x2 = fromX2;
            renderer.y2 = fromY2;
            renderer.x3 = toX2;
            renderer.y3 = toY2;
            renderer.x4 = toX2;
            renderer.y4 = toY2;
            renderer.drawQuad(door.texture + TextureLoader.BASE_DOORS_F);

            setWallLighting(fromX1, -fromY1, fromX2, -fromY2, !door.vert);

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

            renderer.x1 = fromX1;
            renderer.y1 = fromY1;
            renderer.x2 = fromX1;
            renderer.y2 = fromY1;
            renderer.x3 = fromX2;
            renderer.y3 = fromY2;
            renderer.x4 = fromX2;
            renderer.y4 = fromY2;
            renderer.drawQuadExtruded(door.texture + TextureLoader.BASE_DOORS_S);
        }

        renderer.z1 = -HALF_WALL;
        renderer.z2 = HALF_WALL;
        renderer.z3 = HALF_WALL;
        renderer.z4 = -HALF_WALL;
    }

    // render objects, decorations and transparents
    @SuppressWarnings("MagicNumber")
    private void renderObjects() {
        TouchedCell[] localTouchedCells = tracer.touchedCells;
        int[][] localPassableMap = state.passableMap;
        int[][] localTexMap = state.texMap;
        int[][] localDrawnAutoWalls = state.drawnAutoWalls;

        for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
            TouchedCell tc = localTouchedCells[i];
            int pass = localPassableMap[tc.y][tc.x];
            int tex = localTexMap[tc.y][tc.x];

            if ((pass & Level.PASSABLE_MASK_OBJ_OR_DECOR) != 0) {
                float mx = (float)tc.x + 0.5f;
                float my = (float)tc.y + 0.5f;

                float fromX = mx + flatObjDy;
                float toX = mx - flatObjDy;
                float fromY = my - flatObjDx;
                float toY = my + flatObjDx;

                renderer.x1 = fromX;
                renderer.y1 = -fromY;
                renderer.x2 = fromX;
                renderer.y2 = -fromY;
                renderer.x3 = toX;
                renderer.y3 = -toY;
                renderer.x4 = toX;
                renderer.y4 = -toY;

                setObjLighting(mx, my);

                if ((pass & Level.PASSABLE_IS_OBJECT) != 0) {
                    renderer.drawQuad(state.objectsMap[tc.y][tc.x]);
                }

                if ((pass & Level.PASSABLE_MASK_DECORATION) != 0) {
                    renderer.drawQuad(tex);
                }
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

                if (((localDrawnAutoWalls[tc.y][tc.x] & AUTO_WALL_MASK_DOOR) == 0) && state.autoWalls.canTake()) {
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

                renderer.x1 = fromX;
                renderer.y1 = -fromY;
                renderer.x2 = fromX;
                renderer.y2 = -fromY;
                renderer.x3 = toX;
                renderer.y3 = -toY;
                renderer.x4 = toX;
                renderer.y4 = -toY;

                setWallLighting(fromX, fromY, toX, toY, vert);
                renderer.drawQuad(tex);
            } else if (((pass & Level.PASSABLE_IS_TRANSP) != 0) && ((pass & Level.PASSABLE_IS_NOTRANS) == 0) && (tex
                    != 0)) {

                for (int s = 0; s < 4; s++) {
                    if ((localPassableMap[tc.y + PortalTracer.Y_CELL_ADD[s]][tc.x + PortalTracer.X_CELL_ADD[s]]
                            & Level.PASSABLE_MASK_WALL_N_TRANSP) == 0) {

                        int fromX = tc.x + PortalTracer.X_ADD[s];
                        int fromY = tc.y + PortalTracer.Y_ADD[s];
                        int toX = tc.x + PortalTracer.X_ADD[(s + 1) % 4];
                        int toY = tc.y + PortalTracer.Y_ADD[(s + 1) % 4];

                        renderer.x1 = (float)fromX;
                        renderer.y1 = -(float)fromY;
                        renderer.x2 = (float)fromX;
                        renderer.y2 = -(float)fromY;
                        renderer.x3 = (float)toX;
                        renderer.y3 = -(float)toY;
                        renderer.x4 = (float)toX;
                        renderer.y4 = -(float)toY;

                        int mx = (fromX < toX ? fromX : toX);
                        int my = (fromY < toY ? fromY : toY);
                        int autoWallMask = ((s == 1 || s == 3) ? AUTO_WALL_MASK_VERTICAL : AUTO_WALL_MASK_HORIZONTAL);

                        if (((localDrawnAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
                            localDrawnAutoWalls[my][mx] |= autoWallMask;
                            appendAutoWall(fromX, fromY, toX, toY, AUTO_WALL_TYPE_TRANSP);
                        }

                        setWallLighting((float)fromX, (float)fromY, (float)toX, (float)toY, (s == 1 || s == 3));

                        if (s == 0 || s == 3) {
                            renderer.drawQuadFlipLR(tex);
                        } else {
                            renderer.drawQuad(tex);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("MagicNumber")
    private void renderMonsters(long elapsedTime, int texIdx, boolean deadCorpses) {
        renderer.z1 = -HALF_WALL;
        renderer.z2 = HALF_WALL * MONSTER_SIZE_MULT;
        renderer.z3 = HALF_WALL * MONSTER_SIZE_MULT;
        renderer.z4 = -HALF_WALL;

        int fromTex = texIdx * TextureLoader.MONSTERS_IN_TEXTURE * TextureLoader.COUNT_MONSTER;
        int toTex = fromTex + TextureLoader.MONSTERS_IN_TEXTURE * TextureLoader.COUNT_MONSTER - 1;

        boolean[][] localTouchedCellsMap = tracer.touchedCellsMap;

        for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
            if ((deadCorpses && mon.health > 0)
                    || (!deadCorpses && mon.health <= 0)
                    || (mon.texture < fromTex)
                    || (mon.texture > toTex)
                    || !(localTouchedCellsMap[mon.prevY][mon.prevX] || localTouchedCellsMap[mon.cellY][mon.cellX])) {

                continue;
            }

            float fromX = mon.x + flatObjDy * MONSTER_SIZE_MULT;
            float toX = mon.x - flatObjDy * MONSTER_SIZE_MULT;
            float fromY = mon.y - flatObjDx * MONSTER_SIZE_MULT;
            float toY = mon.y + flatObjDx * MONSTER_SIZE_MULT;

            int tex = mon.texture - fromTex;
            setObjLighting(mon.x, mon.y);

            renderer.x1 = fromX;
            renderer.y1 = -fromY;
            renderer.x2 = fromX;
            renderer.y2 = -fromY;
            renderer.x3 = toX;
            renderer.y3 = -toY;
            renderer.x4 = toX;
            renderer.y4 = -toY;

            if (mon.health > 0) {
                if ((mon.hitTimeout <= 0) && (mon.attackTimeout > 0)) {
                    tex += 15;
                } else {
                    if (mon.isAimedOnHero) {
                        tex += 2;
                    } else if (mon.chaseMode) {
                        tex += ((((int)currentHeroA + 360 + 45 - mon.dir * 90) % 360) / 90);
                    }

                    if (mon.hitTimeout > 0) {
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

            renderer.drawQuadMon(tex);
        }
    }

    private void renderFloorArrows() {
        renderer.z1 = -HALF_WALL + GameMath.EPSILON;
        renderer.z2 = -HALF_WALL + GameMath.EPSILON;
        renderer.z3 = -HALF_WALL + GameMath.EPSILON;
        renderer.z4 = -HALF_WALL + GameMath.EPSILON;

        TouchedCell[] localTouchedCells = tracer.touchedCells;
        int[][] localWallsMap = state.wallsMap;
        int[][] localArrowsMap = state.arrowsMap;

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

            float lff = getLightness(fx, fy);
            float lft = getLightness(fx, ty);
            float ltf = getLightness(tx, fy);
            float ltt = getLightness(tx, ty);

            renderer.r1 = lft;
            renderer.g1 = lft;
            renderer.b1 = lft;
            renderer.r2 = lff;
            renderer.g2 = lff;
            renderer.b2 = lff;
            renderer.r3 = ltf;
            renderer.g3 = ltf;
            renderer.b3 = ltf;
            renderer.r4 = ltt;
            renderer.g4 = ltt;
            renderer.b4 = ltt;

            renderer.x1 = fx;
            renderer.y1 = -ty;
            renderer.x2 = fx;
            renderer.y2 = -fy;
            renderer.x3 = tx;
            renderer.y3 = -fy;
            renderer.x4 = tx;
            renderer.y4 = -ty;

            renderer.drawQuad(arrowTex);
        }
    }

    @SuppressWarnings({ "MagicNumber", "ConstantConditions" })
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

        for (int i = 0, len = tracer.touchedCellsCount; i < len; i++) {
            TouchedCell tc = localTouchedCells[i];

            // calc params

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

                renderer.z1 = -HALF_WALL;
                renderer.z2 = -HALF_WALL;
                renderer.z3 = -HALF_WALL;
                renderer.z4 = -HALF_WALL;

                if (floorTex1 == floorTex2 && floorTex1 == floorTex3 && floorTex1 == floorTex4) {
                    if (floorTex1 != 0) {
                        renderer.r1 = lft;
                        renderer.g1 = lft;
                        renderer.b1 = lft;
                        renderer.r2 = lff;
                        renderer.g2 = lff;
                        renderer.b2 = lff;
                        renderer.r3 = ltf;
                        renderer.g3 = ltf;
                        renderer.b3 = ltf;
                        renderer.r4 = ltt;
                        renderer.g4 = ltt;
                        renderer.b4 = ltt;

                        renderer.x1 = fx;
                        renderer.y1 = ty;
                        renderer.x2 = fx;
                        renderer.y2 = fy;
                        renderer.x3 = tx;
                        renderer.y3 = fy;
                        renderer.x4 = tx;
                        renderer.y4 = ty;

                        renderer.drawQuad(floorTex1);
                    }
                } else {
                    if (floorTex1 != 0) {
                        renderer.r1 = lfm;
                        renderer.g1 = lfm;
                        renderer.b1 = lfm;
                        renderer.r2 = lff;
                        renderer.g2 = lff;
                        renderer.b2 = lff;
                        renderer.r3 = lmf;
                        renderer.g3 = lmf;
                        renderer.b3 = lmf;
                        renderer.r4 = lmm;
                        renderer.g4 = lmm;
                        renderer.b4 = lmm;

                        renderer.x1 = fx;
                        renderer.y1 = my;
                        renderer.x2 = fx;
                        renderer.y2 = fy;
                        renderer.x3 = mx;
                        renderer.y3 = fy;
                        renderer.x4 = mx;
                        renderer.y4 = my;

                        renderer.drawQuad1(floorTex1);
                    }

                    if (floorTex2 != 0) {
                        renderer.r1 = lmm;
                        renderer.g1 = lmm;
                        renderer.b1 = lmm;
                        renderer.r2 = lmf;
                        renderer.g2 = lmf;
                        renderer.b2 = lmf;
                        renderer.r3 = ltf;
                        renderer.g3 = ltf;
                        renderer.b3 = ltf;
                        renderer.r4 = ltm;
                        renderer.g4 = ltm;
                        renderer.b4 = ltm;

                        renderer.x1 = mx;
                        renderer.y1 = my;
                        renderer.x2 = mx;
                        renderer.y2 = fy;
                        renderer.x3 = tx;
                        renderer.y3 = fy;
                        renderer.x4 = tx;
                        renderer.y4 = my;

                        renderer.drawQuad2(floorTex2);
                    }

                    if (floorTex3 != 0) {
                        renderer.r1 = lft;
                        renderer.g1 = lft;
                        renderer.b1 = lft;
                        renderer.r2 = lfm;
                        renderer.g2 = lfm;
                        renderer.b2 = lfm;
                        renderer.r3 = lmm;
                        renderer.g3 = lmm;
                        renderer.b3 = lmm;
                        renderer.r4 = lmt;
                        renderer.g4 = lmt;
                        renderer.b4 = lmt;

                        renderer.x1 = fx;
                        renderer.y1 = ty;
                        renderer.x2 = fx;
                        renderer.y2 = my;
                        renderer.x3 = mx;
                        renderer.y3 = my;
                        renderer.x4 = mx;
                        renderer.y4 = ty;

                        renderer.drawQuad3(floorTex3);
                    }

                    if (floorTex4 != 0) {
                        renderer.r1 = lmt;
                        renderer.g1 = lmt;
                        renderer.b1 = lmt;
                        renderer.r2 = lmm;
                        renderer.g2 = lmm;
                        renderer.b2 = lmm;
                        renderer.r3 = ltm;
                        renderer.g3 = ltm;
                        renderer.b3 = ltm;
                        renderer.r4 = ltt;
                        renderer.g4 = ltt;
                        renderer.b4 = ltt;

                        renderer.x1 = mx;
                        renderer.y1 = ty;
                        renderer.x2 = mx;
                        renderer.y2 = my;
                        renderer.x3 = tx;
                        renderer.y3 = my;
                        renderer.x4 = tx;
                        renderer.y4 = ty;

                        renderer.drawQuad4(floorTex4);
                    }
                }

                // render ceil

                renderer.z1 = HALF_WALL;
                renderer.z2 = HALF_WALL;
                renderer.z3 = HALF_WALL;
                renderer.z4 = HALF_WALL;

                if (ceilTex1 == ceilTex2 && ceilTex1 == ceilTex3 && ceilTex1 == ceilTex4) {
                    if (ceilTex1 != 0) {
                        renderer.r1 = ltt;
                        renderer.g1 = ltt;
                        renderer.b1 = ltt;
                        renderer.r2 = ltf;
                        renderer.g2 = ltf;
                        renderer.b2 = ltf;
                        renderer.r3 = lff;
                        renderer.g3 = lff;
                        renderer.b3 = lff;
                        renderer.r4 = lft;
                        renderer.g4 = lft;
                        renderer.b4 = lft;

                        renderer.x1 = tx;
                        renderer.y1 = ty;
                        renderer.x2 = tx;
                        renderer.y2 = fy;
                        renderer.x3 = fx;
                        renderer.y3 = fy;
                        renderer.x4 = fx;
                        renderer.y4 = ty;

                        renderer.drawQuadFlipLR(ceilTex1);
                    }
                } else {
                    if (ceilTex1 != 0) {
                        renderer.r1 = lmm;
                        renderer.g1 = lmm;
                        renderer.b1 = lmm;
                        renderer.r2 = lmf;
                        renderer.g2 = lmf;
                        renderer.b2 = lmf;
                        renderer.r3 = lff;
                        renderer.g3 = lff;
                        renderer.b3 = lff;
                        renderer.r4 = lmt;
                        renderer.g4 = lmt;
                        renderer.b4 = lmt;

                        renderer.x1 = mx;
                        renderer.y1 = my;
                        renderer.x2 = mx;
                        renderer.y2 = fy;
                        renderer.x3 = fx;
                        renderer.y3 = fy;
                        renderer.x4 = fx;
                        renderer.y4 = my;

                        renderer.drawQuadFlipLR1(ceilTex1);
                    }

                    if (ceilTex2 != 0) {
                        renderer.r1 = ltm;
                        renderer.g1 = ltm;
                        renderer.b1 = ltm;
                        renderer.r2 = ltf;
                        renderer.g2 = ltf;
                        renderer.b2 = ltf;
                        renderer.r3 = lmf;
                        renderer.g3 = lmf;
                        renderer.b3 = lmf;
                        renderer.r4 = lmm;
                        renderer.g4 = lmm;
                        renderer.b4 = lmm;

                        renderer.x1 = tx;
                        renderer.y1 = my;
                        renderer.x2 = tx;
                        renderer.y2 = fy;
                        renderer.x3 = mx;
                        renderer.y3 = fy;
                        renderer.x4 = mx;
                        renderer.y4 = my;

                        renderer.drawQuadFlipLR2(ceilTex2);
                    }

                    if (ceilTex3 != 0) {
                        renderer.r1 = lmt;
                        renderer.g1 = lmt;
                        renderer.b1 = lmt;
                        renderer.r2 = lmm;
                        renderer.g2 = lmm;
                        renderer.b2 = lmm;
                        renderer.r3 = lfm;
                        renderer.g3 = lfm;
                        renderer.b3 = lfm;
                        renderer.r4 = lft;
                        renderer.g4 = lft;
                        renderer.b4 = lft;

                        renderer.x1 = mx;
                        renderer.y1 = ty;
                        renderer.x2 = mx;
                        renderer.y2 = my;
                        renderer.x3 = fx;
                        renderer.y3 = my;
                        renderer.x4 = fx;
                        renderer.y4 = ty;

                        renderer.drawQuadFlipLR3(ceilTex3);
                    }

                    if (ceilTex4 != 0) {
                        renderer.r1 = ltt;
                        renderer.g1 = ltt;
                        renderer.b1 = ltt;
                        renderer.r2 = ltm;
                        renderer.g2 = ltm;
                        renderer.b2 = ltm;
                        renderer.r3 = lmm;
                        renderer.g3 = lmm;
                        renderer.b3 = lmm;
                        renderer.r4 = lmt;
                        renderer.g4 = lmt;
                        renderer.b4 = lmt;

                        renderer.x1 = tx;
                        renderer.y1 = ty;
                        renderer.x2 = tx;
                        renderer.y2 = my;
                        renderer.x3 = mx;
                        renderer.y3 = my;
                        renderer.x4 = mx;
                        renderer.y4 = ty;

                        renderer.drawQuadFlipLR4(ceilTex4);
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
            // ltf *= EXTRUDE_LIGHT_MULT;
            ltt *= EXTRUDE_LIGHT_MULT;
            lfm *= EXTRUDE_LIGHT_MULT;
            lmf *= EXTRUDE_LIGHT_MULT;
            lmm *= EXTRUDE_LIGHT_MULT;
            lmt *= EXTRUDE_LIGHT_MULT;
            ltm *= EXTRUDE_LIGHT_MULT;

            if (tc.x > 0) {
                int ceilTex2l = localCeilMap2[tc.y][tc.x - 1];
                int ceilTex4l = localCeilMap4[tc.y][tc.x - 1];

                if ((ceilTex2l == 0 && ceilTex1 != 0) || (ceilTex2l != 0 && ceilTex1 == 0)) {
                    renderer.x1 = fx;
                    renderer.y1 = fy;
                    renderer.x2 = fx;
                    renderer.y2 = my;
                    renderer.x3 = fx;
                    renderer.y3 = my;
                    renderer.x4 = fx;
                    renderer.y4 = fy;

                    renderer.r1 = lff;
                    renderer.g1 = lff;
                    renderer.b1 = lff;
                    renderer.r2 = lfm;
                    renderer.g2 = lfm;
                    renderer.b2 = lfm;
                    renderer.r3 = lfm;
                    renderer.g3 = lfm;
                    renderer.b3 = lfm;
                    renderer.r4 = lff;
                    renderer.g4 = lff;
                    renderer.b4 = lff;

                    if (ceilTex1 != 0) {
                        renderer.drawQuad1(ceilTex1);
                    } else {
                        renderer.drawQuad2(ceilTex2l);
                    }
                }

                if ((ceilTex4l == 0 && ceilTex3 != 0) || (ceilTex4l != 0 && ceilTex3 == 0)) {
                    renderer.x1 = fx;
                    renderer.y1 = my;
                    renderer.x2 = fx;
                    renderer.y2 = ty;
                    renderer.x3 = fx;
                    renderer.y3 = ty;
                    renderer.x4 = fx;
                    renderer.y4 = my;

                    renderer.r1 = lfm;
                    renderer.g1 = lfm;
                    renderer.b1 = lfm;
                    renderer.r2 = lft;
                    renderer.g2 = lft;
                    renderer.b2 = lft;
                    renderer.r3 = lft;
                    renderer.g3 = lft;
                    renderer.b3 = lft;
                    renderer.r4 = lfm;
                    renderer.g4 = lfm;
                    renderer.b4 = lfm;

                    if (ceilTex3 != 0) {
                        renderer.drawQuad3(ceilTex3);
                    } else {
                        renderer.drawQuad4(ceilTex4l);
                    }
                }
            }

            if (tc.y < (state.levelHeight - 1)) {
                int ceilTex1d = localCeilMap1[tc.y + 1][tc.x];
                int ceilTex2d = localCeilMap2[tc.y + 1][tc.x];

                if ((ceilTex1d == 0 && ceilTex3 != 0) || (ceilTex1d != 0 && ceilTex3 == 0)) {
                    renderer.x1 = fx;
                    renderer.y1 = ty;
                    renderer.x2 = mx;
                    renderer.y2 = ty;
                    renderer.x3 = mx;
                    renderer.y3 = ty;
                    renderer.x4 = fx;
                    renderer.y4 = ty;

                    renderer.r1 = lft;
                    renderer.g1 = lft;
                    renderer.b1 = lft;
                    renderer.r2 = lmt;
                    renderer.g2 = lmt;
                    renderer.b2 = lmt;
                    renderer.r3 = lmt;
                    renderer.g3 = lmt;
                    renderer.b3 = lmt;
                    renderer.r4 = lft;
                    renderer.g4 = lft;
                    renderer.b4 = lft;

                    if (ceilTex3 != 0) {
                        renderer.drawQuad3(ceilTex3);
                    } else {
                        renderer.drawQuad3(ceilTex1d);
                    }
                }

                if ((ceilTex2d == 0 && ceilTex4 != 0) || (ceilTex2d != 0 && ceilTex4 == 0)) {
                    renderer.x1 = mx;
                    renderer.y1 = ty;
                    renderer.x2 = tx;
                    renderer.y2 = ty;
                    renderer.x3 = tx;
                    renderer.y3 = ty;
                    renderer.x4 = mx;
                    renderer.y4 = ty;

                    renderer.r1 = lmt;
                    renderer.g1 = lmt;
                    renderer.b1 = lmt;
                    renderer.r2 = ltt;
                    renderer.g2 = ltt;
                    renderer.b2 = ltt;
                    renderer.r3 = ltt;
                    renderer.g3 = ltt;
                    renderer.b3 = ltt;
                    renderer.r4 = lmt;
                    renderer.g4 = lmt;
                    renderer.b4 = lmt;

                    if (ceilTex4 != 0) {
                        renderer.drawQuad4(ceilTex4);
                    } else {
                        renderer.drawQuad2(ceilTex2d);
                    }
                }
            }

            if ((ceilTex1 == 0 && ceilTex2 != 0) || (ceilTex1 != 0 && ceilTex2 == 0)) {
                renderer.x1 = mx;
                renderer.y1 = fy;
                renderer.x2 = mx;
                renderer.y2 = my;
                renderer.x3 = mx;
                renderer.y3 = my;
                renderer.x4 = mx;
                renderer.y4 = fy;

                renderer.r1 = lmf;
                renderer.g1 = lmf;
                renderer.b1 = lmf;
                renderer.r2 = lmm;
                renderer.g2 = lmm;
                renderer.b2 = lmm;
                renderer.r3 = lmm;
                renderer.g3 = lmm;
                renderer.b3 = lmm;
                renderer.r4 = lmf;
                renderer.g4 = lmf;
                renderer.b4 = lmf;

                if (ceilTex2 != 0) {
                    renderer.drawQuad2(ceilTex2);
                } else {
                    renderer.drawQuad1(ceilTex1);
                }
            }

            if ((ceilTex3 == 0 && ceilTex4 != 0) || (ceilTex3 != 0 && ceilTex4 == 0)) {
                renderer.x1 = mx;
                renderer.y1 = my;
                renderer.x2 = mx;
                renderer.y2 = ty;
                renderer.x3 = mx;
                renderer.y3 = ty;
                renderer.x4 = mx;
                renderer.y4 = my;

                renderer.r1 = lmm;
                renderer.g1 = lmm;
                renderer.b1 = lmm;
                renderer.r2 = lmt;
                renderer.g2 = lmt;
                renderer.b2 = lmt;
                renderer.r3 = lmt;
                renderer.g3 = lmt;
                renderer.b3 = lmt;
                renderer.r4 = lmm;
                renderer.g4 = lmm;
                renderer.b4 = lmm;

                if (ceilTex4 != 0) {
                    renderer.drawQuad4(ceilTex4);
                } else {
                    renderer.drawQuad3(ceilTex3);
                }
            }

            if ((ceilTex1 == 0 && ceilTex3 != 0) || (ceilTex1 != 0 && ceilTex3 == 0)) {
                renderer.x1 = fx;
                renderer.y1 = my;
                renderer.x2 = mx;
                renderer.y2 = my;
                renderer.x3 = mx;
                renderer.y3 = my;
                renderer.x4 = fx;
                renderer.y4 = my;

                renderer.r1 = lfm;
                renderer.g1 = lfm;
                renderer.b1 = lfm;
                renderer.r2 = lmm;
                renderer.g2 = lmm;
                renderer.b2 = lmm;
                renderer.r3 = lmm;
                renderer.g3 = lmm;
                renderer.b3 = lmm;
                renderer.r4 = lfm;
                renderer.g4 = lfm;
                renderer.b4 = lfm;

                if (ceilTex3 != 0) {
                    renderer.drawQuad3(ceilTex3);
                } else {
                    renderer.drawQuad1(ceilTex1);
                }
            }

            if ((ceilTex2 == 0 && ceilTex4 != 0) || (ceilTex2 != 0 && ceilTex4 == 0)) {
                renderer.x1 = mx;
                renderer.y1 = my;
                renderer.x2 = tx;
                renderer.y2 = my;
                renderer.x3 = tx;
                renderer.y3 = my;
                renderer.x4 = mx;
                renderer.y4 = my;

                renderer.r1 = lmm;
                renderer.g1 = lmm;
                renderer.b1 = lmm;
                renderer.r2 = ltm;
                renderer.g2 = ltm;
                renderer.b2 = ltm;
                renderer.r3 = ltm;
                renderer.g3 = ltm;
                renderer.b3 = ltm;
                renderer.r4 = lmm;
                renderer.g4 = lmm;
                renderer.b4 = lmm;

                if (ceilTex4 != 0) {
                    renderer.drawQuad4(ceilTex4);
                } else {
                    renderer.drawQuad2(ceilTex2);
                }
            }
        }
    }

    @SuppressWarnings("MagicNumber")
    private void renderBullets() {
        boolean[][] localTouchedCellsMap = tracer.touchedCellsMap;

        for (Bullet bullet = state.bullets.first(); bullet != null; bullet = (Bullet)bullet.next) {
            int tex = bullet.getTexture();

            if (tex < 0 || !localTouchedCellsMap[(int)bullet.y][(int)bullet.x]) {
                continue;
            }

            float fromX = bullet.x + flatObjDy;
            float toX = bullet.x - flatObjDy;
            float fromY = bullet.y - flatObjDx;
            float toY = bullet.y + flatObjDx;

            renderer.x1 = fromX;
            renderer.y1 = -fromY;
            renderer.x2 = fromX;
            renderer.y2 = -fromY;
            renderer.x3 = toX;
            renderer.y3 = -toY;
            renderer.x4 = toX;
            renderer.y4 = -toY;

            float zoff = HALF_WALL * 0.5f * (float)Math.sin(bullet.dist / bullet.params.maxDist * Math.PI * 1.5);

            renderer.z1 = -HALF_WALL + zoff;
            renderer.z2 = HALF_WALL + zoff;
            renderer.z3 = HALF_WALL + zoff;
            renderer.z4 = -HALF_WALL + zoff;

            setObjLighting(bullet.x, bullet.y);
            renderer.drawQuad(TextureLoader.BASE_BULLETS + tex);
        }
    }

    private void renderExplosions() {
        boolean[][] localTouchedCellsMap = tracer.touchedCellsMap;

        for (Explosion explosion = state.explosions.first(); explosion != null; explosion = (Explosion)explosion.next) {
            int tex = explosion.getTexture();

            if (tex < 0 || !localTouchedCellsMap[(int)explosion.y][(int)explosion.x]) {
                continue;
            }

            float fromX = explosion.x + flatObjDy;
            float toX = explosion.x - flatObjDy;
            float fromY = explosion.y - flatObjDx;
            float toY = explosion.y + flatObjDx;

            renderer.x1 = fromX;
            renderer.y1 = -fromY;
            renderer.x2 = fromX;
            renderer.y2 = -fromY;
            renderer.x3 = toX;
            renderer.y3 = -toY;
            renderer.x4 = toX;
            renderer.y4 = -toY;

            setObjLighting(explosion.x, explosion.y);
            renderer.drawQuad(TextureLoader.BASE_EXPLOSIONS + tex);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void render(GL10 gl, long elapsedTime, float ypos, float xrot) {
        updateDoors(elapsedTime);

        currentHeroX = state.heroX;
        currentHeroY = state.heroY;
        currentHeroA = state.heroA;
        currentHeroCs = engine.heroCs;
        currentHeroSn = engine.heroSn;

        float currentHeroAr = engine.heroAr;

        tracer.trace(currentHeroX, currentHeroY, currentHeroAr, 44.0f * GameMath.G2RAD_F);
        renderer.frustrumModelIdentity(gl);

        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_BLEND);
        gl.glEnable(GL10.GL_DEPTH_TEST);

        gl.glTranslatef(0.0f, ypos, -0.1f);
        gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(90.0f - currentHeroA, 0.0f, 0.0f, 1.0f);
        gl.glTranslatef(-currentHeroX, currentHeroY, 0.0f);

        renderer.a1 = 1.0f;
        renderer.a2 = 1.0f;
        renderer.a3 = 1.0f;
        renderer.a4 = 1.0f;

        // Arrows on floor

        renderer.init();
        renderFloorArrows();
        gl.glEnable(GL10.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL10.GL_GREATER, Renderer.ALPHA_VALUE);
        renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MAIN]);
        renderer.flush(gl);
        gl.glDisable(GL10.GL_ALPHA_TEST);

        // Floor and Ceiling

        renderer.init();
        renderFloorAndCeil();
        gl.glDisable(GL10.GL_CULL_FACE); // necessary for extrude, and has no affect on floor and ceil
        renderer.flush(gl);
        gl.glEnable(GL10.GL_CULL_FACE);

        // Walls and Door Sides

        renderer.init();
        renderLevel();
        renderer.flush(gl);

        // Doors

        renderer.init();
        renderDoors();

        gl.glDisable(GL10.GL_CULL_FACE); // necessary for doors and transparents, and has no affect on monsters and objects
        renderer.flush(gl);

        // Monsters, Objects & Transparents

        flatObjDx = (float)Math.cos(-currentHeroAr) * 0.5f;
        flatObjDy = (float)Math.sin(-currentHeroAr) * 0.5f;

        gl.glEnable(GL10.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL10.GL_GREATER, Renderer.ALPHA_VALUE);

        // objects rendered after monsters (so if monster stay in cell with object, monster will be in front)
        // or reverse order, but set appropriate depth test function

        renderer.init();
        renderMonsters(elapsedTime, 0, false);
        renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MONSTERS_1]);
        renderer.flush(gl);

        renderer.init();
        renderMonsters(elapsedTime, 1, false);
        renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MONSTERS_2]);
        renderer.flush(gl);

        renderer.z1 = -HALF_WALL;
        renderer.z2 = HALF_WALL;
        renderer.z3 = HALF_WALL;
        renderer.z4 = -HALF_WALL;

        renderer.init();
        renderExplosions();
        renderBullets();

        renderer.z1 = -HALF_WALL;
        renderer.z2 = HALF_WALL;
        renderer.z3 = HALF_WALL;
        renderer.z4 = -HALF_WALL;

        renderObjects();
        renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MAIN]);
        renderer.flush(gl);

        // dead corpses rendered to be in back

        renderer.init();
        renderMonsters(elapsedTime, 0, true);
        renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MONSTERS_1]);
        renderer.flush(gl);

        renderer.init();
        renderMonsters(elapsedTime, 1, true);
        renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MONSTERS_2]);
        renderer.flush(gl);

        gl.glDisable(GL10.GL_ALPHA_TEST);
        gl.glEnable(GL10.GL_CULL_FACE);
    }

    @SuppressWarnings("MagicNumber")
    public void surfaceSizeChanged(GL10 gl) {
        float size = 0.1f * (float)Math.tan(Math.toRadians(50.0) / 2);
        renderer.initFrustum(gl, -size, size, -size / engine.ratio, size / engine.ratio, 0.1f, 100.0f);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }
}
