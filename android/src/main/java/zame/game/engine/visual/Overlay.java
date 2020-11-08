package zame.game.engine.visual;

import android.content.res.Resources;

import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.state.State;
import zame.game.engine.util.GameMath;
import zame.game.feature.achievements.Achievements;

public class Overlay implements EngineObject {
    /**
     * This enum is used for building a gradient.
     * Coordinates represent bottom-left and top-right corners of the gradient rectangle.
     * CAUTION!!! DON'T CHANGE THE ORDER OF ELEMENTS! They are arranged like that for a reason.
     * The ordinals of these elements are used in some other methods for math computations.
     */
    private enum GradientDirection {
        Right(0.9f, 0.0f, 1.0f, 1.0f),
        Top(0.0f, 0.8f, 1.0f, 1.0f),
        Bottom(0.0f, 0.0f, 1.0f, 0.2f),
        Left(0.0f, 0.0f, 0.1f, 1.0f);

        final float x1;
        final float y1;
        final float x2;
        final float y2;

        GradientDirection(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    private static final float GRADIENT_BOUNDARY_1 = GameMath.PI_F / 3.0f; // top-left boundary
    private static final float GRADIENT_BOUNDARY_2 = GameMath.PI_F * 5.0f / 6.0f; // left-bottom boundary
    private static final float GRADIENT_BOUNDARY_3 = GameMath.PI_M2F - GRADIENT_BOUNDARY_2; // bottom-right boundary
    private static final float GRADIENT_BOUNDARY_4 = GameMath.PI_M2F - GRADIENT_BOUNDARY_1; // right-top boundary

    private static final float DURATION_BLOOD = 300.0f;
    private static final float DURATION_GENERAL = 500.0f;

    public static final int BLOOD = 1;
    public static final int ITEM = 2;
    public static final int MARK = 3;

    // @:formatter:off
    private static final float[][] COLORS = { new float[] { 1.0f, 0.0f, 0.0f }, // BLOOD
            new float[] { 1.0f, 1.0f, 1.0f }, // ITEM
            new float[] { 1.0f, 1.0f, 1.0f } // MARK
    };
    // @:formatter:on

    private Engine engine;
    private Config config;
    private Renderer renderer;
    private State state;
    private Labels labels;
    private Resources resources;
    private int overlayType;
    private long overlayTime;
    private String shownLabel;
    private long labelTime;
    private final long[] hitSideTime = { 0, 0, 0, 0 }; // timers for all the four gradients

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.config = engine.config;
        this.renderer = engine.renderer;
        this.state = engine.state;
        this.labels = engine.labels;
        this.resources = engine.activity.getResources();
    }

    public void reload() {
        overlayType = 0;
        shownLabel = null;

        for (int i = 0, len = hitSideTime.length; i < len; i++) {
            hitSideTime[i] = 0L;
        }
    }

    public void showOverlay(int type) {
        overlayType = type;
        overlayTime = engine.elapsedTime;
    }

    public void showLabel(int type) {
        shownLabel = labels.map[type];
        labelTime = engine.elapsedTime;
    }

    public void showAchievement(int resId) {
        shownLabel = String.format(
                labels.map[Labels.LABEL_ACHIEVEMENT_UNLOCKED],
                Achievements.cleanupTitle(resources.getString(resId)));

        labelTime = engine.elapsedTime;
    }

    public void showHitSide(float mx, float my) {
        hitSideTime[getDirection(mx, my, state.heroX, state.heroY, state.heroA).ordinal()] = engine.elapsedTime;
    }

    private void appendOverlayColor(float r, float g, float b, float a) {
        float d = renderer.a1 + a - renderer.a1 * a;

        //noinspection MagicNumber
        if (d < 0.001) {
            return;
        }

        renderer.r1 = (renderer.r1 * renderer.a1 - renderer.r1 * renderer.a1 * a + r * a) / d;
        renderer.g1 = (renderer.g1 * renderer.a1 - renderer.g1 * renderer.a1 * a + g * a) / d;
        renderer.b1 = (renderer.b1 * renderer.a1 - renderer.b1 * renderer.a1 * a + b * a) / d;
        renderer.a1 = d;
    }

