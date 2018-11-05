package zame.game.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import zame.game.R;
import zame.game.fragments.dialogs.ShareDialogFragment;
import zame.game.managers.SoundManager;

public class MenuFragment extends BaseFragment {
    private View playWrapperView;
    private ShareDialogFragment shareDialogFragment;
    private MenuFragmentGPlayHelper gPlayHelper = new MenuFragmentGPlayHelper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shareDialogFragment = ShareDialogFragment.newInstance();
        gPlayHelper.onCreate();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_menu, container, false);

        playWrapperView = viewGroup.findViewById(R.id.play_wrapper);

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

        viewGroup.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                shareDialogFragment.show(getFragmentManager());
            }
        });

        gPlayHelper.createFragmentView(viewGroup, activity);
        return viewGroup;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
        playWrapperView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.bounce));
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            gPlayHelper.updateRateWrapVisibility();
        }
    }
}
