package zame.game.core.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import zame.game.App;
import zame.game.R;
import zame.game.flavour.config.AppConfig;

public final class Common {
    private Common() {}

    public static int dpToPx(Context context, int dp) {
        return (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics());
    }

    // public static String getLocalizedAssetPath(
    //         AssetManager assetManager,
    //         @SuppressWarnings("SameParameterValue") String pathTemplate) {
    //
    //     String path = String.format(
    //             Locale.US,
    //             pathTemplate,
    //             "-" + Locale.getDefault().getLanguage().toLowerCase(Locale.US));
    //
    //     try {
    //         InputStream is = assetManager.open(path);
    //         is.close();
    //     } catch (Exception ex) {
    //         path = String.format(Locale.US, pathTemplate, "");
    //     }
    //
    //     return "file:///android_asset/" + path;
    // }

    @SuppressWarnings({ "UnusedReturnValue", "unused", "RedundantSuppression" })
    public static boolean openBrowser(Context context, String uri) {
        final String[] appPackageNames = { "com.android.chrome", "com.android.browser", };

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

            final List<ResolveInfo> list = context.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            outer:
            for (String appPackage : appPackageNames) {
                for (ResolveInfo resolveInfo : list) {
                    String packageName = resolveInfo.activityInfo.packageName;

                    if (packageName != null && packageName.startsWith(appPackage)) {
                        intent.setPackage(packageName);
                        break outer;
                    }
                }
            }

            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));
            return true;
        } catch (Exception ex) {
            log(ex);
            showToast("Could not launch the browser application.");
        }

        return false;
    }

    public static void openViewIntent(Context context, String uriString) {
        Uri uri;

        try {
            uri = Uri.parse(uriString);
        } catch (Exception ex) {
            Common.log(ex);
            return;
        }

        openExternalIntent(context, new Intent(Intent.ACTION_VIEW, uri));
    }

    public static void openExternalIntent(Context context, Intent intent) {
        openExternalIntent(context, intent, true);
    }

    private static void openExternalIntent(
            Context context,
            Intent intent,
            @SuppressWarnings("SameParameterValue") boolean logExceptions) {

        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));
        } catch (Exception ex) {
            if (logExceptions) {
                log(ex);
            }

            showToast("Could not startBatch external intent.");
        }

    }

    public static void safeRename(String tmpName, String fileName) {
        String oldName = fileName + ".old";

        if ((new File(oldName)).exists()) {
            //noinspection ResultOfMethodCallIgnored
            (new File(oldName)).delete();
        }

        if ((new File(fileName)).exists()) {
            //noinspection ResultOfMethodCallIgnored
            (new File(fileName)).renameTo(new File(oldName));
        }

        //noinspection ResultOfMethodCallIgnored
        (new File(tmpName)).renameTo(new File(fileName));

        if ((new File(oldName)).exists()) {
            //noinspection ResultOfMethodCallIgnored
            (new File(oldName)).delete();
        }
    }

    public static void log(String message) {
        Log.e(AppConfig.TAG, message);
    }

    public static void log(Throwable tr) {
        Log.e(AppConfig.TAG, "Exception: " + tr, tr);
    }

    public static void log(String message, Throwable tr) {
        Log.e(AppConfig.TAG, message, tr);
    }

    public static boolean canUseKey(int keyCode) {
        return ((keyCode != KeyEvent.KEYCODE_BACK) && (keyCode != KeyEvent.KEYCODE_HOME) && (keyCode
                != KeyEvent.KEYCODE_MENU) && (keyCode != KeyEvent.KEYCODE_ENDCALL));
    }

    public static byte[] readBytes(InputStream is) throws IOException {
        byte[] buffer;

        buffer = new byte[is.available()];

        //noinspection ResultOfMethodCallIgnored
        is.read(buffer);

        is.close();

        return buffer;
    }

    public static String getTimeString(int seconds) {
        StringBuilder sb = new StringBuilder();

        //noinspection MagicNumber
        int hrs = seconds / 3600;

        //noinspection MagicNumber
        int mins = (seconds / 60) % 60;

        //noinspection MagicNumber
        int secs = seconds % 60;

        if (hrs > 0) {
            sb.append(hrs);
            sb.append(":");

            if (mins < 10) {
                sb.append("0");
            }
        }

        sb.append(mins);
        sb.append(":");

        if (secs < 10) {
            sb.append("0");
        }

        sb.append(secs);
        return sb.toString();
    }

    public static String urlEncode(String text) {
        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (Exception ex) {
            log(ex);
        }

        return text;
    }

    public static Object defaultize(Object obj, Object def) {
        return (obj == null ? def : obj);
    }

    public static Typeface loadIngameTypeface(Context context) {
        if (App.self.cachedTypeface == null) {
            try {
                App.self.cachedTypeface = ResourcesCompat.getFont(context, R.font.ingame);
            } catch (Exception ex) {
                log(ex);
                App.self.cachedTypeface = Typeface.DEFAULT;
            }
        }

        return App.self.cachedTypeface;
    }

    public static void showToast(final String message) {
        try {
            App.self.handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(App.self, message, Toast.LENGTH_LONG).show();
                    } catch (Throwable innerFatality) {
                        // ignored
                    }
                }
            });
        } catch (Throwable fatality) {
            // ignored
        }
    }

    public static void showToast(final Context context, final int resourceId) {
        try {
            App.self.handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(App.self, context.getResources().getText(resourceId), Toast.LENGTH_LONG).show();
                    } catch (Throwable innerFatality) {
                        // ignored
                    }
                }
            });
        } catch (Throwable fatality) {
            // ignored
        }
    }

    public static Bitmap createBitmap(int width, int height, String oomErrorMessage) {
        return createBitmap(width, height, 0, 0, oomErrorMessage);
    }

    public static Bitmap createBitmap(int hqWidth, int hqHeight, int lqWidth, int lqHeight, String oomErrorMessage) {
        Bitmap result = null;

        try {
            result = Bitmap.createBitmap(hqWidth, hqHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError oom) {
            // do nothing, because some chienese phones / tablets returns null instead of throwing OutOfMemoryError
        }

        if (result == null) {
            try {
                result = Bitmap.createBitmap(hqWidth, hqHeight, Bitmap.Config.ARGB_4444);
            } catch (OutOfMemoryError oom) {
                // do nothing, because some chienese phones / tablets returns null instead of throwing OutOfMemoryError
            }
        }

        // ok, there is chinese phone / tablet
        if (result == null && lqWidth > 0 && lqHeight > 0) {
            try {
                result = Bitmap.createBitmap(lqWidth, lqHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError oom) {
                // do nothing, because some chienese phones / tablets returns null instead of throwing OutOfMemoryError
            }

            if (result == null) {
                try {
                    result = Bitmap.createBitmap(lqWidth, lqHeight, Bitmap.Config.ARGB_4444);
                } catch (OutOfMemoryError oom) {
                    // do nothing, because some chienese phones / tablets returns null instead of throwing OutOfMemoryError
                }
            }
        }

        // we did everything we could...
        if (result == null) {
            showToast(oomErrorMessage);
            throw new OutOfMemoryError(oomErrorMessage);
        }

        return result;
    }
}
