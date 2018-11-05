package zame.game.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import zame.game.MainActivity;
import zame.game.App;
import zame.game.R;
import zame.game.managers.SoundManager;

public class DeleteProfileDialogFragment extends BaseDialogFragment {
    protected MainActivity activity;

    public static DeleteProfileDialogFragment newInstance() {
        return new DeleteProfileDialogFragment();
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
                .setTitle(R.string.dlg_delete_profile)
                .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        App.self.profile.clear();
                        App.self.profile.save();
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
        return SoundManager.FOCUS_MASK_DELETE_PROFILE_DIALOG;
    }
}
