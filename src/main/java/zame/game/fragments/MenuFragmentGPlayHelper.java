package zame.game.fragments;

import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import zame.game.MainActivity;
import zame.game.App;
import zame.game.R;
import zame.game.fragments.dialogs.RateGameDialogFragment;
import zame.game.managers.SoundManager;

class MenuFragmentGPlayHelper {
    private RateGameDialogFragment rateGameDialogFragment;
    private ViewGroup rateGameWrap;

    void onCreate() {
        rateGameDialogFragment = RateGameDialogFragment.newInstance(false);
    }

    void createFragmentView(ViewGroup viewGroup, final MainActivity activity) {
        rateGameWrap = viewGroup.findViewById(R.id.rate_game_wrap);

        rateGameWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                App.self.trackerInst.send("BarLine.Rate");
                rateGameDialogFragment.show(activity.getSupportFragmentManager());
            }
        });

        updateRateWrapVisibility();
    }

    void updateRateWrapVisibility() {
        SharedPreferences sp = App.self.getSharedPreferences();

        if (sp.getBoolean("QuitWithoutRate", false) && !sp.getBoolean("RateAtLeastOnce", false)) {
            rateGameWrap.setVisibility(View.VISIBLE);
        } else {
            rateGameWrap.setVisibility(View.GONE);
        }
    }
}
