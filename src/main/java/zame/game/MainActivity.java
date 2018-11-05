package zame.game;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import zame.game.engine.Engine;
import zame.game.engine.State;
import zame.game.fragments.AchievementsFragment;
import zame.game.fragments.ActionBarFragment;
import zame.game.fragments.BaseFragment;
import zame.game.fragments.GameFragment;
import zame.game.fragments.MenuFragment;
import zame.game.fragments.OptionsFragment;
import zame.game.fragments.PrepareFragment;
import zame.game.fragments.SelectEpisodeFragment;
import zame.game.fragments.dialogs.QuitWarnDialogFragment;
import zame.game.managers.SoundManager;
import zame.game.providers.CachedTexturesProvider;

public class MainActivity extends AppCompatActivity {
    public interface BackPressedHandler {
        boolean onBackPressed();
    }

    private static final String TAG_FRAGMENT_CURRENT = "MainActivity.CurrentFragment";

    public static MainActivity self;

    public SoundManager soundManager;
    public Engine engine;
    public MenuFragment menuFragment;
    public SelectEpisodeFragment selectEpisodeFragment;
    public GameFragment gameFragment;
    public OptionsFragment optionsFragment;
    public AchievementsFragment achievementsFragment;
    public PrepareFragment prepareFragment;
    public QuitWarnDialogFragment quitWarnDialogFragment;

    private Fragment currentFragment;
    @SuppressWarnings("FieldCanBeLocal") private Fragment prevFragment;
    private ActionBarFragment prevActionBarFragment;
    private final Handler handler = new Handler();

    private MainActivityGPlayHelper gPlayHelper = new MainActivityGPlayHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // forceLocale(new Locale("ru"));
        // ViewServer.get(this).addWindow(this);

        soundManager = SoundManager.getInstance(false);
        soundManager.initialize();

        engine = new Engine(this);

        menuFragment = new MenuFragment();
        selectEpisodeFragment = new SelectEpisodeFragment();
        gameFragment = new GameFragment();
        optionsFragment = new OptionsFragment();
        achievementsFragment = new AchievementsFragment();
        prepareFragment = new PrepareFragment();

        quitWarnDialogFragment = QuitWarnDialogFragment.newInstance();
        gPlayHelper.onCreate();

        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        self = this;

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.hide();

        if (App.self.cachedTexturesTask != null || CachedTexturesProvider.needToUpdateCache()) {
            showFragment(prepareFragment);
        } else {
            showFragment(menuFragment);
        }
    }

    // private void forceLocale(Locale locale) {
    //     Locale.setDefault(locale);
    //
    //     Resources res = getResources();
    //     Configuration conf = res.getConfiguration();
    //
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    //         conf.setLocale(locale);
    //     } else {
    //         conf.locale = locale;
    //     }
    //
    //     res.updateConfiguration(conf, res.getDisplayMetrics());
    // }

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
        soundManager.onWindowFocusChanged(false, SoundManager.FOCUS_MASK_MAIN_ACTIVITY);

        // if home screen button pressed, lock screen button pressed, call received or something similar - return to menu screen
        /*
        if (currentFragment == gameFragment) {
			gameFragment.hideDialogs();
			showFragment(menuFragment);
		}
		*/

        super.onPause();
    }

    public synchronized void showFragment(final Fragment fragment) {
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
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, TAG_FRAGMENT_CURRENT);

        // TRANSIT_UNSET and TRANSIT_NONE causes bug with some cyanogenmod roms (fragment not changed)
        // TRANSIT_FRAGMENT_FADE + WebView (inside PromoView) = screen flashes during fragment change
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        try {
            transaction.commit();

            try {
                getSupportFragmentManager().executePendingTransactions(); // fix FC due to "java.lang.IllegalStateException: Fragment already added"
            } catch (Exception ex) {
                Common.log(ex);
            }
        } catch (Exception ex) {
            Common.log(ex.toString());
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

    @SuppressWarnings("unused")
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
        self = null;
        soundManager.shutdown();

        super.onDestroy();
    }

    public boolean tryAndLoadInstantState() {
        if (!engine.hasInstantSave()) {
            engine.state.init();
            return false;
        }

        engine.state.init();
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
