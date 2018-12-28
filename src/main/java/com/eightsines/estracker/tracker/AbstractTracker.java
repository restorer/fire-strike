package com.eightsines.estracker.tracker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.eightsines.estracker.Tracker;
import zame.game.flavour.config.AppConfig;

public abstract class AbstractTracker implements Tracker {
    @SuppressWarnings("WeakerAccess") protected boolean shouldSendCrashes;
    @SuppressWarnings("WeakerAccess") protected boolean shouldSendAnalytics;

    @Override
    public boolean getCrashedConsent() {
        return shouldSendCrashes;
    }

    @Override
    public void setCrashesConsent(boolean shouldSendCrashes) {
        this.shouldSendCrashes = shouldSendCrashes;
    }

    @Override
    public boolean getAnalyticsConsent() {
        return shouldSendAnalytics;
    }

    @Override
    public void setAnalyticsConsent(boolean shouldSendAnalytics) {
        this.shouldSendAnalytics = shouldSendAnalytics;
    }

    @Override
    public void trackEvent(@NonNull String name) {
        trackEvent(name, null);
    }

    @Override
    public void trackEvent(@NonNull String name, @Nullable String param) {
        if (shouldSendAnalytics) {
            trackEventInternal(AppConfig.DEBUG ? ("[debug] " + name) : name, param);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected abstract void trackEventInternal(@NonNull String name, @Nullable String param);
}
