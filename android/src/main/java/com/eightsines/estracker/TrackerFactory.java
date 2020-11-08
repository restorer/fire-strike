package com.eightsines.estracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eightsines.estracker.tracker.AggregateTracker;
import com.eightsines.estracker.tracker.DummyTracker;

import java.util.ArrayList;
import java.util.List;

public final class TrackerFactory {
    // private static final String PREFIX_COUNTLY = "countly";
    // private static final String PREFIX_MIXPANEL = "mixpanel";
    // private static final String PREFIX_GAMEANALYTICS = "gameanalytics";
    // private static final String PREFIX_APPCENTER = "appcenter";

    private TrackerFactory() {
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    @NonNull
    public static Tracker create(@NonNull Context context, @Nullable String config, boolean isDebugLog) {
        if (config == null) {
            return new DummyTracker();
        }

        List<Tracker> trackers = new ArrayList<>();

        for (String part : config.split("\\|\\|\\|")) {
            String[] parts = part.split("\\|");

            if (parts.length < 2) {
                throw new RuntimeException("Invalid tracker config: \"" + part + "\"");
            }

            // if (PREFIX_COUNTLY.equals(parts[0])) {
            //     if (parts.length < 3) {
            //         throw new RuntimeException("Invalid config for Countly: \"" + part + "\"");
            //     }
            //
            //     trackers.add(new CountlyTracker(context,
            //             parts[1],
            //             parts[2],
            //             parts.length > 3 ? parts[3] : null,
            //             isDebugLog));
            // } else if (PREFIX_MIXPANEL.equals(parts[0])) {
            //     trackers.add(new MixpanelTracker(context, parts[1]));
            // } else if (PREFIX_GAMEANALYTICS.equals(parts[0])) {
            //     if (parts.length < 3) {
            //         throw new RuntimeException("Invalid config for GameAnalytics: \"" + part + "\"");
            //     }
            //
            //     trackers.add(new GameAnalyticsTracker(parts[1], parts[2], isDebugLog));
            // } else if (PREFIX_APPCENTER.equals(parts[0])) {
            //     trackers.add(new AppCenterTracker(context, parts[1]));
            // } else {
            //     throw new RuntimeException("Unsupported tracker config: \"" + part + "\"");
            // }
        }

        if (trackers.isEmpty()) {
            return new DummyTracker();
        }

        if (trackers.size() == 1) {
            return trackers.get(0);
        }

        return new AggregateTracker(trackers);
    }
}
