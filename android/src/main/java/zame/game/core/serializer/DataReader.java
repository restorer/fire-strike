package zame.game.core.serializer;

import android.util.SparseArray;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class DataReader {
    private static class SignaturedObjectInputStream {
        private final ObjectInputStream is;
        private final CRC32 crc32 = new CRC32();

        SignaturedObjectInputStream(ObjectInputStream is) {
            this.is = is;
            crc32.update(DataUtils.INITIAL_SIGNATURE_VALUE);
        }

        boolean readAndCheckSignature() {
            try {
                long sig = is.readLong();
                return (sig == crc32.getValue());
            } catch (Exception ex) {
                return false;
            }
        }

        boolean readBoolean() throws IOException {
            boolean value = is.readBoolean();
            crc32.update(value ? 1 : 0);
            return value;
        }

        byte readByte() throws IOException {
            byte value = is.readByte();
            crc32.update(value);
            return value;
        }

        char readChar() throws IOException {
            char value = is.readChar();
            crc32.update(value);
            return value;
        }

        double readDouble() throws IOException {
            double value = is.readDouble();
            long raw = Double.doubleToRawLongBits(value);
            crc32.update((int)raw);

            //noinspection MagicNumber
            crc32.update((int)(raw >> 32));

            return value;
        }

        float readFloat() throws IOException {
            float value = is.readFloat();
            crc32.update(Float.floatToRawIntBits(value));
            return value;
        }

        int readInt() throws IOException {
            int value = is.readInt();
            crc32.update(value);
            return value;
        }

        long readLong() throws IOException {
            long value = is.readLong();
            crc32.update((int)value);

            //noinspection MagicNumber
            crc32.update((int)(value >> 32));

            return value;
        }

        short readShort() throws IOException {
            short value = is.readShort();
            crc32.update(value);
            return value;
        }

        @SuppressWarnings("CharsetObjectCanBeUsed")
        String readUTF() throws IOException {
            String value = is.readUTF();

            try {
                crc32.update(value.getBytes("UTF-8"));
            } catch (Exception ex) {
                // ignored
            }

            return value;
        }
    }

    private static class ArrayList1d<T> extends ArrayList<T> {
        private static final long serialVersionUID = 0L;

        ArrayList1d(int capacity) {
            super(capacity);
        }
    }

    private static class ArrayList2d<T> extends ArrayList<T> {
        private static final long serialVersionUID = 0L;

        ArrayList2d(int capacity) {
            super(capacity);
        }
    }

    private static class ArrayList2dv<T> extends ArrayList<T> {
        private static final long serialVersionUID = 0L;

        ArrayList2dv(int capacity) {
            super(capacity);
        }
    }

    public static int readFrom(ObjectInputStream ois, DataItem item, String signature) throws
            IOException,
            UnknownSignatureException {

        return readFrom(ois, item, signature, 1);
    }

    public static int readFrom(ObjectInputStream ois, DataItem item, String signature, int maxSupportedVersion) throws
            IOException,
            UnknownSignatureException {

        SignaturedObjectInputStream is = new SignaturedObjectInputStream(ois);

        String signatureAndVersion = is.readUTF();
        int version;

        if (signatureAndVersion == null) {
            throw new UnknownSignatureException(UnknownSignatureException.INVALID_SIGNATURE);
        }

        if (!signatureAndVersion.startsWith(signature + ".")) {
            throw new UnknownSignatureException(UnknownSignatureException.UNKNOWN_SIGNATURE);
        }

        try {
            version = Integer.parseInt(signatureAndVersion.substring(signature.length() + 1));
        } catch (NumberFormatException ex) {
            throw new UnknownSignatureException(UnknownSignatureException.INVALID_VERSION, ex);
        }

        if (version > maxSupportedVersion) {
            throw new UnknownSignatureException(UnknownSignatureException.UNSUPPORTED_VERSION);
        }

        DataReader reader = readInnerObject(is);
        item.readFrom(reader);

        //noinspection UnusedAssignment
        reader = null;

        System.gc();

        if (!is.readAndCheckSignature()) {
            throw new UnknownSignatureException(UnknownSignatureException.INVALID_CHECKSUM);
        }

        return version;
    }

    private static DataReader readInnerObject(SignaturedObjectInputStream is) throws IOException {
        DataReader reader = new DataReader();

        for (; ; ) {
            short id = is.readShort();

            if (id == DataUtils.MARKER_END) {
                break;
            }

            int arrayType = ((int)id & DataUtils.MASK_ARRAY) >> DataUtils.SHIFT_ARRAY;
            int dataType = ((int)id & DataUtils.MASK_TYPE) >> DataUtils.SHIFT_TYPE;
            int fieldId = (int)id & DataUtils.MASK_FIELD_ID;

            if (arrayType == DataUtils.ARRAY_1D) {
                int length = is.readInt();
                ArrayList1d<Object> list = new ArrayList1d<>(length);
                reader.values.put(fieldId, list);

                for (int i = 0; i < length; i++) {
                    list.add(readInnerValue(dataType, is));
                }
            } else if (arrayType == DataUtils.ARRAY_2D) {
                int height = is.readInt();
                int width = is.readInt();
                ArrayList2d<Object> list = new ArrayList2d<>(height);
                reader.values.put(fieldId, list);

                for (int i = 0; i < height; i++) {
                    ArrayList<Object> line = new ArrayList<>(width);
                    list.add(line);

                    for (int j = 0; j < width; j++) {
                        line.add(readInnerValue(dataType, is));
                    }
                }
            } else if (arrayType == DataUtils.ARRAY_2DV) {
                int length = is.readInt();
                ArrayList2dv<Object> list = new ArrayList2dv<>(length);
                reader.values.put(fieldId, list);

                for (int i = 0; i < length; i++) {
                    int lineLength = is.readInt();
                    ArrayList<Object> line = new ArrayList<>(lineLength);
                    list.add(line);

                    for (int j = 0; j < lineLength; j++) {
                        line.add(readInnerValue(dataType, is));
                    }
                }
            } else {
                reader.values.put(fieldId, readInnerValue(dataType, is));
            }
        }

        return reader;
    }

    private static Object readInnerValue(int dataType, SignaturedObjectInputStream is) throws IOException {
        switch (dataType) {
            case DataUtils.TYPE_OBJECT:
                return readInnerObject(is);

            case DataUtils.TYPE_BYTE:
                return is.readByte();

            case DataUtils.TYPE_SHORT:
                return is.readShort();

            case DataUtils.TYPE_INT:
                return is.readInt();

            case DataUtils.TYPE_LONG:
                return is.readLong();

            case DataUtils.TYPE_FLOAT:
                return is.readFloat();

            case DataUtils.TYPE_DOUBLE:
                return is.readDouble();

            case DataUtils.TYPE_BOOLEAN:
                return is.readBoolean();

            case DataUtils.TYPE_CHAR:
                return is.readChar();

            case DataUtils.TYPE_STRING:
                return is.readUTF();

            case DataUtils.TYPE_NULL:
                return null;
        }

        return null;
    }

    private static final DataReader emptyDataReader = new DataReader();
    private final SparseArray<Object> values = new SparseArray<>();

    protected DataReader() {
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public boolean has(int fieldId) {
        return (values.get(fieldId) != null);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void readItem(int fieldId, DataItem item) {
        Object val = values.get(fieldId);
        item.readFrom(val instanceof DataReader ? (DataReader)val : emptyDataReader);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void readDataVector(int fieldId, DataVector<?> dataVector) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = Math.min(dataVector.data.length, list.size());
            dataVector.count = length;

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);
                ((DataItem)dataVector.data[i]).readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
            }
        } else {
            dataVector.count = 0;
        }
    }

    public <T extends DataListItem<?> & DataItem> void readDataList(int fieldId, DataList<T> dataList) {
        Object val = values.get(fieldId);
        dataList.clear();

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();

            for (int i = 0; i < length; i++) {
                DataItem item = dataList.take();

                if (item == null) {
                    break;
                }

                Object v = list.get(i);
                item.readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
            }
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public <T extends DataItem> void readList(int fieldId, List<DataItem> resultList, Class<T> theClass) {
        Object val = values.get(fieldId);
        resultList.clear();

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                try {
                    DataItem inst = theClass.newInstance();
                    inst.readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
                    resultList.add(inst);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public <T extends DataItem> void readList2d(int fieldId, List<List<T>> resultList, Class<T> theClass) {
        Object val = values.get(fieldId);
        resultList.clear();

        if (val instanceof ArrayList2dv) {
            ArrayList2dv<?> list = (ArrayList2dv<?>)val;
            int length = list.size();

            for (int i = 0; i < length; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);
                int lineLength = line.size();

                ArrayList<T> resultLine = new ArrayList<>(lineLength);
                resultList.add(resultLine);

                for (int j = 0; j < lineLength; j++) {
                    Object v = line.get(j);

                    try {
                        T inst = theClass.newInstance();
                        inst.readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
                        resultLine.add(inst);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void readObjectArray(int fieldId, DataItem[] resList) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = Math.min(resList.length, list.size());

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);
                resList[i].readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public byte readByte(int fieldId, byte def) {
        Object val = values.get(fieldId);
        return (val instanceof Number ? ((Number)val).byteValue() : def);
    }

    @SuppressWarnings({ "WeakerAccess", "unused", "RedundantSuppression" })
    public byte readByte(int fieldId) {
        return readByte(fieldId, (byte)0);
    }

    @SuppressWarnings("WeakerAccess")
    public short readShort(int fieldId, short def) {
        Object val = values.get(fieldId);
        return (val instanceof Number ? ((Number)val).shortValue() : def);
    }

    @SuppressWarnings({ "WeakerAccess", "unused", "RedundantSuppression" })
    public short readShort(int fieldId) {
        return readShort(fieldId, (short)0);
    }

    @SuppressWarnings("WeakerAccess")
    public int readInt(int fieldId, int def) {
        Object val = values.get(fieldId);
        return (val instanceof Number ? ((Number)val).intValue() : def);
    }

    @SuppressWarnings("WeakerAccess")
    public int readInt(int fieldId) {
        return readInt(fieldId, 0);
    }

    @SuppressWarnings("WeakerAccess")
    public long readLong(int fieldId, @SuppressWarnings("SameParameterValue") long def) {
        Object val = values.get(fieldId);
        return (val instanceof Number ? ((Number)val).longValue() : def);
    }

    @SuppressWarnings("WeakerAccess")
    public long readLong(int fieldId) {
        return readLong(fieldId, 0);
    }

    @SuppressWarnings("WeakerAccess")
    public float readFloat(int fieldId, @SuppressWarnings("SameParameterValue") float def) {
        Object val = values.get(fieldId);
        return (val instanceof Number ? ((Number)val).floatValue() : def);
    }

    public float readFloat(int fieldId) {
        return readFloat(fieldId, 0.0f);
    }

    public boolean readBoolean(int fieldId, boolean def) {
        Object val = values.get(fieldId);
        return (val instanceof Boolean ? (Boolean)val : def);
    }

    public boolean readBoolean(int fieldId) {
        return readBoolean(fieldId, false);
    }

    @SuppressWarnings("WeakerAccess")
    public double readDouble(int fieldId, @SuppressWarnings("SameParameterValue") double def) {
        Object val = values.get(fieldId);
        return (val instanceof Number ? ((Number)val).doubleValue() : def);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public double readDouble(int fieldId) {
        return readDouble(fieldId, 0.0);
    }

    @SuppressWarnings("WeakerAccess")
    public char readChar(int fieldId, @SuppressWarnings("SameParameterValue") char def) {
        Object val = values.get(fieldId);
        return (val instanceof Character ? (Character)val : def);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public char readChar(int fieldId) {
        return readChar(fieldId, ' ');
    }

    public String readString(int fieldId, String def) {
        Object val = values.get(fieldId);
        return (val instanceof String ? (String)val : def);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public String readString(int fieldId) {
        return readString(fieldId, "");
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public byte[] readByteArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            byte[] result = new byte[length];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).byteValue();
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public byte[] readByteArray(int fieldId, int minLength) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            byte[] result = new byte[Math.max(length, minLength)];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).byteValue();
                }
            }

            return result;
        } else {
            return new byte[minLength];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public short[] readShortArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            short[] result = new short[length];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).shortValue();
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public short[] readShortArray(int fieldId, int minLength) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            short[] result = new short[Math.max(length, minLength)];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).shortValue();
                }
            }

            return result;
        } else {
            return new short[minLength];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public int[] readIntArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            int[] result = new int[length];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).intValue();
                }
            }

            return result;
        } else {
            return null;
        }
    }

    public int[] readIntArray(int fieldId, int minLength) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            int[] result = new int[Math.max(length, minLength)];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).intValue();
                }
            }

            return result;
        } else {
            return new int[minLength];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public long[] readLongArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            long[] result = new long[length];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).longValue();
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public long[] readLongArray(int fieldId, int minLength) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            long[] result = new long[Math.max(length, minLength)];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).longValue();
                }
            }

            return result;
        } else {
            return new long[minLength];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public float[] readFloatArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            float[] result = new float[length];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).floatValue();
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public float[] readFloatArray(int fieldId, int minLength) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            float[] result = new float[Math.max(length, minLength)];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).floatValue();
                }
            }

            return result;
        } else {
            return new float[minLength];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public boolean[] readBooleanArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            boolean[] result = new boolean[length];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Boolean) {
                    result[i] = (Boolean)v;
                }
            }

            return result;
        } else {
            return null;
        }
    }

    public boolean[] readBooleanArray(int fieldId, int minLength) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            boolean[] result = new boolean[Math.max(length, minLength)];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Boolean) {
                    result[i] = (Boolean)v;
                }
            }

            return result;
        } else {
            return new boolean[minLength];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public double[] readDoubleArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            double[] result = new double[length];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).doubleValue();
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public double[] readDoubleArray(int fieldId, int minLength) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            double[] result = new double[Math.max(length, minLength)];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Number) {
                    result[i] = ((Number)v).doubleValue();
                }
            }

            return result;
        } else {
            return new double[minLength];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public char[] readCharArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            char[] result = new char[length];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Character) {
                    result[i] = (Character)v;
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public char[] readCharArray(int fieldId, int minLength) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)val;
            int length = list.size();
            char[] result = new char[Math.max(length, minLength)];

            for (int i = 0; i < length; i++) {
                Object v = list.get(i);

                if (v instanceof Character) {
                    result[i] = (Character)v;
                }
            }

            return result;
        } else {
            return new char[minLength];
        }
    }

    public String[] readStringArray(int fieldId) {
        if (values.get(fieldId) instanceof ArrayList1d) {
            ArrayList1d<?> list = (ArrayList1d<?>)values.get(fieldId);
            int length = list.size();
            String[] result = new String[length];

            for (int i = 0; i < length; i++) {
                if (list.get(i) instanceof String) {
                    result[i] = (String)list.get(i);
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public byte[][] readByte2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            byte[][] result = new byte[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).byteValue();
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public byte[][] readByte2dArray(int fieldId, int minHeight, int minWidth) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            byte[][] result = new byte[Math.max(height, minHeight)][Math.max(width, minWidth)];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).byteValue();
                    }
                }
            }

            return result;
        } else {
            return new byte[minHeight][minWidth];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public short[][] readShort2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            short[][] result = new short[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).shortValue();
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public short[][] readShort2dArray(int fieldId, int minHeight, int minWidth) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            short[][] result = new short[Math.max(height, minHeight)][Math.max(width, minWidth)];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).shortValue();
                    }
                }
            }

            return result;
        } else {
            return new short[minHeight][minWidth];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public int[][] readInt2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            int[][] result = new int[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).intValue();
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }

    public int[][] readInt2dArray(int fieldId, int minHeight, int minWidth) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            int[][] result = new int[Math.max(height, minHeight)][Math.max(width, minWidth)];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).intValue();
                    }
                }
            }

            return result;
        } else {
            return new int[minHeight][minWidth];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public long[][] readLong2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            long[][] result = new long[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).longValue();
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public long[][] readLong2dArray(int fieldId, int minHeight, int minWidth) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            long[][] result = new long[Math.max(height, minHeight)][Math.max(width, minWidth)];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).longValue();
                    }
                }
            }

            return result;
        } else {
            return new long[minHeight][minWidth];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public float[][] readFloat2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            float[][] result = new float[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).floatValue();
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public float[][] readFloat2dArray(int fieldId, int minHeight, int minWidth) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            float[][] result = new float[Math.max(height, minHeight)][Math.max(width, minWidth)];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).floatValue();
                    }
                }
            }

            return result;
        } else {
            return new float[minHeight][minWidth];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public boolean[][] readBoolean2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            boolean[][] result = new boolean[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Boolean) {
                        result[i][j] = (Boolean)v;
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public boolean[][] readBoolean2dArray(int fieldId, int minHeight, int minWidth) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            boolean[][] result = new boolean[Math.max(height, minHeight)][Math.max(width, minWidth)];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Boolean) {
                        result[i][j] = (Boolean)v;
                    }
                }
            }

            return result;
        } else {
            return new boolean[minHeight][minWidth];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public double[][] readDouble2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            double[][] result = new double[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).doubleValue();
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public double[][] readDouble2dArray(int fieldId, int minHeight, int minWidth) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            double[][] result = new double[Math.max(height, minHeight)][Math.max(width, minWidth)];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Number) {
                        result[i][j] = ((Number)v).doubleValue();
                    }
                }
            }

            return result;
        } else {
            return new double[minHeight][minWidth];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public char[][] readChar2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            char[][] result = new char[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Character) {
                        result[i][j] = (Character)v;
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public char[][] readChar2dArray(int fieldId, int minHeight, int minWidth) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            char[][] result = new char[Math.max(height, minHeight)][Math.max(width, minWidth)];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof Character) {
                        result[i][j] = (Character)v;
                    }
                }
            }

            return result;
        } else {
            return new char[minHeight][minWidth];
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public String[][] readString2dArray(int fieldId) {
        Object val = values.get(fieldId);

        if (val instanceof ArrayList2d) {
            ArrayList2d<?> list = (ArrayList2d<?>)val;
            int height = list.size();
            int width = ((ArrayList<?>)list.get(0)).size();
            String[][] result = new String[height][width];

            for (int i = 0; i < height; i++) {
                ArrayList<?> line = (ArrayList<?>)list.get(i);

                for (int j = 0; j < width; j++) {
                    Object v = line.get(j);

                    if (v instanceof String) {
                        result[i][j] = (String)v;
                    }
                }
            }

            return result;
        } else {
            return null;
        }
    }
}
