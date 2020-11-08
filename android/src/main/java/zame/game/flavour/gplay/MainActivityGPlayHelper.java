package zame.game.flavour.gplay;

import android.content.Intent;

import zame.game.App;
import zame.game.R;
import zame.game.feature.main.MainActivity;
import zame.game.flavour.config.AppConfig;

public class MainActivityGPlayHelper {
    private RateGameDialogFragment rateGameDialogFragment;
    @SuppressWarnings("BooleanVariableAlwaysNegated") private boolean rateGameDialogShown;

    public void onCreate() {
        if (AppConfig.SHOULD_ASK_RATE) {
            rateGameDialogFragment = RateGameDialogFragment.newInstance(true);
            rateGameDialogShown = false;
        }
    }

    public boolean onBackPressed(MainActivity activity) {
        if (AppConfig.SHOULD_ASK_RATE
                && !rateGameDialogShown
                && App.self.preferences.getInt(R.string.key_quit_count) == 3 - 1) {

            rateGameDialogShown = true;
            rateGameDialogFragment.show(activity.getSupportFragmentManager());
            return false;
        }

        return true;
    }

    public void quitGame() {
        if (AppConfig.SHOULD_ASK_RATE && App.self.preferences.getBoolean(R.string.key_is_consent_chosen)) {
            App.self.preferences.putInt(
                    R.string.key_quit_count,
                    App.self.preferences.getInt(R.string.key_quit_count) + 1);
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void onActivityResult(MainActivity activity, int requestCode, int resultCode, Intent data) {
    }
}
