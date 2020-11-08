package zame.game.core.serializer;

import java.io.IOException;

public interface DataItem {
    void writeTo(DataWriter writer) throws IOException;

    void readFrom(DataReader reader);
}
