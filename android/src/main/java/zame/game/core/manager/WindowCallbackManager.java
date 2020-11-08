package zame.game.core.manager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.Nullable;

import java.util.List;

import zame.game.feature.sound.SoundManager;

public final class WindowCallbackManager {
    private WindowCallbackManager() {}

    @SuppressLint("ObsoleteSdkInt")
    public static void attachWindowCallback(final Window window, final SoundManager soundManager, final int focusMask) {
        final Window.Callback windowCallback = window.getCallback();

        window.setCallback(new Window.Callback() {
            @Override
            @TargetApi(12)
            public boolean dispatchGenericMotionEvent(MotionEvent event) {
                return VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1
                        && windowCallback != null
                        && windowCallback.dispatchGenericMotionEvent(event);
            }

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                return windowCallback != null && windowCallback.dispatchKeyEvent(event);
            }

            @Override
            @TargetApi(11)
            public boolean dispatchKeyShortcutEvent(KeyEvent event) {
                return VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB
                        && windowCallback != null
                        && windowCallback.dispatchKeyShortcutEvent(event);
            }

            @Override
            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                return windowCallback != null && windowCallback.dispatchPopulateAccessibilityEvent(event);
            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent event) {
                return windowCallback != null && windowCallback.dispatchTouchEvent(event);
            }

            @Override
            public boolean dispatchTrackballEvent(MotionEvent event) {
                return windowCallback != null && windowCallback.dispatchTrackballEvent(event);
            }

            @Override
            @TargetApi(11)
            public void onActionModeFinished(ActionMode mode) {
                if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && windowCallback != null) {
                    windowCallback.onActionModeFinished(mode);
                }
            }

            @Override
            public void onProvideKeyboardShortcuts(
                    List<KeyboardShortcutGroup> data,
                    @Nullable Menu menu,
                    int deviceId) {

                if (windowCallback != null && VERSION.SDK_INT >= VERSION_CODES.N) {
                    windowCallback.onProvideKeyboardShortcuts(data, menu, deviceId);
                }
            }

            @Override
            public void onPointerCaptureChanged(boolean hasCapture) {
                if (windowCallback != null && VERSION.SDK_INT >= VERSION_CODES.O) {
                    windowCallback.onPointerCaptureChanged(hasCapture);
                }
            }

            @Override
            @TargetApi(11)
            public void onActionModeStarted(ActionMode mode) {
                if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && windowCallback != null) {
                    windowCallback.onActionModeStarted(mode);
                }
            }

            @Override
            public void onAttachedToWindow() {
                if (windowCallback != null) {
                    windowCallback.onAttachedToWindow();
                }
            }

            @Override
            public void onContentChanged() {
                if (windowCallback != null) {
                    windowCallback.onContentChanged();
                }
            }

            @Override
            public boolean onCreatePanelMenu(int featureId, Menu menu) {
                return windowCallback != null && windowCallback.onCreatePanelMenu(featureId, menu);
            }

            @Override
            public View onCreatePanelView(int featureId) {
                if (windowCallback != null) {
                    return windowCallback.onCreatePanelView(featureId);
                } else {
                    return null;
                }
            }

            @Override
            public void onDetachedFromWindow() {
                if (windowCallback != null) {
                    windowCallback.onDetachedFromWindow();
                }
            }

            @Override
            public boolean onMenuItemSelected(int featureId, MenuItem item) {
                return windowCallback != null && windowCallback.onMenuItemSelected(featureId, item);
            }

            @Override
            public boolean onMenuOpened(int featureId, Menu menu) {
                return windowCallback != null && windowCallback.onMenuOpened(featureId, menu);
            }

            @Override
            public void onPanelClosed(int featureId, Menu menu) {
                if (windowCallback != null) {
                    windowCallback.onPanelClosed(featureId, menu);
                }
            }

            @Override
            public boolean onPreparePanel(int featureId, View view, Menu menu) {
                return windowCallback != null && windowCallback.onPreparePanel(featureId, view, menu);
            }

            @Override
            public boolean onSearchRequested() {
                return windowCallback != null && windowCallback.onSearchRequested();
            }

            @Override
            public boolean onSearchRequested(SearchEvent searchEvent) {
                return VERSION.SDK_INT >= VERSION_CODES.M && windowCallback != null && windowCallback.onSearchRequested(
                        searchEvent);
            }

            @Override
            public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
                if (windowCallback != null) {
                    windowCallback.onWindowAttributesChanged(attrs);
                }
            }

            @Override
            public void onWindowFocusChanged(boolean hasFocus) {
                soundManager.onWindowFocusChanged(hasFocus, focusMask);

                if (windowCallback != null) {
                    windowCallback.onWindowFocusChanged(hasFocus);
                }
            }

            @Override
            @TargetApi(12)
            public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
                if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1 && windowCallback != null) {
                    return windowCallback.onWindowStartingActionMode(callback);
                } else {
                    return null;
                }
            }

            @Nullable
            @Override
            public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
                if (windowCallback != null && VERSION.SDK_INT >= VERSION_CODES.M) {
                    return windowCallback.onWindowStartingActionMode(callback, type);
                } else {
                    return null;
                }
            }
        });
    }
}
