package zame.game.feature.prepare;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import zame.game.App;
import zame.game.R;
import zame.game.core.util.Common;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.graphics.TextureLoader;

public final class CachedTexturesProvider {
    static final String BROADCAST_ACTION = "local:CachedTexturesProvider";
    static final String EXTRA_PROGRESS = "EXTRA_PROGRESS";

    // @formatter:off
    private static final ColorMatrixColorFilter GRAY_COLOR_FILTER = new ColorMatrixColorFilter(new ColorMatrix(new float[] {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            1, 0, 0, 0, 0, }));
    // @formatter:on

    public static final String[][] mainTexMap = { { "texmap_1_p", "texmap_1_a" }, };

    public interface IOnComplete {
        void onComplete();
    }

    public static class Task extends AsyncTask<Void, Integer, Boolean> {
        IOnComplete onComplete;
        AssetManager assetManager;
        BitmapFactory.Options bitmapOptions;
        int totalCount;
        int cachedCount;

        @SuppressLint("ObsoleteSdkInt")
        Task(IOnComplete onComplete) {
            super();

            this.onComplete = onComplete;
            this.assetManager = App.self.getAssets();

            bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inDither = false;
            bitmapOptions.inScaled = false;
            bitmapOptions.inPurgeable = false;
            bitmapOptions.inInputShareable = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // пробуем уменьшить расход памяти, ибо всё равно потом все битмапы пургаются и за-null-иваются
                bitmapOptions.inMutable = true;
            }
        }

        private Bitmap loadAlphaImage(String pixelsName, String alphaName) {
            Bitmap pixelsBitmap = decodeTexture(assetManager, pixelsName, bitmapOptions);

            if (alphaName == null) {
                return pixelsBitmap;
            }

            Bitmap result = Common.createBitmap(
                    pixelsBitmap.getWidth(),
                    pixelsBitmap.getHeight(),
                    "Can't alloc result bitmap for tiles");

            Canvas canvas = new Canvas(result);
            canvas.setDensity(Bitmap.DENSITY_NONE);

            canvas.drawBitmap(pixelsBitmap, 0.0f, 0.0f, null);
            pixelsBitmap.recycle();

            //noinspection UnusedAssignment
            pixelsBitmap = null;

            // https://stackoverflow.com/questions/5098680/how-to-combine-two-opaque-bitmaps-into-one-with-alpha-channel
            Bitmap alphaBitmap = decodeTexture(assetManager, alphaName, bitmapOptions);

            Paint alphaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            alphaPaint.setColorFilter(GRAY_COLOR_FILTER);
            alphaPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            canvas.drawBitmap(alphaBitmap, 0.0f, 0.0f, alphaPaint);

            alphaBitmap.recycle();

            //noinspection UnusedAssignment
            alphaBitmap = null;

            //noinspection UnusedAssignment
            canvas = null;

            return result;
        }

        private Bitmap loadMainTexture(String[] texList) {
            //noinspection MagicNumber
            Bitmap img = Common.createBitmap(2048, 2048, "Can't alloc bitmap for tiles");

            Canvas canvas = new Canvas(img);
            canvas.setDensity(Bitmap.DENSITY_NONE);

            Bitmap tiles = loadAlphaImage(texList[0], texList[1]);
            canvas.drawBitmap(tiles, 0.0f, (float)(TextureLoader.ROW_TILES * (Renderer.SIZE_TILE + 2)), null);
            tiles.recycle();

            //noinspection UnusedAssignment
            tiles = null;

            Bitmap common = loadAlphaImage("texmap_common_p", "texmap_common_a");
            canvas.drawBitmap(common, 0.0f, (float)(TextureLoader.ROW_COMMON * (Renderer.SIZE_TILE + 2)), null);
            common.recycle();

            //noinspection UnusedAssignment
            common = null;

            //noinspection UnusedAssignment
            canvas = null;

            return img;
        }

        private Bitmap loadMonstersTexture(String pixelsName, String alphaName) {
            //noinspection MagicNumber
            Bitmap img = Common.createBitmap(2048, 2048, "Can't alloc bitmap for tiles");

            Canvas canvas = new Canvas(img);
            canvas.setDensity(Bitmap.DENSITY_NONE);

            Bitmap monsters = loadAlphaImage(pixelsName, alphaName);
            canvas.drawBitmap(monsters, 0.0f, 0.0f, null);
            monsters.recycle();

            //noinspection UnusedAssignment
            monsters = null;

            //noinspection UnusedAssignment
            canvas = null;

            return img;
        }

        private void saveToCache(int tex, int set, Bitmap img) {
            //noinspection BooleanVariableAlwaysNegated
            boolean success = false;

            try {
                FileOutputStream fos = new FileOutputStream(getCachePath(tex, set));

                //noinspection MagicNumber
                success = img.compress(Bitmap.CompressFormat.PNG, 90, fos);

                try {
                    fos.flush();
                    fos.close();
                } catch (Exception innerEx) {
                    Common.log(innerEx);
                }
            } catch (Exception ex) {
                Common.log(ex);
            }

            if (!success) {
                final String errorMessage = "Can't save bitmap to cache";
                Common.showToast(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Bitmap img;

            for (int i = 0, lenI = TextureLoader.TEXTURES_TO_LOAD.length; i < lenI; i++) {
                TextureLoader.TextureToLoad texToLoad = TextureLoader.TEXTURES_TO_LOAD[i];

                if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MAIN) {
                    totalCount += mainTexMap.length;
                } else {
                    totalCount++;
                }
            }

            for (int i = 0, lenI = TextureLoader.TEXTURES_TO_LOAD.length; i < lenI; i++) {
                TextureLoader.TextureToLoad texToLoad = TextureLoader.TEXTURES_TO_LOAD[i];

                if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MAIN) {
                    for (int j = 0, lenJ = mainTexMap.length; j < lenJ; j++) {
                        img = loadMainTexture(mainTexMap[j]);
                        saveToCache(texToLoad.tex, j + 1, img);
                        img.recycle();

                        //noinspection UnusedAssignment
                        img = null;

                        onImageCached();
                    }
                } else {
                    if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MONSTERS_1
                            || texToLoad.type == TextureLoader.TextureToLoad.TYPE_MONSTERS_2) {

                        img = loadMonstersTexture(texToLoad.pixelsName, texToLoad.alphaName);
                    } else {
                        img = loadAlphaImage(texToLoad.pixelsName, texToLoad.alphaName);
                    }

                    saveToCache(texToLoad.tex, 0, img);
                    img.recycle();

                    //noinspection UnusedAssignment
                    img = null;

                    onImageCached();
                }
            }

