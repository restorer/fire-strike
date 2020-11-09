package com.eightsines.espromo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.Locale;

import zame.game.BuildConfig;

public class PromoView extends FrameLayout {
    interface Listener {
        void onActivePromoLoaded();

        void onActivePromoDismissed();
    }

    private static final String PROMO_URL = "https://eightsines.com/promo/?package=";

    private static final long RELOAD_INTERVAL = 10L * 1000L;
    private static final long ROTATE_INTERVAL = 15L * 1000L;

    private static final int STATE_INITIALIZED = 0;
    private static final int STATE_LOADING = 1;
    private static final int STATE_LOADED = 2;
    private static final int STATE_DISMISSED = 3;

    private final Handler handler = new Handler();
    private Context context;
    private WebView prevWebView;
    private WebView currentWebView;
    private int state;
    private boolean activePromoLoaded;
    private Listener listener;
    private String debugLogTag;

    private final Runnable loadPromoRunnable = new Runnable() {
        @Override
        public void run() {
            debugLog("loadPromoRunnable.run");
            loadPromo();
        }
    };

    private final Runnable reloadPromoRunnable = new Runnable() {
        @Override
        public void run() {
            debugLog("reloadPromoRunnable.run");
            reloadPromo();
        }
    };

    private final Runnable rotatePromoRunnable = new Runnable() {
        @Override
        public void run() {
            debugLog("rotatePromoRunnable.run");
            rotatePromo();
        }
    };

    private final Runnable promoLoadedRunnable = new Runnable() {
        @Override
        public void run() {
            debugLog("promoLoadedRunnable.run");
            promoLoaded();
        }
    };

    private final Runnable promoDismissedRunnable = new Runnable() {
        @Override
        public void run() {
            promoDismissed();
        }
    };

