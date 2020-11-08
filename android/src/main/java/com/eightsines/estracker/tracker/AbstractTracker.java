package com.eightsines.estracker.tracker;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eightsines.estracker.Tracker;

import zame.game.flavour.config.AppConfig;

public abstract class AbstractTracker implements Tracker {
    @SuppressWarnings("WeakerAccess") protected static final String PREFIX_DEBUG = "DEBUG_";

    @SuppressWarnings("WeakerAccess") protected boolean shouldSendCrashes;
    @SuppressWarnings("WeakerAccess") protected boolean shouldSendAnalytics;

    @Override
    public boolean getCrashesConsent() {
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
            trackEventInternal(AppConfig.DEBUG ? (PREFIX_DEBUG + name) : name, param);
        }
    }

    @Override
    public void onActivityCreate(Activity activity) {
    }

    @Override
    public void onActivityStart(Activity activity) {
    }

    @Override
    public void onActivityPause(Activity activity) {
    }

    @Override
    public void onActivityStop(Activity activity) {
    }

    @SuppressWarnings("WeakerAccess")
    protected abstract void trackEventInternal(@NonNull String name, @Nullable String param);
}
