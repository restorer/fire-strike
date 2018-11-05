package zame.game;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.io.IOException;
import zame.game.managers.SoundManager;
import zame.game.misc.Tracker;
import zame.game.misc.TrackerDummy;
import zame.game.providers.CachedTexturesProvider;
import zame.game.store.Profile;

public class App extends Application {
    public static App self;

    public final Handler handler = new Handler();
    public boolean isLargeDevice;
    public String internalRoot;
    public Tracker trackerInst;
    public SoundManager soundManagerInst;
    public Profile profile = new Profile();
    public CachedTexturesProvider.Task cachedTexturesTask;
    public Typeface cachedTypeface;
    public volatile boolean cachedTexturesReady;
    public float controlsScale;

    private String cachedVersionName;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(new Fabric.Builder(this).kits(new Crashlytics()).debuggable(BuildConfig.DEBUG).build());

        self = this;
        isLargeDevice = getResources().getBoolean(R.bool.gloomy_device_large);
        internalRoot = getInternalStoragePath() + File.separator;

        initPreferences();
        initTracker();

        if (getSharedPreferences().getBoolean("FirstRun", true)) {
            profile.load(false);
            profile.save();

            getSharedPreferences().edit().putBoolean("FirstRun", false).apply();
        } else {
            profile.load();
        }
    }

    public SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public LocalBroadcastManager getLocalBroadcastManager() {
        return LocalBroadcastManager.getInstance(getApplicationContext());
    }

    private String getInternalStoragePath() {
        String result = "";
        final String errorMessage = "Can't open internal storage";

        if (getFilesDir() == null) {
            Common.log("MyApplication.getInternalStoragePath : getFilesDir() == null");
        } else {
            try {
                result = getFilesDir().getCanonicalPath();
            } catch (IOException ex) {
                Common.log(errorMessage, ex);
            }
        }

        if (TextUtils.isEmpty(result)) {
            Toast.makeText(this, "Critical error!\n" + errorMessage + ".", Toast.LENGTH_LONG).show();
            throw new RuntimeException(errorMessage);
        }

        return result;
    }

    private void initPreferences() {
        SharedPreferences sp = getSharedPreferences();

        //noinspection MagicNumber
        controlsScale = isLargeDevice ? 0.75f : 1.0f;

        if (sp.getInt("RotateSpeed", 0) == 0) {
            sp.edit().putInt("RotateSpeed", isLargeDevice ? 8 : 4).apply();
        }

        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
    }

    private void initTracker() {
        trackerInst = new TrackerDummy();
    }

    public String getVersionName() {
        if (cachedVersionName == null) {
            cachedVersionName = "xxxx.xx.xx.xxxx";

            try {
                cachedVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception ex) {
                Common.log(ex);
            }
        }

        return cachedVersionName;
    }
}
