package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;

public class Timeout extends DataListItem<Timeout> implements DataItem {
    private static final int FIELD_MARK_ID = 1;
    private static final int FIELD_DELAY = 2;

    public int markId;
    public int delay;

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_MARK_ID, markId);
        writer.write(FIELD_DELAY, delay);
    }

    @Override
    public void readFrom(DataReader reader) {
        markId = reader.readInt(FIELD_MARK_ID);
        delay = reader.readInt(FIELD_DELAY);
    }
}
