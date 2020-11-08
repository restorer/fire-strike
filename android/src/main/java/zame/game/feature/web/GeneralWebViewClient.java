package zame.game.feature.web;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.util.Locale;

import zame.game.core.util.Common;
import zame.game.core.util.IntentProvider;

public class GeneralWebViewClient extends WebViewClient {
    static final String ERROR_PAGE_URL = "file:///android_asset/web/error.html";

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        final String MAILTO_PREFIX = "mailto:";

        if (url.startsWith(MAILTO_PREFIX)) {
            Intent intent = IntentProvider.getEmailIntent(
                    view.getContext(),
                    url.replaceFirst(MAILTO_PREFIX, "").trim());

            Common.openExternalIntent(view.getContext(), intent);
            return true;
        }

        return false;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        final String ANDROID_ASSET = "file:///android_asset/";

        if (url.startsWith(ANDROID_ASSET)) {
            try {
                Uri uri = Uri.parse(url.replaceFirst(ANDROID_ASSET, ""));
                String path = uri.getPath();

                if (path != null) {
                    InputStream stream = view.getContext().getAssets().open(path);
                    return new WebResourceResponse("text/html", "UTF-8", stream);
                } else {
                    throw new RuntimeException("uri.getPath() returns null");
                }
            } catch (Exception ex) {
                Common.log(ex);
            }
        }

        return null;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Common.log(String.format(Locale.US, "%d %s \"%s\"", errorCode, description, failingUrl));

        if (!failingUrl.startsWith(ERROR_PAGE_URL)) {
            view.stopLoading();

            view.loadUrl(String.format(
                    Locale.US,
                    "%s?url=%s&description=%s&code=%s",
                    ERROR_PAGE_URL,
                    Common.urlEncode(failingUrl),
                    Common.urlEncode(description),
                    errorCode));
        }
    }
}
