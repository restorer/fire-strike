package zame.game.engine.state;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;
import zame.game.core.util.Common;

public abstract class BaseState implements DataItem {
    private static final String SIGNATURE = "FireStrike5";

    public static final int LOAD_RESULT_SUCCESS = 0;
    private static final int LOAD_RESULT_NOT_FOUND = 1;
    static final int LOAD_RESULT_ERROR = 2;

    @Override
    public abstract void writeTo(DataWriter writer) throws IOException;

    @Override
    public abstract void readFrom(DataReader reader);

    protected int getVersion() {
        return 1;
    }

    protected void versionUpgrade(int version) {
    }

    public boolean save(String path) {
        String tmpPath = path + ".tmp";

        try {
            FileOutputStream fo = new FileOutputStream(tmpPath, false);
            ObjectOutputStream os = new ObjectOutputStream(fo);

            DataWriter.writeTo(os, this, SIGNATURE, getVersion());

            os.flush();
            fo.close();

            Common.safeRename(tmpPath, path);
            return true;
        } catch (Exception ex) {
            Common.log(ex.toString());
            return false;
        }
    }

    public int load(String path) {
        try {
            FileInputStream fi = new FileInputStream(path);
            ObjectInputStream is = new ObjectInputStream(fi);

            versionUpgrade(DataReader.readFrom(is, this, SIGNATURE, getVersion()));
            return LOAD_RESULT_SUCCESS;
        } catch (FileNotFoundException ex) {
            return LOAD_RESULT_NOT_FOUND;
        } catch (Exception ex) {
            Common.log(ex.toString());
            return LOAD_RESULT_ERROR;
        }
    }
}
