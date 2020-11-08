package zame.game.engine.entity;

import java.util.Random;

import zame.game.core.serializer.DataListItem;
import zame.game.engine.level.LevelRenderer;

public class BulletTrace extends DataListItem<BulletTrace> {
    public static final int MAX_TICKS = 10;

    public float x;
    public float y;
    public float z; // 0 - at middle, 0.25 - lower than middle
    public float off;
    public long ticks;

    @SuppressWarnings({ "MagicNumber", "SameParameterValue" })
    static void append(Random random, LevelRenderer levelRenderer, float x, float y, float z) {
        BulletTrace bulletTrace = levelRenderer.bulletTraces.take();

        if (bulletTrace == null) {
            levelRenderer.bulletTraces.release(levelRenderer.bulletTraces.first());
            bulletTrace = levelRenderer.bulletTraces.take();
        }

        bulletTrace.x = x;
        bulletTrace.y = y;
        bulletTrace.z = z + random.nextFloat() * 0.05f - 0.025f;
        bulletTrace.off = random.nextFloat() * 0.05f - 0.025f;
        bulletTrace.ticks = MAX_TICKS;
    }
}
