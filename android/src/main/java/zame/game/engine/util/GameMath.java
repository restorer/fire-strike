package zame.game.engine.util;

public final class GameMath {
    public static final float PI_F = (float)Math.PI;
    public static final float PI_M2F = (float)(Math.PI * 2.0);
    public static final float PI_D2F = (float)(Math.PI / 2.0);
    public static final float G2RAD_F = (float)(Math.PI / 180.0);
    public static final float RAD2G_F = (float)(180.0 / Math.PI);
    public static final float EPSILON = 0.000000001f;
    public static final float ONE_MINUS_LITTLE = 1.0f - 0.0001f;
    public static final float SIGHT_OFFSET = 0.01f;

    private GameMath() {}

    @SuppressWarnings("ManualMinMaxCalculation")
    public static float getAngle(float dx, float dy) {
        float l = (float)Math.sqrt(dx * dx + dy * dy);
        float a = (float)Math.acos(dx / (l < EPSILON ? EPSILON : l));

        return (dy < 0 ? a : (PI_M2F - a));
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public static float getAngle(float dx, float dy, float l) {
        float a = (float)Math.acos(dx / (l < EPSILON ? EPSILON : l));
        return (dy < 0 ? a : (PI_M2F - a));
    }

    // modified Level_CheckLine from wolf3d for iphone by Carmack
    public static boolean traceLine(
            int levelWidth,
            int levelHeight,
            int[][] passableMap,
            float x1,
            float y1,
            float x2,
            float y2,
            int mask) {

        int cx1 = (int)x1;
        int cy1 = (int)y1;
        int cx2 = (int)x2;
        int cy2 = (int)y2;
        int maxX = levelWidth - 1;
        int maxY = levelHeight - 1;

        // level has one-cell border
        if (cx1 <= 0 || cx1 >= maxX || cy1 <= 0 || cy1 >= maxY || cx2 <= 0 || cx2 >= maxX || cy2 <= 0 || cy2 >= maxY) {
            return false;
        }

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
                if ((passableMap[(int)y][cx1] & mask) != 0) {
                    return false;
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
                if ((passableMap[cy1][(int)x] & mask) != 0) {
                    return false;
                }

                x += stepX;
                cy1 += stepY;
            } while (cy1 != cy2);
        }

        return true;
    }
}
