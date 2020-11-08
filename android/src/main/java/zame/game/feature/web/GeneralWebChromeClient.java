package zame.game.feature.web;

import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import zame.game.core.util.Common;

public class GeneralWebChromeClient extends WebChromeClient {
    private final ProgressBar progressBar;
    private boolean isProgressBarVisible;

    GeneralWebChromeClient(ProgressBar progressBar) {
        super();
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        if (progress >= 0 && progress < 100) {
            if (!isProgressBarVisible) {
                isProgressBarVisible = true;
                progressBar.setVisibility(View.GONE); // fix bug
                progressBar.setVisibility(View.VISIBLE);
                // progressBar.bringToFront();
            }
        } else {
            if (isProgressBarVisible) {
                isProgressBarVisible = false;
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        WebView.HitTestResult result = view.getHitTestResult();

        // just for case
        if (result == null) {
            return false;
        }

        String data = result.getExtra();

        if (data == null) {
            return false;
        }

        Uri uri;

        try {
            uri = Uri.parse(data);
        } catch (Exception ex) {
            Common.log(ex);
            return false;
        }

        Common.openExternalIntent(view.getContext(), new Intent(Intent.ACTION_VIEW, uri));
        return false;
    }
}
