package zame.game.engine.entity;

import java.io.IOException;

import zame.game.core.serializer.DataItem;
import zame.game.core.serializer.DataListItem;
import zame.game.core.serializer.DataReader;
import zame.game.core.serializer.DataWriter;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.level.Level;
import zame.game.engine.state.State;
import zame.game.feature.sound.SoundManager;

public class Door extends DataListItem<Door> implements EngineObject, DataItem {
    private static final int FIELD_X = 1;
    private static final int FIELD_Y = 2;
    private static final int FIELD_TEXTURE = 3;
    private static final int FIELD_VERT = 4;
    private static final int FIELD_OPEN_POS = 5;
    private static final int FIELD_DIR = 6;
    private static final int FIELD_STICKED = 7;
    private static final int FIELD_REQUIRED_KEY = 8;
    private static final int FIELD_UID = 9;

    private static final float OPEN_POS_MAX = 0.9f;
    public static final float OPEN_POS_PASSABLE = 0.7f;
    private static final int OPEN_TIME = 1000 * 5;

    protected Engine engine;
    protected State state;

    public int x;
    public int y;
    public int texture;
    public int dir;
    public float openPos;
    public boolean vert;
    public int uid; // required for save/load for autoWalls

    public boolean sticked;
    public int requiredKey;

    private long lastTime;
    public Mark mark;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.state = engine.state;
    }

    public void init() {
        openPos = 0.0f;
        dir = 0;
        lastTime = 0;
        sticked = false;
        requiredKey = 0;
        mark = null;
    }

    @Override
    public void writeTo(DataWriter writer) throws IOException {
        writer.write(FIELD_X, x);
        writer.write(FIELD_Y, y);
        writer.write(FIELD_TEXTURE, TextureLoader.packTexId(texture));
        writer.write(FIELD_VERT, vert);
        writer.write(FIELD_OPEN_POS, openPos);
        writer.write(FIELD_DIR, dir);
        writer.write(FIELD_STICKED, sticked);
        writer.write(FIELD_REQUIRED_KEY, requiredKey);
        writer.write(FIELD_UID, uid);
    }

    @Override
    public void readFrom(DataReader reader) {
        x = reader.readInt(FIELD_X);
        y = reader.readInt(FIELD_Y);
        texture = TextureLoader.unpackTexId(reader.readInt(FIELD_TEXTURE));
        vert = reader.readBoolean(FIELD_VERT);
        openPos = reader.readFloat(FIELD_OPEN_POS);
        dir = reader.readInt(FIELD_DIR);
        sticked = reader.readBoolean(FIELD_STICKED);
        requiredKey = reader.readInt(FIELD_REQUIRED_KEY);
        uid = reader.readInt(FIELD_UID);

        // previously was lastTime = elapsedTime, but that was incorrect,
        // because at load process elapsedTime can be > than actual elapsedTime
        lastTime = 0;
    }

    public void stick(boolean opened, boolean instant) {
        sticked = true;
        dir = (opened ? 1 : -1);
        lastTime = (instant ? 0 : engine.elapsedTime);
        engine.level.requestBuildPathToWave();
    }

    public void unstick() {
        sticked = false;
        engine.level.requestBuildPathToWave();
    }

    @SuppressWarnings("MagicNumber")
    private float getVolume() {
        float dx = state.heroX - ((float)x + 0.5f);
        float dy = state.heroY - ((float)y + 0.5f);
        float dist = (float)Math.sqrt(dx * dx + dy * dy);

        return (1.0f / Math.max(1.0f, dist * 0.5f));
    }

    public boolean open() {
        if (dir != 0) {
            return false;
        }

        lastTime = engine.elapsedTime;
        dir = 1;

        engine.soundManager.playSound(SoundManager.SOUND_DOOR_OPEN, getVolume());
        return true;
    }

    public void tryClose() {
        if (sticked || (dir != 0) || (openPos < OPEN_POS_MAX)) {
            return;
        } else if ((state.passableMap[y][x] & Level.PASSABLE_MASK_DOOR) != 0) {
            lastTime = engine.elapsedTime;
            return;
        } else if ((engine.elapsedTime - lastTime) < OPEN_TIME) {
            return;
        }

        dir = -1;
        lastTime = engine.elapsedTime;
        engine.soundManager.playSound(SoundManager.SOUND_DOOR_CLOSE, getVolume());
    }

    public boolean isOpenedOrCanOpen() {
        return !sticked
                || (state.passableMap[y][x] & Level.PASSABLE_IS_DOOR) == 0
                || (requiredKey != 0 && (state.heroKeysMask & requiredKey) != 0);
    }

    @SuppressWarnings("MagicNumber")
    public void updateBeforeRender() {
        if (dir > 0) {
            openPos = (float)(engine.elapsedTime - lastTime) / 300.0f;
            state.wallsMap[y][x] = 0; // clear door mark for PortalTracer

            if (openPos >= OPEN_POS_PASSABLE) {
                if ((state.passableMap[y][x] & Level.PASSABLE_IS_DOOR) != 0) {
                    state.passableMap[y][x] &= ~Level.PASSABLE_IS_DOOR;
                    engine.level.requestBuildPathToWave();
                }

                if (openPos >= OPEN_POS_MAX) {
                    openPos = OPEN_POS_MAX;
                    dir = 0;
                }
            }
        } else if (dir < 0) {
            openPos = Door.OPEN_POS_MAX - (float)(engine.elapsedTime - lastTime) / 200.0f;

            if (openPos < OPEN_POS_PASSABLE) {
                if ((dir == -1) && ((state.passableMap[y][x] & Level.PASSABLE_MASK_DOOR) != 0)) {
                    if (!sticked) {
                        dir = 1;
                    }

                    if (openPos <= 0.0f) {
                        openPos = 0.0f;
                    }

                    lastTime = engine.elapsedTime;
                    return;
                }

                dir = -2;

                if ((state.passableMap[y][x] & Level.PASSABLE_IS_DOOR) == 0) {
                    state.passableMap[y][x] |= Level.PASSABLE_IS_DOOR;
                    engine.level.requestBuildPathToWave();
                }

                if (openPos <= 0.0f) {
                    state.wallsMap[y][x] = (vert ? -1 : -2); // mark door for PortalTracer
                    openPos = 0.0f;
                    dir = 0;
                }
            }
        }
    }
}
