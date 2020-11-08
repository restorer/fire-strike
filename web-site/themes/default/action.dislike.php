<?php defined('POPLR') or die() ?>

<div class="section">
	<h2 class="section__title"><?php echo $poplr->etxt('app.dislike.title') ?></h2>
	<?php if ($poplrAction->thankYou) : ?>
		<p class="dislike__thanks l-section"><?php echo $poplr->etxt('app.dislike.thankYou') ?></p>
	<?php else : ?>
		<form class="dislike__form l-section" method="post">
			<textarea
				class="dislike__response"
				name="response"
				rows="10"
				cols="100"
				placeholder="<?php echo $poplr->etxt('app.dislike.placeholder') ?>"
			></textarea>
			<input class="dislike__send" type="submit" name="send" value="<?php echo $this->etxt('app.dislike.send') ?>" />
		</form>
	<?php endif ?>
</div>
