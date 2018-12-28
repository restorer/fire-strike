package zame.game.feature.consent;

import android.os.Bundle;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
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