            return true;
        }

        private void onImageCached() {
            System.gc();
            cachedCount++;

            //noinspection MagicNumber
            publishProgress((int)((float)cachedCount / (float)totalCount * 100.0f));
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_PROGRESS, progress[0]);

            App.self.broadcastManager.sendBroadcast(BROADCAST_ACTION, bundle);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            onComplete.onComplete();
        }
    }

    private CachedTexturesProvider() {
    }

    public static Bitmap decodeTexture(AssetManager assetManager, String name, BitmapFactory.Options bitmapOptions) {
        Bitmap result = null;
        byte[] bytes;

        try {
            bytes = Common.readBytes(assetManager.open("textures/" + name + ".tex"));
        } catch (IOException ex) {
            final String ioErrorMessage = "Can't open \"" + name + "\" texture";
            Common.showToast(ioErrorMessage);
            throw new RuntimeException(ioErrorMessage);
        }

        for (int i = 0, len = bytes.length; i < len; i++) {
            //noinspection MagicNumber
            bytes[i] ^= 0xAA;
        }

        if (bitmapOptions != null) {
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }

        try {
            result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (OutOfMemoryError oom) {
            // ignored
        } catch (Exception ex) {
            final String decodeErrorMessage = "Can't decode \"" + name + "\" texture";
            Common.showToast(decodeErrorMessage);
            throw new RuntimeException(decodeErrorMessage);
        }

        if (result == null && bitmapOptions != null) {
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_4444;

            try {
                result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (OutOfMemoryError oom) {
                // ignored
            } catch (Exception ex) {
                final String decodeErrorMessage = "Can't decode \"" + name + "\" texture";
                Common.showToast(decodeErrorMessage);
                throw new RuntimeException(decodeErrorMessage);
            }
        }

        //noinspection UnusedAssignment
        bytes = null;

        if (result == null) {
            final String oomErrorMessage = "Out of memory (or can't decode) \"" + name + "\" texture";
            Common.showToast(oomErrorMessage);
            throw new OutOfMemoryError(oomErrorMessage);
        }

        return result;
    }

    public static int normalizeSetNum(String[][] texMap, int setNum) {
        return ((setNum < 1 || setNum > texMap.length) ? 1 : setNum);
    }

    public static String getCachePath(int tex, int set) {
        StringBuilder sb = new StringBuilder(App.self.internalRoot);

        sb.append("tex_");
        sb.append(tex);

        if (set != 0) {
            sb.append('_');
            sb.append(set);
        }

        sb.append(".png");
        return sb.toString();
    }

    private static int getCacheVersion() {
        try {
            InputStream is = App.self.getAssets().open("settings.properties");

            Properties properties = new Properties();
            properties.load(is);
            is.close();

            return Integer.parseInt(properties.getProperty("textures_cache_version"));
        } catch (Exception ex) {
            Common.log(ex);
        }

        return 1;
    }

    public static boolean needToUpdateCache() {
        App.self.cachedTexturesReady = false;

        if (App.self.preferences.getInt(R.string.key_cached_textures_version) != getCacheVersion()) {
            return true;
        }

        for (int i = 0, lenI = TextureLoader.TEXTURES_TO_LOAD.length; i < lenI; i++) {
            TextureLoader.TextureToLoad texToLoad = TextureLoader.TEXTURES_TO_LOAD[i];

            if (texToLoad.type == TextureLoader.TextureToLoad.TYPE_MAIN) {
                for (int j = 0, lenJ = mainTexMap.length; j < lenJ; j++) {
                    if (!(new File(getCachePath(texToLoad.tex, j + 1))).exists()) {
                        return true;
                    }
                }
            } else if (!(new File(getCachePath(texToLoad.tex, 0))).exists()) {
                return true;
            }
        }

        App.self.cachedTexturesReady = true;
        return false;
    }

    static void updateCache() {
        App.self.handler.post(new Runnable() {
            @Override
            public void run() {
                if (App.self.cachedTexturesTask != null) {
                    return;
                }

                App.self.cachedTexturesTask = new Task(new IOnComplete() {
                    @SuppressWarnings("MagicNumber")
                    @Override
                    public void onComplete() {
                        App.self.preferences.putInt(R.string.key_cached_textures_version, getCacheVersion());
                        App.self.cachedTexturesReady = true;
                        App.self.cachedTexturesTask = null;

                        Bundle bundle = new Bundle();
                        bundle.putInt(EXTRA_PROGRESS, 101);

                        App.self.broadcastManager.sendBroadcast(BROADCAST_ACTION, bundle);
                    }
                });

                App.self.cachedTexturesTask.execute();
            }
        });
    }
}
