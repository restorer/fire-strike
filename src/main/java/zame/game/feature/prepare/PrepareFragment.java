package zame.game.feature.prepare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import zame.game.App;
import zame.game.R;
import zame.game.core.app.BaseFragment;

public class PrepareFragment extends BaseFragment {
    public static PrepareFragment newInstance() {
        return new PrepareFragment();
    }

    private ProgressBar progressView;

    private final BroadcastReceiver cacheUpdateProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(CachedTexturesProvider.EXTRA_PROGRESS, 0);

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
        progressView = viewGroup.findViewById(R.id.progress);

        //noinspection BooleanVariableAlwaysNegated
        boolean needToUpdateCache = (App.self.cachedTexturesTask != null
                || CachedTexturesProvider.needToUpdateCache());

        if (!needToUpdateCache) {
            activity.showFragment(activity.menuFragment);
        } else {
            App.self.getBroadcastManager()
                    .registerReceiver(cacheUpdateProgressReceiver,
                            new IntentFilter(CachedTexturesProvider.BROADCAST_ACTION));

            if (App.self.cachedTexturesTask == null) {
                CachedTexturesProvider.updateCache();
            }
        }

        return viewGroup;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.soundManager.setPlaylist(null);
    }

    @Override
    public void onDestroyView() {
        App.self.getBroadcastManager().unregisterReceiver(cacheUpdateProgressReceiver);
        super.onDestroyView();
    }
}
