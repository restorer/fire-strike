package com.eightsines.estracker.tracker;

/*
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import ly.count.android.sdk.AdvertisingIdAdapter;
import ly.count.android.sdk.Countly;
import ly.count.android.sdk.DeviceId;
import ly.count.android.sdk.OpenUDIDAdapter;

// https://resources.count.ly/docs/countly-sdk-for-android

public class CountlyTracker extends AbstractTracker {
    private static final String TAG = CountlyTracker.class.getSimpleName();

    private static final String PREF_DEVICE_ID = "__CountlyTracker_DeviceId";
    private static final String SEGMENT_PARAM = "Param";
    private static final String COUNTLY_PREFERENCES = "COUNTLY_STORE";
    private static final String COUNTLY_PREFERENCE_CONNECTIONS = "CONNECTIONS";
    private static final long SINGLE_WAIT_FOR_COUNTLY_MILLIS = 100L;
    private static final long MAX_WAIT_FOR_COUNTLY_MILLIS = 10000L;
    private static final String[] CONSENT_CRASHES = { Countly.CountlyFeatureNames.crashes };

    private static final String[] CONSENT_ANALYTICS = {
            Countly.CountlyFeatureNames.sessions,
            Countly.CountlyFeatureNames.events,
            Countly.CountlyFeatureNames.views,
            Countly.CountlyFeatureNames.location,
            Countly.CountlyFeatureNames.attribution,
            Countly.CountlyFeatureNames.users };

    private Context context;
    private boolean isCountlyInitialized;
    private Thread.UncaughtExceptionHandler systemUncaughtExceptionHandler;
    private boolean isUncaughtExceptionHandlerCalled;

    @SuppressWarnings("FieldCanBeLocal")
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            if (isUncaughtExceptionHandlerCalled || systemUncaughtExceptionHandler == null) {
                return;
            }

            isUncaughtExceptionHandlerCalled = true;

            try {
                Log.e(TAG, "Uncaught exception: " + e.toString(), e);

                final Handler handler = new Handler(Looper.getMainLooper());
                final AtomicLong alreadyWaitMillis = new AtomicLong();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isCountlyInitialized
                                    && alreadyWaitMillis.get() < MAX_WAIT_FOR_COUNTLY_MILLIS
                                    && TextUtils.isEmpty(context.getSharedPreferences(
                                    COUNTLY_PREFERENCES,
                                    Context.MODE_PRIVATE)
                                    .getString(COUNTLY_PREFERENCE_CONNECTIONS, ""))) {

                                alreadyWaitMillis.getAndAdd(SINGLE_WAIT_FOR_COUNTLY_MILLIS);
                                handler.postDelayed(this, SINGLE_WAIT_FOR_COUNTLY_MILLIS);
                                return;
                            }

                            if (systemUncaughtExceptionHandler != null) {
                                systemUncaughtExceptionHandler.uncaughtException(t, e);
                            }
                        } catch (Throwable ignored) {
                            // Ignored - we should never fail here
                        }
                    }
                });
            } catch (Throwable ignored) {
                // Ignored - we should never fail here
            }
        }
    };

    public CountlyTracker(
            @NonNull Context context,
            @NonNull String serverUrl,
            @NonNull String appKey,
            @Nullable String salt,
            boolean isDebugLog) {

        super();
        this.context = context;

        String deviceId = null;
        DeviceId.Type idMode = null;

        try {
            if (AdvertisingIdAdapter.isAdvertisingIdAvailable()) {
                idMode = DeviceId.Type.ADVERTISING_ID;
            } else if (OpenUDIDAdapter.isOpenUDIDAvailable()) {
                idMode = DeviceId.Type.OPEN_UDID;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to detect idMode: " + t.toString());
        }

        if (idMode == null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            deviceId = sp.getString(PREF_DEVICE_ID, null);

            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString();
                sp.edit().putString(PREF_DEVICE_ID, deviceId).apply();
            }
        }

        systemUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

        try {
            Countly.sharedInstance()
                    .setRequiresConsent(true)
                    .setHttpPostForced(true)
                    .init(context, serverUrl, appKey, deviceId, idMode)
                    .enableCrashReporting()
                    .setViewTracking(true)
                    .setIfStarRatingShownAutomatically(false);

            if (salt != null) {
                Countly.sharedInstance().enableParameterTamperingProtection(salt);
            }

            if (isDebugLog) {
                Countly.sharedInstance().setLoggingEnabled(true);
            }

            isCountlyInitialized = true;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to initialize countly: " + t.toString());
        }
    }

    @Override
    public void setCrashesConsent(boolean shouldSendCrashes) {
        super.setCrashesConsent(shouldSendCrashes);

        if (isCountlyInitialized) {
            try {
                Countly.sharedInstance().setConsent(CONSENT_CRASHES, shouldSendCrashes);
            } catch (Throwable t) {
                Log.e(TAG, "Failed to set crashes consent: " + t.toString());
            }
        }
    }

    @Override
    public void setAnalyticsConsent(boolean shouldSendAnalytics) {
        super.setAnalyticsConsent(shouldSendAnalytics);

        if (isCountlyInitialized) {
            try {
                Countly.sharedInstance().setConsent(CONSENT_ANALYTICS, shouldSendAnalytics);
            } catch (Throwable t) {
                Log.e(TAG, "Failed to set analytics consent: " + t.toString());
            }
        }
    }

    @Override
    public void onActivityStart(Activity activity) {
        super.onActivityStart(activity);

        if (isCountlyInitialized) {
            try {
                Countly.sharedInstance().onStart(activity);
            } catch (Throwable t) {
                Log.e(TAG, "Failed to track activity start: " + t.toString());
            }
        }
    }

    @Override
    public void onActivityStop(Activity activity) {
        super.onActivityStop(activity);

        if (isCountlyInitialized) {
            try {
                Countly.sharedInstance().onStop();
            } catch (Throwable t) {
                Log.e(TAG, "Failed to track activity stop: " + t.toString());
            }
        }
    }

    @Override
    protected void trackEventInternal(@NonNull String name, @Nullable String param) {
        if (!isCountlyInitialized) {
            return;
        }

        Map<String, String> segmentation = null;

        if (param != null) {
            segmentation = new HashMap<>();
            segmentation.put(SEGMENT_PARAM, param);
        }

        try {
            Countly.sharedInstance().recordEvent(name, segmentation, 1);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to track event: " + t.toString());
        }
    }
}
*/
