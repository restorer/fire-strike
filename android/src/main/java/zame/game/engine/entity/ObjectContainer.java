package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;
import zame.game.engine.level.Level;
import zame.game.engine.level.LevelRenderer;
import zame.game.engine.state.State;

public class ObjectContainer extends DataListItem<ObjectContainer> implements DataItem {
    public static final int MAX_OBJECTS = 8;

    private static final int FIELD_DATA = 1;
    private static final int FIELD_COUNT = 2;

    public int[] data = new int[MAX_OBJECTS];
    public int count;

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_DATA, data);
        writer.write(FIELD_COUNT, count);
    }

    @Override
    public void readFrom(DataReader reader) {
        data = reader.readIntArray(FIELD_DATA, MAX_OBJECTS);
        count = reader.readInt(FIELD_COUNT);
    }

    public void clear() {
        count = 0;
    }

    public int get(int index) {
        return data[index];
    }

    public void set(int index, int value) {
        data[index] = value;
    }

    public void removeAt(int index) {
        for (int i = index, len = count - 1; i < len; i++) {
            data[i] = data[i + 1];
        }

        count--;
    }

    public boolean append(int value) {
        if (count >= MAX_OBJECTS) {
            return false;
        }

        data[count++] = value;
        return true;
    }

    static boolean dropAt(int value, State state, LevelRenderer levelRenderer, int cx, int cy) {
        if (cx < 0
                || cy < 0
                || cx >= state.levelWidth
                || cy >= state.levelHeight
                || (state.passableMap[cy][cx] & Level.PASSABLE_MASK_OBJECT_DROP) != 0) {

            return false;
        }

        ObjectContainer container = state.objectsMap.get(cy).get(cx);
        boolean wasEmpty = container.count == 0;

        if (!container.append(value)) {
            return false;
        }

        if (wasEmpty) {
            state.passableMap[cy][cx] |= Level.PASSABLE_IS_OBJECT;
            levelRenderer.modLightMap(cx, cy, LevelRenderer.LIGHT_OBJECT);
        }

        return true;
    }
}
