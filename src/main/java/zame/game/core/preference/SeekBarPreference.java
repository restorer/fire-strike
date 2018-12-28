/*
 * The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * http://android.hlidskialf.com/blog/code/android-seekbar-preference
 * http://www.bryandenny.com/index.php/2010/05/25/what-i-learned-from-writing-my-first-android-application/
 *
 * Modified by restorer (added/fixed min parameter, fixed "Cancel" behaviour)
 *
 */

package zame.game.core.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import java.util.Locale;
import zame.game.R;

public class SeekBarPreference extends DialogPreference {
    static {
        PreferenceFragmentCompat.registerPreferenceFragment(SeekBarPreference.class,
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
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);

        progress = (restore ? getPersistedInt(progress) : ((defaultValue == null) ? progress : (Integer)defaultValue));

        if (!restore && shouldPersist()) {
            persistInt(progress);
        }

        setSummary(String.format(Locale.US, SUMMARY_FORMAT, String.valueOf(progress), String.valueOf(max)));
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
