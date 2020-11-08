<?php

defined('POPLR') or die();

class Poplr_AppCore {
	public $poplr = null;

	public $languages = array(
		'en' => 'default',
		'ru' => 'russian',
	);

	public function __construct($poplr) {
		$this->poplr = $poplr;
		$this->process();
	}

	public function process() {
		$this->poplr->availActions['index'] = array();
		$this->poplr->availActions['help'] = array();
		$this->poplr->availActions['dislike'] = array();
		$this->poplr->availActions['privacy'] = array();
		$this->poplr->availActions['licenses'] = array();
		$this->poplr->availActions['viewdislikes'] = array();

		$this->poplr->afterDefaults[] = array($this, 'afterDefaults');

		if (!$this->setLanguage($this->poplr->fromGet('hl'))) {
			$this->setLanguage($this->poplr->fromCookie('hl'));
		}
	}

	public function afterDefaults() {
		$this->poplr->metas[] = array(
			'name' => 'theme-color',
			'content' => '#121212',
		);

		$this->poplr->metas[] = array(
			'name' => 'msapplication-TileColor',
			'content' => '#121212',
		);

		$this->poplr->metas[] = array(
			'name' => 'msapplication-TileImage',
			'content' => "{$this->poplr->httpRoot}/ms-icon-144x144.png",
		);

		foreach (array('57x57', '60x60', '72x72', '76x76', '114x114', '120x120', '144x144', '152x152', '180x180') as $sizes) {
			$this->poplr->links[] = array(
				'rel' => 'apple-touch-icon',
				'sizes' => $sizes,
				'href' => "{$this->poplr->httpRoot}/apple-touch-icon-{$sizes}.png",
			);
		}

		foreach (array('16x16', '32x32', '96x96', '192x192') as $sizes) {
			$this->poplr->links[] = array(
				'rel' => 'icon',
				'type' => 'image/png',
				'sizes' => $sizes,
				'href' => "{$this->poplr->httpRoot}/favicon-{$sizes}.png",
			);
		}

		$this->poplr->links[] = array(
			'rel' => 'manifest',
			'href' => "{$this->poplr->httpRoot}/manifest.json",
		);
	}

	public function setLanguage($languageKey) {
		if (!array_key_exists($languageKey, $this->languages)) {
			return false;
		}

		$this->poplr->language = $this->languages[$languageKey];
		$this->poplr->sendCookie('hl', $languageKey, 60*60*24*30);
		return true;
	}
}

$poplr->modules['core'] = new Poplr_AppCore($poplr);
