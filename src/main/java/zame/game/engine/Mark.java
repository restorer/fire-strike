package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class Mark extends DataListItem implements DataItem {
    private static final int FIELD_ID = 1;
    private static final int FIELD_X = 2;
    private static final int FIELD_Y = 3;
    private static final int FIELD_ENABLED = 4;

    public int id;
    public int x;
    public int y;
    public boolean enabled = true;

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_ID, id);
        writer.write(FIELD_X, x);
        writer.write(FIELD_Y, y);
        writer.write(FIELD_ENABLED, enabled);
    }

    @Override
    public void readFrom(DataReader reader) {
        id = reader.readInt(FIELD_ID);
        x = reader.readInt(FIELD_X);
        y = reader.readInt(FIELD_Y);
        enabled = reader.readBoolean(FIELD_ENABLED, true);
    }
}
