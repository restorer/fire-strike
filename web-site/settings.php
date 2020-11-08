<?php

defined('POPLR') or die();

$__settings = array(
	'siteName' => 'Fire Strike Retro',
	'httpRoot' => '/fire-strike',
	'db' => array(
		'dsn' => 'sqlite:/mnt/persistent/firestrike.db',
		'username' => '',
		'password' => '',
        'initSql' => array(
            'CREATE TABLE IF NOT EXISTS dislikes (id INTEGER PRIMARY KEY, createdAt TEXT NOT NULL, response TEXT NOT NULL)',
            'CREATE INDEX IF NOT EXISTS dislikes_response ON dislikes (response)'
        ),
	),
);

if (is_readable(__DIR__ . '/settings.local.php')) {
    $__settings = array_replace_recursive($__settings, require __DIR__ . '/settings.local.php');
}

return $__settings;
