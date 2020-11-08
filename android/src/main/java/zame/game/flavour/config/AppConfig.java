package zame.game.flavour.config;

import zame.game.BuildConfig;

public final class AppConfig {
    public static final String TAG = "FSR";

    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final boolean SHOULD_ASK_CONSENT = false;
    public static final boolean SHOULD_ASK_RATE = false;
    public static final boolean MEDIADTOR_TEST_ADS = false;

    public static final String TRACKER_CONFIG = null;
    public static final String MEDIADTOR_APPLICATION_KEY = null;

    public static final String LINK_HELP =
            "https://eightsines.com/fire-strike/index.php?action=help&utm_medium=referral&utm_source=ingame&utm_campaign=ingame&hl=";

    public static final String LINK_DISLIKE =
            "https://eightsines.com/fire-strike/index.php?action=dislike&utm_medium=referral&utm_source=ingame&utm_campaign=ingame&hl=";

    public static final String LINK_VK = "https://vk.com/gloomy.dungeons";
    public static final String LINK_FACEBOOK = "https://www.facebook.com/gloomy.dungeons/";
    public static final String LINK_TELEGRAM = "https://t.me/gloomy_dungeons";

    private AppConfig() {}
}
