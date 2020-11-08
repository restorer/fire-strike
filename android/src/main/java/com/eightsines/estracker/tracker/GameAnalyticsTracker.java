package com.eightsines.estracker.tracker;

/*
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gameanalytics.sdk.GAProgressionStatus;
import com.gameanalytics.sdk.GameAnalytics;

import zame.game.App;
import zame.game.feature.config.EventsConfig;
import zame.game.flavour.config.AppConfig;

public class GameAnalyticsTracker extends AbstractTracker {
    @SuppressWarnings("BooleanVariableAlwaysNegated") private boolean isInitialized;
    private String gameKey;
    private String gameSecret;

    public GameAnalyticsTracker(String gameKey, String gameSecret, boolean isDebugLog) {
        super();

        this.gameKey = gameKey;
        this.gameSecret = gameSecret;

        if (isDebugLog) {
            GameAnalytics.setEnabledInfoLog(true);
            GameAnalytics.setEnabledVerboseLog(true);
        }

        GameAnalytics.setEnabledErrorReporting(false);
        GameAnalytics.setEnabledEventSubmission(false);
        GameAnalytics.configureBuild(App.self.getVersionName());

        // GameAnalytics.configureAvailableResourceCurrencies()
        // GameAnalytics.configureAvailableResourceItemTypes()
        // GameAnalytics.configureAvailableCustomDimensions01()
        // GameAnalytics.configureAvailableCustomDimensions02()
        // GameAnalytics.configureAvailableCustomDimensions03()
    }

    @Override
    public void setCrashesConsent(boolean shouldSendCrashes) {
        super.setCrashesConsent(shouldSendCrashes);

        // probably (not tested) setEnabledEventSubmission() has more weight than setEnabledErrorReporting()
        GameAnalytics.setEnabledErrorReporting(shouldSendCrashes);
    }

    @Override
    public void setAnalyticsConsent(boolean shouldSendAnalytics) {
        super.setAnalyticsConsent(shouldSendAnalytics);
        GameAnalytics.setEnabledEventSubmission(shouldSendAnalytics);
    }

    @Override
    public void onActivityCreate(Activity activity) {
        super.onActivityCreate(activity);

        if (!isInitialized) {
            isInitialized = true;
            GameAnalytics.initializeWithGameKey(activity, gameKey, gameSecret);
        }
    }

    @Override
    public void trackEvent(@NonNull String name, @Nullable String param) {
        super.trackEvent(name, param);

        if (!isInitialized || !shouldSendAnalytics || param == null) {
            return;
        }

        switch (name) {
            case EventsConfig.EV_GAME_LEVEL_STARTED:
                GameAnalytics.addProgressionEventWithProgressionStatus(
                        GAProgressionStatus.Start,
                        fixEventId(AppConfig.DEBUG ? (PREFIX_DEBUG + param) : param));

                break;

            case EventsConfig.EV_GAME_GAME_OVER:
                GameAnalytics.addProgressionEventWithProgressionStatus(
                        GAProgressionStatus.Fail,
                        fixEventId(AppConfig.DEBUG ? (PREFIX_DEBUG + param) : param));

                break;

            case EventsConfig.EV_GAME_LEVEL_FINISHED:
                GameAnalytics.addProgressionEventWithProgressionStatus(
                        GAProgressionStatus.Complete,
                        fixEventId(AppConfig.DEBUG ? (PREFIX_DEBUG + param) : param));

                break;
        }
    }

    @Override
    protected void trackEventInternal(@NonNull String name, @Nullable String param) {
        if (!isInitialized) {
            return;
        }

        if (param != null) {
            GameAnalytics.addDesignEventWithEventId(fixEventId(name) + ":" + fixEventId(param));
        } else {
            GameAnalytics.addDesignEventWithEventId(fixEventId(name));
        }
    }

    private String fixEventId(@NonNull String param) {
        // According to documentation "_" isn't allowed symbol, but this symbol is used in example "StartGame:ClassLevel1_5"
        return param.replaceAll("[^0-9A-Za-z_]", "_");
    }
}
*/
