package zame.game.feature.options;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import com.jakewharton.processphoenix.ProcessPhoenix;
import zame.game.R;
import zame.game.core.app.BaseDialogFragment;
import zame.game.feature.main.MainActivity;
import zame.game.feature.sound.SoundManager;

public class ConsentForAdsChangedDialogFragment extends BaseDialogFragment {
    public static ConsentForAdsChangedDialogFragment newInstance() {
        ConsentForAdsChangedDialogFragment dialogFragment = new ConsentForAdsChangedDialogFragment();
        dialogFragment.setCancelable(false);
        return dialogFragment;
    }

    private MainActivity activity;

    public ConsentForAdsChangedDialogFragment() {
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
                .setMessage(R.string.options_consent_for_ads_changed)
                .setPositiveButton(R.string.core_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ProcessPhoenix.triggerRebirth(activity);
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
