package zame.game.engine.hud;

import zame.game.engine.data.DataListItem;

public class AutoMapPathCell extends DataListItem {
    int x;
    int y;
    float cx;
    float cy;
    boolean hasFrom;
    boolean hasTo;
    float fdx;
    float fdy;
    float tdx;
    float tdy;

    @SuppressWarnings("MagicNumber")
    void initFrom(int px, int py, int cx, int cy, int nx, int ny) {
        x = cx;
        y = cy;
        this.cx = (float)cx + 0.5f;
        this.cy = (float)cy + 0.5f;

        hasFrom = (px != cx || py != cy);
        hasTo = (nx != cx || ny != cy);

        fdx = (float)(px - cx) * 0.5f;
        fdy = (float)(py - cy) * 0.5f;
        tdx = (float)(nx - cx) * 0.5f;
        tdy = (float)(ny - cy) * 0.5f;
    }
}
