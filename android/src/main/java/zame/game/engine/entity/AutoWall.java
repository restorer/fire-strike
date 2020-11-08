package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;

public class AutoWall extends DataListItem<AutoWall> implements DataItem {
    private static final int FIELD_FROM_X = 1;
    private static final int FIELD_FROM_Y = 2;
    private static final int FIELD_TO_X = 3;
    private static final int FIELD_TO_Y = 4;
    private static final int FIELD_VERT = 5;
    private static final int FIELD_TYPE = 6;
    private static final int FIELD_DOOR_UID = 7;

    public float fromX;
    public float fromY;
    public float toX;
    public float toY;
    public boolean vert;
    public int type;
    public int doorUid; // required for save/load
    public Door door;

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_FROM_X, fromX);
        writer.write(FIELD_FROM_Y, fromY);
        writer.write(FIELD_TO_X, toX);
        writer.write(FIELD_TO_Y, toY);
        writer.write(FIELD_VERT, vert);
        writer.write(FIELD_TYPE, type);
        writer.write(FIELD_DOOR_UID, doorUid);
    }

    @Override
    public void readFrom(DataReader reader) {
        fromX = reader.readFloat(FIELD_FROM_X);
        fromY = reader.readFloat(FIELD_FROM_Y);
        toX = reader.readFloat(FIELD_TO_X);
        toY = reader.readFloat(FIELD_TO_Y);
        vert = reader.readBoolean(FIELD_VERT);
        type = reader.readInt(FIELD_TYPE);
        doorUid = reader.readInt(FIELD_DOOR_UID);
    }
}