    @SuppressWarnings("MagicNumber")
    public void renderOverlay() {
        renderer.r1 = 0.0f;
        renderer.g1 = 0.0f;
        renderer.b1 = 0.0f;
        renderer.a1 = 0.0f;

        float bloodAlpha = Math.max(
                0.0f,
                0.4f - ((float)state.heroHealth / 20.0f) * 0.4f);    // less than 20 health - show blood overlay

        if (bloodAlpha > 0.0f) {
            appendOverlayColor(COLORS[BLOOD - 1][0], COLORS[BLOOD - 1][1], COLORS[BLOOD - 1][2], bloodAlpha);
        }

        if (overlayType != 0 && !engine.inWallpaperMode) {
            float alpha = 0.5f - (float)(engine.elapsedTime - overlayTime) / DURATION_GENERAL;

            if (alpha > 0.0f) {
                appendOverlayColor(
                        COLORS[overlayType - 1][0],
                        COLORS[overlayType - 1][1],
                        COLORS[overlayType - 1][2],
                        alpha);
            } else {
                overlayType = 0;
            }
        }

        if (renderer.a1 < 0.001f) {
            return;
        }

        renderer.startBatch();

        renderer.r2 = renderer.r1;
        renderer.g2 = renderer.g1;
        renderer.b2 = renderer.b1;
        renderer.a2 = renderer.a1;

        renderer.r3 = renderer.r1;
        renderer.g3 = renderer.g1;
        renderer.b3 = renderer.b1;
        renderer.a3 = renderer.a1;

        renderer.r4 = renderer.r1;
        renderer.g4 = renderer.g1;
        renderer.b4 = renderer.b1;
        renderer.a4 = renderer.a1;

        renderer.setCoordsQuadRectFlat(0.0f, 0.0f, 1.0f, 1.0f);
        renderer.batchQuad();

        renderer.useOrtho(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);
        renderer.renderBatch(Renderer.FLAG_BLEND);
    }

    @SuppressWarnings("MagicNumber")
    private void renderLabelLine(float sy, float ey, String str, float op) {
        renderer.startBatch();

        if (engine.game.renderMode == Game.RENDER_MODE_GAME) {
            renderer.setColorQuadRGBA(0.0f, 0.0f, 0.0f, op * 0.25f);
        } else {
            renderer.setColorQuadRGBA(1.0f, 0.0f, 0.0f, op * 0.5f);
        }

        float my = sy + (ey - sy) * 0.5f;
        renderer.setCoordsQuadRectFlat(-1.0f, my - 0.25f, 1.0f, my + 0.25f);
        renderer.batchQuad();

        renderer.useOrtho(-1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f);
        renderer.renderBatch(Renderer.FLAG_BLEND);

        labels.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, op);
        labels.batch(-engine.ratio + 0.1f, sy, engine.ratio - 0.1f, ey, str, 0.25f, Labels.ALIGN_CC);
        labels.renderBatch();
    }

