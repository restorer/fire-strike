package zame.game.core.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import zame.game.R;
import zame.game.core.util.Common;

public class KeyMapPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private static final int HELP_TEXT_SIZE = 16;

    @Override
    @SuppressWarnings("deprecation")
    protected View onCreateDialogView(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        TextView text = new TextView(context);
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        text.setTextSize(HELP_TEXT_SIZE);
        text.setText(R.string.core_preference_press_key);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        layout.addView(text, params);
        return layout;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (Common.canUseKey(keyCode)) {
                    KeyMapPreference pref = (KeyMapPreference)getPreference();

                    pref.setValue(keyCode);
                    pref.updateSummary();

                    if (pref.shouldPersist()) {
                        pref.persistInt(pref.getValue());
                    }

                    dialog.dismiss();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            KeyMapPreference pref = (KeyMapPreference)getPreference();

            pref.setValue(0);
            pref.updateSummary();

            if (pref.shouldPersist()) {
                pref.persistInt(pref.getValue());
            }
        }
    }
}
