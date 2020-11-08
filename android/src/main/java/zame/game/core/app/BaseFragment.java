package zame.game.core.app;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import zame.game.App;
import zame.game.feature.main.MainActivity;

public abstract class BaseFragment extends Fragment {
    protected boolean isForgottenFragment;
    protected MainActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (!(context instanceof MainActivity)) {
            isForgottenFragment = true;
        } else {
            this.activity = (MainActivity)context;

            if (this.activity.engine == null) {
                isForgottenFragment = true;
            } else {
                this.activity.setupTabs();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isForgottenFragment) {
            onShowBanner();
        }
    }

    protected void onShowBanner() {
        App.self.mediadtor.hideBanner(activity);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
    }
}
