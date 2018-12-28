package zame.game.core.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import zame.game.App;
import zame.game.feature.main.MainActivity;

public abstract class BaseFragment extends Fragment {
    protected MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.activity = (MainActivity)context;
        this.activity.setupTabs();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onShowBanner();
    }

    protected void onShowBanner() {
        App.self.mediadtor.hideBanner(activity);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
    }
}
