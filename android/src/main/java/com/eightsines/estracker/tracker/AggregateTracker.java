package com.eightsines.estracker.tracker;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eightsines.estracker.Tracker;

import java.util.List;

public class AggregateTracker implements Tracker {
    private final List<Tracker> trackers;

    public AggregateTracker(List<Tracker> trackers) {
        this.trackers = trackers;
    }

    @Override
    public boolean getCrashesConsent() {
        for (Tracker tracker : trackers) {
            if (tracker.getCrashesConsent()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setCrashesConsent(boolean shouldSendCrashes) {
        for (Tracker tracker : trackers) {
            tracker.setCrashesConsent(shouldSendCrashes);
        }
    }

    @Override
    public boolean getAnalyticsConsent() {
        for (Tracker tracker : trackers) {
            if (tracker.getAnalyticsConsent()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setAnalyticsConsent(boolean shouldSendAnalytics) {
        for (Tracker tracker : trackers) {
            tracker.setAnalyticsConsent(shouldSendAnalytics);
        }
    }

    @Override
    public void trackEvent(@NonNull String name) {
        for (Tracker tracker : trackers) {
            tracker.trackEvent(name);
        }
    }

    @Override
    public void trackEvent(@NonNull String name, @Nullable String param) {
        for (Tracker tracker : trackers) {
            tracker.trackEvent(name, param);
        }
    }

    @Override
    public void onActivityCreate(Activity activity) {
        for (Tracker tracker : trackers) {
            tracker.onActivityCreate(activity);
        }
    }

    @Override
    public void onActivityStart(Activity activity) {
        for (Tracker tracker : trackers) {
            tracker.onActivityStart(activity);
        }
    }

    @Override
    public void onActivityPause(Activity activity) {
        for (Tracker tracker : trackers) {
            tracker.onActivityPause(activity);
        }
    }

    @Override
    public void onActivityStop(Activity activity) {
        for (Tracker tracker : trackers) {
            tracker.onActivityStop(activity);
        }
    }
}
