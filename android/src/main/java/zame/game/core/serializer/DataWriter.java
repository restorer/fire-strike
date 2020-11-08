package zame.game.core.serializer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.zip.CRC32;

public class DataWriter {
    private static class SignaturedObjectOutputStream {
        private final ObjectOutputStream os;
        private final CRC32 crc32 = new CRC32();

        SignaturedObjectOutputStream(ObjectOutputStream os) {
            this.os = os;
            crc32.update(DataUtils.INITIAL_SIGNATURE_VALUE);
        }

        void writeSignature() throws IOException {
            os.writeLong(crc32.getValue());
        }

        void writeBoolean(boolean value) throws IOException {
            crc32.update(value ? 1 : 0);
            os.writeBoolean(value);
        }

        void writeByte(int value) throws IOException {
            crc32.update(value);
            os.writeByte(value);
        }

        void writeChar(int value) throws IOException {
            crc32.update(value);
            os.writeChar(value);
        }

        void writeDouble(double value) throws IOException {
            long raw = Double.doubleToRawLongBits(value);

            crc32.update((int)raw);

            //noinspection MagicNumber
            crc32.update((int)(raw >> 32));

            os.writeDouble(value);
        }

        void writeFloat(float value) throws IOException {
            crc32.update(Float.floatToRawIntBits(value));
            os.writeFloat(value);
        }

        void writeInt(int value) throws IOException {
            crc32.update(value);
            os.writeInt(value);
        }

        void writeLong(long value) throws IOException {
            crc32.update((int)value);

            //noinspection MagicNumber
            crc32.update((int)(value >> 32));

            os.writeLong(value);
        }

        void writeShort(int value) throws IOException {
            crc32.update(value);
            os.writeShort(value);
        }

        @SuppressWarnings("CharsetObjectCanBeUsed")
        void writeUTF(String value) throws IOException {
            try {
                crc32.update(value.getBytes("UTF-8"));
            } catch (Exception ex) {
                // ignored
            }

            os.writeUTF(value);
        }
    }

    protected SignaturedObjectOutputStream os;

    public static void writeTo(ObjectOutputStream oos, DataItem item, String signature) throws IOException {
        writeTo(oos, item, signature, 1);
    }

    public static void writeTo(ObjectOutputStream oos, DataItem item, String signature, int version) throws
            IOException {

        SignaturedObjectOutputStream os = new SignaturedObjectOutputStream(oos);
        DataWriter writer = new DataWriter(os);

        os.writeUTF(signature + "." + version);
        item.writeTo(writer);
        os.writeShort(DataUtils.MARKER_END);
        os.writeSignature();
    }

    protected DataWriter(SignaturedObjectOutputStream os) {
        this.os = os;
    }

    public void write(int fieldId, DataItem value) throws IOException {
        os.writeShort((DataUtils.TYPE_OBJECT << DataUtils.SHIFT_TYPE) | fieldId);

        value.writeTo(this);
        os.writeShort(DataUtils.MARKER_END);
    }

