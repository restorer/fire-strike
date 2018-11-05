package zame.game;

import android.content.Intent;
import android.content.SharedPreferences;
import zame.game.fragments.dialogs.RateGameDialogFragment;

class MainActivityGPlayHelper {
    private RateGameDialogFragment rateGameDialogFragment;
    @SuppressWarnings("BooleanVariableAlwaysNegated") private boolean rateGameDialogShown;

    void onCreate() {
        rateGameDialogFragment = RateGameDialogFragment.newInstance(true);
        rateGameDialogShown = false;
    }

    boolean onBackPressed(MainActivity activity) {
        SharedPreferences sp = App.self.getSharedPreferences();

        if (!rateGameDialogShown && sp.getInt("QuitCount", 0) == 3 - 1) {
            rateGameDialogShown = true;
            rateGameDialogFragment.show(activity.getSupportFragmentManager());
            return false;
        }

        return true;
    }

    void quitGame() {
        SharedPreferences sp = App.self.getSharedPreferences();
        sp.edit().putInt("QuitCount", sp.getInt("QuitCount", 0) + 1).apply();
    }

    @SuppressWarnings("unused")
    void onActivityResult(MainActivity activity, int requestCode, int resultCode, Intent data) {
    }
}
