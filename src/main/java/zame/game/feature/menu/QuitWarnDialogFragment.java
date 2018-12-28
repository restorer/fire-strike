package zame.game.feature.menu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import zame.game.feature.main.MainActivity;
import zame.game.R;
import zame.game.core.app.BaseDialogFragment;
import zame.game.feature.sound.SoundManager;

public class QuitWarnDialogFragment extends BaseDialogFragment {
    public static QuitWarnDialogFragment newInstance() {
        return new QuitWarnDialogFragment();
    }

    private MainActivity activity;

    public QuitWarnDialogFragment() {
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
                .setTitle(R.string.game_quit_warn)
                .setPositiveButton(R.string.menu_quit_warn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        shouldSoundPauseInstantlyOnDismiss = true;
                        activity.quitGame();
                    }
                })
                .setNegativeButton(R.string.menu_quit_warn_no, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        soundManager.setPlaylist(SoundManager.LIST_MAIN);
    }

    @Override
    public int getFocusMask() {
        return SoundManager.FOCUS_MASK_QUIT_WARN_DIALOG;
    }
}
