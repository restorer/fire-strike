package zame.game.feature.consent;

import android.os.Bundle;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import zame.game.R;

public class ConsentPreferencesFragment extends PreferenceFragmentCompat {
    public ConsentPreferencesFragment() {
        super();
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_consent);
    }
}
