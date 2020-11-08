package zame.game.engine.entity;

import zame.game.core.serializer.DataListItem;
import zame.game.engine.level.LevelRenderer;

public class DebugTraceInfo extends DataListItem<DebugTraceInfo> {
    public float sx;
    public float sy;
    public float ex;
    public float ey;
    public int hit;
    public int ticks;

    static void addInfo(LevelRenderer levelRenderer, float sx, float sy, float ex, float ey, int hit) {
        DebugTraceInfo traceInfo = levelRenderer.debugTraceInfos.take();

        if (traceInfo == null) {
            levelRenderer.debugTraceInfos.release(levelRenderer.debugTraceInfos.first());
            traceInfo = levelRenderer.debugTraceInfos.take();
        }

        traceInfo.sx = sx;
        traceInfo.sy = sy;
        traceInfo.ex = ex;
        traceInfo.ey = ey;
        traceInfo.hit = hit;
        traceInfo.ticks = 0;
    }
}
