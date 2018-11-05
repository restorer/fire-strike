package zame.game.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import zame.game.App;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.managers.SoundManager;

public class RestartWarnDialogFragment extends BaseDialogFragment {
    protected MainActivity activity;

    public static RestartWarnDialogFragment newInstance() {
        return new RestartWarnDialogFragment();
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
                .setTitle(R.string.dlg_new_game)
                .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        App.self.trackerInst.send("RestartPressed", "");
                        activity.engine.deleteInstantSave();
                        activity.showFragment(activity.selectEpisodeFragment);
                    }
                })
                .setNegativeButton(R.string.dlg_cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        soundManager.setPlaylist(SoundManager.LIST_MAIN);
    }

    @Override
    public int getFocusMask() {
        return SoundManager.FOCUS_MASK_RESTART_WARN_DIALOG;
    }
}
