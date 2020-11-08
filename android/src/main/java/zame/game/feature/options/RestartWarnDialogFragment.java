package zame.game.feature.options;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import zame.game.App;
import zame.game.R;
import zame.game.core.app.BaseDialogFragment;
import zame.game.feature.config.EventsConfig;
import zame.game.feature.sound.SoundManager;

public class RestartWarnDialogFragment extends BaseDialogFragment {
    public static RestartWarnDialogFragment newInstance() {
        return new RestartWarnDialogFragment();
    }

    public RestartWarnDialogFragment() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getContext() != null;

        return new AlertDialog.Builder(getContext()).setIcon(R.drawable.ic_dialog_alert)
                .setMessage(R.string.options_restart_warn)
                .setPositiveButton(R.string.core_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (isForgottenFragment) {
                            return;
                        }

                        App.self.tracker.trackEvent(EventsConfig.EV_OPTIONS_RESTART_CONFIRMED);

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
