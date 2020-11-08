package zame.game.core.app;

import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import zame.game.App;
import zame.game.R;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(wrapBaseContext(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        ensureImmersive();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensureImmersive();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            ensureImmersive();
        }
    }

    protected void ensureImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();

            // Just for case
            if (decorView != null) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            }
        }
    }

    protected Context wrapBaseContext(Context newBase) {
        String language = App.self.preferences.getString(R.string.key_language, R.string.val_language_sys);
        Locale currentLocale;

        if (App.self.getString(R.string.val_language_en).equals(language)) {
            currentLocale = Locale.US;
        } else if (App.self.getString(R.string.val_language_ru).equals(language)) {
            currentLocale = App.LOCALE_RU;
        } else {
            currentLocale = App.self.systemDefaultLocale;
        }

        Locale.setDefault(currentLocale);
        return AppContextWrapper.wrap(newBase, currentLocale);
    }
}
