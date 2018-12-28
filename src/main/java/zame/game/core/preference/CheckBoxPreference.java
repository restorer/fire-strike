package zame.game.core.preference;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

public class CheckBoxPreference extends android.support.v7.preference.CheckBoxPreference {
    public CheckBoxPreference(Context context) {
        super(context);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        final TextView summaryView = (TextView)holder.findViewById(android.R.id.summary);

        if (summaryView != null) {
            summaryView.setMaxLines(Integer.MAX_VALUE);
        }

        super.onBindViewHolder(holder);
    }
}
