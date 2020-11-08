package zame.game.engine.visual;

import zame.game.App;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.state.State;

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
    private State state;
    private float iconSize;
    private float startX;
    private float startKeysX;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
        this.config = engine.config;
        this.labels = engine.labels;
        this.weapons = engine.weapons;
        this.state = engine.state;
    }

    public void surfaceSizeChanged() {
        //noinspection MagicNumber
        iconSize = (float)Math.min(engine.height, engine.width) / 5.0f * App.self.controlsScale;

        startX = -1.0f;
        startKeysX = -1.0f;
    }

    @SuppressWarnings("MagicNumber")
    private void batchStatIcon(int pos, int texNum) {
        float pointerX = startX;
        float pointerY = ((float)pos * DIST_Y_STATS + OFFSET_Y_STATS) * iconSize;

        float sx = (pointerX / (float)engine.width * engine.ratio) - 0.125f * App.self.controlsScale;
        float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * App.self.controlsScale;

        renderer.setCoordsQuadRect(sx, sy, sx + 0.25f * App.self.controlsScale, sy + 0.25f * App.self.controlsScale);
        renderer.batchTexQuad(texNum);
    }

    @SuppressWarnings("MagicNumber")
    private void batchStatLabel(int pos, int value) {
        float pointerX = startX;
        float pointerY = ((float)pos * DIST_Y_STATS + OFFSET_Y_STATS) * iconSize;

        float sx = (pointerX / (float)engine.width * engine.ratio) + 0.0625f * App.self.controlsScale;
        float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * App.self.controlsScale;

        float ex = sx + 0.125f * App.self.controlsScale; // 0.0625f
        float ey = sy + 0.25f * App.self.controlsScale;

        labels.batch(sx, sy, ex, ey, value, 0.05f * App.self.controlsScale, Labels.ALIGN_CL); // 0.0625f
    }

    @SuppressWarnings("MagicNumber")
    private void batchKeyIcon(int pos, int texNum) {
        float pointerX = startKeysX;
        float pointerY = ((float)pos * DIST_Y_KEYS + OFFSET_Y_KEYS) * iconSize;

        float sx = (pointerX / (float)engine.width * engine.ratio) - 0.125f * App.self.controlsScale;
        float sy = (1.0f - pointerY / (float)engine.height) - 0.125f * App.self.controlsScale;

        renderer.setCoordsQuadRect(
                sx,
                sy,
                sx + 0.25f * App.self.controlsScale,
                sy + 0.25f * App.self.controlsScale);

        renderer.batchTexQuad(texNum);
    }

    @SuppressWarnings("MagicNumber")
    public void render() {
        if (startX < 0.0f) {
            if (config.leftHandAim) {
                startX = (float)engine.width - iconSize * 1.0f;
                startKeysX = startX - (0.5f + 0.1f) * iconSize;
            } else {
                startX = iconSize * 0.5f;
                startKeysX = startX + (1.0f + 0.1f) * iconSize;
            }
        }

        renderer.startBatch();
        renderer.setCoordsQuadZ(0.0f);
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 0.8f);

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_HEALTH) == 0) {
            batchStatIcon(0, TextureLoader.ICON_HEALTH);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_ARMOR) == 0) {
            batchStatIcon(1, TextureLoader.ICON_ARMOR);
        }

        if ((weapons.currentParams.ammoIdx >= 0)
                && (state.heroAmmo[weapons.currentParams.ammoIdx] >= 0)
                && (state.disabledControlsMask & Controls.CONTROL_STATS_AMMO) == 0) {

            batchStatIcon(2, TextureLoader.ICON_AMMO);
        }

        int keyPos = 0;
        renderer.setColorQuadA(1.0f);

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_KEYS) == 0) {
            if ((state.heroKeysMask & 1) != 0) {
                batchKeyIcon(keyPos, TextureLoader.ICON_BLUE_KEY);
                keyPos++;
            }

            if ((state.heroKeysMask & 2) != 0) {
                batchKeyIcon(keyPos, TextureLoader.ICON_RED_KEY);
                keyPos++;
            }

            if ((state.heroKeysMask & 4) != 0) {
                batchKeyIcon(keyPos, TextureLoader.ICON_GREEN_KEY);
            }
        }

        renderer.useOrtho(0.0f, engine.ratio, 0.0f, 1.0f, 0.0f, 1.0f);
        renderer.renderBatch(Renderer.FLAG_BLEND, Renderer.TEXTURE_MAIN);

        labels.startBatch(true);

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_HEALTH) == 0) {
            batchStatLabel(0, state.heroHealth);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_ARMOR) == 0) {
            batchStatLabel(1, state.heroArmor);
        }

        if ((weapons.currentParams.ammoIdx >= 0)
                && (state.heroAmmo[weapons.currentParams.ammoIdx] >= 0)
                && (state.disabledControlsMask & Controls.CONTROL_STATS_AMMO) == 0) {

            batchStatLabel(2, state.heroAmmo[weapons.currentParams.ammoIdx]);
        }

        labels.renderBatch();
    }

    @SuppressWarnings("MagicNumber")
    void renderHelp(Controls controls) {
        float xOff = 0.03125f * App.self.controlsScale * (float)engine.width / engine.ratio;
        float yOff = 0.03125f * App.self.controlsScale * (float)engine.height;

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_HEALTH) == 0
                && (state.controlsHelpMask & Controls.CONTROL_STATS_HEALTH) != 0) {

            controls.renderHelpArrowWithText(
                    startX + xOff,
                    OFFSET_Y_STATS * iconSize + yOff,
                    Controls.DIAG_SIZE_XLG,
                    !engine.config.leftHandAim,
                    false,
                    Labels.LABEL_HELP_STATS_HEALTH);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_AMMO) == 0
                && (state.controlsHelpMask & Controls.CONTROL_STATS_AMMO) != 0) {

            controls.renderHelpArrowWithText(
                    startX + xOff,
                    (DIST_Y_STATS * 2.0f + OFFSET_Y_STATS) * iconSize + yOff,
                    Controls.DIAG_SIZE,
                    !engine.config.leftHandAim,
                    false,
                    Labels.LABEL_HELP_STATS_AMMO);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_ARMOR) == 0
                && (state.controlsHelpMask & Controls.CONTROL_STATS_ARMOR) != 0) {

            controls.renderHelpArrowWithText(
                    startX + xOff,
                    (DIST_Y_STATS + OFFSET_Y_STATS) * iconSize + yOff,
                    Controls.DIAG_SIZE_LG,
                    !engine.config.leftHandAim,
                    false,
                    Labels.LABEL_HELP_STATS_ARMOR);
        }

        if ((state.disabledControlsMask & Controls.CONTROL_STATS_KEYS) == 0
                && (state.controlsHelpMask & Controls.CONTROL_STATS_KEYS) != 0) {

            controls.renderHelpArrowWithText(
                    startKeysX + xOff,
                    OFFSET_Y_KEYS * iconSize + yOff,
                    Controls.DIAG_SIZE,
                    !engine.config.leftHandAim,
                    false,
                    Labels.LABEL_HELP_STATS_KEYS);
        }
    }
}
