package zame.game.core.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

public class CheckBoxPreference extends androidx.preference.CheckBoxPreference {
    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public CheckBoxPreference(Context context) {
        super(context);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public CheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
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
