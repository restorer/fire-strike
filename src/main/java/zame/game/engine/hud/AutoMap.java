package zame.game.engine.hud;

import javax.microedition.khronos.opengles.GL10;
import zame.game.engine.AutoWall;
import zame.game.engine.Bullet;
import zame.game.engine.Door;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Explosion;
import zame.game.engine.GameMath;
import zame.game.engine.Labels;
import zame.game.engine.Level;
import zame.game.engine.LevelRenderer;
import zame.game.engine.Monster;
import zame.game.engine.PortalTracer;
import zame.game.engine.Renderer;
import zame.game.engine.State;
import zame.game.engine.TextureLoader;
import zame.game.engine.TouchedCell;
import zame.game.engine.TraceInfo;
import zame.game.engine.controls.Controls;

public class AutoMap implements EngineObject {
    private static final float AUTOMAP_AREA = 30.0f;
    private static final float AUTOMAP_VIEW_OFFSET = 0.4f;
    private static final float AUTOMAP_VIEW_SIZE = 0.35f;

    private Engine engine;
    private Renderer renderer;
    private TextureLoader textureLoader;
    private Level level;
    private LevelRenderer levelRenderer;
    private State state;

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
        this.textureLoader = engine.textureLoader;
        this.level = engine.level;
        this.levelRenderer = engine.levelRenderer;
        this.state = engine.state;
    }

    @SuppressWarnings("MagicNumber")
    public void render(GL10 gl) {
        if ((state.disabledControlsMask & Controls.CONTROL_MINIMAP) != 0) {
            return;
        }

        gl.glDisable(GL10.GL_DEPTH_TEST);

        renderer.initOrtho(gl,
                true,
                false,
                -AUTOMAP_AREA * engine.ratio,
                AUTOMAP_AREA * engine.ratio,
                -AUTOMAP_AREA,
                AUTOMAP_AREA,
                0.0f,
                1.0f);

        float autoMapOffX = engine.config.leftHandAim
                ? AUTOMAP_AREA * AUTOMAP_VIEW_OFFSET - AUTOMAP_AREA * engine.ratio
                : AUTOMAP_AREA * engine.ratio - AUTOMAP_AREA * AUTOMAP_VIEW_OFFSET;

        float autoMapOffY = AUTOMAP_AREA - AUTOMAP_AREA * AUTOMAP_VIEW_OFFSET;

        gl.glPushMatrix(); // push model matrix
        gl.glTranslatef(autoMapOffX, autoMapOffY, 0.0f);

        // ----

        gl.glEnable(GL10.GL_STENCIL_TEST);
        gl.glStencilFunc(GL10.GL_ALWAYS, 1, 0xff);
        gl.glStencilOp(GL10.GL_REPLACE, GL10.GL_REPLACE, GL10.GL_REPLACE);
        gl.glStencilMask(0xff);
        gl.glClear(GL10.GL_STENCIL_BUFFER_BIT);

        renderer.init();

        renderer.x1 = -AUTOMAP_AREA * AUTOMAP_VIEW_SIZE;
        renderer.y1 = -AUTOMAP_AREA * AUTOMAP_VIEW_SIZE;

        renderer.x2 = -AUTOMAP_AREA * AUTOMAP_VIEW_SIZE;
        renderer.y2 = AUTOMAP_AREA * AUTOMAP_VIEW_SIZE;

        renderer.x3 = AUTOMAP_AREA * AUTOMAP_VIEW_SIZE;
        renderer.y3 = AUTOMAP_AREA * AUTOMAP_VIEW_SIZE;

        renderer.x4 = AUTOMAP_AREA * AUTOMAP_VIEW_SIZE;
        renderer.y4 = -AUTOMAP_AREA * AUTOMAP_VIEW_SIZE;

        renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 0.75f);
        renderer.drawQuad(TextureLoader.ICON_MAP);

        gl.glEnable(GL10.GL_ALPHA_TEST);

        // not using Renderer.ALPHA_VALUE to be able to render icon with alpha < 0.5
        gl.glAlphaFunc(GL10.GL_GREATER, 0.1f);

        // gl.glColorMask(false, false, false, false);
        renderer.bindTexture(gl, textureLoader.textures[TextureLoader.TEXTURE_MAIN]);
        renderer.flush(gl);
        gl.glDisable(GL10.GL_ALPHA_TEST);
        // gl.glColorMask(true, true, true, true);

        gl.glStencilFunc(GL10.GL_EQUAL, 1, 0xff);
        gl.glStencilOp(GL10.GL_KEEP, GL10.GL_KEEP, GL10.GL_KEEP);
        gl.glStencilMask(0);

        // ----

        gl.glRotatef(90.0f - levelRenderer.currentHeroA, 0.0f, 0.0f, 1.0f);
        gl.glTranslatef(-levelRenderer.currentHeroX, levelRenderer.currentHeroY, 0.0f);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        renderer.init();

        if (levelRenderer.debugOnAutomap) {
            renderer.r1 = 0.0f;
            renderer.r2 = 0.0f;
            renderer.g1 = 1.0f;
            renderer.g2 = 1.0f;
            renderer.b1 = 0.0f;
            renderer.b2 = 0.0f;
            renderer.a1 = 1.0f;
            renderer.a2 = 1.0f;

            for (int i = 0, len = levelRenderer.tracer.touchedCellsCountPriorToPostProcess; i < len; i++) {
                TouchedCell tc = levelRenderer.tracer.touchedCells[i];

                renderer.drawLine((float)tc.x, -(float)tc.y, (float)tc.x + 1.0f, -(float)tc.y - 1.0f);
                renderer.drawLine((float)tc.x, -(float)tc.y - 1.0f, (float)tc.x + 1.0f, -(float)tc.y);
            }

            renderer.g1 = 0.5f;
            renderer.g2 = 0.5f;

            for (int i = levelRenderer.tracer.touchedCellsCountPriorToPostProcess, len = levelRenderer.tracer.touchedCellsCount;
                    i < len;
                    i++) {

                TouchedCell tc = levelRenderer.tracer.touchedCells[i];

                renderer.drawLine((float)tc.x, -(float)tc.y, (float)tc.x + 1.0f, -(float)tc.y - 1.0f);
                renderer.drawLine((float)tc.x, -(float)tc.y - 1.0f, (float)tc.x + 1.0f, -(float)tc.y);
            }
        }

        renderer.r1 = 1.0f;
        renderer.r2 = 1.0f;
        renderer.g1 = 1.0f;
        renderer.g2 = 1.0f;
        renderer.b1 = 1.0f;
        renderer.b2 = 1.0f;
        renderer.a1 = 0.75f;
        renderer.a2 = 0.75f;

        for (AutoWall aw = state.autoWalls.first(); aw != null; aw = (AutoWall)aw.next) {
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

                renderer.drawLine(fromX, -fromY, toX, -toY);
            } else {
                renderer.drawLine(aw.fromX, -aw.fromY, aw.toX, -aw.toY);
            }
        }

        int[][] localPassableMap = state.passableMap;
        int[][] localArrowsMap = state.arrowsMap;
        boolean[][] localAwTouchedCellsMap = levelRenderer.awTouchedCellsMap;
        float cycle = (float)(engine.elapsedTime % 1000) / 1000.0f;

        // if (profile.isPurchased(Store.SECRETS)) {
        //     renderer.r1 = 0.0f; renderer.r2 = 0.0f;
        //     renderer.g1 = 1.0f; renderer.g2 = 1.0f;
        //
        //     for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
        //         if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_IS_SECRET) == 0) {
        //             continue;
        //         }
        //
        //         float x = (float)tc.x;
        // 	       float y = (float)tc.y;
        //
        // 	       renderer.drawLine(x + 0.35f, -(y + 0.5f), x + 0.65f, -(y + 0.5f));
        // 	       renderer.drawLine(x + 0.5f, -(y + 0.35f), x + 0.5f, -(y + 0.65f));
        //     }
        // }

        for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
            int arrowTex = localArrowsMap[tc.y][tc.x];

            if (arrowTex == 0) {
                continue;
            }

            float x = (float)tc.x;
            float y = (float)tc.y;
            float arrOffset = cycle * 0.5f - 0.5f;

            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    renderer.a1 = cycle;
                    renderer.a2 = cycle;
                } else {
                    renderer.a1 = 1.0f - cycle;
                    renderer.a2 = 1.0f - cycle;
                }

                switch (arrowTex) {
                    case TextureLoader.ARROW_UP:
                        renderer.drawLine(x + 0.25f, -(y + 0.75f - arrOffset), x + 0.5f, -(y + 0.25f - arrOffset));
                        renderer.drawLine(x + 0.5f, -(y + 0.25f - arrOffset), x + 0.75f, -(y + 0.75f - arrOffset));
                        break;

                    case TextureLoader.ARROW_RT:
                        renderer.drawLine(x + 0.25f + arrOffset, -(y + 0.75f), x + 0.75f + arrOffset, -(y + 0.5f));
                        renderer.drawLine(x + 0.75f + arrOffset, -(y + 0.5f), x + 0.25f + arrOffset, -(y + 0.25f));
                        break;

                    case TextureLoader.ARROW_DN:
                        renderer.drawLine(x + 0.25f, -(y + 0.25f + arrOffset), x + 0.5f, -(y + 0.75f + arrOffset));
                        renderer.drawLine(x + 0.5f, -(y + 0.75f + arrOffset), x + 0.75f, -(y + 0.25f + arrOffset));
                        break;

                    case TextureLoader.ARROW_LT:
                        renderer.drawLine(x + 0.75f - arrOffset, -(y + 0.75f), x + 0.25f - arrOffset, -(y + 0.5f));
                        renderer.drawLine(x + 0.25f - arrOffset, -(y + 0.5f), x + 0.75f - arrOffset, -(y + 0.25f));
                        break;
                }

                arrOffset += 0.5f;
            }
        }

        renderer.a1 = 1.0f;
        renderer.a2 = 1.0f;

        for (AutoMapPathCell pc = level.autoMapPathCells.first(); pc != null; pc = (AutoMapPathCell)pc.next) {
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

        renderer.a1 = 0.5f;
        renderer.a2 = 0.5f;

        for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
            if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_IS_DECOR_ITEM) == 0) {
                continue;
            }

            float x = (float)tc.x + 0.25f;
            float y = (float)tc.y + 0.25f;

            renderer.drawLine(x, -y, x + 0.5f, -y);
            renderer.drawLine(x + 0.5f, -y, x + 0.5f, -(y + 0.5f));
            renderer.drawLine(x + 0.5f, -(y + 0.5f), x, -(y + 0.5f));
            renderer.drawLine(x, -(y + 0.5f), x, -y);
        }

        for (TouchedCell tc = state.awTouchedCells.first(); tc != null; tc = (TouchedCell)tc.next) {
            if ((localPassableMap[tc.y][tc.x] & Level.PASSABLE_IS_OBJECT) == 0) {
                continue;
            }

            float x = (float)tc.x;
            float y = (float)tc.y;

            renderer.drawLine(x + 0.5f, -(y + 0.25f), x + 0.75f, -(y + 0.5f));
            renderer.drawLine(x + 0.75f, -(y + 0.5f), x + 0.5f, -(y + 0.75f));
            renderer.drawLine(x + 0.5f, -(y + 0.75f), x + 0.25f, -(y + 0.5f));
            renderer.drawLine(x + 0.25f, -(y + 0.5f), x + 0.5f, -(y + 0.25f));
        }

        if (levelRenderer.debugOnAutomap) {
            renderer.g1 = 0.0f;
            renderer.g2 = 0.0f;

            renderer.a1 = 1.0f;
            renderer.a2 = 1.0f;

            for (Monster mon = state.monsters.first(); mon != null; mon = (Monster)mon.next) {
                if (mon.health <= 0) {
                    renderer.r1 = 0.5f;
                    renderer.b1 = 0.5f;
                    renderer.r2 = 0.5f;
                    renderer.b2 = 0.5f;
                } else if (mon.chaseMode) {
                    renderer.r1 = 1.0f;
                    renderer.b1 = 0.0f;
                    renderer.r2 = 1.0f;
                    renderer.b2 = 0.0f;
                } else {
                    renderer.r1 = 0.0f;
                    renderer.b1 = 1.0f;
                    renderer.r2 = 0.0f;
                    renderer.b2 = 1.0f;
                }

                float mdx = (float)Math.cos((float)mon.shootAngle * GameMath.G2RAD_F) * 0.75f;
                float mdy = (float)Math.sin((float)mon.shootAngle * GameMath.G2RAD_F) * 0.75f;

                renderer.drawLine(mon.x, -mon.y, mon.x + mdx, -mon.y + mdy);

                mdx *= 0.25;
                mdy *= 0.25;

                renderer.drawLine(mon.x + mdy, -mon.y - mdx, mon.x - mdy, -mon.y + mdx);
            }

            renderer.r1 = 1.0f;
            renderer.b1 = 0.5f;
            renderer.r2 = 1.0f;
            renderer.b2 = 0.5f;

            for (Bullet bullet = state.bullets.first(); bullet != null; bullet = (Bullet)bullet.next) {
                float x = bullet.x;
                float y = bullet.y;

                renderer.drawLine(x, -(y - 0.125f), x + 0.125f, -y);
                renderer.drawLine(x + 0.125f, -y, x, -(y + 0.125f));
                renderer.drawLine(x, -(y + 0.125f), x - 0.125f, -y);
                renderer.drawLine(x - 0.125f, -y, x, -(y - 0.125f));
            }

            renderer.g1 = 0.5f;
            renderer.g2 = 0.5f;

            for (Explosion explosion = state.explosions.first();
                    explosion != null;
                    explosion = (Explosion)explosion.next) {

                float x = explosion.x;
                float y = explosion.y;

                renderer.drawLine(x, -(y - 0.125f), x + 0.125f, -y);
                renderer.drawLine(x + 0.125f, -y, x, -(y + 0.125f));
                renderer.drawLine(x, -(y + 0.125f), x - 0.125f, -y);
                renderer.drawLine(x - 0.125f, -y, x, -(y - 0.125f));
            }

            for (TraceInfo traceInfo = levelRenderer.tracesInfo.first(); traceInfo != null; ) {
                TraceInfo nextTraceInfo = (TraceInfo)traceInfo.next;

                if (traceInfo.hit == Bullet.HitParams.HIT_OUT) {
                    renderer.setLineRGB(0.0f, 1.0f, 0.0f);
                } else if (traceInfo.hit == Bullet.HitParams.HIT_WALL) {
                    renderer.setLineRGB(0.5f, 0.5f, 0.5f);
                } else if (traceInfo.hit == Bullet.HitParams.HIT_MONSTER) {
                    renderer.setLineRGB(0.0f, 1.0f, 0.0f);
                } else if (traceInfo.hit == Bullet.HitParams.HIT_HERO) {
                    renderer.setLineRGB(1.0f, 0.0f, 0.0f);
                } else {
                    renderer.setLineRGB(0.0f, 0.0f, 1.0f);
                }

                renderer.drawLine(traceInfo.sx, -traceInfo.sy, traceInfo.ex, -traceInfo.ey);
                traceInfo.ticks++;

                if (traceInfo.ticks > 10) {
                    levelRenderer.tracesInfo.release(traceInfo);
                }

                //noinspection AssignmentToForLoopParameter
                traceInfo = nextTraceInfo;
            }

            renderer.r1 = 1.0f;
            renderer.r2 = 1.0f;
            renderer.g1 = 1.0f;
            renderer.g2 = 1.0f;
            renderer.b1 = 0.0f;
            renderer.b2 = 0.0f;
            renderer.a1 = 1.0f;
            renderer.a2 = 1.0f;

            for (int i = 0, len = levelRenderer.tracer.wallsCount; i < len; i++) {
                PortalTracer.Wall wall = levelRenderer.tracer.walls[i];
                renderer.drawLine((float)wall.fromX, -(float)wall.fromY, (float)wall.toX, -(float)wall.toY);
            }
        }

        renderer.flush(gl, false);

        gl.glPopMatrix(); // pop model matrix
        gl.glTranslatef(autoMapOffX, autoMapOffY, 0.0f);
        renderer.init();

        final float hw = 0.4f;
        final float hh = 0.5f;

        renderer.r1 = 1.0f;
        renderer.r2 = 1.0f;
        renderer.g1 = 1.0f;
        renderer.g2 = 1.0f;
        renderer.b1 = 1.0f;
        renderer.b2 = 1.0f;
        renderer.a1 = 1.0f;
        renderer.a2 = 1.0f;

        renderer.drawLine(-hw, -hh, 0.0f, hh);
        renderer.drawLine(0.0f, hh, hw, -hh);
        renderer.drawLine(-hw, -hh, hw, -hh);

        renderer.flush(gl, false);

        gl.glDisable(GL10.GL_STENCIL_TEST);
        gl.glDisable(GL10.GL_BLEND);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
    }

    @SuppressWarnings("MagicNumber")
    private void drawPoint(float x, float y) {
        renderer.drawLine(x - 0.05f, -(y - 0.05f), x + 0.05f, -(y - 0.05f));
        renderer.drawLine(x + 0.05f, -(y - 0.05f), x + 0.05f, -(y + 0.05f));
        renderer.drawLine(x + 0.05f, -(y + 0.05f), x - 0.05f, -(y + 0.05f));
        renderer.drawLine(x - 0.05f, -(y + 0.05f), x - 0.05f, -(y - 0.05f));
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

    @SuppressWarnings({ "MagicNumber", "ConstantConditions" })
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

                                levelRenderer.appendAutoWall(fromX,
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
    public void renderHelp(GL10 gl, Controls controls) {
        if ((state.disabledControlsMask & Controls.CONTROL_MINIMAP) != 0
                || (state.controlsHelpMask & Controls.CONTROL_MINIMAP) == 0) {

            return;
        }

        float xOff = (float)engine.width / engine.ratio * (AUTOMAP_VIEW_OFFSET + AUTOMAP_VIEW_SIZE) * 0.5f;

        controls.drawHelpArrowWithText(gl,
                (engine.config.leftHandAim ? xOff : (float)engine.width - xOff),
                (float)engine.height * (AUTOMAP_VIEW_OFFSET + AUTOMAP_VIEW_SIZE) * 0.5f,
                Controls.DIAG_SIZE_LG,
                engine.config.leftHandAim,
                false,
                Labels.LABEL_HELP_MINIMAP);
    }

    @SuppressWarnings("ConstantConditions")
    public void updatePathTo() {
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
