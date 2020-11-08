package zame.game.feature.prepare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import zame.game.App;
import zame.game.R;
import zame.game.core.app.AppBroadcastReceiver;
import zame.game.core.app.BaseFragment;

public class PrepareFragment extends BaseFragment {
    public static PrepareFragment newInstance() {
        return new PrepareFragment();
    }

    private ProgressBar progressView;

    private final AppBroadcastReceiver cacheUpdateProgressReceiver = new AppBroadcastReceiver() {
        @Override
        public void onReceive(Bundle bundle) {
            int progress = bundle.getInt(CachedTexturesProvider.EXTRA_PROGRESS, 0);

            if (progress > 100) {
                activity.processNext();
            } else {
                progressView.setProgress(progress);
            }
        }
    };

    public PrepareFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.prepare_fragment, container, false);

        if (!isForgottenFragment) {
            progressView = viewGroup.findViewById(R.id.progress);

            //noinspection BooleanVariableAlwaysNegated
            boolean needToUpdateCache = (App.self.cachedTexturesTask != null
                    || CachedTexturesProvider.needToUpdateCache());

            if (!needToUpdateCache) {
                activity.showFragment(activity.menuFragment);
            } else {
                App.self.broadcastManager.registerReceiver(
                        cacheUpdateProgressReceiver,
                        CachedTexturesProvider.BROADCAST_ACTION);

                if (App.self.cachedTexturesTask == null) {
                    CachedTexturesProvider.updateCache();
                }
            }
        }

        return viewGroup;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isForgottenFragment) {
            activity.soundManager.setPlaylist(null);
        }
    }

    @Override
    public void onDestroyView() {
        if (!isForgottenFragment) {
            App.self.broadcastManager.unregisterReceiver(cacheUpdateProgressReceiver);
        }

        super.onDestroyView();
    }
}
