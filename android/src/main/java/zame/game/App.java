package zame.game;

import android.app.Application;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.eightsines.esmediadtor.Mediadtor;
import com.eightsines.estracker.Tracker;
import com.eightsines.estracker.TrackerFactory;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import zame.game.core.app.AppBroadcastManager;
import zame.game.core.manager.PreferencesManager;
import zame.game.core.util.Common;
import zame.game.engine.state.Profile;
import zame.game.feature.config.EventsConfig;
import zame.game.feature.prepare.CachedTexturesProvider;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.AppConfig;

public class App extends Application {
    public static final Locale LOCALE_RU = new Locale("ru", "RU");

    public static App self;

    public final Handler handler = new Handler();
    public final Locale systemDefaultLocale = Locale.getDefault();
    public final AppBroadcastManager broadcastManager = new AppBroadcastManager();
    public Tracker tracker;
    public String internalRoot;
    public Profile profile;
    public PreferencesManager preferences;
    public boolean isLargeDevice;
    public float controlsScale;
    public Mediadtor mediadtor;
    public Typeface cachedTypeface;
    public SoundManager soundManagerInstance;
    public CachedTexturesProvider.Task cachedTexturesTask;
    public volatile boolean cachedTexturesReady;
    private String cachedVersionName;
    public boolean isLimitAdTrackingEnabled = true;

    @Override
    public void onCreate() {
        super.onCreate();

        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return;
        }

        self = this;

        tracker = TrackerFactory.create(
                this,
                AppConfig.TRACKER_CONFIG,
                AppConfig.DEBUG);

        internalRoot = getInternalStoragePath() + File.separator;
        profile = new Profile();
        preferences = new PreferencesManager();
        isLargeDevice = getResources().getBoolean(R.bool.gloomy_device_large);

        //noinspection MagicNumber
        controlsScale = isLargeDevice ? 0.75f : 1.0f;

        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

        if (preferences.getInt(R.string.key_rotate_speed) == 0) {
            preferences.putInt(R.string.key_rotate_speed, isLargeDevice ? 8 : 4);
        }

        applyConsent();
        profile.loadInitial(this);
    }

    public void applyConsent() {
        if (AppConfig.DEBUG) {
            Common.log("applyConsent -- isLimitAdTrackingEnabled = " + isLimitAdTrackingEnabled);
        }

        boolean isConsentChosen = preferences.getBoolean(R.string.key_is_consent_chosen);

        tracker.setCrashesConsent(isConsentChosen && preferences.getBoolean(R.string.key_consent_crashes));
        tracker.setAnalyticsConsent(isConsentChosen && preferences.getBoolean(R.string.key_consent_analytics));

        mediadtor = new Mediadtor(
                AppConfig.MEDIADTOR_APPLICATION_KEY,
                isConsentChosen
                        && preferences.getBoolean(R.string.key_consent_ad_personalization)
                        && !isLimitAdTrackingEnabled,
                AppConfig.MEDIADTOR_TEST_ADS,
                AppConfig.DEBUG ? AppConfig.TAG : null);

        if (tracker.getAnalyticsConsent() && !preferences.getBoolean(R.string.key_install_event_sent)) {
            tracker.trackEvent(EventsConfig.EV_JUST_INSTALLED, getVersionName());
            preferences.putBoolean(R.string.key_install_event_sent, true);
        }
    }

    public String getVersionName() {
        if (cachedVersionName == null) {
            cachedVersionName = "x.x";

            try {
                cachedVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception ex) {
                Common.log(ex);
            }
        }

        return cachedVersionName;
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
}
