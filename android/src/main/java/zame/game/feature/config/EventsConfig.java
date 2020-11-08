package zame.game.feature.config;

public final class EventsConfig {
    // 0-9A-Za-z, 32 chars max (to be compatible with GameAnalytics)

    public static final String EV_JUST_INSTALLED = "JustInstalled";

    public static final String EV_GAME_LEVEL_STARTED = "GameLevelStarted";
    public static final String EV_GAME_LEVEL_FINISHED = "GameLevelFinished";
    public static final String EV_GAME_SCRIPTED = "GameScripted";
    public static final String EV_GAME_CODE_ENTERED = "GameCodeEntered";
    public static final String EV_GAME_GAME_OVER = "GameGameOver";
    public static final String EV_GAME_GAME_OVER_REWARDED_STATE = "GameGameOverRewardedState";

    public static final String PAR_REWARDED_STATE_AVAILABLE = "Available";
    public static final String PAR_REWARDED_STATE_NOT_AVAILABLE = "NotAvailable";

    public static final String EV_GAME_GAME_OVER_RESTART = "GameGameOverRestart";
    public static final String EV_GAME_GAME_OVER_CONTINUE_REWARDED = "GameGameOverContinueRewarded";
    public static final String EV_GAME_GAME_OVER_REWARDED_SHOWN = "GameGameOverRewardedShown";

    public static final String EV_OPTIONS_HELP_REQUESTED = "OptionsHelpRequested";
    public static final String EV_OPTIONS_RESTART_CONFIRMED = "OptionsRestartConfirmed";

    public static final String EV_MENU_RATE_BAR_PRESSED = "MenuRateBarPressed";
    public static final String EV_MENU_RATE = "MenuRate";

    public static final String PAR_MENU_RATE_SHOWN = "Shown";
    public static final String PAR_MENU_RATE_SHOWN_LIKE = "ShownLike";
    public static final String PAR_MENU_RATE_SHOWN_DISLIKE = "ShownDislike";

    private EventsConfig() {}
}
