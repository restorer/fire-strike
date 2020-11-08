<?php

defined('POPLR') or die();

class Poplr_ActionIndex {
	public $poplr = null;

	public function __construct($poplr) {
		$this->poplr = $poplr;
		$this->process();
	}

	public function process() {
		$this->poplr->afterDefaults[] = array($this, 'afterDefaults');
	}

	public function afterDefaults() {
		$this->poplr->links[] = Poplr_Core::makeStylesheetSpec($this->poplr->getThemedUrl('css/jquery.bxslider.css'));

		$this->poplr->scripts['foot'][] = array('src' => $this->poplr->getThemedUrl('js/jquery.js'));
		$this->poplr->scripts['foot'][] = array('src' => $this->poplr->getThemedUrl('js/jquery.bxslider.js'));

		$this->poplr->scripts['foot'][] = array('inline' => '
$(function() {
	$(".home__gallery-slides").bxSlider({
		infiniteLoop: true,
		pager: false,
		slideWidth: 780
	});
});
		');
	}
}

$poplr->actions['index'] = new Poplr_ActionIndex($poplr);
