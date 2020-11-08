package com.eightsines.esmediadtor;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eightsines.espromo.PromoView;

import java.util.ArrayList;
import java.util.List;

public class Mediadtor {
    // private static final long ROTATE_INTERVAL_APPODEAL = 30L * 1000L;
    private static final long ROTATE_INTERVAL_ESPROMO = 15L * 1000L;
    private static final long RECHECK_INTERVAL = 5000L;

    // private final String applicationKey;
    // private final boolean isConsentGiven;
    // private final boolean isTestingAds;
    private final String debugLogTag;
    private final Handler handler;
    @SuppressWarnings({ "FieldCanBeLocal", "unused", "RedundantSuppression" }) private MediadtorListener listener;
    private Activity bannerActivity;
    private ViewGroup bannerContainerView;
    private List<View> bannerViews;
    private int bannerIndex;

    // private RewardedVideoCallbacks rewardedVideoCallbacks = new RewardedVideoCallbacks() {
    //     @Override
    //     public void onRewardedVideoLoaded(boolean isPrecache) {
    //         // ignore
    //     }
    //
    //     @Override
    //     public void onRewardedVideoFailedToLoad() {
    //         // ignore
    //     }
    //
    //     @Override
    //     public void onRewardedVideoShown() {
    //         // ignore
    //     }
    //
    //     @Override
    //     public void onRewardedVideoFinished(double amount, String name) {
    //         // ignore
    //     }
    //
    //     @Override
    //     public void onRewardedVideoClosed(boolean finished) {
    //         listener.onRewardedVideoClosed(true);
    //     }
    //
    //     @Override
    //     public void onRewardedVideoExpired() {
    //         // ignore
    //     }
    //
    //     @Override
    //     public void onRewardedVideoClicked() {
    //         // ignore
    //     }
    // };

    private final Runnable rotateBannerRunnable = new Runnable() {
        @Override
        public void run() {
            debugLog("rotateBannerRunnable.run");
            handler.removeCallbacks(rotateBannerRunnable);

            if (bannerActivity == null || bannerContainerView == null || bannerViews == null) {
                return;
            }

            for (View view : bannerViews) {
                bannerContainerView.removeView(view);
            }

            int bannersCount = bannerViews.size();

            if (bannersCount == 0) {
                return;
            }

            if (bannerIndex < 0 || bannerIndex > bannersCount) {
                bannerIndex = 0;
            }

            View bannerView = bannerViews.get(bannerIndex);

            boolean isBannerLoaded = false;
            long delayInterval = RECHECK_INTERVAL;

            for (int i = 0; i < bannersCount; i++) {
                // if ((bannerView instanceof BannerView) && Appodeal.isLoaded(Appodeal.BANNER)) {
                //     isBannerLoaded = true;
                //     delayInterval = ROTATE_INTERVAL_APPODEAL;
                //     break;
                // }

                if ((bannerView instanceof PromoView) && ((PromoView)bannerView).isActivePromoLoaded()) {
                    isBannerLoaded = true;
                    delayInterval = ROTATE_INTERVAL_ESPROMO;
                    break;
                }

                bannerIndex = (bannerIndex + 1) % bannersCount;
                bannerView = bannerViews.get(bannerIndex);
            }

            debugLog("adding banner view of " + bannerView.getClass().getSimpleName());
            bannerContainerView.addView(bannerView);

            // if (bannerView instanceof BannerView) {
            //     Appodeal.show(bannerActivity, Appodeal.BANNER_VIEW);
            // }

            if (isBannerLoaded) {
                bannerIndex = (bannerIndex + 1) % bannersCount;
            }

            handler.postDelayed(rotateBannerRunnable, delayInterval);
        }
    };

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public Mediadtor(
            @Nullable String applicationKey,
            boolean isConsentGiven,
            boolean isTestingAds,
            String debugLogTag) {

        // this.applicationKey = applicationKey;
        // this.isConsentGiven = isConsentGiven;
        // this.isTestingAds = isTestingAds;
        this.debugLogTag = debugLogTag;

        handler = new Handler();
    }

