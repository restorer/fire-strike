package zame.game.feature.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import zame.game.App;
import zame.game.core.app.BaseActivity;
import zame.game.feature.main.MainActivity;

public class LauncherActivity extends BaseActivity {
    private static final long START_GAME_DELAY = 1500L;

    private final Runnable startGameRunnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(LauncherActivity.this, MainActivity.class));
            finish();
        }
    };

    @Override
    protected Context wrapBaseContext(Context newBase) {
        return newBase;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // new AdvertisingInfoTask(new Runnable() {
        //     @Override
        //     public void run() {
        //         if (!isFinishing()) {
        //             App.self.handler.postDelayed(startGameRunnable, START_GAME_DELAY);
        //         }
        //     }
        // }).execute();

        App.self.handler.postDelayed(startGameRunnable, START_GAME_DELAY);
    }

    // private static class AdvertisingInfoTask extends AsyncTask<Void, Void, Void> {
    //     private final Runnable runnable;
    //
    //     AdvertisingInfoTask(Runnable runnable) {
    //         super();
    //         this.runnable = runnable;
    //     }
    //
    //     @Override
    //     protected Void doInBackground(Void... params) {
    //         try {
    //             AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(App.self);
    //
    //             if (info != null) {
    //                 App.self.isLimitAdTrackingEnabled = info.isLimitAdTrackingEnabled();
    //             }
    //         } catch (GooglePlayServicesNotAvailableException e) {
    //             App.self.isLimitAdTrackingEnabled = false;
    //         } catch (Exception e) {
    //             Common.log(e);
    //         }
    //
    //         return null;
    //     }
    //
    //     @Override
    //     protected void onPostExecute(Void result) {
    //         App.self.applyConsent();
    //         runnable.run();
    //     }
    // }
}
