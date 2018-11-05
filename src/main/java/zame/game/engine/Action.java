package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class Action implements DataItem {
	private static final int FIELD_TYPE = 1;
	private static final int FIELD_MARK_ID = 2;
	private static final int FIELD_PARAM = 3;

	int type;
	int markId;
	int param;

	@Override
	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_TYPE, type);
		writer.write(FIELD_MARK_ID, markId);
		writer.write(FIELD_PARAM, param);
	}

	@Override
	public void readFrom(DataReader reader) {
		type = reader.readInt(FIELD_TYPE);
		markId = reader.readInt(FIELD_MARK_ID);
		param = reader.readInt(FIELD_PARAM);
	}
}
