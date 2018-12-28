package zame.game.feature.achievements;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import zame.game.App;
import zame.game.R;
import zame.game.feature.sound.SoundManager;
import zame.game.core.app.BaseFragment;
import zame.game.engine.state.Profile;

public class AchievementsFragment extends BaseFragment {
    public static AchievementsFragment newInstance() {
        return new AchievementsFragment();
    }

    private Profile profile;
    private ListView itemsList;

    public AchievementsFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.profile = App.self.profile;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.achievements_fragment, container, false);

        itemsList = viewGroup.findViewById(R.id.items);
        refreshAdapter();

        viewGroup.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                activity.showFragment(activity.menuFragment);
            }
        });

        ((TextView)viewGroup.findViewById(R.id.exp)).setText(getString(R.string.achievements_exp, profile.exp));
        return viewGroup;
    }

    private void refreshAdapter() {
        BaseAdapter adapter = new AchievementsAdapter(activity, profile);
        itemsList.setAdapter(adapter);
        itemsList.setVisibility(View.VISIBLE);
    }
}
