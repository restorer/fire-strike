<?php

defined('POPLR') or die();

class Poplr_ActionViewDislikes {
    public $poplr = null;
    public $dislikes = null;

    public function __construct($poplr) {
        $this->poplr = $poplr;
        $this->process();
    }

    public function process() {
        // (odmin + (key1 + d)), 12
        if ($this->poplr->fromPost('code') === '249b305bdacb') {
            $this->dislikes = $this->poplr->db->queryAll("SELECT id, createdAt, response FROM dislikes ORDER BY id DESC");
        }
    }
}

$poplr->actions['viewdislikes'] = new Poplr_ActionViewDislikes($poplr);
