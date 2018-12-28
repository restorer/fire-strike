package zame.game.feature.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import zame.game.App;
import zame.game.R;
import zame.game.core.app.BaseFragment;
import zame.game.core.util.Common;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.AppConfig;
import zame.game.flavour.gplay.MenuFragmentGPlayHelper;

public class MenuFragment extends BaseFragment {
    public static MenuFragment newInstance() {
        return new MenuFragment();
    }

    private View playWrapperView;
    private ViewGroup bannerWrapperView;
    private MenuFragmentGPlayHelper gPlayHelper = new MenuFragmentGPlayHelper();

    public MenuFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gPlayHelper.onCreate();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.menu_fragment, container, false);

        playWrapperView = viewGroup.findViewById(R.id.play_wrapper);
        bannerWrapperView = viewGroup.findViewById(R.id.banner_wrapper);

        viewGroup.findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);

                if (!activity.tryAndLoadInstantState() || activity.engine.state.showEpisodeSelector) {
                    activity.showFragment(activity.selectEpisodeFragment);
                } else {
                    activity.continueGame();
                }
            }
        });

        viewGroup.findViewById(R.id.options).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                activity.showFragment(activity.optionsFragment);
            }
        });

        viewGroup.findViewById(R.id.achievements).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                activity.showFragment(activity.achievementsFragment);
            }
        });

        viewGroup.findViewById(R.id.like_vk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                Common.openViewIntent(activity, AppConfig.LINK_VK);
            }
        });

        viewGroup.findViewById(R.id.like_facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                Common.openViewIntent(activity, AppConfig.LINK_FACEBOOK);
            }
        });

        viewGroup.findViewById(R.id.like_telegram).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                Common.openViewIntent(activity, AppConfig.LINK_TELEGRAM);
            }
        });

        gPlayHelper.createFragmentView(viewGroup, activity);
        return viewGroup;
    }

    @Override
    protected void onShowBanner() {
        App.self.mediadtor.showBanner(activity, bannerWrapperView);
    }

    @Override
    public void onStart() {
        super.onStart();
        playWrapperView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.bounce));
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            gPlayHelper.updateRateWrapVisibility();
        }
    }
}
