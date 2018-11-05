package zame.game.engine;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.os.Build;
import javax.microedition.khronos.opengles.GL10;
import zame.game.App;
import zame.game.Common;
import zame.game.providers.CachedTexturesProvider;

public class TextureLoader implements EngineObject {
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_LOADING = 0;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_LABELS = TEXTURE_LOADING + 1;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_RENDER_TO = TEXTURE_LABELS + 1;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_RENDER_TO_FBO = TEXTURE_RENDER_TO + 1;
    public static final int TEXTURE_MAIN = TEXTURE_RENDER_TO_FBO + 1;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_KNIFE = TEXTURE_MAIN + 1;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_PISTOL = TEXTURE_KNIFE + 4;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_DBLPISTOL = TEXTURE_PISTOL + 4;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_SHTG = TEXTURE_DBLPISTOL + 4;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_AK47 = TEXTURE_SHTG + 5;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_TMP = TEXTURE_AK47 + 4;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_GRENADE = TEXTURE_TMP + 4;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_MONSTERS_1 = TEXTURE_GRENADE + 8;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_MONSTERS_2 = TEXTURE_MONSTERS_1 + 1;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_SKY = TEXTURE_MONSTERS_2 + 1;
    @SuppressWarnings("WeakerAccess") public static final int TEXTURE_LAST = TEXTURE_SKY + 1;

    static final int RENDER_TO_SIZE = 256;
    static final int RENDER_TO_FBO_SIZE = 512;

    public static final int ROW_COMMON = 0;
    public static final int ROW_TILES = 6;

    @SuppressWarnings("WeakerAccess") public static final int BASE_ICONS = ROW_COMMON * 15;
    @SuppressWarnings("WeakerAccess") public static final int BASE_OBJECTS = BASE_ICONS + 10;
    @SuppressWarnings("WeakerAccess") public static final int BASE_BULLETS = BASE_OBJECTS + 19;
    @SuppressWarnings("WeakerAccess") public static final int BASE_EXPLOSIONS = BASE_BULLETS + 4;
    @SuppressWarnings("WeakerAccess") public static final int BASE_ARROWS = BASE_EXPLOSIONS + 3;
    public static final int BASE_WEAPONS = BASE_ARROWS + 4;
    public static final int BASE_BACKS = (ROW_COMMON + 4) * 15;

    static final int BASE_WALLS = ROW_TILES * 15; // BASE_WALLS must be greater than 0
    static final int BASE_TRANSP_WALLS = BASE_WALLS + 44;
    static final int BASE_TRANSP_WINDOWS = BASE_TRANSP_WALLS + 9;
    static final int BASE_DOORS_F = BASE_TRANSP_WINDOWS + 8;
    static final int BASE_DOORS_S = BASE_DOORS_F + 8;
    static final int BASE_DECOR_ITEM = BASE_DOORS_S + 8;
    static final int BASE_DECOR_LAMP = BASE_DECOR_ITEM + 10;
    static final int BASE_FLOOR = BASE_DECOR_LAMP + 2;
    static final int BASE_CEIL = BASE_FLOOR + 10;

    private static final int PACKED_WALLS = 1 << 16;
    private static final int PACKED_TRANSP_WALLS = 2 << 16;
    private static final int PACKED_TRANSP_WINDOWS = 4 << 16;
    private static final int PACKED_DOORS_F = 5 << 16;
    private static final int PACKED_DOORS_S = 6 << 16;
    private static final int PACKED_OBJECTS = 7 << 16;
    private static final int PACKED_DECOR_ITEM = 8 << 16;
    private static final int PACKED_DECOR_LAMP = 9 << 16;
    private static final int PACKED_FLOOR = 10 << 16;
    private static final int PACKED_CEIL = 11 << 16;
    private static final int PACKED_BULLETS = 12 << 16;
    private static final int PACKED_ARROWS = 13 << 16;

    // private static final int PACKED_TEXMAP_MONSTERS = 1 << 16;

    static final int COUNT_MONSTER = 0x10; // block = [up, rt, dn, lt], monster = block[walk_a, walk_b, hit], die[3], shoot
    static final int MONSTERS_IN_TEXTURE = 3;