    public void write(int fieldId, byte value) throws IOException {
        os.writeShort((DataUtils.TYPE_BYTE << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeByte(value);
    }

    public void write(int fieldId, short value) throws IOException {
        os.writeShort((DataUtils.TYPE_SHORT << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeShort(value);
    }

    public void write(int fieldId, int value) throws IOException {
        os.writeShort((DataUtils.TYPE_INT << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeInt(value);
    }

    public void write(int fieldId, long value) throws IOException {
        os.writeShort((DataUtils.TYPE_LONG << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeLong(value);
    }

    public void write(int fieldId, float value) throws IOException {
        os.writeShort((DataUtils.TYPE_FLOAT << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeFloat(value);
    }

    public void write(int fieldId, double value) throws IOException {
        os.writeShort((DataUtils.TYPE_DOUBLE << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeDouble(value);
    }

    public void write(int fieldId, boolean value) throws IOException {
        os.writeShort((DataUtils.TYPE_BOOLEAN << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeBoolean(value);
    }

    public void write(int fieldId, char value) throws IOException {
        os.writeShort((DataUtils.TYPE_CHAR << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeChar(value);
    }

    public void write(int fieldId, String value) throws IOException {
        os.writeShort((DataUtils.TYPE_STRING << DataUtils.SHIFT_TYPE) | fieldId);
        os.writeUTF(value);
    }

    public void write(int fieldId, DataItem[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_OBJECT << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                list[i].writeTo(this);
                os.writeShort(DataUtils.MARKER_END);
            }
        }
    }

    public void write(int fieldId, DataVector<?> list) throws IOException {
        os.writeShort((DataUtils.TYPE_OBJECT << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY) | fieldId);

        int length = list.count;
        os.writeInt(length);

        for (int i = 0; i < length; i++) {
            ((DataItem)list.data[i]).writeTo(this);
            os.writeShort(DataUtils.MARKER_END);
        }
    }

    public <T extends DataListItem<T> & DataItem> void write(int fieldId, DataList<T> list) throws IOException {
        os.writeShort((DataUtils.TYPE_OBJECT << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY) | fieldId);

        int length = list.count();
        os.writeInt(length);

        for (T item = list.first(); item != null; item = item.next) {
            item.writeTo(this);
            os.writeShort(DataUtils.MARKER_END);
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void writeList(int fieldId, List<?> list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            //noinspection PointlessBitwiseExpression
            os.writeShort((DataUtils.TYPE_OBJECT << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int length = list.size();
            os.writeInt(length);

            for (int i = 0; i < length; i++) {
                ((DataItem)list.get(i)).writeTo(this);
                os.writeShort(DataUtils.MARKER_END);
            }
        }
    }

    public void writeList2d(int fieldId, List<?> list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_OBJECT << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_2DV << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int length = list.size();
            os.writeInt(length);

            for (int i = 0; i < length; i++) {
                List<?> line = (List<?>)list.get(i);
                int lineLength = line.size();
                os.writeInt(lineLength);

                for (int j = 0; j < lineLength; j++) {
                    ((DataItem)line.get(j)).writeTo(this);
                    os.writeShort(DataUtils.MARKER_END);
                }
            }
        }
    }

    public void write(int fieldId, byte[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_BYTE << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY) | fieldId);
            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeByte(list[i]);
            }
        }
    }

    public void write(int fieldId, short[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_SHORT << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY) | fieldId);
            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeShort(list[i]);
            }
        }
    }

    public void write(int fieldId, int[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_INT << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY) | fieldId);
            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeInt(list[i]);
            }
        }
    }

    public void write(int fieldId, long[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_LONG << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY) | fieldId);
            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeLong(list[i]);
            }
        }
    }

    public void write(int fieldId, float[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_FLOAT << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY) | fieldId);
            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeFloat(list[i]);
            }
        }
    }

    public void write(int fieldId, double[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_DOUBLE << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeDouble(list[i]);
            }
        }
    }

    public void write(int fieldId, boolean[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_BOOLEAN << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeBoolean(list[i]);
            }
        }
    }

    public void write(int fieldId, char[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_CHAR << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY) | fieldId);
            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeChar(list[i]);
            }
        }
    }

    public void write(int fieldId, String[] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_STRING << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_1D << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int length = list.length;
            os.writeInt(length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < length; i++) {
                os.writeUTF(list[i]);
            }
        }
    }

    public void write(int fieldId, byte[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_BYTE << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY) | fieldId);
            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeByte(list[i][j]);
                }
            }
        }
    }

    public void write(int fieldId, short[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_SHORT << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY) | fieldId);
            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeShort(list[i][j]);
                }
            }
        }
    }

    public void write(int fieldId, int[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_INT << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY) | fieldId);
            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeInt(list[i][j]);
                }
            }
        }
    }

    public void write(int fieldId, long[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_LONG << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY) | fieldId);
            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeLong(list[i][j]);
                }
            }
        }
    }

    public void write(int fieldId, float[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_FLOAT << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY) | fieldId);
            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeFloat(list[i][j]);
                }
            }
        }
    }

    public void write(int fieldId, double[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_DOUBLE << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeDouble(list[i][j]);
                }
            }
        }
    }

    public void write(int fieldId, boolean[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_BOOLEAN << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeBoolean(list[i][j]);
                }
            }
        }
    }

    public void write(int fieldId, char[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_CHAR << DataUtils.SHIFT_TYPE) | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY) | fieldId);
            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeChar(list[i][j]);
                }
            }
        }
    }

    public void write(int fieldId, String[][] list) throws IOException {
        if (list == null) {
            os.writeShort((DataUtils.TYPE_NULL << DataUtils.SHIFT_TYPE) | fieldId);
        } else {
            os.writeShort((DataUtils.TYPE_STRING << DataUtils.SHIFT_TYPE)
                    | (DataUtils.ARRAY_2D << DataUtils.SHIFT_ARRAY)
                    | fieldId);

            int height = list.length;
            int width = list[0].length;
            os.writeInt(height);
            os.writeInt(width);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    os.writeUTF(list[i][j]);
                }
            }
        }
    }
}
