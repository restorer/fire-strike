package zame.game.feature.options;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import zame.game.App;
import zame.game.feature.main.MainActivity;
import zame.game.R;
import zame.game.core.app.BaseDialogFragment;
import zame.game.feature.sound.SoundManager;

public class RestartWarnDialogFragment extends BaseDialogFragment {
    public static RestartWarnDialogFragment newInstance() {
        return new RestartWarnDialogFragment();
    }

    private MainActivity activity;

    public RestartWarnDialogFragment() {
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
        return new AlertDialog.Builder(activity).setIcon(R.drawable.ic_dialog_alert)
                .setMessage(R.string.options_restart_warn)
                .setPositiveButton(R.string.core_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        App.self.tracker.trackEvent("RestartPressed", "");

                        activity.engine.deleteInstantSave();
                        App.self.profile.clear(activity);
                        App.self.profile.save(activity);

                        activity.showFragment(activity.selectEpisodeFragment);
                    }
                })
                .setNegativeButton(R.string.core_cancel, null)
                .create();
    }

    // @Override
    // public void onStart() {
    //     super.onStart();
    //     soundManager.setPlaylist(SoundManager.LIST_MAIN);
    // }

    @Override
    public int getFocusMask() {
        return SoundManager.FOCUS_MASK_RESTART_WARN_DIALOG;
    }
}