    private void debugLog(String message) {
        if (debugLogTag != null) {
            Log.w(debugLogTag, "[Mediadtor] " + message);
        }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void onActivityCreate(@NonNull Activity activity, @NonNull MediadtorListener listener) {
        // if (applicationKey == null) {
        //     return;
        // }
        //
        // this.listener = listener;
        //
        // Appodeal.disableLocationPermissionCheck();
        // Appodeal.disableWriteExternalStoragePermissionCheck();
        //
        // Appodeal.setTesting(isTestingAds);
        // Appodeal.setLogLevel(debugLogTag != null ? Log.LogLevel.debug : Log.LogLevel.none);
        //
        // Appodeal.initialize(
        //         activity,
        //         applicationKey,
        //         Appodeal.INTERSTITIAL | Appodeal.REWARDED_VIDEO | Appodeal.BANNER,
        //         isConsentGiven);
        //
        // Appodeal.set728x90Banners(true);
        // Appodeal.setRewardedVideoCallbacks(rewardedVideoCallbacks);
        //
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        //         && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //         != PackageManager.PERMISSION_GRANTED) {
        //
        //     Appodeal.requestAndroidMPermissions(activity, null);
        // }
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void onActivityResume(@NonNull Activity activity) {
        if (bannerContainerView == null || bannerViews == null) {
            return;
        }

        // if (applicationKey != null) {
        //     Appodeal.onResume(activity, Appodeal.BANNER);
        //     handler.postDelayed(rotateBannerRunnable, ROTATE_INTERVAL_APPODEAL);
        // } else {
        //     handler.postDelayed(rotateBannerRunnable, ROTATE_INTERVAL_ESPROMO);
        // }

        handler.postDelayed(rotateBannerRunnable, ROTATE_INTERVAL_ESPROMO);
    }

    public void onActivityPause() {
        handler.removeCallbacks(rotateBannerRunnable);
    }

    public boolean isInterstitialLoaded() {
        // return (applicationKey == null ? false : Appodeal.isLoaded(Appodeal.INTERSTITIAL));
        return false;
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void showInterstitial(@NonNull Activity activity) {
        // if (applicationKey != null) {
        //     Appodeal.show(activity, Appodeal.INTERSTITIAL);
        // }
    }

    public boolean isRewardedVideoEnabled() {
        // return (applicationKey != null);
        return false;
    }

    public boolean isRewardedVideoLoaded() {
        // return (applicationKey == null ? false : Appodeal.isLoaded(Appodeal.REWARDED_VIDEO));
        return false;
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void showRewardedVideo(@NonNull Activity activity) {
        // if (applicationKey == null) {
        //     Appodeal.show(activity, Appodeal.REWARDED_VIDEO);
        // }
    }

    public void showBanner(@NonNull Activity activity, @NonNull ViewGroup containerView) {
        hideBanner(activity);

        bannerActivity = activity;
        bannerContainerView = containerView;
        bannerViews = new ArrayList<>();

        // if (applicationKey != null) {
        //     bannerViews.add(Appodeal.getBannerView(activity));
        // }

        bannerViews.add(new PromoView(activity, null, debugLogTag));

        if (containerView instanceof FrameLayout) {
            for (View view : bannerViews) {
                // boolean isAppodealBanner = (view instanceof BannerView);
                //
                // FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                //         ViewGroup.LayoutParams.MATCH_PARENT,
                //         isAppodealBanner ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT);
                //
                // if (isAppodealBanner) {
                //     lp.gravity = Gravity.CENTER_VERTICAL;
                // }
                //
                // view.setLayoutParams(lp);

                view.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }

        rotateBannerRunnable.run();
    }

    @SuppressWarnings({ "unused", "RedundantSuppression" })
    public void hideBanner(@NonNull Activity activity) {
        handler.removeCallbacks(rotateBannerRunnable);

        // if (applicationKey != null) {
        //     Appodeal.hide(activity, Appodeal.BANNER);
        //
        //     if (bannerActivity != null && activity != bannerActivity) {
        //         Appodeal.hide(bannerActivity, Appodeal.BANNER);
        //     }
        // }

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
