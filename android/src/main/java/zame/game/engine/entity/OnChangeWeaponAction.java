package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;

public class OnChangeWeaponAction extends DataListItem<OnChangeWeaponAction> implements DataItem {
    private static final int FIELD_MARK_ID = 1;

    public int markId;

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_MARK_ID, markId);
    }

    @Override
    public void readFrom(DataReader reader) {
        markId = reader.readInt(FIELD_MARK_ID);
    }
}
