<?php defined('POPLR') or die() ?>

<div class="section">
    <h2 class="section__title"><?php echo $poplr->etxt('app.viewdislikes.title') ?></h2>
    <?php if ($poplrAction->dislikes === null) : ?>
        <form class="view-dislikes__form l-section" method="post">
            <input
                class="view-dislikes__code"
                name="code"
                autocomplete="off"
                placeholder="<?php echo $poplr->etxt('app.viewdislikes.code') ?>"
            />
            <input class="view-dislikes__send" name="send" type="submit" value="<?php echo $this->etxt('app.viewdislikes.send') ?>" />
        </form>
    <?php else : ?>
        <?php foreach ($poplrAction->dislikes as $row) : ?>
            <div class="section__content typography l-section">
                <h3><span><strong>#<?php echo htmlspecialchars($row['id']) ?></strong> <?php echo $row['createdAt'] ?></span></h3>
                <p><?php echo htmlspecialchars($row['response']) ?></p>
            </div>
        <?php endforeach ?>
    <?php endif ?>
</div>