    public static final int ICON_JOY = BASE_ICONS;
    public static final int ICON_MENU = BASE_ICONS + 1;
    public static final int ICON_SHOOT = BASE_ICONS + 2;
    @SuppressWarnings("WeakerAccess") public static final int ICON_MAP = BASE_ICONS + 3;
    @SuppressWarnings("WeakerAccess") public static final int ICON_HEALTH = BASE_ICONS + 4;
    @SuppressWarnings("WeakerAccess") public static final int ICON_ARMOR = BASE_ICONS + 5;
    @SuppressWarnings("WeakerAccess") public static final int ICON_AMMO = BASE_ICONS + 6;
    @SuppressWarnings("WeakerAccess") public static final int ICON_BLUE_KEY = BASE_ICONS + 7;
    @SuppressWarnings("WeakerAccess") public static final int ICON_RED_KEY = BASE_ICONS + 8;
    @SuppressWarnings("WeakerAccess") public static final int ICON_GREEN_KEY = BASE_ICONS + 9;

    static final int OBJ_ARMOR_GREEN = BASE_OBJECTS - 1 + 1;
    static final int OBJ_ARMOR_RED = BASE_OBJECTS - 1 + 2;
    static final int OBJ_KEY_BLUE = BASE_OBJECTS - 1 + 3;
    static final int OBJ_KEY_RED = BASE_OBJECTS - 1 + 4;
    static final int OBJ_STIM = BASE_OBJECTS - 1 + 5;
    static final int OBJ_MEDI = BASE_OBJECTS - 1 + 6;
    static final int OBJ_CLIP = BASE_OBJECTS - 1 + 7;
    static final int OBJ_CBOX = BASE_OBJECTS - 1 + 8;
    static final int OBJ_SHELL = BASE_OBJECTS - 1 + 9;
    static final int OBJ_SBOX = BASE_OBJECTS - 1 + 10;
    static final int OBJ_BPACK = BASE_OBJECTS - 1 + 11;
    static final int OBJ_WINCHESTER = BASE_OBJECTS - 1 + 12;
    static final int OBJ_KEY_GREEN = BASE_OBJECTS - 1 + 13;
    static final int OBJ_AK47 = BASE_OBJECTS - 1 + 14;
    static final int OBJ_TMP = BASE_OBJECTS - 1 + 15;
    static final int OBJ_DBLPIST = BASE_OBJECTS - 1 + 16;
    static final int OBJ_GRENADE = BASE_OBJECTS - 1 + 17;
    static final int OBJ_GBOX = BASE_OBJECTS - 1 + 18;
    static final int OBJ_OPENMAP = BASE_OBJECTS - 1 + 19;

    public static final int ARROW_UP = BASE_ARROWS - 1 + 1;
    public static final int ARROW_RT = BASE_ARROWS - 1 + 2;
    public static final int ARROW_DN = BASE_ARROWS - 1 + 3;
    public static final int ARROW_LT = BASE_ARROWS - 1 + 4;

    static final int[] WALL_LIGHTS = { BASE_WALLS - 1 + 7,
            BASE_WALLS - 1 + 9,
            BASE_WALLS - 1 + 10,
            BASE_WALLS - 1 + 12,
            BASE_WALLS - 1 + 20,
            BASE_WALLS - 1 + 21,
            BASE_WALLS - 1 + 22,
            BASE_WALLS - 1 + 23,
            BASE_WALLS - 1 + 24,
            BASE_WALLS - 1 + 25,
            BASE_WALLS - 1 + 26,
            BASE_WALLS - 1 + 27,
            BASE_WALLS - 1 + 28,
            BASE_WALLS - 1 + 29,
            BASE_WALLS - 1 + 30,
            BASE_WALLS - 1 + 31,
            BASE_WALLS - 1 + 32,
            BASE_WALLS - 1 + 33,
            BASE_WALLS - 1 + 34,
            BASE_WALLS - 1 + 35, };

    static final int[] DITEM_LIGHTS = { BASE_DECOR_ITEM - 1 + 1,
            BASE_DECOR_ITEM - 1 + 2,
            BASE_DECOR_ITEM - 1 + 8,
            BASE_DECOR_ITEM - 1 + 10, };