    public PromoView(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    public PromoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public PromoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    public PromoView(@NonNull Context context, @Nullable Listener listener, @Nullable String debugLogTag) {
        super(context);
        this.listener = listener;

        synchronized (this) {
            this.debugLogTag = debugLogTag;
        }

        initialize(context);
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public boolean isActivePromoLoaded() {
        return activePromoLoaded;
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    @Nullable
    public Listener getListener() {
        return listener;
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    @Nullable
    public String getDebugLogTag() {
        synchronized (this) {
            return debugLogTag;
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void setDebugLogTag(@Nullable String debugLogTag) {
        synchronized (this) {
            this.debugLogTag = debugLogTag;
        }
    }

    private void debugLog(String message) {
        String debugLogTagLocal;

        synchronized (this) {
            debugLogTagLocal = debugLogTag;
        }

        if (debugLogTagLocal != null) {
            Log.w(debugLogTagLocal, "[PromoView] " + message);
        }
    }

    private void initialize(@NonNull Context context) {
        this.context = context;

        prevWebView = createWebView();
        currentWebView = createWebView();

        loadPromo();
    }

    @SuppressLint({ "AddJavascriptInterface", "ObsoleteSdkInt", "SetJavaScriptEnabled" })
    @NonNull
    private WebView createWebView() {
        WebView webView = new WebView(context);
        webView.addJavascriptInterface(new JsApi(), "promoApi");
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        webView.setWebViewClient(new PromoWebViewClient());
        webView.setWebChromeClient(new PromoWebChromeClient());
        webView.setVisibility(View.INVISIBLE);

        webView.setBackgroundColor(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webSettings.setDisplayZoomControls(false);
        }

        webView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        addView(webView);
        return webView;
    }

    private void loadPromo() {
        debugLog("loadPromo: state = " + state);

        handler.removeCallbacks(loadPromoRunnable);
        handler.removeCallbacks(reloadPromoRunnable);
        handler.removeCallbacks(rotatePromoRunnable);

        if (state != STATE_INITIALIZED) {
            return;
        }

        if (isNetworkConnected()) {
            state = STATE_LOADING;

            String url = PROMO_URL + context.getPackageName() + "&lang=" + Locale.getDefault()
                    .getLanguage()
                    .toLowerCase(Locale.US);

            if (BuildConfig.DEBUG) {
                url += "&mode=debug";
            }

            debugLog("loadPromo: isNetworkConnected() = true, state = " + state + ", url = \"" + url + "\"");
            currentWebView.loadUrl(url);
        } else {
            debugLog("loadPromo: isNetworkConnected() = false");
            handler.postDelayed(loadPromoRunnable, RELOAD_INTERVAL);
        }
    }

    private void reloadPromo() {
        debugLog("reloadPromo");

        currentWebView.setVisibility(View.INVISIBLE);
        currentWebView.stopLoading();
        currentWebView.loadData("", "text/html", null);

        state = STATE_INITIALIZED;
        loadPromo();
    }

    private void rotatePromo() {
        debugLog("rotatePromo");

        WebView tmpWebView = prevWebView;
        prevWebView = currentWebView;
        currentWebView = tmpWebView;

        reloadPromo();
    }

    private void promoLoaded() {
        debugLog("promoLoaded: state = " + state);

        if (state != STATE_LOADING) {
            return;
        }

        currentWebView.setVisibility(View.VISIBLE);
        prevWebView.setVisibility(View.INVISIBLE);

        try {
            prevWebView.stopLoading();
            prevWebView.loadData("", "text/html", null);
        } catch (Throwable e) {
            debugLog("promoLoaded: exception in prevWebView, e = " + e);

            // Something bad happened inside WebView. Just re-create it.
            prevWebView = createWebView();
            prevWebView.loadData("", "text/html", null);
        }

        state = STATE_LOADED;
        handler.postDelayed(rotatePromoRunnable, ROTATE_INTERVAL);

        activePromoLoaded = true;
        debugLog("promoLoaded: activePromoLoaded = true");

        if (listener != null) {
            debugLog("promoLoaded: calling listener.onActivePromoLoaded()");
            listener.onActivePromoLoaded();
        }
    }

    private void promoDismissed() {
        debugLog("promoDismissed: state = " + state);

        if (state != STATE_LOADING && state != STATE_LOADED) {
            return;
        }

        prevWebView.setVisibility(View.INVISIBLE);
        currentWebView.setVisibility(View.INVISIBLE);

        try {
            prevWebView.stopLoading();
            prevWebView.loadData("", "text/html", null);
        } catch (Throwable e) {
            debugLog("promoDismissed: exception in prevWebView, e = " + e);

            // Something bad happened inside WebView. Just re-create it.
            prevWebView = createWebView();
            prevWebView.loadData("", "text/html", null);
        }

        try {
            currentWebView.stopLoading();
            currentWebView.loadData("", "text/html", null);
        } catch (Throwable e) {
            debugLog("promoDismissed: exception in currentWebView, e = " + e);

            // Something bad happened inside WebView. Just re-create it.
            currentWebView = createWebView();
            currentWebView.loadData("", "text/html", null);
        }

        state = STATE_DISMISSED;
        handler.post(rotatePromoRunnable); // rotate immediately

        activePromoLoaded = false;
        debugLog("promoDismissed: activePromoLoaded = false");

        if (listener != null) {
            debugLog("promoDismissed: calling listener.onActivePromoDismissed()");
            listener.onActivePromoDismissed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        debugLog("onWindowFocusChanged: state = " + state + ", hasWindowFocus = " + hasWindowFocus);

        if (state != STATE_INITIALIZED) {
            return;
        }

        if (hasWindowFocus) {
            loadPromo();
        } else {
            handler.removeCallbacks(loadPromoRunnable);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        debugLog("onAttachedToWindow: state = " + state);

        if (state == STATE_INITIALIZED) {
            loadPromo();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        debugLog("onDetachedFromWindow");

        handler.removeCallbacks(loadPromoRunnable);
        handler.removeCallbacks(reloadPromoRunnable);
        handler.removeCallbacks(rotatePromoRunnable);
        state = STATE_INITIALIZED;

        super.onDetachedFromWindow();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void openExternalBrowser(@NonNull final String uri) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    context.startActivity((new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri))).addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));
                } catch (Exception ex) {
                    try {
                        Toast.makeText(context, "Could not launch the browser application.", Toast.LENGTH_LONG).show();
                    } catch (Exception inner) {
                        // ignored
                    }
                }
            }
        });
    }

    private void openExternalIntent(@NonNull final Intent intent) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET));
                } catch (Exception ex) {
                    try {
                        Toast.makeText(context, "Could not startBatch external intent.", Toast.LENGTH_LONG).show();
                    } catch (Exception inner) {
                        // ignored
                    }
                }
            }
        });
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    private class JsApi {
        @JavascriptInterface
        public void loaded() {
            //noinspection MagicNumber
            handler.postDelayed(promoLoadedRunnable, 100L);
        }

        @JavascriptInterface
        public void dismiss() {
            handler.post(promoDismissedRunnable);
        }
    }

    private class PromoWebViewClient extends WebViewClient {
        @SuppressLint("ObsoleteSdkInt")
        @Override
        public void onPageFinished(@NonNull WebView view, @NonNull String url) {
            try {
                view.setBackgroundColor(0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    view.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
                }
            } catch (Throwable e) {
                debugLog("PromoWebViewClient.onPageFinished failed: " + e);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(@NonNull WebView view, String url) {
            final String MAILTO_PREFIX = "mailto:";

            try {
                if (url == null || !url.startsWith(MAILTO_PREFIX)) {
                    return false;
                }

                Intent intent = new Intent(
                        Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", url.replaceFirst(MAILTO_PREFIX, ""), null));

                openExternalIntent(intent);
                return true;
            } catch (Throwable e) {
                debugLog("PromoWebViewClient.shouldOverrideUrlLoading failed: " + e);
            }

            return false;
        }

        @SuppressLint("NewApi")
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, @NonNull String url) {
            final String ANDROID_ASSET = "file:///android_asset/";

            try {
                if (!url.startsWith(ANDROID_ASSET)) {
                    return null;
                }

                Uri uri = Uri.parse(url.replaceFirst(ANDROID_ASSET, ""));
                String path = uri.getPath();

                if (path != null) {
                    InputStream stream = view.getContext().getAssets().open(path);
                    return new WebResourceResponse("text/html", "UTF-8", stream);
                }
            } catch (Throwable e) {
                debugLog("PromoWebViewClient.shouldInterceptRequest failed: " + e);
            }

            return null;
        }

        @Override
        public void onReceivedError(
                @NonNull WebView view,
                int errorCode,
                String description,
                String failingUrl) {

            //noinspection UnnecessaryCallToStringValueOf
            debugLog("PromoWebViewClient.onReceivedError: errorCode = "
                    + errorCode
                    + ", description = \""
                    + String.valueOf(description)
                    + "\", failingUrl = \""
                    + String.valueOf(failingUrl) + "\"");

            try {
                view.stopLoading();
                view.loadData("", "text/html", null);
            } catch (Throwable e) {
                debugLog("PromoWebViewClient.onReceivedError failed: " + e);
            }

            handler.post(reloadPromoRunnable);
        }

        @Override
        public void onReceivedHttpAuthRequest(
                @NonNull WebView view,
                @NonNull HttpAuthHandler httpAuthHandler,
                @NonNull String host,
                @NonNull String realm) {

            try {
                view.stopLoading();
                view.loadData("", "text/html", null);
            } catch (Throwable e) {
                debugLog("PromoWebViewClient.onReceivedHttpAuthRequest failed: " + e);
            }

            handler.post(reloadPromoRunnable);
        }
    }

    private class PromoWebChromeClient extends WebChromeClient {
        private WebView childWebView;

        @Override
        public boolean onCreateWindow(
                @NonNull WebView view,
                boolean dialog,
                boolean userGesture,
                @NonNull Message resultMsg) {

            try {
                if (childWebView != null) {
                    childWebView.stopLoading();
                    childWebView.destroy();
                }

                createChildWebView(view.getContext());
                ((WebView.WebViewTransport)resultMsg.obj).setWebView(childWebView);
                resultMsg.sendToTarget();
                return true;
            } catch (Throwable e) {
                debugLog("PromoWebChromeClient.onCreateWindow failed: " + e);
                return false;
            }
        }

        private void createChildWebView(Context context) {
            childWebView = new WebView(context);

            childWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(@NonNull WebView view, String url) {
                    try {
                        if (url != null) {
                            url = url.trim();

                            if (!TextUtils.isEmpty(url)) {
                                openExternalBrowser(url);
                            }
                        }

                        childWebView.stopLoading();
                        childWebView.destroy();
                        childWebView = null;
                    } catch (Throwable e) {
                        debugLog("Child WebViewClient.shouldOverrideUrlLoading failed: " + e);
                    }

                    return true;
                }
            });
        }
    }
}
