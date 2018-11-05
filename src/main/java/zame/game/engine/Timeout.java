package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class Timeout extends DataListItem implements DataItem {
	private static final int FIELD_MARK_ID = 1;
	private static final int FIELD_DELAY = 2;

	int markId;
	int delay;

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
