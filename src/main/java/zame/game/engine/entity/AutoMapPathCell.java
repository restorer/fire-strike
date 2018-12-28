package zame.game.engine.entity;

import zame.game.core.serializer.DataListItem;

public class AutoMapPathCell extends DataListItem<AutoMapPathCell> {
    public int x;
    public int y;
    public float cx;
    public float cy;
    public boolean hasFrom;
    public boolean hasTo;
    public float fdx;
    public float fdy;
    public float tdx;
    public float tdy;

    @SuppressWarnings("MagicNumber")
    public void initFrom(int px, int py, int cx, int cy, int nx, int ny) {
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
