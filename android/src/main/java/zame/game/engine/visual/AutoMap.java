package zame.game.engine.visual;

import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.entity.AutoMapPathCell;
import zame.game.engine.entity.AutoWall;
import zame.game.engine.entity.Bullet;
import zame.game.engine.entity.DebugTraceInfo;
import zame.game.engine.entity.Door;
import zame.game.engine.entity.Explosion;
import zame.game.engine.entity.Monster;
import zame.game.engine.entity.TouchedCell;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.level.Level;
import zame.game.engine.level.LevelRenderer;
import zame.game.engine.level.PortalTracer;
import zame.game.engine.state.State;
import zame.game.engine.util.GameMath;

public class AutoMap implements EngineObject {
    private static final float AUTOMAP_AREA = 30.0f;
    private static final float AUTOMAP_VIEW_OFFSET = 0.4f;
    private static final float AUTOMAP_VIEW_SIZE = 0.35f;
    private static final float AUTOMAP_EDGE_MULT = 9.95f;

    private Engine engine;
    private Renderer renderer;
    private Level level;
    private LevelRenderer levelRenderer;
    private State state;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
        this.level = engine.level;
        this.levelRenderer = engine.levelRenderer;
        this.state = engine.state;
    }

    @SuppressWarnings("MagicNumber")
    public void render() {
        if ((state.disabledControlsMask & Controls.CONTROL_MINIMAP) != 0) {
            return;
        }

        float autoMapOffX = engine.config.leftHandAim
                ? AUTOMAP_AREA * AUTOMAP_VIEW_OFFSET - AUTOMAP_AREA * engine.ratio
                : AUTOMAP_AREA * engine.ratio - AUTOMAP_AREA * AUTOMAP_VIEW_OFFSET;

        float autoMapOffY = AUTOMAP_AREA - AUTOMAP_AREA * AUTOMAP_VIEW_OFFSET;

        float currentHeroX = levelRenderer.currentHeroX;
        float currentHeroY = levelRenderer.currentHeroY;

        // ----

        renderer.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 0.75f);

        renderer.setCoordsQuadRectFlat(
                -AUTOMAP_AREA * AUTOMAP_VIEW_SIZE,
                -AUTOMAP_AREA * AUTOMAP_VIEW_SIZE,
                AUTOMAP_AREA * AUTOMAP_VIEW_SIZE,
                AUTOMAP_AREA * AUTOMAP_VIEW_SIZE);

        renderer.batchTexQuad(TextureLoader.ICON_MAP);

        renderer.useOrtho(
                -AUTOMAP_AREA * engine.ratio,
                AUTOMAP_AREA * engine.ratio,
                -AUTOMAP_AREA,
                AUTOMAP_AREA,
                0.0f,
                1.0f);

        renderer.gl.glTranslatef(autoMapOffX, autoMapOffY, 0.0f);

        renderer.renderBatch(
                Renderer.FLAG_BLEND
                        | Renderer.FLAG_ALPHA
                        | Renderer.FLAG_ALPHA_LOWER
                        | Renderer.FLAG_STENCIL_REPLACE,
                Renderer.TEXTURE_MAIN);

        // ----

        renderer.startBatch();

        if (levelRenderer.debugOnAutomap) {
            renderer.setColorLineRGBA(0.0f, 1.0f, 0.0f, 1.0f);

            for (int i = 0, len = levelRenderer.tracer.touchedCellsCountPriorToPostProcess; i < len; i++) {
                TouchedCell tc = levelRenderer.tracer.touchedCells[i];

                renderer.batchLine((float)tc.x, -(float)tc.y, (float)tc.x + 1.0f, -(float)tc.y - 1.0f);
                renderer.batchLine((float)tc.x, -(float)tc.y - 1.0f, (float)tc.x + 1.0f, -(float)tc.y);
            }

            renderer.setColorLineRGB(0.0f, 0.5f, 0.0f);

            for (int i = levelRenderer.tracer.touchedCellsCountPriorToPostProcess, len =
                    levelRenderer.tracer.touchedCellsCount;
                    i < len;
                    i++) {

                TouchedCell tc = levelRenderer.tracer.touchedCells[i];

                renderer.batchLine((float)tc.x, -(float)tc.y, (float)tc.x + 1.0f, -(float)tc.y - 1.0f);
                renderer.batchLine((float)tc.x, -(float)tc.y - 1.0f, (float)tc.x + 1.0f, -(float)tc.y);
            }
        }

        renderer.setColorLineRGBA(1.0f, 1.0f, 1.0f, 0.75f);

        for (AutoWall aw = state.autoWalls.first(); aw != null; aw = aw.next) {
            if (aw.door != null) {
                float fromX = aw.fromX;
                float fromY = aw.fromY;
                float toX = aw.toX;
                float toY = aw.toY;

                if (aw.vert) {
                    fromY += aw.door.openPos;
                } else {
                    fromX += aw.door.openPos;
                }

                renderer.batchLine(fromX, -fromY, toX, -toY);
            } else {
                renderer.batchLine(aw.fromX, -aw.fromY, aw.toX, -aw.toY);
            }
        }

        int[][] localPassableMap = state.passableMap;
        int[][] localArrowsMap = state.arrowsMap;
        boolean[][] localAwTouchedCellsMap = levelRenderer.awTouchedCellsMap;
        float cycle = (float)(engine.elapsedTime % 1000) / 1000.0f;

        // if (profile.isPurchased(Store.SECRETS)) {
        //     renderer.setColorLineRGB(0.0f, 1.0f, 1.0f);
        //
        //     for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = tc.next) {
        //         if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_IS_SECRET) == 0) {
        //             continue;
        //         }
        //
        //         float x = (float)tc.x;
        // 	       float y = (float)tc.y;
        //
        // 	       renderer.batchLine(x + 0.35f, -(y + 0.5f), x + 0.65f, -(y + 0.5f));
        // 	       renderer.batchLine(x + 0.5f, -(y + 0.35f), x + 0.5f, -(y + 0.65f));
        //     }
        // }

        for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = tc.next) {
            int arrowTex = localArrowsMap[tc.y][tc.x];

            if (arrowTex == 0) {
                continue;
            }

            float x = (float)tc.x;
            float y = (float)tc.y;
            float arrOffset = cycle * 0.5f - 0.5f;

            for (int i = 0; i < 2; i++) {
                renderer.setColorLineA(i == 0 ? cycle : 1.0f - cycle);

                switch (arrowTex) {
                    case TextureLoader.ARROW_UP:
                        renderer.batchLine(x + 0.25f, -(y + 0.75f - arrOffset), x + 0.5f, -(y + 0.25f - arrOffset));
                        renderer.batchLine(x + 0.5f, -(y + 0.25f - arrOffset), x + 0.75f, -(y + 0.75f - arrOffset));
                        break;

                    case TextureLoader.ARROW_RT:
                        renderer.batchLine(x + 0.25f + arrOffset, -(y + 0.75f), x + 0.75f + arrOffset, -(y + 0.5f));
                        renderer.batchLine(x + 0.75f + arrOffset, -(y + 0.5f), x + 0.25f + arrOffset, -(y + 0.25f));
                        break;

                    case TextureLoader.ARROW_DN:
                        renderer.batchLine(x + 0.25f, -(y + 0.25f + arrOffset), x + 0.5f, -(y + 0.75f + arrOffset));
                        renderer.batchLine(x + 0.5f, -(y + 0.75f + arrOffset), x + 0.75f, -(y + 0.25f + arrOffset));
                        break;

                    case TextureLoader.ARROW_LT:
                        renderer.batchLine(x + 0.75f - arrOffset, -(y + 0.75f), x + 0.25f - arrOffset, -(y + 0.5f));
                        renderer.batchLine(x + 0.25f - arrOffset, -(y + 0.5f), x + 0.75f - arrOffset, -(y + 0.25f));
                        break;
                }

                arrOffset += 0.5f;
            }
        }

        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);

        for (AutoMapPathCell pc = level.autoMapPathCells.first(); pc != null; pc = pc.next) {
            if (!localAwTouchedCellsMap[pc.y][pc.x]) {
                continue;
            }

            if (pc.hasFrom) {
                drawPoint(pc.cx + pc.fdx * (1.0f - cycle), pc.cy + pc.fdy * (1.0f - cycle));
            }

            if (pc.hasTo) {
                drawPoint(pc.cx + pc.tdx * cycle, pc.cy + pc.tdy * cycle);
            }
        }

        renderer.setColorLineA(0.5f);

        for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = tc.next) {
            if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_MASK_AUTOMAP_ITEM) == 0) {
                continue;
            }

            float x = (float)tc.x + 0.25f;
            float y = (float)tc.y + 0.25f;

            renderer.batchLine(x, -y, x + 0.5f, -y);
            renderer.batchLine(x + 0.5f, -y, x + 0.5f, -(y + 0.5f));
            renderer.batchLine(x + 0.5f, -(y + 0.5f), x, -(y + 0.5f));
            renderer.batchLine(x, -(y + 0.5f), x, -y);
        }

        for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = tc.next) {
            if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_IS_OBJECT) == 0) {
                continue;
            }

            float x = (float)tc.x;
            float y = (float)tc.y;

            renderer.batchLine(x + 0.5f, -(y + 0.25f), x + 0.75f, -(y + 0.5f));
            renderer.batchLine(x + 0.75f, -(y + 0.5f), x + 0.5f, -(y + 0.75f));
            renderer.batchLine(x + 0.5f, -(y + 0.75f), x + 0.25f, -(y + 0.5f));
            renderer.batchLine(x + 0.25f, -(y + 0.5f), x + 0.5f, -(y + 0.25f));
        }

        renderer.setColorQuadRGBA(1.0f, 0.0f, 0.0f, 0.5f);

        for (Monster mon = state.monsters.first(); mon != null; mon = mon.next) {
            if (mon.health <= 0) {
                continue;
            }

            float dx = mon.x - currentHeroX;
            float dy = mon.y - currentHeroY;
            float dist = Math.max(GameMath.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));

            drawPoint(currentHeroX + (dx / dist) * AUTOMAP_EDGE_MULT, currentHeroY + (dy / dist) * AUTOMAP_EDGE_MULT);
        }

        if (levelRenderer.debugOnAutomap) {
            renderer.setColorLineA(1.0f);

            for (Monster mon = state.monsters.first(); mon != null; mon = mon.next) {
                if (mon.health <= 0) {
                    renderer.setColorLineRGB(0.5f, 0.0f, 0.5f);
                } else if (mon.chaseMode) {
                    renderer.setColorLineRGB(1.0f, 0.0f, 0.0f);
                } else {
                    renderer.setColorLineRGB(0.0f, 0.0f, 1.0f);
                }

                float mdx = (float)Math.cos((float)mon.shootAngle * GameMath.G2RAD_F) * 0.75f;
                float mdy = (float)Math.sin((float)mon.shootAngle * GameMath.G2RAD_F) * 0.75f;

                renderer.batchLine(mon.x, -mon.y, mon.x + mdx, -mon.y + mdy);

                mdx *= 0.25;
                mdy *= 0.25;

                renderer.batchLine(mon.x + mdy, -mon.y - mdx, mon.x - mdy, -mon.y + mdx);
            }

            renderer.setColorLineRGB(1.0f, 0.0f, 0.5f);

            for (Bullet bullet = state.bullets.first(); bullet != null; bullet = bullet.next) {
                float x = bullet.x;
                float y = bullet.y;

                renderer.batchLine(x, -(y - 0.125f), x + 0.125f, -y);
                renderer.batchLine(x + 0.125f, -y, x, -(y + 0.125f));
                renderer.batchLine(x, -(y + 0.125f), x - 0.125f, -y);
                renderer.batchLine(x - 0.125f, -y, x, -(y - 0.125f));
            }

            renderer.setColorLineRGB(1.0f, 0.5f, 0.5f);

            for (Explosion explosion = state.explosions.first();
                    explosion != null;
                    explosion = explosion.next) {

                float x = explosion.x;
                float y = explosion.y;

                renderer.batchLine(x, -(y - 0.125f), x + 0.125f, -y);
                renderer.batchLine(x + 0.125f, -y, x, -(y + 0.125f));
                renderer.batchLine(x, -(y + 0.125f), x - 0.125f, -y);
                renderer.batchLine(x - 0.125f, -y, x, -(y - 0.125f));
            }

            for (DebugTraceInfo traceInfo = levelRenderer.debugTraceInfos.first(); traceInfo != null; ) {
                DebugTraceInfo nextTraceInfo = traceInfo.next;

                if (traceInfo.hit == Bullet.HitParams.HIT_OUT) {
                    renderer.setColorLineRGB(0.0f, 1.0f, 0.0f);
                } else if (traceInfo.hit == Bullet.HitParams.HIT_WALL) {
                    renderer.setColorLineRGB(0.5f, 0.5f, 0.5f);
                } else if (traceInfo.hit == Bullet.HitParams.HIT_MONSTER) {
                    renderer.setColorLineRGB(0.0f, 1.0f, 0.0f);
                } else if (traceInfo.hit == Bullet.HitParams.HIT_HERO) {
                    renderer.setColorLineRGB(1.0f, 0.0f, 0.0f);
                } else {
                    renderer.setColorLineRGB(0.0f, 0.0f, 1.0f);
                }

                renderer.batchLine(traceInfo.sx, -traceInfo.sy, traceInfo.ex, -traceInfo.ey);
                traceInfo.ticks++;

                if (traceInfo.ticks > 10) {
                    levelRenderer.debugTraceInfos.release(traceInfo);
                }

                traceInfo = nextTraceInfo;
            }

            renderer.setColorLineRGBA(1.0f, 1.0f, 0.0f, 1.0f);

            for (int i = 0, len = levelRenderer.tracer.wallsCount; i < len; i++) {
                PortalTracer.Wall wall = levelRenderer.tracer.walls[i];
                renderer.batchLine((float)wall.fromX, -(float)wall.fromY, (float)wall.toX, -(float)wall.toY);
            }
        }

        renderer.pushModelviewMatrix();
        renderer.gl.glRotatef(90.0f - levelRenderer.currentHeroA, 0.0f, 0.0f, 1.0f);
        renderer.gl.glTranslatef(-currentHeroX, currentHeroY, 0.0f);
        renderer.renderBatch(Renderer.FLAG_BLEND | Renderer.FLAG_STENCIL_KEEP);
        renderer.popModelviewMatrix();

        // ----

        renderer.startBatch();

        final float hw = 0.4f;
        final float hh = 0.5f;

        renderer.setColorLineRGBA(1.0f, 1.0f, 1.0f, 1.0f);

        renderer.batchLine(-hw, -hh, 0.0f, hh);
        renderer.batchLine(0.0f, hh, hw, -hh);
        renderer.batchLine(-hw, -hh, hw, -hh);

        renderer.renderBatch(Renderer.FLAG_BLEND | Renderer.FLAG_STENCIL_KEEP);
    }

    @SuppressWarnings("MagicNumber")
    private void drawPoint(float x, float y) {
        renderer.setCoordsQuadRectFlat(x - 0.15f, -(y - 0.15f), x + 0.15f, -(y + 0.15f));
        renderer.batchQuad();
    }

    /*
    public void openSecrets() {
        int[][] localPassableMap = state.passableMap;

        for (int cy = 0, levelHeight = state.levelHeight; cy < levelHeight; cy++) {
            for (int cx = 0, levelWidth = state.levelWidth; cx < levelWidth; cx++) {
                int pass = localPassableMap[cy][cx];

                if ((pass & Level.PASSABLE_IS_SECRET) != 0
                        && (pass & Level.PASSABLE_IS_WALL) == 0
                        && !levelRenderer.awTouchedCellsMap[cy][cx]
                        && state.awTouchedCells.canTake()) {

                    levelRenderer.awTouchedCellsMap[cy][cx] = true;
                    state.awTouchedCells.take().initFrom(cx, cy);
                }
            }
        }
    }
    */

    @SuppressWarnings({ "MagicNumber", "ManualMinMaxCalculation" })
    public void openAllMap() {
        int[][] localTexMap = state.texMap;
        int[][] localPassableMap = state.passableMap;
        int[][] localDrawnAutoWalls = state.drawnAutoWalls;
        Door[][] localDoorsMap = level.doorsMap;
        boolean[][] localAwTouchedCellsMap = levelRenderer.awTouchedCellsMap;

        for (int cy = 0, levelHeight = state.levelHeight; cy < levelHeight; cy++) {
            for (int cx = 0, levelWidth = state.levelWidth; cx < levelWidth; cx++) {
                int tex = localTexMap[cy][cx];
                int pass = localPassableMap[cy][cx];
                Door door = localDoorsMap[cy][cx];

                if ((pass & Level.PASSABLE_IS_WALL) != 0) {
                    for (int s = 0; s < 4; s++) {
                        int tx = cx + PortalTracer.X_CELL_ADD[s];
                        int ty = cy + PortalTracer.Y_CELL_ADD[s];

                        if (tx > 0
                                && ty > 0
                                && tx < levelWidth
                                && ty < levelHeight
                                && (localPassableMap[ty][tx] & Level.PASSABLE_IS_WALL) == 0) {

                            int fromX = cx + PortalTracer.X_ADD[s];
                            int fromY = cy + PortalTracer.Y_ADD[s];
                            int toX = cx + PortalTracer.X_ADD[(s + 1) % 4];
                            int toY = cy + PortalTracer.Y_ADD[(s + 1) % 4];

                            int mx = (fromX < toX ? fromX : toX);
                            int my = (fromY < toY ? fromY : toY);

                            int autoWallMask = ((s == 1 || s == 3)
                                    ? LevelRenderer.AUTO_WALL_MASK_VERTICAL
                                    : LevelRenderer.AUTO_WALL_MASK_HORIZONTAL);

                            if (((localDrawnAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
                                localDrawnAutoWalls[my][mx] |= autoWallMask;
                                levelRenderer.appendAutoWall(fromX, fromY, toX, toY, LevelRenderer.AUTO_WALL_TYPE_WALL);
                            }
                        }
                    }
                } else if ((pass & Level.PASSABLE_IS_TRANSP_WINDOW) != 0) {
                    float fromX;
                    float fromY;
                    float toX;
                    float toY;

                    boolean vert = ((pass & Level.PASSABLE_IS_TRANSP_WINDOW_VERT) != 0);

                    if (vert) {
                        fromX = (float)cx + 0.5f;
                        toX = fromX;
                        fromY = (float)cy;
                        toY = fromY + 1.0f;
                    } else {
                        fromX = (float)cx;
                        toX = fromX + 1.0f;
                        fromY = (float)cy + 0.5f;
                        toY = fromY;
                    }

                    if (((localDrawnAutoWalls[cy][cx] & LevelRenderer.AUTO_WALL_MASK_DOOR) == 0)
                            && state.autoWalls.canTake()) {

                        localDrawnAutoWalls[cy][cx] |= LevelRenderer.AUTO_WALL_MASK_DOOR;
                        AutoWall aw = state.autoWalls.take();

                        aw.fromX = fromX;
                        aw.fromY = fromY;
                        aw.toX = toX;
                        aw.toY = toY;
                        aw.vert = vert;
                        aw.type = LevelRenderer.AUTO_WALL_TYPE_TRANSP;
                        aw.doorUid = -1;
                        aw.door = null;
                    }
                } else if (((pass & Level.PASSABLE_IS_TRANSP) != 0) && (tex != 0)) {
                    for (int s = 0; s < 4; s++) {
                        int tx = cx + PortalTracer.X_CELL_ADD[s];
                        int ty = cy + PortalTracer.Y_CELL_ADD[s];

                        if (tx > 0
                                && ty > 0
                                && tx < levelWidth
                                && ty < levelHeight
                                && (localPassableMap[ty][tx] & Level.PASSABLE_MASK_WALL_N_TRANSP) == 0) {

                            int fromX = cx + PortalTracer.X_ADD[s];
                            int fromY = cy + PortalTracer.Y_ADD[s];
                            int toX = cx + PortalTracer.X_ADD[(s + 1) % 4];
                            int toY = cy + PortalTracer.Y_ADD[(s + 1) % 4];

                            int mx = (fromX < toX ? fromX : toX);
                            int my = (fromY < toY ? fromY : toY);

                            int autoWallMask = ((s == 1 || s == 3)
                                    ? LevelRenderer.AUTO_WALL_MASK_VERTICAL
                                    : LevelRenderer.AUTO_WALL_MASK_HORIZONTAL);

                            if (((localDrawnAutoWalls[my][mx] & autoWallMask) == 0) && state.autoWalls.canTake()) {
                                localDrawnAutoWalls[my][mx] |= autoWallMask;

                                levelRenderer.appendAutoWall(
                                        fromX,
                                        fromY,
                                        toX,
                                        toY,
                                        LevelRenderer.AUTO_WALL_TYPE_TRANSP);
                            }
                        }
                    }
                } else if (door != null) {
                    float fromX;
                    float fromY;
                    float toX;
                    float toY;

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

                    if (((localDrawnAutoWalls[door.y][door.x] & LevelRenderer.AUTO_WALL_MASK_DOOR) == 0)
                            && state.autoWalls.canTake()) {

                        localDrawnAutoWalls[door.y][door.x] |= LevelRenderer.AUTO_WALL_MASK_DOOR;
                        AutoWall aw = state.autoWalls.take();

                        aw.fromX = fromX;
                        aw.fromY = fromY;
                        aw.toX = toX;
                        aw.toY = toY;
                        aw.vert = door.vert;
                        aw.type = LevelRenderer.AUTO_WALL_TYPE_DOOR;
                        aw.doorUid = door.uid;
                        aw.door = door;
                    }
                }

                if ((pass & Level.PASSABLE_IS_WALL) == 0
                        && !localAwTouchedCellsMap[cy][cx]
                        && state.awTouchedCells.canTake()) {

                    localAwTouchedCellsMap[cy][cx] = true;
                    state.awTouchedCells.take().initFrom(cx, cy);
                }
            }
        }
    }

    @SuppressWarnings("MagicNumber")
    void renderHelp(Controls controls) {
        if ((state.disabledControlsMask & Controls.CONTROL_MINIMAP) != 0
                || (state.controlsHelpMask & Controls.CONTROL_MINIMAP) == 0) {

            return;
        }

        float xOff = (float)engine.width / engine.ratio * (AUTOMAP_VIEW_OFFSET + AUTOMAP_VIEW_SIZE) * 0.5f;

        controls.renderHelpArrowWithText(
                (engine.config.leftHandAim ? xOff : (float)engine.width - xOff),
                (float)engine.height * (AUTOMAP_VIEW_OFFSET + AUTOMAP_VIEW_SIZE) * 0.5f,
                Controls.DIAG_SIZE_LG,
                engine.config.leftHandAim,
                false,
                Labels.LABEL_HELP_MINIMAP);
    }

    public void updatePathTo() {
        if (level.buildPathToWavePending) {
            return;
        }

        level.autoMapPathCells.clear();

        if (state.pathToX < 0 || state.pathToY < 0) {
            return;
        }

        int[][] localPathToWaveMap = level.pathToWaveMap;
        int maxX = state.levelWidth - 1;
        int maxY = state.levelHeight - 1;

        int cx = (int)state.heroX;
        int cy = (int)state.heroY;
        int waveIdx = localPathToWaveMap[cy][cx];
        int px = cx;
        int py = cy;

        for (; ; ) {
            int nx = cx;
            int ny = cy;

            if (cy > 0 && localPathToWaveMap[cy - 1][cx] < waveIdx) {
                // nx = cx;
                ny = cy - 1;
                waveIdx = localPathToWaveMap[ny][cx];
            }

            if (cy < maxY && localPathToWaveMap[cy + 1][cx] < waveIdx) {
                // nx = cx;
                ny = cy + 1;
                waveIdx = localPathToWaveMap[ny][cx];
            }

            if (cx > 0 && localPathToWaveMap[cy][cx - 1] < waveIdx) {
                nx = cx - 1;
                ny = cy;
                waveIdx = localPathToWaveMap[ny][cx];
            }

            if (cx < maxX && localPathToWaveMap[cy][cx + 1] < waveIdx) {
                nx = cx + 1;
                ny = cy;
                waveIdx = localPathToWaveMap[ny][cx];
            }

            if (nx == cx && ny == cy) {
                break;
            }

            if ((state.passableMap[cy][cx] & Level.PASSABLE_IS_WALL) == 0) {
                AutoMapPathCell pc = level.autoMapPathCells.take();

                if (pc == null) {
                    return;
                }

                pc.initFrom(px, py, cx, cy, nx, ny);
            }

            px = cx;
            py = cy;
            cx = nx;
            cy = ny;
        }

        if ((px != cx || py != cy) && (state.passableMap[cy][cx] & Level.PASSABLE_IS_WALL) == 0) {
            AutoMapPathCell pc = level.autoMapPathCells.take();

            if (pc != null) {
                pc.initFrom(px, py, cx, cy, cx, cy);
            }
        }
    }
}
