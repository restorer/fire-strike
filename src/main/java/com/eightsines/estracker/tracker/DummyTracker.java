package com.eightsines.estracker.tracker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class DummyTracker extends AbstractTracker {
    private static final String TAG = "EsTracker";

    @Override
    protected void trackEventInternal(@NonNull String name, @Nullable String param) {
        if (param == null) {
            Log.e(TAG, name);
        } else {
            Log.e(TAG, name + ": " + param);
        }
    }
}
