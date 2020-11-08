package zame.game.core.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppBroadcastManager {
    private final List<Pair<AppBroadcastReceiver, String>> receivers = new ArrayList<>();

    public void registerReceiver(AppBroadcastReceiver receiver, String action) {
        receivers.add(Pair.create(receiver, action));
    }

    public void unregisterReceiver(AppBroadcastReceiver receiver) {
        Iterator<Pair<AppBroadcastReceiver, String>> iterator = receivers.iterator();

        while (iterator.hasNext()) {
            Pair<AppBroadcastReceiver, String> pair = iterator.next();

            if (pair.first == receiver) {
                iterator.remove();
            }
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void sendBroadcast(String action) {
        sendBroadcast(action, new Bundle());
    }

    public void sendBroadcast(String action, Bundle bundle) {
        for (Pair<AppBroadcastReceiver, String> pair : receivers) {
            if (TextUtils.equals(action, pair.second)) {
                pair.first.onReceive(bundle);
            }
        }
    }
}
