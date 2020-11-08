package com.eightsines.estracker.tracker;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
