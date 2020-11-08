package zame.game.flavour.config;

import zame.game.R;
import zame.game.engine.state.ProfileLevel;

public final class ProfileConfig {
    public static ProfileLevel[] createLevelsList() {
        return new ProfileLevel[] { new ProfileLevel("e00m00", -1, R.drawable.char_commander, ProfileLevel.AD_NONE),
                new ProfileLevel("e00m01", -1, R.drawable.char_commander, ProfileLevel.AD_NONE),

                new ProfileLevel("e01m01", 1, R.drawable.char_soldier, ProfileLevel.AD_REWARDED),
                new ProfileLevel("e01m02", 1, R.drawable.char_soldier, ProfileLevel.AD_REWARDED),
                new ProfileLevel("e01m03", 1, R.drawable.char_soldier, ProfileLevel.AD_REWARDED),
                new ProfileLevel("e01m04", 1, R.drawable.char_soldier, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e01m05", 1, R.drawable.char_soldier, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e01m06", 1, R.drawable.char_soldier, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e01m07", 1, R.drawable.char_soldier, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e01m08", 1, R.drawable.char_soldier, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e01m09", 1, R.drawable.char_soldier, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e01m10", 1, R.drawable.char_soldier, ProfileLevel.AD_INTERSTITIAL),

                new ProfileLevel("e02m01", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m02", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m03", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m04", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m05", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m06", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m07", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m08", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m09", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e02m10", 2, R.drawable.char_commander, ProfileLevel.AD_INTERSTITIAL),

                new ProfileLevel("e03m01", 3, R.drawable.char_doctor, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e03m02", 3, R.drawable.char_doctor, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e03m03", 3, R.drawable.char_doctor, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e03m04", 3, R.drawable.char_doctor, ProfileLevel.AD_INTERSTITIAL),
                new ProfileLevel("e03m05", 3, R.drawable.char_doctor, ProfileLevel.AD_INTERSTITIAL),

                new ProfileLevel("e99m99", -2, R.drawable.char_doctor, ProfileLevel.AD_NONE),

                new ProfileLevel("e77m01", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m02", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m03", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m04", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m05", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m06", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m07", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m08", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m09", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE),
                new ProfileLevel("e77m10", -3, R.drawable.char_soldier, ProfileLevel.AD_NONE), };
    }

    private ProfileConfig() {}
}