    @SuppressWarnings("MagicNumber")
    public void renderLabels() {
        if (shownLabel == null && state.shownMessageId < 0) {
            return;
        }

        if (shownLabel != null) {
            float op = Math.min(1.0f, 3.0f - (float)(engine.elapsedTime - labelTime) / 500.0f);

            if (op <= 0.0f) {
                shownLabel = null;
            } else {
                renderLabelLine(-0.75f, (state.shownMessageId >= 0 ? 0.0f : 1.0f), shownLabel, op);
            }
        }

        if (engine.game.renderMode == Game.RENDER_MODE_GAME
                && state.shownMessageId >= 0
                && state.shownMessageId < Labels.LABEL_LAST) {

            renderLabelLine(0.0f, 0.75f, labels.map[state.shownMessageId], 1.0f);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void renderEndLevelLayer(float dt) {
        renderer.startBatch();

        renderer.a2 = Math.min(1.0f, dt) * 0.9f;
        renderer.a3 = renderer.a2;

        renderer.a1 = Math.min(1.0f, dt * 0.5f) * 0.5f;
        renderer.a4 = renderer.a1;

        renderer.setColorQuadRGB(0.0f, 0.0f, 0.0f);
        renderer.setCoordsQuadRectFlat(0.0f, 0.0f, 1.0f, 1.0f);
        renderer.batchQuad();

        renderer.useOrtho(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);
        renderer.renderBatch(Renderer.FLAG_BLEND | Renderer.FLAG_SMOOTH);
    }

    @SuppressWarnings("MagicNumber")
    public void renderHitSide() {
        renderer.startBatch();
        renderer.setColorQuadRGB(COLORS[BLOOD - 1][0], COLORS[BLOOD - 1][1], COLORS[BLOOD - 1][2]);

        // repeat for each of the four sides
        for (int i = 0, len = hitSideTime.length; i < len; i++) {
            if (hitSideTime[i] == 0L) {
                continue;
            }

            // mn stands for "magic number". Used for math.
            int mn = (i + 1) * 3;

            // The logic behind it.
            // Remember the ortho quadrants location:
            // 2 | 3
            // -----
            // 1 | 4
            //
            // These are the corners of the gradient rectangle, in this order.
            // So when we assign alphas to the corners, we assign 1 to the brightest side,
            // and 0 to the faintest side.
            // Then for right side gradient we would have: 0 0 1 1 (in this order),
            // because the right corners are the brightest, and so on.
            // So what if we wrote this as a binary number?
            //
            // Right:  0011 = 3  = (0 + 1) * 3 , where Right.ordinal()  == 0
            // Top:    0110 = 6  = (1 + 1) * 3 , where Top.ordinal()    == 1
            // Bottom: 1001 = 9  = (2 + 1) * 3 , where Bottom.ordinal() == 2
            // Left:   1100 = 12 = (3 + 1) * 3 , where Left.ordinal()   == 3
            //
            // So basically mn == (ordinal + 1) * 3 that exactly equals the bit sequence of gradients.
            // This will allow us to use bitwise operations in a uniform way to obtain alpha values
            // we need in each certain case.

            int deltaTime = (int)(engine.elapsedTime - hitSideTime[i]);

            if (deltaTime < DURATION_BLOOD) {
                // delayRatio was, at the beginning, just the fraction of the time interval during which
                // the gradient should fade, but later, we changed it to ease computations,
                // adding here many otherwise repeated operations.
                // First of all, as the gradient should fade with time and not become brighter,
                // the quantity should actually decrease, so we subtract it from 1 to reach the effect.
                // Then, we thought that it would look better if we decreased opacity of the gradient as a whole,
                // so we added a multiplier to obtain that.
                float delayRatio = (1 - deltaTime / DURATION_BLOOD) * 0.8f;

                // So there is where all the interesting things happen.
                // Now we should retrieve the alpha bits from the mn variable.
                // Remember that counting right-to-left, as with place values,
                // we have:
                // a1 - quadrant 1 - 3rd position - x2 ^ 3
                // a2 - quadrant 2 - 2nd position - x2 ^ 2
                // a3 - quadrant 3 - 1st position - x2 ^ 1
                // a4 - quadrant 4 - 0th position - x2 ^ 0
                //
                // So now we should convert these positions back to 1s and 0s.
                // For that, we could use the bitwise AND to retrieve the needed bit:
                // a1 - **** & 1000 = *000 (1000 = 8 = 2^3)
                // a2 - **** & 0100 = 0*00 (0100 = 4 = 2^2)
                // a3 - **** & 0010 = 00*0 (0010 = 2 = 2^1)
                // a4 - **** & 0001 = 000* (0001 = 1 = 2^0)
                //
                // So now we have isolated this bit, and the one bit that is still left
                // needs to be moved to the 0th position in order to become a decimal 1 or 0:
                // a1 - *000 >> 3 = 000* = 0 or 1
                // a2 - 0*00 >> 2 = 000* = 0 or 1
                // a3 - 00*0 >> 1 = 000* = 0 or 1
                // a4 - 000* >> 0 = 000* = 0 or 1
                //
                // Now if you look at the assignments you will see that is exactly what was done.
                // Finally, we multiply everything by delayRatio in order to establish proper fading.

                renderer.a1 = ((mn & 8) >> 3) * delayRatio;
                renderer.a2 = ((mn & 4) >> 2) * delayRatio;
                renderer.a3 = ((mn & 2) >> 1) * delayRatio;
                renderer.a4 = (mn & 1) * delayRatio;

                // set the coordinates of the gradient rectangle, taken from a enum element with a given ordinal
                GradientDirection grad = GradientDirection.values()[i];
                renderer.setCoordsQuadRectFlat(grad.x1, grad.y1, grad.x2, grad.y2); // from top

                renderer.batchQuad();
            } else {
                hitSideTime[i] = 0L;
            }
        }

        renderer.useOrtho(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);
        renderer.renderBatch(Renderer.FLAG_BLEND | Renderer.FLAG_SMOOTH);
    }

    public void renderGammaLayer() {
        renderer.useOrtho(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);
        renderer.startBatch();

        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, config.gamma);
        renderer.setCoordsQuadRectFlat(0.0f, 0.0f, 1.0f, 1.0f);
        renderer.batchQuad();

        renderer.renderBatch(Renderer.FLAG_BLEND | Renderer.FLAG_BLEND_GAMMA);
    }

    private Overlay.GradientDirection getDirection(float mx, float my, float px, float py, float pa) {
        float paRad = pa * GameMath.G2RAD_F;
        float gamma = GameMath.getAngle(px - mx, py - my);
        float phi = (paRad - gamma > 0 ? paRad : GameMath.PI_M2F + paRad) - gamma;

        if (phi <= GRADIENT_BOUNDARY_1 || phi >= GRADIENT_BOUNDARY_4) {
            return GradientDirection.Bottom;
        }

        if (phi <= GRADIENT_BOUNDARY_2) {
            return GradientDirection.Left;
        }

        if (phi <= GRADIENT_BOUNDARY_3) {
            return GradientDirection.Top;
        }

        return GradientDirection.Right;
    }
}
