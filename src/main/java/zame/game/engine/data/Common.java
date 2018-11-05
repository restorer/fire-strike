package zame.game.engine.data;

final class Common {
	static final String SIGNATURE = "FireStrike3";
	static final int INITIAL_SIGNATURE_VALUE = 594828924;
	static final short MARKER_END = -1;
	static final int MASK_ARRAY = 0xC000;
	static final int SHIFT_ARRAY = 14;
	static final int MASK_TYPE = 0x3C00;
	static final int SHIFT_TYPE = 10;
	static final int MASK_FIELD_ID = 0x03FF;
	static final int ARRAY_1D = 1;
	static final int ARRAY_2D = 2;
	static final int ARRAY_2DV = 3;
	static final int TYPE_OBJECT = 0;
	static final int TYPE_BYTE = 1;
	static final int TYPE_SHORT = 2;
	static final int TYPE_INT = 3;
	static final int TYPE_LONG = 4;
	static final int TYPE_FLOAT = 5;
	static final int TYPE_DOUBLE = 6;
	static final int TYPE_BOOLEAN = 7;
	static final int TYPE_CHAR = 8;
	static final int TYPE_STRING = 9;
	static final int TYPE_NULL = 10;

	private Common() {}
}
