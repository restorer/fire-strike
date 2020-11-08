package zame.game.core.manager;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import zame.game.App;

public class PreferencesManager {
    public boolean getBoolean(int keyResId) {
        return getPreferences().getBoolean(getRes(keyResId), false);
    }

    public boolean getBoolean(int keyResId, boolean defValue) {
        return getPreferences().getBoolean(getRes(keyResId), defValue);
    }

    public void putBoolean(int keyResId, boolean value) {
        getPreferences().edit().putBoolean(getRes(keyResId), value).apply();
    }

    public int getInt(int keyResId) {
        return getPreferences().getInt(getRes(keyResId), 0);
    }

    public int getInt(int keyResId, int defValue) {
        return getPreferences().getInt(getRes(keyResId), defValue);
    }

    public void putInt(int keyResId, int value) {
        getPreferences().edit().putInt(getRes(keyResId), value).apply();
    }

    public String getString(int keyResId, @Nullable String defValue) {
        return getPreferences().getString(getRes(keyResId), defValue);
    }

    public String getString(int keyResId, int defValueResId) {
        return getPreferences().getString(getRes(keyResId), getRes(defValueResId));
    }

    private String getRes(int keyResId) {
        return App.self.getString(keyResId);
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.self.getApplicationContext());
    }
}
