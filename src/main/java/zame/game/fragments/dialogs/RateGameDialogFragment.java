package zame.game.fragments.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Locale;
import zame.game.Common;
import zame.game.CommonGPlayHelper;
import zame.game.MainActivity;
import zame.game.App;
import zame.game.R;
import zame.game.managers.SoundManager;

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
        final ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_rate_game, null);

        viewGroup.findViewById(R.id.like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shouldSoundPauseInstantlyOnDismiss = true;
                App.self.getSharedPreferences().edit().putBoolean("RateAtLeastOnce", true).apply();
                App.self.trackerInst.send("Rate", "Like");
                CommonGPlayHelper.openMarket(activity, App.self.getPackageName());

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
                App.self.getSharedPreferences().edit().putBoolean("RateAtLeastOnce", true).apply();

                App.self.trackerInst.send("Rate", "Dislike");

                Common.openBrowser(activity,
                        CommonGPlayHelper.RATE_DISLIKE_LINK + Locale.getDefault().getLanguage().toLowerCase());

                if (shownOnQuit) {
                    activity.quitGame();
                } else {
                    RateGameDialogFragment.this.dismiss();
                }
            }
        });

        App.self.trackerInst.send("Rate", "Show");

        return new AlertDialog.Builder(activity).setIcon(R.drawable.ic_dialog_alert)
                .setTitle(R.string.dlg_rate_title)
                .setView(viewGroup)
                .setPositiveButton(shownOnQuit ? R.string.dlg_rate_quit : R.string.dlg_cancel,
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

        // set QuitWithoutRate on show, because user can hide dialog using "Back" button
        App.self.getSharedPreferences().edit().putBoolean("QuitWithoutRate", true).apply();
    }

    @Override
    public int getFocusMask() {
        return SoundManager.FOCUS_MASK_RATE_GAME_DIALOG;
    }
}