    static final int[] CEIL_LIGHTS = { BASE_CEIL - 1 + 2, BASE_CEIL - 1 + 4, BASE_CEIL - 1 + 6, };

    static final int[] DLAMP_LIGHTS = { BASE_DECOR_LAMP - 1 + 1, };

    public static class TextureToLoad {
        @SuppressWarnings("WeakerAccess") public static final int TYPE_RESOURCE = 0;
        public static final int TYPE_MAIN = 1;
        public static final int TYPE_MONSTERS_1 = 2;
        public static final int TYPE_MONSTERS_2 = 3;

        public int tex;
        public String pixelsName; // used in CachedTexturesProvider
        public String alphaName; // used in CachedTexturesProvider
        public int type;

        TextureToLoad(int tex, String pixelsName, String alphaName) {
            this.tex = tex;
            this.pixelsName = pixelsName;
            this.alphaName = alphaName;
            this.type = TYPE_RESOURCE;
        }

        TextureToLoad(int tex, String pixelsName, String alphaName, int type) {
            this.tex = tex;
            this.pixelsName = pixelsName;
            this.alphaName = alphaName;
            this.type = type;
        }
    }

    // @formatter:off
	public static final TextureToLoad[] TEXTURES_TO_LOAD = {
		new TextureToLoad(TEXTURE_MAIN, null, null, TextureToLoad.TYPE_MAIN),
		new TextureToLoad(TEXTURE_MONSTERS_1, "texmap_mon_1_p", "texmap_mon_1_a", TextureToLoad.TYPE_MONSTERS_1),
		new TextureToLoad(TEXTURE_MONSTERS_2, "texmap_mon_2_p", "texmap_mon_2_a", TextureToLoad.TYPE_MONSTERS_2),

		new TextureToLoad(TEXTURE_KNIFE, "hit_knife_1_p", "hit_knife_1_a"),
		new TextureToLoad(TEXTURE_KNIFE + 1, "hit_knife_2_p", "hit_knife_2_a"),
		new TextureToLoad(TEXTURE_KNIFE + 2, "hit_knife_3_p", "hit_knife_3_a"),
		new TextureToLoad(TEXTURE_KNIFE + 3, "hit_knife_4_p", "hit_knife_4_a"),

		new TextureToLoad(TEXTURE_PISTOL, "hit_pist_1_p", "hit_pist_1_a"),
		new TextureToLoad(TEXTURE_PISTOL + 1, "hit_pist_2_p", "hit_pist_2_a"),
		new TextureToLoad(TEXTURE_PISTOL + 2, "hit_pist_3_p", "hit_pist_3_a"),
		new TextureToLoad(TEXTURE_PISTOL + 3, "hit_pist_4_p", "hit_pist_4_a"),

		new TextureToLoad(TEXTURE_DBLPISTOL, "hit_dblpist_1_p", "hit_dblpist_1_a"),
		new TextureToLoad(TEXTURE_DBLPISTOL + 1, "hit_dblpist_2_p", "hit_dblpist_2_a"),
		new TextureToLoad(TEXTURE_DBLPISTOL + 2, "hit_dblpist_3_p", "hit_dblpist_3_a"),
		new TextureToLoad(TEXTURE_DBLPISTOL + 3, "hit_dblpist_4_p", "hit_dblpist_4_a"),

		new TextureToLoad(TEXTURE_SHTG, "hit_shtg_1_p", "hit_shtg_1_a"),
		new TextureToLoad(TEXTURE_SHTG + 1, "hit_shtg_2_p", "hit_shtg_2_a"),
		new TextureToLoad(TEXTURE_SHTG + 2, "hit_shtg_3_p", "hit_shtg_3_a"),
		new TextureToLoad(TEXTURE_SHTG + 3, "hit_shtg_4_p", "hit_shtg_4_a"),
		new TextureToLoad(TEXTURE_SHTG + 4, "hit_shtg_5_p", "hit_shtg_5_a"),

		new TextureToLoad(TEXTURE_AK47, "hit_ak47_1_p", "hit_ak47_1_a"),
		new TextureToLoad(TEXTURE_AK47 + 1, "hit_ak47_2_p", "hit_ak47_2_a"),
		new TextureToLoad(TEXTURE_AK47 + 2, "hit_ak47_3_p", "hit_ak47_3_a"),
		new TextureToLoad(TEXTURE_AK47 + 3, "hit_ak47_4_p", "hit_ak47_4_a"),

		new TextureToLoad(TEXTURE_TMP, "hit_tmp_1_p", "hit_tmp_1_a"),
		new TextureToLoad(TEXTURE_TMP + 1, "hit_tmp_2_p", "hit_tmp_2_a"),
		new TextureToLoad(TEXTURE_TMP + 2, "hit_tmp_3_p", "hit_tmp_3_a"),
		new TextureToLoad(TEXTURE_TMP + 3, "hit_tmp_4_p", "hit_tmp_4_a"),

		new TextureToLoad(TEXTURE_GRENADE, "hit_rocket_1_p", "hit_rocket_1_a"),
		new TextureToLoad(TEXTURE_GRENADE + 1, "hit_rocket_2_p", "hit_rocket_2_a"),
		new TextureToLoad(TEXTURE_GRENADE + 2, "hit_rocket_3_p", "hit_rocket_3_a"),
		new TextureToLoad(TEXTURE_GRENADE + 3, "hit_rocket_4_p", "hit_rocket_4_a"),
		new TextureToLoad(TEXTURE_GRENADE + 4, "hit_rocket_5_p", "hit_rocket_5_a"),
		new TextureToLoad(TEXTURE_GRENADE + 5, "hit_rocket_6_p", "hit_rocket_6_a"),
		new TextureToLoad(TEXTURE_GRENADE + 6, "hit_rocket_7_p", "hit_rocket_7_a"),
		new TextureToLoad(TEXTURE_GRENADE + 7, "hit_rocket_8_p", "hit_rocket_8_a"),

		new TextureToLoad(TEXTURE_SKY, "sky_1", null),
	};
    // @formatter:on

