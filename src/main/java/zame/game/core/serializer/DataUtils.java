package zame.game.core.serializer;

final class DataUtils {
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

    private DataUtils() {}

    @SuppressWarnings("unchecked")
    static <T> T instantiate(Class<T> theClass) throws IllegalAccessException, InstantiationException {
        if (Byte.class.equals(theClass)) {
            return (T)Byte.valueOf((byte)0);
        }

        if (Short.class.equals(theClass)) {
            return (T)Short.valueOf((short)0);
        }

        if (Integer.class.equals(theClass)) {
            return (T)Integer.valueOf(0);
        }

        if (Long.class.equals(theClass)) {
            return (T)Long.valueOf(0L);
        }

        if (Float.class.equals(theClass)) {
            return (T)Float.valueOf(0.0f);
        }

        if (Double.class.equals(theClass)) {
            return (T)Double.valueOf(0.0);
        }

        if (Boolean.class.equals(theClass)) {
            return (T)Boolean.valueOf(false);
        }

        if (Character.class.equals(theClass)) {
            return (T)Character.valueOf(' ');
        }

        return theClass.newInstance();
    }
}
