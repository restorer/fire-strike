package zame.game;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {
    private static final long START_GAME_DELAY = 1500L;

    private final Handler handler = new Handler();

    private final Runnable startGameRunnable = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(LauncherActivity.this, MainActivity.class));
            finish();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(startGameRunnable, START_GAME_DELAY);
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(startGameRunnable);
        super.onStop();
    }
}
