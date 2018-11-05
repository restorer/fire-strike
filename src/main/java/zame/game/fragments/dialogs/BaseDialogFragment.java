package zame.game.fragments.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import zame.game.Common;
import zame.game.managers.SoundManager;
import zame.game.managers.WindowCallbackManager;

public abstract class BaseDialogFragment extends DialogFragment {
    protected Window window;
    protected SoundManager soundManager;
    protected boolean shouldSoundPauseInstantlyOnDismiss;

    public void show(FragmentManager manager) {
        if (soundManager == null) {
            soundManager = SoundManager.getInstance(false);
        }

        soundManager.instantPause = false;

        try {
            show(manager, null);
        } catch (Exception ex) {
            Common.log(ex);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (soundManager == null) {
            soundManager = SoundManager.getInstance(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(true);

            if (window != getDialog().getWindow()) {
                window = getDialog().getWindow();
                WindowCallbackManager.attachWindowCallback(window, soundManager, getFocusMask());
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        soundManager.instantPause = shouldSoundPauseInstantlyOnDismiss;
        soundManager.onWindowFocusChanged(false, getFocusMask());
    }

    public abstract int getFocusMask();
}
