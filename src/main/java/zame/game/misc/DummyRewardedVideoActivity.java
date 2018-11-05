package zame.game.misc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import zame.game.R;

public class DummyRewardedVideoActivity extends AppCompatActivity {
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy_rewarded_video);

        //noinspection MagicNumber
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DummyRewardedVideoActivity.this.setResult(Activity.RESULT_OK);
                DummyRewardedVideoActivity.this.finish();
            }
        }, 2500L);
    }
}
