package com.eightsines.estracker;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Tracker {
    @SuppressWarnings({ "unused", "RedundantSuppression" })
    boolean getCrashesConsent();

    void setCrashesConsent(boolean shouldSendCrashes);

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    boolean getAnalyticsConsent();

    void setAnalyticsConsent(boolean shouldSendAnalytics);

    void trackEvent(@NonNull String name);

    void trackEvent(@NonNull String name, @Nullable String param);

    void onActivityCreate(Activity activity);

    void onActivityStart(Activity activity);

    void onActivityPause(Activity activity);

    void onActivityStop(Activity activity);
}
