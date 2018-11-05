package zame.game.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import zame.game.MainActivity;

public abstract class BaseFragment extends Fragment {
    public MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity)context;
        this.activity.setupTabs();
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
    }
}
