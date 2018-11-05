package zame.game.engine.hud;

import javax.microedition.khronos.opengles.GL10;
import zame.game.App;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Labels;
import zame.game.engine.Renderer;
import zame.game.engine.State;
import zame.game.engine.TextureLoader;
import zame.game.engine.Weapons;
import zame.game.engine.controls.Controls;

public class Stats implements EngineObject {
    private static final float DIST_Y_STATS = 0.5f;
    private static final float DIST_Y_KEYS = 0.5f;
    private static final float OFFSET_Y_STATS = 1.25f;
    private static final float OFFSET_Y_KEYS = 1.25f;

    private Engine engine;
    private Renderer renderer;
    private Config config;
    private Labels labels;
    private Weapons weapons;
    private TextureLoader textureLoader;
    private State state;
    private float iconSize;
    private float startX;
    private float startKeysX;

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
        this.config = engine.config;
        this.labels = engine.labels;
        this.weapons = engine.weapons;
        this.textureLoader = engine.textureLoader;
        this.state = engine.state;
    }

    public void surfaceSizeChanged() {
        //noinspection MagicNumber
        iconSize = (float)Math.min(engine.height, engine.width) / 5.0f * App.self.controlsScale;

        startX = -1.0f;
        startKeysX = -1.0f;
    }

    @SuppressWarnings("MagicNumber")
    private void drawStatIcon(@SuppressWarnings("unused") GL10 gl, int pos, int texNum) {
        float pointerX = startX;
        float pointerY = ((float)pos * DIST_Y_STATS + OFFSET_Y_STATS) * iconSize;

        float sx = (pointerX / (float)engine.width * engine.ratio) - 0.125f * App.self.controlsScale;
        float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * App.self.controlsScale;

        float ex = sx + 0.25f * App.self.controlsScale;
        float ey = sy + 0.25f * App.self.controlsScale;

        renderer.x1 = sx;
        renderer.y1 = sy;
        renderer.x2 = sx;
        renderer.y2 = ey;
        renderer.x3 = ex;
        renderer.y3 = ey;
        renderer.x4 = ex;
        renderer.y4 = sy;

        renderer.drawQuad(texNum);
    }

    @SuppressWarnings("MagicNumber")
    private void drawStatLabel(GL10 gl, int pos, int value) {
        float pointerX = startX;
        float pointerY = ((float)pos * DIST_Y_STATS + OFFSET_Y_STATS) * iconSize;

        float sx = (pointerX / (float)engine.width * engine.ratio) + 0.0625f * App.self.controlsScale;
        float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * App.self.controlsScale;

        float ex = sx + 0.125f * App.self.controlsScale; // 0.0625f
        float ey = sy + 0.25f * App.self.controlsScale;

        labels.draw(gl, sx, sy, ex, ey, value, 0.05f * App.self.controlsScale, Labels.ALIGN_CL); // 0.0625f
    }

    @SuppressWarnings("MagicNumber")
    private void drawKeyIcon(@SuppressWarnings("unused") GL10 gl, int pos, int texNum) {
        float pointerX = startKeysX;
        float pointerY = ((float)pos * DIST_Y_KEYS + OFFSET_Y_KEYS) * iconSize;

        float sx = (pointerX / (float)engine.width * engine.ratio) - 0.125f * App.self.controlsScale;
        float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * App.self.controlsScale;

        float ex = sx + 0.25f * App.self.controlsScale;
        float ey = sy + 0.25f * App.self.controlsScale;

        renderer.x1 = sx;
        renderer.y1 = sy;
        renderer.x2 = sx;
        renderer.y2 = ey;
        renderer.x3 = ex;
        renderer.y3 = ey;
        renderer.x4 = ex;
        renderer.y4 = sy;

        renderer.drawQuad(texNum);
    }

    @SuppressWarnings("MagicNumber")
    public void render(GL10 gl) {
        if (startX < 0.0f) {
            if (config.leftHandAim) {
                startX = (float)engine.width - iconSize * 1.0f;
                startKeysX = startX - (0.5f + 0.1f) * iconSize;
            } else {
                startX = iconSize * 0.5f;
                startKeysX = startX + (1.0f + 0.1f) * iconSize;
            }
        }

        renderer.initOrtho(gl, true, false, 0.0f, engine.ratio, 0.0f, 1.0f, 0.0f, 1.0f);

        renderer.z1 = 0.0f;
        renderer.z2 = 0.0f;
        renderer.z3 = 0.0f;
        renderer.z4 = 0.0f;

        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        renderer.r1 = 1.0f;
        renderer.g1 = 1.0f;
        renderer.b1 = 1.0f;
        renderer.a1 = 0.8f;
        renderer.r2 = 1.0f;
        renderer.g2 = 1.0f;
        renderer.b2 = 1.0f;
        renderer.a2 = 0.8f;
        renderer.r3 = 1.0f;
        renderer.g3 = 1.0f;
        renderer.b3 = 1.0f;
        renderer.a3 = 0.8f;
        renderer.r4 = 1.0f;
        renderer.g4 = 1.0f;
        renderer.b4 = 1.0f;
        renderer.a4 = 0.8f;

        renderer.init();

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_HEALTH) == 0) {
            drawStatIcon(gl, 0, TextureLoader.ICON_HEALTH);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_ARMOR) == 0) {
            drawStatIcon(gl, 1, TextureLoader.ICON_ARMOR);
        }

        if ((weapons.currentParams.ammoIdx >= 0)
                && (state.heroAmmo[weapons.currentParams.ammoIdx] >= 0)
                && (state.disabledControlsMask & Controls.CONTROL_STATS_AMMO) == 0) {

            drawStatIcon(gl, 2, TextureLoader.ICON_AMMO);
        }

        int keyPos = 0;

        renderer.a1 = 1.0f;
        renderer.a2 = 1.0f;
        renderer.a3 = 1.0f;
        renderer.a4 = 1.0f;

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_KEYS) == 0) {
            if ((state.heroKeysMask & 1) != 0) {
                drawKeyIcon(gl, keyPos, TextureLoader.ICON_BLUE_KEY);
                keyPos++;
            }

            if ((state.heroKeysMask & 2) != 0) {
                drawKeyIcon(gl, keyPos, TextureLoader.ICON_RED_KEY);
                keyPos++;
            }

            if ((state.heroKeysMask & 4) != 0) {
                drawKeyIcon(gl, keyPos, TextureLoader.ICON_GREEN_KEY);
            }
        }

        renderer.bindTextureCtl(gl, textureLoader.textures[TextureLoader.TEXTURE_MAIN]);
        renderer.flush(gl);

        renderer.init();

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_HEALTH) == 0) {
            drawStatLabel(gl, 0, state.heroHealth);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_ARMOR) == 0) {
            drawStatLabel(gl, 1, state.heroArmor);
        }

        if ((weapons.currentParams.ammoIdx >= 0)
                && (state.heroAmmo[weapons.currentParams.ammoIdx] >= 0)
                && (state.disabledControlsMask & Controls.CONTROL_STATS_AMMO) == 0) {

            drawStatLabel(gl, 2, state.heroAmmo[weapons.currentParams.ammoIdx]);
        }

        renderer.bindTextureBlur(gl, textureLoader.textures[TextureLoader.TEXTURE_LABELS]);
        renderer.flush(gl);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
    }

    @SuppressWarnings("MagicNumber")
    public void renderHelp(GL10 gl, Controls controls) {
        float xOff = 0.03125f * App.self.controlsScale * (float)engine.width / engine.ratio;
        float yOff = 0.03125f * App.self.controlsScale * (float)engine.height;

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_HEALTH) == 0
                && (state.controlsHelpMask & Controls.CONTROL_STATS_HEALTH) != 0) {

            controls.drawHelpArrowWithText(gl,
                    startX + xOff,
                    OFFSET_Y_STATS * iconSize + yOff,
                    Controls.DIAG_SIZE_XLG,
                    !engine.config.leftHandAim,
                    false,
                    Labels.LABEL_HELP_STATS_HEALTH);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_AMMO) == 0
                && (state.controlsHelpMask & Controls.CONTROL_STATS_AMMO) != 0) {

            controls.drawHelpArrowWithText(gl,
                    startX + xOff,
                    (DIST_Y_STATS * 2.0f + OFFSET_Y_STATS) * iconSize + yOff,
                    Controls.DIAG_SIZE,
                    !engine.config.leftHandAim,
                    false,
                    Labels.LABEL_HELP_STATS_AMMO);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_ARMOR) == 0
                && (state.controlsHelpMask & Controls.CONTROL_STATS_ARMOR) != 0) {

            controls.drawHelpArrowWithText(gl,
                    startX + xOff,
                    (DIST_Y_STATS + OFFSET_Y_STATS) * iconSize + yOff,
                    Controls.DIAG_SIZE_LG,
                    !engine.config.leftHandAim,
                    false,
                    Labels.LABEL_HELP_STATS_ARMOR);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_KEYS) == 0
                && (state.controlsHelpMask & Controls.CONTROL_STATS_KEYS) != 0) {

            controls.drawHelpArrowWithText(gl,
                    startKeysX + xOff,
                    OFFSET_Y_KEYS * iconSize + yOff,
                    Controls.DIAG_SIZE,
                    !engine.config.leftHandAim,
                    false,
                    Labels.LABEL_HELP_STATS_KEYS);
        }
    }
}
