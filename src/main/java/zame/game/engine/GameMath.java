package zame.game.engine;

public final class GameMath {
    public static final float PI_F = (float)Math.PI;
    static final float PI_M2F = (float)(Math.PI * 2.0);
    static final float PI_D2F = (float)(Math.PI / 2.0);
    public static final float G2RAD_F = (float)(Math.PI / 180.0);
    static final float RAD2G_F = (float)(180.0 / Math.PI);
    static final float EPSILON = 0.000000001f;
    static final float LITTLE = 0.0001f;
    static final float ONE_MINUS_LITTLE = 1.0f - LITTLE;

    private GameMath() {}

    static float getAngle(float dx, float dy) {
        float l = (float)Math.sqrt(dx * dx + dy * dy);
        float a = (float)Math.acos(dx / (l < EPSILON ? EPSILON : l));

        return (dy < 0 ? a : (PI_M2F - a));
    }

    static float getAngle(float dx, float dy, float l) {
        float a = (float)Math.acos(dx / (l < EPSILON ? EPSILON : l));
        return (dy < 0 ? a : (PI_M2F - a));
    }
}
