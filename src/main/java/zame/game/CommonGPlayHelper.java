package zame.game;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class CommonGPlayHelper {
    public static final String RATE_DISLIKE_LINK = "http://eightsines.com/fire-strike/index.php?action=dislike&utm_medium=referral&utm_source=ingame&utm_campaign=ingame&hl=";

    private CommonGPlayHelper() {
    }

    public static void openMarket(Context context, String packageName) {
        try {
            context.startActivity((new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + packageName))).addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));
        } catch (Exception ex) {
            Common.log(ex);
            Common.showToast("Could not launch the market application.");
        }
    }
}
