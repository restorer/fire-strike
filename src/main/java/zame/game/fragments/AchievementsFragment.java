package zame.game.fragments;

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
import zame.game.managers.SoundManager;
import zame.game.store.AchievementsAdapter;
import zame.game.store.Profile;

public class AchievementsFragment extends BaseFragment {
    private Profile profile;
    private ListView itemsList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.profile = App.self.profile;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_achievements, container, false);

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

    protected void refreshAdapter() {
        BaseAdapter adapter = new AchievementsAdapter(activity, profile);
        itemsList.setAdapter(adapter);
        itemsList.setVisibility(View.VISIBLE);
    }
}
