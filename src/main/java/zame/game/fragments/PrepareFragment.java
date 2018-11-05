package zame.game.fragments;

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
import android.widget.TextView;
import zame.game.App;
import zame.game.R;
import zame.game.providers.CachedTexturesProvider;

public class PrepareFragment extends BaseFragment {
    protected ViewGroup viewGroup;
    protected ProgressBar progressView;
    protected TextView infoView;

    protected final BroadcastReceiver cacheUpdateProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(CachedTexturesProvider.EXTRA_PROGRESS, 0);

            if (progress > 100) {
                activity.showFragment(activity.menuFragment);
            } else {
                progressView.setProgress(progress);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_prepare, container, false);

        progressView = viewGroup.findViewById(R.id.progress);
        infoView = viewGroup.findViewById(R.id.info);

        //noinspection BooleanVariableAlwaysNegated
        boolean needToUpdateCache = (App.self.cachedTexturesTask != null
                || CachedTexturesProvider.needToUpdateCache());

        if (!needToUpdateCache) {
            activity.showFragment(activity.menuFragment);
        } else {
            App.self.getLocalBroadcastManager()
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
        App.self.getLocalBroadcastManager().unregisterReceiver(cacheUpdateProgressReceiver);
        super.onDestroyView();
    }
}
