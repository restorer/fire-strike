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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
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
import java.io.InputStream;
import java.util.Locale;
import zame.game.BuildConfig;

public class PromoView extends FrameLayout {
    interface Listener {
        void onActivePromoLoaded();

        void onActivePromoDismissed();
    }

    private static final String PROMO_URL = "http://mobile.zame-dev.org/promo/index.php?package=";

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

    private Runnable loadPromoRunnable = new Runnable() {
        @Override
        public void run() {
            loadPromo();
        }
    };

    private Runnable reloadPromoRunnable = new Runnable() {
        @Override
        public void run() {
            reloadPromo();
        }
    };

    private Runnable rotatePromoRunnable = new Runnable() {
        @Override
        public void run() {
            rotatePromo();
        }
    };

    private Runnable promoLoadedRunnable = new Runnable() {
        @Override
        public void run() {
            promoLoaded();
        }
    };

    private Runnable promoDismissedRunnable = new Runnable() {
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

    @SuppressWarnings("unused")
    public boolean isActivePromoLoaded() {
        return activePromoLoaded;
    }

    @SuppressWarnings("unused")
    @Nullable
    public Listener getListener() {
        return listener;
    }

    @SuppressWarnings("unused")
    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
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

        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        addView(webView);

        return webView;
    }

    private void loadPromo() {
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
                currentWebView.loadUrl(url + "&mode=debug");
            } else {
                currentWebView.loadUrl(url);
            }
        } else {
            handler.postDelayed(loadPromoRunnable, RELOAD_INTERVAL);
        }
    }

    private void reloadPromo() {
        currentWebView.setVisibility(View.INVISIBLE);
        currentWebView.stopLoading();
        currentWebView.loadData("", "text/html", null);

        state = STATE_INITIALIZED;
        loadPromo();
    }

    private void rotatePromo() {
        WebView tmpWebView = prevWebView;
        prevWebView = currentWebView;
        currentWebView = tmpWebView;

        reloadPromo();
    }

    private void promoLoaded() {
        if (state == STATE_LOADING) {
            currentWebView.setVisibility(View.VISIBLE);

            prevWebView.setVisibility(View.INVISIBLE);
            prevWebView.stopLoading();
            prevWebView.loadData("", "text/html", null);

            state = STATE_LOADED;
            handler.postDelayed(rotatePromoRunnable, ROTATE_INTERVAL);

            activePromoLoaded = true;

            if (listener != null) {
                listener.onActivePromoLoaded();
            }
        }
    }

    private void promoDismissed() {
        if (state == STATE_LOADING || state == STATE_LOADED) {
            prevWebView.setVisibility(View.INVISIBLE);
            prevWebView.stopLoading();
            prevWebView.loadData("", "text/html", null);

            currentWebView.setVisibility(View.INVISIBLE);
            currentWebView.stopLoading();
            currentWebView.loadData("", "text/html", null);

            state = STATE_DISMISSED;
            handler.post(rotatePromoRunnable); // rotate immediately

            activePromoLoaded = false;

            if (listener != null) {
                listener.onActivePromoDismissed();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

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

        if (state == STATE_INITIALIZED) {
            loadPromo();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(loadPromoRunnable);
        handler.removeCallbacks(reloadPromoRunnable);
        handler.removeCallbacks(rotatePromoRunnable);
        state = STATE_INITIALIZED;

        super.onDetachedFromWindow();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

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
                    context.startActivity((new Intent(Intent.ACTION_VIEW,
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

    @SuppressWarnings("unused")
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
            view.setBackgroundColor(0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                view.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
            final String MAILTO_PREFIX = "mailto:";

            if (url.startsWith(MAILTO_PREFIX)) {
                Intent intent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", url.replaceFirst(MAILTO_PREFIX, ""), null));

                openExternalIntent(intent);
                return true;
            }

            return false;
        }

        @SuppressLint("NewApi")
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, @NonNull String url) {
            final String ANDROID_ASSET = "file:///android_asset/";

            if (url.startsWith(ANDROID_ASSET)) {
                try {
                    Uri uri = Uri.parse(url.replaceFirst(ANDROID_ASSET, ""));
                    String path = uri.getPath();

                    if (path != null) {
                        InputStream stream = view.getContext().getAssets().open(path);
                        return new WebResourceResponse("text/html", "UTF-8", stream);
                    }
                } catch (Exception ex) {
                    // ignored
                }
            }

            return null;
        }

        @Override
        public void onReceivedError(@NonNull WebView view,
                int errorCode,
                @NonNull String description,
                @NonNull String failingUrl) {

            view.stopLoading();
            view.loadData("", "text/html", null);

            handler.post(reloadPromoRunnable);
        }

        @Override
        public void onReceivedHttpAuthRequest(@NonNull WebView view,
                @NonNull HttpAuthHandler httpAuthHandler,
                @NonNull String host,
                @NonNull String realm) {

            view.stopLoading();
            view.loadData("", "text/html", null);

            handler.post(reloadPromoRunnable);
        }
    }

    private class PromoWebChromeClient extends WebChromeClient {
        private WebView childWebView;

        @Override
        public boolean onCreateWindow(@NonNull WebView view,
                boolean dialog,
                boolean userGesture,
                @NonNull Message resultMsg) {

            try {
                if (childWebView != null) {
                    childWebView.stopLoading();
                    childWebView.destroy();
                }

                childWebView = new WebView(view.getContext());

                childWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        url = url.trim();

                        if (!TextUtils.isEmpty(url)) {
                            openExternalBrowser(url);
                        }

                        childWebView.stopLoading();
                        childWebView.destroy();
                        childWebView = null;

                        return true;
                    }
                });

                ((WebView.WebViewTransport)resultMsg.obj).setWebView(childWebView);
                resultMsg.sendToTarget();
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }
}
