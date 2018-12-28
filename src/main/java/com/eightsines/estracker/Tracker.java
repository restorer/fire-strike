package com.eightsines.estracker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Tracker {
    @SuppressWarnings("unused")
    boolean getCrashedConsent();

    void setCrashesConsent(boolean shouldSendCrashes);

    @SuppressWarnings("unused")
    boolean getAnalyticsConsent();

    void setAnalyticsConsent(boolean shouldSendAnalytics);

    void trackEvent(@NonNull String name);

    void trackEvent(@NonNull String name, @Nullable String param);
}
