package zame.game.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import java.util.Locale;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.App;
import zame.game.R;
import zame.game.fragments.dialogs.DeleteProfileDialogFragment;
import zame.game.fragments.dialogs.RestartWarnDialogFragment;
import zame.game.misc.GeneralWebActivity;

public class OptionsFragment extends PreferenceFragmentCompat
        implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback, MainActivity.BackPressedHandler {

    private MainActivity activity;
    private RestartWarnDialogFragment restartWarnDialogFragment;
    private DeleteProfileDialogFragment deleteProfileDialogFragment;
    private PreferenceScreen rootPreferenceScreen;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity)context;
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        rootPreferenceScreen = getPreferenceScreen();

        restartWarnDialogFragment = RestartWarnDialogFragment.newInstance();
        deleteProfileDialogFragment = DeleteProfileDialogFragment.newInstance();

        findPreference("About").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(activity, GeneralWebActivity.class);
                intent.putExtra(GeneralWebActivity.EXTRA_URL,
                        Common.getLocalizedAssetPath(activity.getAssets(), "web/about%s.html")
                                + "?appName="
                                + Common.urlEncode(getString(R.string.app_name))
                                + "&ver="
                                + Common.urlEncode(App.self.getVersionName()));

                activity.startActivity(intent);
                return true;
            }
        });

        findPreference("Help").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String lang = Locale.getDefault().getLanguage().toLowerCase();
                App.self.trackerInst.send("Help", String.valueOf(lang));

                Intent intent = new Intent(activity, GeneralWebActivity.class);
                intent.putExtra(GeneralWebActivity.EXTRA_URL, Common.HELP_LINK + lang);
                activity.startActivity(intent);

                return true;
            }
        });

        findPreference("Restart").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restartWarnDialogFragment.show(getFragmentManager());
                return true;
            }
        });

        findPreference("DeleteProfile").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                deleteProfileDialogFragment.show(getFragmentManager());
                return true;
            }
        });

        findPreference("EnableSound").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity.soundManager.setSoundEnabledSetting((Boolean)newValue);
                activity.soundManager.onSettingsUpdated();
                return true;
            }
        });

        findPreference("MusicVolume").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity.soundManager.setMusicVolumeSetting((Integer)newValue);
                return true;
            }
        });

        findPreference("EffectsVolume").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity.soundManager.setEffectsVolumeSetting((Integer)newValue);
                return true;
            }
        });

        findPreference("RotateScreen").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((zame.game.libs.FrameLayout)activity.findViewById(R.id.root_container)).updateRotateScreen((Boolean)newValue);
                return true;
            }
        });
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceStartScreen(android.support.v7.preference.PreferenceFragmentCompat caller,
            PreferenceScreen pref) {

        caller.setPreferenceScreen(pref);
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (getPreferenceScreen() == rootPreferenceScreen) {
            return false;
        }

        setPreferenceScreen(rootPreferenceScreen);
        return true;
    }
}
