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

package zame.game.libs;

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

    int mMin;
    int mMax;
    int mValue;
    String mSummary = "%s/%s";

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray app = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
        mMin = app.getInt(R.styleable.SeekBarPreference_xmin, 0);
        mMax = app.getInt(R.styleable.SeekBarPreference_xmax, 100);
        app.recycle();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);

        mValue = (restore ? getPersistedInt(mValue) : ((defaultValue == null) ? mValue : (Integer)defaultValue));

        if (!restore && shouldPersist()) {
            persistInt(mValue);
        }

        setSummary(String.format(Locale.US, mSummary, String.valueOf(mValue), String.valueOf(mMax)));
    }

    public void setMin(int min) {
        mMin = min;
    }

    public int getMin() {
        return mMin;
    }

    public void setMax(int max) {
        mMax = max;
    }

    public int getMax() {
        return mMax;
    }

    public void setProgress(int progress) {
        mValue = progress;
    }

    public int getProgress() {
        return mValue;
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
