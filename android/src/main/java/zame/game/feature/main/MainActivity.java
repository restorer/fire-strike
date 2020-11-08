package zame.game.feature.main;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.eightsines.esmediadtor.MediadtorListener;

import zame.game.App;
import zame.game.R;
import zame.game.core.app.ActionBarFragment;
import zame.game.core.app.BackPressedHandler;
import zame.game.core.app.BaseActivity;
import zame.game.core.app.BaseFragment;
import zame.game.core.util.Common;
import zame.game.engine.Engine;
import zame.game.engine.state.State;
import zame.game.feature.achievements.AchievementsFragment;
import zame.game.feature.consent.ConsentChooserFragment;
import zame.game.feature.game.GameFragment;
import zame.game.feature.game.SelectEpisodeFragment;
import zame.game.feature.menu.MenuFragment;
import zame.game.feature.menu.QuitWarnDialogFragment;
import zame.game.feature.options.OptionsFragment;
import zame.game.feature.prepare.CachedTexturesProvider;
import zame.game.feature.prepare.PrepareFragment;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.AppConfig;
import zame.game.flavour.gplay.MainActivityGPlayHelper;

public class MainActivity extends BaseActivity {
    private static final String TAG_FRAGMENT_CURRENT = "MainActivity.CurrentFragment";

    public SoundManager soundManager;
    public Engine engine;

    private PrepareFragment prepareFragment;
    private ConsentChooserFragment consentChooserFragment;
    public MenuFragment menuFragment;
    public OptionsFragment optionsFragment;
    public AchievementsFragment achievementsFragment;
    public SelectEpisodeFragment selectEpisodeFragment;
    public GameFragment gameFragment;
    private QuitWarnDialogFragment quitWarnDialogFragment;

    private Fragment currentFragment;
    @SuppressWarnings("FieldCanBeLocal") private Fragment prevFragment;
    private ActionBarFragment prevActionBarFragment;
    private final Handler handler = new Handler();

    private final MainActivityGPlayHelper gPlayHelper = new MainActivityGPlayHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        App.self.tracker.onActivityCreate(this);

        soundManager = SoundManager.getInstance(false);
        soundManager.initialize();

        engine = new Engine(this);

        prepareFragment = PrepareFragment.newInstance();
        consentChooserFragment = ConsentChooserFragment.newInstance();
        menuFragment = MenuFragment.newInstance();
        optionsFragment = OptionsFragment.newInstance();
        achievementsFragment = AchievementsFragment.newInstance();
        selectEpisodeFragment = SelectEpisodeFragment.newInstance();
        gameFragment = GameFragment.newInstance();
        quitWarnDialogFragment = QuitWarnDialogFragment.newInstance();

        gPlayHelper.onCreate();
        processNext();
    }

    public void processNext() {
        if (App.self.cachedTexturesTask != null || CachedTexturesProvider.needToUpdateCache()) {
            showFragment(prepareFragment);
            return;
        }

        if (AppConfig.SHOULD_ASK_CONSENT && !App.self.preferences.getBoolean(R.string.key_is_consent_chosen)) {
            showFragment(consentChooserFragment);
            return;
        }

        showFragment(menuFragment);

        App.self.mediadtor.onActivityCreate(this, new MediadtorListener() {
            @Override
            public void onRewardedVideoClosed(boolean shouldGiveReward) {
                engine.onRewardedVideoClosed(shouldGiveReward);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        App.self.tracker.onActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.self.mediadtor.onActivityResume(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        soundManager.onWindowFocusChanged(hasFocus, SoundManager.FOCUS_MASK_MAIN_ACTIVITY);

        if ((currentFragment instanceof BaseFragment) && currentFragment.isVisible()) {
            ((BaseFragment)currentFragment).onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    protected void onPause() {
        if (soundManager != null) {
            soundManager.onWindowFocusChanged(false, SoundManager.FOCUS_MASK_MAIN_ACTIVITY);
        }

        App.self.mediadtor.onActivityPause();
        App.self.tracker.onActivityPause(this);

        // if home screen button_positive pressed, lock screen button_positive pressed, call received or something similar - return to menu screen
        //
        // if (currentFragment == gameFragment) {
        // 	gameFragment.hideDialogs();
        // 	showFragment(menuFragment);
        // }

        super.onPause();
    }

    @Override
    protected void onStop() {
        App.self.tracker.onActivityStop(this);
        super.onStop();
    }

    public void showFragment(final Fragment fragment) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showFragmentInternal(fragment);
            }
        });
    }

    private void showFragmentInternal(Fragment fragment) {
        if (fragment == currentFragment) {
            return;
        }

        if (currentFragment == optionsFragment) {
            engine.config.reload();
        }

        prevFragment = currentFragment;
        currentFragment = fragment;

        try {
            getSupportFragmentManager().executePendingTransactions(); // fix for black-screen issue
        } catch (Exception ex) {
            Common.log(ex.toString());
            recreate();
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, TAG_FRAGMENT_CURRENT);

        // TRANSIT_UNSET and TRANSIT_NONE causes bug with some cyanogenmod roms (fragment not changed)
        // TRANSIT_FRAGMENT_FADE + WebView (inside PromoView) = screen flashes during fragment change
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        try {
            transaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        } catch (Exception ex) {
            Common.log(ex.toString());
            recreate();
        }
    }

    public void setupTabs() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar == null) {
            return; // don't know how this is possible, but just for case
        }

        try {
            if (currentFragment instanceof ActionBarFragment) {
                if (currentFragment != prevActionBarFragment) {
                    //noinspection deprecation
                    actionBar.removeAllTabs();

                    try {
                        ((ActionBarFragment)currentFragment).setupTabs(actionBar);
                    } catch (Exception ex) {
                        // java.lang.IllegalStateException: Fragment ao{40866688} not attached to Activity
                        Common.log(ex);
                    }
                }

                prevActionBarFragment = (ActionBarFragment)currentFragment;

                actionBar.setDisplayOptions(0);

                //noinspection deprecation
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

                actionBar.show();
            } else {
                actionBar.hide();
            }
        } catch (Exception ex) {
            Common.log(ex);
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void showPrevFragment() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (prevFragment == null || prevFragment == currentFragment) {
                    showFragmentInternal(menuFragment);
                } else {
                    showFragmentInternal(prevFragment);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (currentFragment == prepareFragment) {
            return;
        }

        if (currentFragment == consentChooserFragment) {
            quitGame();
            return;
        }

        if ((currentFragment instanceof BackPressedHandler)
                && currentFragment.isVisible()
                && ((BackPressedHandler)currentFragment).onBackPressed()) {

            return;
        }

        if (currentFragment != null && currentFragment != menuFragment) {
            showFragment(menuFragment);
            return;
        }

        if (!gPlayHelper.onBackPressed(this)) {
            return;
        }

        quitWarnDialogFragment.show(getSupportFragmentManager());
    }

    public void quitGame() {
        gPlayHelper.quitGame();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (soundManager != null) {
            soundManager.shutdown();
        }

        super.onDestroy();
    }

    public boolean tryAndLoadInstantState() {
        if (!engine.hasInstantSave()) {
            engine.state.reload();
            return false;
        }

        engine.state.reload();
        return (engine.state.load(engine.instantName) == State.LOAD_RESULT_SUCCESS);
    }

    public void continueGame() {
        engine.game.savedGameParam = (engine.hasInstantSave() ? engine.instantName : "");
        showFragment(gameFragment);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        gPlayHelper.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
