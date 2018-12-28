package zame.game.core.app;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import zame.game.core.manager.WindowCallbackManager;
import zame.game.core.util.Common;
import zame.game.feature.sound.SoundManager;

public abstract class BaseDialogFragment extends DialogFragment {
    private Window window;

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

        if (getDialog() == null) {
            return;
        }

        if (isCancelable()) {
            getDialog().setCanceledOnTouchOutside(true);
        }

        if (window == getDialog().getWindow()) {
            return;
        }

        window = getDialog().getWindow();

        if (window == null) {
            return;
        }

        WindowCallbackManager.attachWindowCallback(window, soundManager, getFocusMask());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = window.getDecorView();

            if (decorView != null) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
