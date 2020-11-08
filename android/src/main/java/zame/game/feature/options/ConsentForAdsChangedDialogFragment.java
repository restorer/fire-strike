package zame.game.feature.options;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.jakewharton.processphoenix.ProcessPhoenix;

import zame.game.R;
import zame.game.core.app.BaseDialogFragment;
import zame.game.feature.sound.SoundManager;

public class ConsentForAdsChangedDialogFragment extends BaseDialogFragment {
    public static ConsentForAdsChangedDialogFragment newInstance() {
        ConsentForAdsChangedDialogFragment dialogFragment = new ConsentForAdsChangedDialogFragment();
        dialogFragment.setCancelable(false);
        return dialogFragment;
    }

    public ConsentForAdsChangedDialogFragment() {
        super();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getContext() != null;

        return new AlertDialog.Builder(getContext()).setIcon(R.drawable.ic_dialog_alert)
                .setMessage(R.string.options_consent_for_ads_changed)
                .setPositiveButton(R.string.core_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (!isForgottenFragment) {
                            ProcessPhoenix.triggerRebirth(activity);
                        }
                    }
                })
                .create();
    }

    // @Override
    // public void onStart() {
    //     super.onStart();
    //     soundManager.setPlaylist(SoundManager.LIST_MAIN);
    // }

    @Override
    public int getFocusMask() {
        return SoundManager.FOCUS_MASK_CONSENT_FOR_ADS_CHANGED_DIALOG;
    }
}
