package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;

public class TouchedCell extends DataListItem<TouchedCell> implements DataItem {
    private static final int FIELD_X = 1;
    private static final int FIELD_Y = 2;

    public int x;
    public int y;

    public void initFrom(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void copyFrom(TouchedCell tc) {
        x = tc.x;
        y = tc.y;
    }

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_X, x);
        writer.write(FIELD_Y, y);
    }

    @Override
    public void readFrom(DataReader reader) {
        x = reader.readInt(FIELD_X);
        y = reader.readInt(FIELD_Y);
    }
}
