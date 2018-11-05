package zame.game.fragments.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import zame.game.App;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.engine.Engine;
import zame.game.managers.SoundManager;

public class GameCodeDialogFragment extends BaseDialogFragment {
    protected MainActivity activity;
    protected Engine engine;

    public static GameCodeDialogFragment newInstance() {
        return new GameCodeDialogFragment();
    }

    public GameCodeDialogFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.activity = (MainActivity)context;
        this.engine = this.activity.engine;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        final ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_game_code, null);

        return new AlertDialog.Builder(activity).setIcon(R.drawable.ic_dialog_alert)
                .setTitle(R.string.dlg_enter_code)
                .setView(viewGroup)
                .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText editText = viewGroup.findViewById(R.id.code);
                        engine.game.unprocessedGameCode = editText.getText().toString();
                        App.self.trackerInst.send("CodeEntered", engine.game.unprocessedGameCode);
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
        return SoundManager.FOCUS_MASK_GAME_CODE_DIALOG;
    }
}
