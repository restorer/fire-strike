package zame.game.feature.launcher;

import android.content.Context;
import android.content.Intent;
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
    protected void onStart() {
        super.onStart();
        App.self.handler.postDelayed(startGameRunnable, START_GAME_DELAY);
    }

    @Override
    protected void onStop() {
        App.self.handler.removeCallbacks(startGameRunnable);
        super.onStop();
    }
}
