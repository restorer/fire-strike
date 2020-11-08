package zame.game.feature.options;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.takisoft.preferencex.PreferenceFragmentCompat;

import java.util.Locale;

import zame.game.App;
import zame.game.R;
import zame.game.core.app.BackPressedHandler;
import zame.game.core.util.Common;
import zame.game.feature.config.EventsConfig;
import zame.game.feature.main.MainActivity;
import zame.game.feature.web.GeneralWebActivity;
import zame.game.flavour.config.AppConfig;

public class OptionsFragment extends PreferenceFragmentCompat
        implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback, BackPressedHandler {

    public static OptionsFragment newInstance() {
        return new OptionsFragment();
    }

    private MainActivity activity;
    private PreferenceScreen rootPreferenceScreen;
    private RestartWarnDialogFragment restartWarnDialogFragment;
    private ConsentForAdsChangedDialogFragment consentForAdsChangedDialogFragment;

    public OptionsFragment() {
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (MainActivity)context;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.soundManager.setPlaylist(null, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        rootPreferenceScreen = getPreferenceScreen();
        restartWarnDialogFragment = RestartWarnDialogFragment.newInstance();
        consentForAdsChangedDialogFragment = ConsentForAdsChangedDialogFragment.newInstance();

        final String currentLanguage = Locale.getDefault().getLanguage().toLowerCase(Locale.US);

        findPreference(getString(R.string.key_consent_category)).setVisible(AppConfig.SHOULD_ASK_CONSENT);

        findPreference(getString(R.string.key_language)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                App.self.cachedTypeface = null;
                activity.recreate();
                return true;
            }
        });

        findPreference(getString(R.string.key_help)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                App.self.tracker.trackEvent(EventsConfig.EV_OPTIONS_HELP_REQUESTED, currentLanguage);

                Intent intent = new Intent(activity, GeneralWebActivity.class);
                intent.putExtra(GeneralWebActivity.EXTRA_URL, AppConfig.LINK_HELP + currentLanguage);
                activity.startActivity(intent);

                return true;
            }
        });

        findPreference(getString(R.string.key_about)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(activity, GeneralWebActivity.class);

                intent.putExtra(
                        GeneralWebActivity.EXTRA_URL,
                        "file:///android_asset/web/about.html?language="
                                + Common.urlEncode(currentLanguage)
                                + "&appName="
                                + Common.urlEncode(getString(R.string.core_app_name))
                                + "&appVersion="
                                + Common.urlEncode(App.self.getVersionName()));

                activity.startActivity(intent);
                return true;
            }
        });

        findPreference(getString(R.string.key_restart)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restartWarnDialogFragment.show(getFragmentManager());
                return true;
            }
        });

        // findPreference(getString(R.string.key_enable_sound)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        //     @Override
        //     public boolean onPreferenceChange(Preference preference, Object newValue) {
        //         activity.soundManager.setSoundEnabledSetting((Boolean)newValue);
        //         activity.soundManager.onSettingsUpdated();
        //         return true;
        //     }
        // });

        // findPreference(getString(R.string.key_music_volume)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        //     @Override
        //     public boolean onPreferenceChange(Preference preference, Object newValue) {
        //         activity.soundManager.setMusicVolumeSetting((Integer)newValue);
        //         activity.soundManager.onSettingsUpdated();
        //         return true;
        //     }
        // });

        // findPreference(getString(R.string.key_effects_volume)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        //     @Override
        //     public boolean onPreferenceChange(Preference preference, Object newValue) {
        //         activity.soundManager.setEffectsVolumeSetting((Integer)newValue);
        //         activity.soundManager.onSettingsUpdated();
        //         return true;
        //     }
        // });

        findPreference(getString(R.string.key_rotate_screen)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((zame.game.core.widget.FrameLayout)activity.findViewById(R.id.root_container)).updateRotateScreen((Boolean)newValue);
                return true;
            }
        });

        findPreference(getString(R.string.key_consent_crashes)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                App.self.tracker.setCrashesConsent((Boolean)newValue);
                return true;
            }
        });

        findPreference(getString(R.string.key_consent_analytics)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                App.self.tracker.setAnalyticsConsent((Boolean)newValue);
                return true;
            }
        });

        findPreference(getString(R.string.key_consent_ad_personalization)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                consentForAdsChangedDialogFragment.show(getFragmentManager());
                return true;
            }
        });

        if (AppConfig.DEBUG) {
            findPreference(getString(R.string.key_debug_crash)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    throw new RuntimeException("Debug crash");
                }
            });
        } else {
            findPreference(getString(R.string.key_debug_crash)).setVisible(false);
        }
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceStartScreen(
            androidx.preference.PreferenceFragmentCompat caller,
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
