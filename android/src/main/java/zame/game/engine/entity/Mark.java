package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;

public class Mark extends DataListItem<Mark> implements DataItem {
    private static final int FIELD_ID = 1;
    private static final int FIELD_X = 2;
    private static final int FIELD_Y = 3;
    private static final int FIELD_ENABLED = 4;
    private static final int FIELD_UNMARKED = 5;

    public int id;
    public int x;
    public int y;
    public boolean enabled = true;
    public boolean unmarked;

    public void configure(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;

        enabled = true;
        unmarked = false;
    }

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_ID, id);
        writer.write(FIELD_X, x);
        writer.write(FIELD_Y, y);
        writer.write(FIELD_ENABLED, enabled);
        writer.write(FIELD_UNMARKED, unmarked);
    }

    @Override
    public void readFrom(DataReader reader) {
        id = reader.readInt(FIELD_ID);
        x = reader.readInt(FIELD_X);
        y = reader.readInt(FIELD_Y);
        enabled = reader.readBoolean(FIELD_ENABLED, true);
        unmarked = reader.readBoolean(FIELD_UNMARKED);
    }
}
