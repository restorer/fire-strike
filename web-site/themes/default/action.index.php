<?php defined('POPLR') or die() ?>

<div class="home__gallery">
    <div class="home__gallery-slides">
        <div>
            <img src="<?php echo $poplr->getThemedUrl('images/illu-home-1.png') ?>" alt="" />
        </div>
        <div>
            <img src="<?php echo $poplr->getThemedUrl('images/illu-home-2.png') ?>" alt="" />
        </div>
        <div>
            <img src="<?php echo $poplr->getThemedUrl('images/illu-home-3.png') ?>" alt="" />
        </div>
        <div>
            <img src="<?php echo $poplr->getThemedUrl('images/illu-home-4.png') ?>" alt="" />
        </div>
    </div>
</div>
<?php /*
<div class="home__badge-container home__badge-container--gplay l-section">
    <a href="https://play.google.com/store/apps/details?id=com.eightsines.firestrike&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1">
        <?php if ($poplr->language === 'russian') : ?>
            <img alt="Доступно в Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/ru_badge_web_generic.png" />
        <?php else : ?>
            <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" />
        <?php endif ?>
    </a>
</div>
<p class="home__badge-legal l-section">
    <?php if ($poplr->language === 'russian') : ?>
        Google Play и логотип Google Play являются товарными знаками корпорации Google LLC.
    <?php else : ?>
        Google Play and the Google Play logo are trademarks of Google LLC.
    <?php endif ?>
</p>
<div class="home__badge-container l-section">
    <a href="http://www.amazon.com/gp/mas/dl/android?p=com.eightsines.firestrike&ref=mas_pm_fire_strike">
        <img alt="Available at Amazon Appstore" src="<?php echo $poplr->getThemedUrl('images/badge-amazon.png') ?>" />
    </a>
</div>
*/ ?>
<div class="home__badge-container l-section">
    <a href="https://f-droid.org/packages/com.eightsines.firestrike.opensource">
        <img alt="Get it on F-Droid" src="https://f-droid.org/badge/get-it-on.png" />
    </a>
</div>
<div class="section">
    <div class="section__content typography l-section">
        <?php echo $poplr->txt('app.index.content') ?>
    </div>
</div>
