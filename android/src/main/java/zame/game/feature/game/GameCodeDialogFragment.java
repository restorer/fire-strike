package zame.game.feature.game;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import zame.game.App;
import zame.game.R;
import zame.game.core.app.BaseDialogFragment;
import zame.game.engine.Engine;
import zame.game.feature.config.EventsConfig;
import zame.game.feature.sound.SoundManager;

public class GameCodeDialogFragment extends BaseDialogFragment {
    public static GameCodeDialogFragment newInstance() {
        return new GameCodeDialogFragment();
    }

    private Engine engine;

    public GameCodeDialogFragment() {
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (!isForgottenFragment) {
            this.engine = this.activity.engine;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getContext() != null;

        @SuppressLint("InflateParams") final ViewGroup viewGroup =
                (ViewGroup)LayoutInflater.from(getContext()).inflate(R.layout.game_dialog_code, null);

        return new AlertDialog.Builder(getContext()).setIcon(R.drawable.ic_dialog_alert)
                .setTitle(R.string.game_enter_code)
                .setView(viewGroup)
                .setPositiveButton(R.string.core_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (isForgottenFragment) {
                            return;
                        }

                        EditText editText = viewGroup.findViewById(R.id.code);
                        engine.game.unprocessedGameCode = editText.getText().toString();
                        App.self.tracker.trackEvent(EventsConfig.EV_GAME_CODE_ENTERED, engine.game.unprocessedGameCode);
                    }
                })
                .setNegativeButton(R.string.core_cancel, null)
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
