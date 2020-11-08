package com.eightsines.estracker.tracker;

/*
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

public class MixpanelTracker extends AbstractTracker {
    private static final String TAG = CountlyTracker.class.getSimpleName();
    private static final String PROPERTY_PARAM = "Param";

    private MixpanelAPI mixpanel;

    public MixpanelTracker(@NonNull Context context, @NonNull String projectToken) {
        super();

        mixpanel = MixpanelAPI.getInstance(context, projectToken, true);
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
    }

    // Mixpanel also automatically tracks "$ae_crashed" event, find a way to configure it separately from analytics

    @Override
    public void setAnalyticsConsent(boolean shouldSendAnalytics) {
        super.setAnalyticsConsent(shouldSendAnalytics);

        if (shouldSendAnalytics && mixpanel.hasOptedOutTracking()) {
            mixpanel.optInTracking();
        } else if (!shouldSendAnalytics && !mixpanel.hasOptedOutTracking()) {
            mixpanel.optOutTracking();
        }
    }

    @Override
    public void onActivityPause(Activity activity) {
        super.onActivityPause(activity);
        mixpanel.flush();
    }

    @Override
    protected void trackEventInternal(@NonNull String name, @Nullable String param) {
        JSONObject properties = null;

        if (param != null) {
            properties = new JSONObject();

            try {
                properties.put(PROPERTY_PARAM, param);
            } catch (Throwable t) {
                Log.e(TAG, "trackEventInternal failed: " + t.toString(), t);
                return;
            }
        }

        mixpanel.track(name, properties);
    }
}
*/
