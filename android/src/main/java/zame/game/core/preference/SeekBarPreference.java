/*
 * The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * http://android.hlidskialf.com/blog/code/android-seekbar-preference
 *
 * Modified by restorer (added/fixed min parameter, fixed "Cancel" behaviour)
 *
 */

package zame.game.core.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.Locale;

import zame.game.R;

public class SeekBarPreference extends DialogPreference {
    static {
        PreferenceFragmentCompat.registerPreferenceFragment(
                SeekBarPreference.class,
                SeekBarPreferenceDialogFragmentCompat.class);
    }

    static final String SUMMARY_FORMAT = "%s/%s";

    private int min;
    private int max;
    private int progress;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray app = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
        min = app.getInt(R.styleable.SeekBarPreference_xmin, 0);
        max = app.getInt(R.styleable.SeekBarPreference_xmax, 100);
        app.recycle();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        progress = getPersistedInt(defaultValue == null ? progress : (Integer)defaultValue);

        if (shouldPersist()) {
            persistInt(progress);
        }

        setSummary(String.format(Locale.US, SUMMARY_FORMAT, progress, max));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        progress = (restore ? getPersistedInt(progress) : ((defaultValue == null) ? progress : (Integer)defaultValue));

        if (!restore && shouldPersist()) {
            persistInt(progress);
        }

        setSummary(String.format(Locale.US, SUMMARY_FORMAT, progress, max));
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMin() {
        return min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public boolean shouldPersist() {
        return super.shouldPersist();
    }

    @Override
    public boolean persistInt(int value) {
        return super.persistInt(value);
    }
}
