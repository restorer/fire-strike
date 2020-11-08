package zame.game.flavour.gplay;

import android.view.View;
import android.view.ViewGroup;

import zame.game.App;
import zame.game.R;
import zame.game.core.manager.PreferencesManager;
import zame.game.feature.config.EventsConfig;
import zame.game.feature.main.MainActivity;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.AppConfig;

public class MenuFragmentGPlayHelper {
    private RateGameDialogFragment rateGameDialogFragment;
    private ViewGroup rateGameWrap;

    public void onCreate() {
        if (AppConfig.SHOULD_ASK_RATE) {
            rateGameDialogFragment = RateGameDialogFragment.newInstance(false);
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void createFragmentView(ViewGroup viewGroup, final MainActivity activity) {
        if (!AppConfig.SHOULD_ASK_RATE) {
            return;
        }

        rateGameWrap = viewGroup.findViewById(R.id.rate_game_wrap);

        rateGameWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                App.self.tracker.trackEvent(EventsConfig.EV_MENU_RATE_BAR_PRESSED);
                rateGameDialogFragment.show(activity.getSupportFragmentManager());
            }
        });

        updateRateWrapVisibility();
    }

    public void updateRateWrapVisibility() {
        if (!AppConfig.SHOULD_ASK_RATE) {
            return;
        }

        PreferencesManager preferences = App.self.preferences;

        if (preferences.getBoolean(R.string.flavour_key_quit_without_rate)
                && !preferences.getBoolean(R.string.flavour_key_rate_at_least_once)) {

            rateGameWrap.setVisibility(View.VISIBLE);
        } else {
            rateGameWrap.setVisibility(View.GONE);
         }
    }
}
