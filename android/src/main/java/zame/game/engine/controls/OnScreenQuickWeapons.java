package zame.game.engine.controls;

import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.visual.Controls;

public class OnScreenQuickWeapons extends OnScreenController {
    public float direction = 1.0f;

    private final boolean[] active = { false, false, false };
    private final int[] mapping = new int[3];
    private float fromY;
    private float toY;

    public OnScreenQuickWeapons(int position) {
        super();

        this.position = position;
        this.controlFlags = Controls.CONTROL_QUICK_WEAPONS;
        this.helpLabelId = Labels.LABEL_HELP_QUICK_WEAPONS;
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void surfaceSizeChanged() {
        super.surfaceSizeChanged();

        float btnOffsetY = owner.iconSize * 0.55f;
        float btnClickArea = owner.iconSize * 0.5f;

        if ((position & Controls.POSITION_TOP) != 0) {
            startY = btnOffsetY;
        } else {
            startY = (float)engine.height - 1.0f - btnOffsetY;
        }

        startX = (float)engine.width * 0.5f;

        fromY = startY - btnClickArea;
        toY = startY + btnClickArea;
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public boolean pointerDown(float x, float y) {
        if (y < fromY || y > toY) {
            return false;
        }

        float btnOffsetX = owner.iconSize * 0.55f;
        float btnClickArea = owner.iconSize * 0.5f;
        int count = 0;

        for (int i = 0, len = state.lastWeapons.length; i < len; i++) {
            if (state.lastWeapons[i] >= 0) {
                mapping[count] = i;
                count++;
            }
        }

        if (count == 0) {
            return false;
        }

        float baseX = startX - btnOffsetX * (float)(count - 1) * direction;

        for (int i = 0; i < count; i++) {
            float fromX = baseX + btnOffsetX * 2.0f * (float)i * direction - btnClickArea;
            float toX = fromX + btnClickArea * 2.0f;

            if (x >= fromX && x <= toX && !engine.weapons.hasNoAmmo(state.lastWeapons[mapping[i]])) {
                active[i] = true;
                game.actionQuickWeapons[mapping[i]] = true;
                return true;
            }
        }

        return false;
    }

    @Override
    public void pointerUp() {
        super.pointerUp();

        for (int i = 0, len = active.length; i < len; i++) {
            active[i] = false;
        }

        for (int i = 0, len = game.actionQuickWeapons.length; i < len; i++) {
            game.actionQuickWeapons[i] = false;
        }
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void render(long elapsedTime, boolean canRenderHelp) {
        float btnOffsetX = owner.iconSize * 0.55f;
        int count = 0;

        for (int i = 0, len = state.lastWeapons.length; i < len; i++) {
            if (state.lastWeapons[i] >= 0) {
                mapping[count] = i;
                count++;
            }
        }

        if (count == 0) {
            return;
        }

        float baseX = startX - btnOffsetX * (float)(count - 1) * direction;

        for (int i = 0; i < count; i++) {
            owner.batchIcon(
                    baseX + btnOffsetX * 2.0f * (float)i * direction,
                    startY,
                    TextureLoader.BASE_WEAPONS + state.lastWeapons[mapping[i]],
                    active[i],
                    engine.weapons.hasNoAmmo(state.lastWeapons[mapping[i]]) ? 0.25f : -1.0f);
        }

        if (canRenderHelp && shouldRenderHelp()) {
            float dt = (float)elapsedTime * 0.0025f;
            int index = (int)(dt / Math.PI) % count;

            if (mapping[index] == state.heroWeapon) {
                index = (index + 1) % count;
            }

            owner.batchIcon(
                    baseX + btnOffsetX * 2.0f * (float)index * direction,
                    startY,
                    TextureLoader.ICON_JOY,
                    false,
                    0.5f - (float)Math.cos(dt * 2.0f) * 0.5f,
                    (float)Math.cos(dt % Math.PI) * 0.5f + 1.5f);

        }
    }
}
