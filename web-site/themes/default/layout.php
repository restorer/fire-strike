<?php defined('POPLR') or die() ?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
    <title><?php echo htmlspecialchars($poplr->title) ?></title>
    <meta name="description" content="<?php echo htmlspecialchars($poplr->title) ?>" />
    <meta name="author" content="EightSines Studio" />
    <meta name="viewport" content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover" />
    <?php foreach ($poplr->metas as $meta) : ?>
        <meta <?php echo Poplr_Core::renderAttributes($meta) ?> />
    <?php endforeach ?>
    <?php foreach ($poplr->links as $link) : ?>
        <link <?php echo Poplr_Core::renderAttributes($link) ?> />
    <?php endforeach ?>
    <?php foreach ($poplr->scripts['head'] as $script) : ?>
        <?php echo Poplr_Core::renderScript($script) ?>
    <?php endforeach ?>
</head>
<body>
    <div class="l-container">
        <div class="header l-section">
            <div class="header__discontinued">Discontinued<br />at 2020-04-20</div>
            <h1 class="header__title">
                <?php if ($poplr->actionName === 'index' || !empty($poplr->params['noHomeLink'])) : ?>
                    <?php echo $poplr->etxt('app.layout.title') ?>
                <?php else : ?>
                    <a href="<?php echo $poplr->actionUrl('index') ?>"><?php echo $poplr->etxt('app.layout.title') ?></a>
                <?php endif ?>
            </h1>
            <p class="header__subtitle"><?php echo $poplr->txt('app.layout.subtitle') ?></p>
            <ul class="header__languages">
                <?php foreach ($poplr->modules['core']->languages as $k => $v) : ?>
                    <li class="header__languages-item">
                        <?php if ($v === $poplr->language) : ?>
                            <strong class="header__languages-active"><?php echo htmlspecialchars($k) ?></strong>
                        <?php else : ?>
                            <a class="header__languages-link" href="<?php echo $poplr->actionUrl(true, array('hl' => $k)) ?>">
                                <?php echo htmlspecialchars($k) ?>
                            </a>
                        <?php endif ?>
                    </li>
                <?php endforeach ?>
            </ul>
        </div>
        <?php $poplr->contentTemplate() ?>
    </div>
    <div class="footer">
        <?php if (empty($poplr->params['noFooterLinks'])) : ?>
            <?php if ($poplr->actionName === 'privacy') : ?>
                <strong class="footer__active"><?php echo $poplr->etxt('app.layout.privacy') ?></strong>
            <?php else : ?>
                <a class="footer__link" href="<?php echo $poplr->actionUrl('privacy') ?>"><?php echo $poplr->etxt('app.layout.privacy') ?></a>
            <?php endif ?>
            <!-- --> <!-- -->
            <?php if ($poplr->actionName === 'licenses') : ?>
                <strong class="footer__active"><?php echo $poplr->etxt('app.layout.licenses') ?></strong>
            <?php else : ?>
                <a class="footer__link" href="<?php echo $poplr->actionUrl('licenses') ?>"><?php echo $poplr->etxt('app.layout.licenses') ?></a>
            <?php endif ?>
            <!-- --> <!-- -->
        <?php endif ?>
        <span class="footer__text">(c) 2018 EightSines Studio</span>
    </div>
    <?php foreach ($poplr->scripts['foot'] as $script) : ?>
        <?php echo Poplr_Core::renderScript($script) ?>
    <?php endforeach ?>
    <?php /*

(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
ga('create', 'UA-1782921-23', 'zame-dev.org');
ga('send', 'pageview');
</script>

    */ ?>
</body>
</html>