package zame.game.core.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import zame.game.R;

public final class IntentProvider {
    private IntentProvider() {
    }

    /*
    public static Intent getTwitterShareIntent(Context context, String title, String url) {
        Intent intent = null;
        String text = url + " - " + title;

        final String[] twitterApps = { "com.twitter.android",
                "com.twidroid",
                "com.handmark.tweetcaster",
                "com.thedeck.android" };

        Intent checkIntent = new Intent();
        checkIntent.setType("text/plain");

        final List<ResolveInfo> list = context.getPackageManager()
                .queryIntentActivities(checkIntent, PackageManager.MATCH_DEFAULT_ONLY);

        outer:
        for (String appPackage : twitterApps) {
            for (ResolveInfo resolveInfo : list) {
                String packageName = resolveInfo.activityInfo.packageName;

                if (packageName != null && packageName.startsWith(appPackage)) {
                    checkIntent.setPackage(packageName);
                    checkIntent.putExtra(Intent.EXTRA_TEXT, text);
                    intent = checkIntent;
                    break outer;
                }
            }
        }

        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/intent/tweet?text=" + Common.urlEncode(text)));
        }

        return intent;
    }

    public static Intent getFacebookShareIntent(Context context, String title, String url) {
        Intent intent = null;

        final String[] facebookApps = { "com.facebook.katana" };

        Intent checkIntent = new Intent();
        checkIntent.setType("text/plain");

        final List<ResolveInfo> list = context.getPackageManager()
                .queryIntentActivities(checkIntent, PackageManager.MATCH_DEFAULT_ONLY);

        outer:
        for (String appPackage : facebookApps) {
            for (ResolveInfo resolveInfo : list) {
                String packageName = resolveInfo.activityInfo.packageName;

                if (packageName != null && packageName.startsWith(appPackage)) {
                    checkIntent.setPackage(packageName);
                    checkIntent.putExtra(Intent.EXTRA_TEXT, url);
                    checkIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                    intent = checkIntent;
                    break outer;
                }
            }
        }

        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://m.facebook.com/sharer.php?u=" + Common.urlEncode(url)));
        }

        return intent;
    }

    public static Intent getVkShareIntent(Context context, String title, String url) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/share.php?url=" + Common.urlEncode(url)));
    }

    public static Intent getEmailIntent(Context context, String email, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", email, null)).putExtra(Intent.EXTRA_SUBJECT, subject)
                .putExtra(Intent.EXTRA_TEXT, body);

        return Intent.createChooser(intent, context.getString(R.string.core_send_email_using));
    }
    */

    public static Intent getEmailIntent(Context context, String email) {
        Intent intent = new Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", email, null)).putExtra(
                Intent.EXTRA_SUBJECT,
                context.getString(R.string.core_app_name));

        return Intent.createChooser(intent, context.getString(R.string.core_send_email_using));
    }
}