    private Engine engine;
    private State state;
    private AssetManager assetManager;
    private boolean texturesInitialized;
    private LevelConfig levelConf;

    public int[] textures = new int[TEXTURE_LAST];

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
        this.state = engine.state;
        this.assetManager = App.self.getAssets();
    }

    private void loadAndBindTexture(GL10 gl, int tex, int set) {
        Bitmap img = BitmapFactory.decodeFile(CachedTexturesProvider.getCachePath(tex, set));

        if (img == null) {
            final String errorMessage = "Can't load cached bitmap";
            Common.showToast(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[tex]);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);

        img.recycle();

        //noinspection UnusedAssignment
        img = null;
    }

    @SuppressLint("ObsoleteSdkInt")
    void onSurfaceCreated(GL10 gl) {
        if (texturesInitialized) {
            gl.glDeleteTextures(TEXTURE_LAST, textures, 0);
        }

        texturesInitialized = true;
        gl.glGenTextures(TEXTURE_LAST, textures, 0);

        // так как все битмапы загружаются в память GPU, и после этого освобождаются,
        // то inPurgeable не несёт особой пользы, а вред может и принести - на некоторых девайсах
        // вне зависимости от значения inInputShareable система может всё равно сделать копию исходных данных
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inDither = false;
        bitmapOptions.inScaled = false;
        bitmapOptions.inPurgeable = false;
        bitmapOptions.inInputShareable = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // пробуем уменьшить расход памяти, ибо всё равно потом все битмапы пургаются и за-null-иваются
            bitmapOptions.inMutable = true;
        }

        Bitmap img = CachedTexturesProvider.decodeTexture(assetManager, "tex_loading", bitmapOptions);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[TEXTURE_LOADING]);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
        img.recycle();

        //noinspection UnusedAssignment
        img = null;

        img = Common.createBitmap(RENDER_TO_SIZE, RENDER_TO_SIZE, "Can't alloc bitmap for render buffer");
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[TEXTURE_RENDER_TO]);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
        img.recycle();

        //noinspection UnusedAssignment
        img = null;

        if (engine.fboSupported) {
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[TEXTURE_RENDER_TO_FBO]);

            gl.glTexImage2D(GL10.GL_TEXTURE_2D,
                    0,
                    GL10.GL_RGBA,
                    RENDER_TO_FBO_SIZE,
                    RENDER_TO_FBO_SIZE,
                    0,
                    GL10.GL_RGBA,
                    GL10.GL_UNSIGNED_BYTE,
                    null);
        }

        System.gc();
    }

    @SuppressWarnings("UnusedReturnValue")
    boolean loadTexture(GL10 gl, int createdTexturesCount) {
        if (createdTexturesCount >= TEXTURES_TO_LOAD.length) {
            return false;
        }

        if (createdTexturesCount == 0) {
            levelConf = LevelConfig.read(assetManager, state.levelName);
        }

        TextureToLoad texToLoad = TEXTURES_TO_LOAD[createdTexturesCount];

        if (texToLoad.type == TextureToLoad.TYPE_MAIN) {
            loadAndBindTexture(gl,
                    texToLoad.tex,
                    CachedTexturesProvider.normalizeSetNum(CachedTexturesProvider.mainTexMap, levelConf.graphicsSet));
        } else {
            loadAndBindTexture(gl, texToLoad.tex, 0);
        }

        return true;
    }

    static int packTexId(int texId) {
        if (texId >= BASE_CEIL) {
            return (texId - BASE_CEIL) | PACKED_CEIL;
        } else if (texId >= BASE_FLOOR) {
            return (texId - BASE_FLOOR) | PACKED_FLOOR;
        } else if (texId >= BASE_DECOR_LAMP) {
            return (texId - BASE_DECOR_LAMP) | PACKED_DECOR_LAMP;
        } else if (texId >= BASE_DECOR_ITEM) {
            return (texId - BASE_DECOR_ITEM) | PACKED_DECOR_ITEM;
        } else if (texId >= BASE_DOORS_S) {
            return (texId - BASE_DOORS_S) | PACKED_DOORS_S;
        } else if (texId >= BASE_DOORS_F) {
            return (texId - BASE_DOORS_F) | PACKED_DOORS_F;
        } else if (texId >= BASE_TRANSP_WINDOWS) {
            return (texId - BASE_TRANSP_WINDOWS) | PACKED_TRANSP_WINDOWS;
        } else if (texId >= BASE_TRANSP_WALLS) {
            return (texId - BASE_TRANSP_WALLS) | PACKED_TRANSP_WALLS;
        } else if (texId >= BASE_WALLS) {
            return (texId - BASE_WALLS) | PACKED_WALLS;
        } else if (texId >= BASE_ARROWS) {
            return (texId - BASE_ARROWS) | PACKED_ARROWS;
        } else if (texId >= BASE_EXPLOSIONS) {
            return texId; // do not pack explosions, because actually is isn't texture, but animation frame
        } else if (texId >= BASE_BULLETS) {
            return (texId - BASE_BULLETS) | PACKED_BULLETS;
        } else if (texId >= BASE_OBJECTS) {
            return (texId - BASE_OBJECTS) | PACKED_OBJECTS;
        } else {
            return texId;
        }
    }

    @SuppressWarnings("MagicNumber")
    static int unpackTexId(int texId) {
        if (texId <= 0) {
            return texId;
        }

        int texBase = texId & 0xFFFF;

        switch (texId & 0xF0000) {
            case PACKED_WALLS:
                return texBase + BASE_WALLS;

            case PACKED_TRANSP_WALLS:
                return texBase + BASE_TRANSP_WALLS;

            case PACKED_TRANSP_WINDOWS:
                return texBase + BASE_TRANSP_WINDOWS;

            case PACKED_DOORS_F:
                return texBase + BASE_DOORS_F;

            case PACKED_DOORS_S:
                return texBase + BASE_DOORS_S;

            case PACKED_OBJECTS:
                return texBase + BASE_OBJECTS;

            case PACKED_DECOR_ITEM:
                return texBase + BASE_DECOR_ITEM;

            case PACKED_DECOR_LAMP:
                return texBase + BASE_DECOR_LAMP;

            case PACKED_FLOOR:
                return texBase + BASE_FLOOR;

            case PACKED_CEIL:
                return texBase + BASE_CEIL;

            case PACKED_BULLETS:
                return texBase + BASE_BULLETS;

            case PACKED_ARROWS:
                return texBase + BASE_ARROWS;

            default:
                return texBase;
        }
    }
}
