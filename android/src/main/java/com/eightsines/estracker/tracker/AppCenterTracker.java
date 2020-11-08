package com.eightsines.estracker.tracker;

/*
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.analytics.EventProperties;
import com.microsoft.appcenter.crashes.AbstractCrashesListener;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.model.ErrorReport;

public class AppCenterTracker extends AbstractTracker {
    private static final String KEY_PARAM = "Param";

    public AppCenterTracker(@NonNull Context context, @NonNull String applicationKey) {
        super();
        AppCenter.start((Application)context.getApplicationContext(), applicationKey, Analytics.class, Crashes.class);

        Crashes.setListener(new AbstractCrashesListener() {
            @Override
            public boolean shouldProcess(ErrorReport report) {
                return shouldSendCrashes;
            }
        });
    }

    @Override
    public void setCrashesConsent(boolean shouldSendCrashes) {
        super.setCrashesConsent(shouldSendCrashes);

        if (shouldSendCrashes) {
            Crashes.notifyUserConfirmation(Crashes.ALWAYS_SEND);
        } else {
            Crashes.notifyUserConfirmation(Crashes.DONT_SEND);
        }
    }

    @Override
    protected void trackEventInternal(@NonNull String name, @Nullable String param) {
        if (param == null) {
            Analytics.trackEvent(name);
        } else {
            Analytics.trackEvent(name, new EventProperties().set(KEY_PARAM, param));
        }
    }
}
*/
