package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class OnChangeWeaponAction extends DataListItem implements DataItem {
    private static final int FIELD_MARK_ID = 1;

    int markId;

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_MARK_ID, markId);
    }

    @Override
    public void readFrom(DataReader reader) {
        markId = reader.readInt(FIELD_MARK_ID);
    }
}
