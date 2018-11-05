package zame.game.libs;

import android.content.Context;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

public class SeekBarPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener {

    private static final int VALUE_TEXT_SIZE = 32;

    private SeekBar mSeekBar;
    private TextView mValueText;

    @Override
    @SuppressWarnings("deprecation")
    protected View onCreateDialogView(Context context) {
        SeekBarPreference pref = (SeekBarPreference)getPreference();

        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        mValueText = new TextView(context);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(VALUE_TEXT_SIZE);

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        layout.addView(mValueText, params);

        mSeekBar = new SeekBar(context);
        mSeekBar.setOnSeekBarChangeListener(this);

        layout.addView(mSeekBar,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        mSeekBar.setMax(pref.mMax - pref.mMin);
        mSeekBar.setProgress(pref.mValue - pref.mMin);
        mValueText.setText(String.valueOf(pref.mValue));

        return layout;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        SeekBarPreference pref = (SeekBarPreference)getPreference();
        mSeekBar.setMax(pref.mMax - pref.mMin);
        mSeekBar.setProgress(pref.mValue - pref.mMin);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult && (mSeekBar != null)) {
            SeekBarPreference pref = (SeekBarPreference)getPreference();
            pref.mValue = mSeekBar.getProgress() + pref.mMin;

            getPreference().setSummary(String.format(Locale.US,
                    pref.mSummary,
                    String.valueOf(pref.mValue),
                    String.valueOf(pref.mMax)));

            if (pref.shouldPersist()) {
                pref.persistInt(pref.mValue);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        SeekBarPreference pref = (SeekBarPreference)getPreference();
        value += pref.mMin;

        mValueText.setText(String.valueOf(value));
        getPreference().callChangeListener(value);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seek) {}

    @Override
    public void onStopTrackingTouch(SeekBar seek) {}
}
