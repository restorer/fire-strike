package com.eightsines.esmediadtor;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.appodeal.ads.utils.Log;
import com.eightsines.espromo.PromoView;

public class Mediadtor {
    private static final long ROTATE_INTERVAL_APPODEAL = 30L * 1000L;
    private static final long ROTATE_INTERVAL_ESPROMO = 15L * 1000L;
    private static final long RECHECK_INTERVAL = 5000L;

    private String applicationKey;
    private boolean isConsentGiven;
    private boolean isTesting;
    private Handler handler;
    private MediadtorListener listener;
    private Activity bannerActivity;
    private ViewGroup bannerContainerView;
    private View[] bannerViews;
    private int bannerIndex;

    private RewardedVideoCallbacks rewardedVideoCallbacks = new RewardedVideoCallbacks() {
        @Override
        public void onRewardedVideoLoaded(boolean isPrecache) {
            // ignore
        }

        @Override
        public void onRewardedVideoFailedToLoad() {
            // ignore
        }

        @Override
        public void onRewardedVideoShown() {
            // ignore
        }

        @Override
        public void onRewardedVideoFinished(double amount, String name) {
            // ignore
        }

        @Override
        public void onRewardedVideoClosed(boolean finished) {
            listener.onRewardedVideoClosed(true);
        }

        @Override
        public void onRewardedVideoExpired() {
            // ignore
        }
    };

    private Runnable rotateBannerRunnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(rotateBannerRunnable);

            if (bannerActivity == null || bannerContainerView == null || bannerViews == null) {
                return;
            }

            for (View view : bannerViews) {
                bannerContainerView.removeView(view);
            }

            int bannersCount = bannerViews.length;

            if (bannersCount == 0) {
                return;
            }

            if (bannerIndex < 0 || bannerIndex > bannersCount) {
                bannerIndex = 0;
            }

            View bannerView = bannerViews[bannerIndex];

            boolean isBannerLoaded = false;
            long delayInterval = RECHECK_INTERVAL;

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < bannersCount; i++) {
                if ((bannerView instanceof BannerView) && Appodeal.isLoaded(Appodeal.BANNER)) {
                    isBannerLoaded = true;
                    delayInterval = ROTATE_INTERVAL_APPODEAL;
                    break;
                }

                if ((bannerView instanceof PromoView) && ((PromoView)bannerView).isActivePromoLoaded()) {
                    isBannerLoaded = true;
                    delayInterval = ROTATE_INTERVAL_ESPROMO;
                    break;
                }

                bannerIndex = (bannerIndex + 1) % bannersCount;
                bannerView = bannerViews[bannerIndex];
            }

            bannerContainerView.addView(bannerView);

            if (bannerView instanceof BannerView) {
                Appodeal.show(bannerActivity, Appodeal.BANNER_VIEW);
            }

            if (isBannerLoaded) {
                bannerIndex = (bannerIndex + 1) % bannersCount;
            }

            handler.postDelayed(rotateBannerRunnable, delayInterval);
        }
    };

    public Mediadtor(@NonNull String applicationKey, boolean isConsentGiven, boolean isTesting) {
        this.applicationKey = applicationKey;
        this.isConsentGiven = isConsentGiven;
        this.isTesting = isTesting;

        handler = new Handler();
    }

    public void onActivityCreate(@NonNull Activity activity, @NonNull MediadtorListener listener) {
        this.listener = listener;

        Appodeal.disableLocationPermissionCheck();
        Appodeal.setTesting(isTesting);
        Appodeal.setLogLevel(isTesting ? Log.LogLevel.debug : Log.LogLevel.none);

        if (!isTesting) {
            Appodeal.disableWriteExternalStoragePermissionCheck();
        }

        // https://www.appodeal.com/sdk/documentation?framework=1&full=1&platform=1
        //
        // Appodeal.setChildDirectedTreatment(true);
        // Appodeal.muteVideosIfCallsMuted(true);

        Appodeal.initialize(
                activity,
                applicationKey,
                Appodeal.INTERSTITIAL | Appodeal.REWARDED_VIDEO | Appodeal.BANNER,
                isConsentGiven);

        Appodeal.setRewardedVideoCallbacks(rewardedVideoCallbacks);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Appodeal.requestAndroidMPermissions(activity, null);
        }
    }

    public void onActivityResume(@NonNull Activity activity) {
        if (bannerContainerView != null && bannerViews != null) {
            Appodeal.onResume(activity, Appodeal.BANNER);
            handler.postDelayed(rotateBannerRunnable, ROTATE_INTERVAL_APPODEAL);
        }
    }

    public void onActivityPause() {
        handler.removeCallbacks(rotateBannerRunnable);
    }

    public boolean isInterstitialLoaded() {
        return Appodeal.isLoaded(Appodeal.INTERSTITIAL);
    }

    public void showInterstitial(@NonNull Activity activity) {
        Appodeal.show(activity, Appodeal.INTERSTITIAL);
    }

    public boolean isRewardedVideoLoaded() {
        return Appodeal.isLoaded(Appodeal.REWARDED_VIDEO);
    }

    public void showRewardedVideo(@NonNull Activity activity) {
        Appodeal.show(activity, Appodeal.REWARDED_VIDEO);
    }

    public void showBanner(@NonNull Activity activity, @NonNull ViewGroup containerView) {
        hideBanner(activity);

        bannerActivity = activity;
        bannerContainerView = containerView;

        bannerViews = new View[] {
                Appodeal.getBannerView(activity),
                new PromoView(activity) };

        if (containerView instanceof FrameLayout) {
            for (View view : bannerViews) {
                boolean isAppodealBanner = (view instanceof BannerView);

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        isAppodealBanner ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT);

                if (isAppodealBanner) {
                    lp.gravity = Gravity.CENTER_VERTICAL;
                }

                view.setLayoutParams(lp);
            }
        }

        rotateBannerRunnable.run();
    }

    public void hideBanner(@NonNull Activity activity) {
        handler.removeCallbacks(rotateBannerRunnable);
        Appodeal.hide(activity, Appodeal.BANNER);

        if (bannerActivity != null && activity != bannerActivity) {
            Appodeal.hide(bannerActivity, Appodeal.BANNER);
        }

        if (bannerContainerView != null && bannerViews != null) {
            for (View view : bannerViews) {
                bannerContainerView.removeView(view);
            }
        }

        bannerActivity = null;
        bannerContainerView = null;
        bannerViews = null;
    }
}
