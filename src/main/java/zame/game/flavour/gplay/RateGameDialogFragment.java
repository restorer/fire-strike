package zame.game.flavour.gplay;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Locale;
import zame.game.App;
import zame.game.R;
import zame.game.core.app.BaseDialogFragment;
import zame.game.core.util.Common;
import zame.game.feature.main.MainActivity;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.AppConfig;

public class RateGameDialogFragment extends BaseDialogFragment {
    private static final String KEY_SHOWN_ON_QUIT = "shownOnQuit";

    private MainActivity activity;

    public static RateGameDialogFragment newInstance(boolean shownOnQuit) {
        RateGameDialogFragment fragment = new RateGameDialogFragment();

        Bundle args = new Bundle();
        args.putBoolean(KEY_SHOWN_ON_QUIT, shownOnQuit);
        fragment.setArguments(args);

        return fragment;
    }

    public RateGameDialogFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity)context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //noinspection ConstantConditions
        final boolean shownOnQuit = getArguments().getBoolean(KEY_SHOWN_ON_QUIT);

        @SuppressLint("InflateParams")
        final ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity)
                .inflate(R.layout.flavour_dialog_rate_game, null);

        viewGroup.findViewById(R.id.like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldSoundPauseInstantlyOnDismiss = true;
                App.self.preferences.putBoolean(R.string.flavour_key_rate_at_least_once, true);
                App.self.tracker.trackEvent("Rate", "Like");
                openMarket(activity, App.self.getPackageName());

                if (shownOnQuit) {
                    activity.quitGame();
                } else {
                    RateGameDialogFragment.this.dismiss();
                }
            }
        });

        viewGroup.findViewById(R.id.dislike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldSoundPauseInstantlyOnDismiss = true;
                App.self.preferences.putBoolean(R.string.flavour_key_rate_at_least_once, true);
                App.self.tracker.trackEvent("Rate", "Dislike");

                Common.openBrowser(activity,
                        AppConfig.LINK_DISLIKE + Locale.getDefault().getLanguage().toLowerCase(Locale.US));

                if (shownOnQuit) {
                    activity.quitGame();
                } else {
                    RateGameDialogFragment.this.dismiss();
                }
            }
        });

        App.self.tracker.trackEvent("Rate", "Show");

        return new AlertDialog.Builder(activity).setIcon(R.drawable.ic_dialog_alert)
                .setTitle(R.string.flavour_rate_game_title)
                .setView(viewGroup)
                .setPositiveButton(shownOnQuit ? R.string.flavour_rate_game_quit : R.string.core_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (shownOnQuit) {
                                    shouldSoundPauseInstantlyOnDismiss = true;
                                    activity.quitGame();
                                }
                            }
                        })
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        soundManager.setPlaylist(SoundManager.LIST_MAIN);

        // set QuitWithoutRate on show, because user can hide dialog using "Back" button_positive
        App.self.preferences.putBoolean(R.string.flavour_key_quit_without_rate, true);
    }

    @Override
    public int getFocusMask() {
        return SoundManager.FOCUS_MASK_RATE_GAME_DIALOG;
    }

    private static void openMarket(Context context, String packageName) {
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
