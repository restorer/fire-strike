package com.eightsines.estracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.eightsines.estracker.tracker.AppCenterTracker;
import com.eightsines.estracker.tracker.DummyTracker;

@SuppressWarnings("unused")
public final class TrackerFactory {
    private TrackerFactory() {
    }

    @NonNull
    public static Tracker create(@NonNull Context context, @Nullable String applicationKey) {
        if (applicationKey == null) {
            return new DummyTracker();
        }

        return new AppCenterTracker(context, applicationKey);
    }
}
