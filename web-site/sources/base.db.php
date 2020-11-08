<?php

defined('POPLR') or die();

require_once(POPLR_SOURCES . '/base.core.php');

class Poplr_Db {
	protected $poplr = null;
	protected $dbh = null;

	public $prefix = '';

	public function __construct($poplr, $settings) {
		$this->poplr = $poplr;
		$this->prefix = Poplr_Core::defaultize($settings, 'prefix', '');

		try {
			$dsn = Poplr_Core::defaultize($settings, 'dsn', '');
			$options = array();

			if (strpos($dsn, 'mysql:') === 0) {
				$options[PDO::MYSQL_ATTR_INIT_COMMAND] = "SET NAMES 'UTF8'";
			}

			$this->dbh = new PDO(
				$dsn,
				Poplr_Core::defaultize($settings, 'username', ''),
				Poplr_Core::defaultize($settings, 'password', ''),
				$options
			);

			$initSql = Poplr_Core::defaultize($settings, 'initSql', array());

			if (!empty($initSql)) {
				foreach ($initSql as $sql) {
					$this->execute($sql);
				}
			}
		} catch (PDOException $e) {
			$poplr->fatalError('db.connectionFailed', $e->getMessage());
		}
	}

	public function quote($value) {
		return $this->dbh->quote($value);
	}

	public function queryAll($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if ($sth === false) {
				$this->poplr->fatalError('db.sqlError', '"prepare" in "queryAll" failed -- ' . $sql);
				return;
			}

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', '"queryAll" failed -- ' . $sql);
				return;
			}

			$result = $sth->fetchAll(PDO::FETCH_ASSOC);
			$sth->closeCursor();
			return $result;
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}

	public function queryRow($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if ($sth === false) {
				$this->poplr->fatalError('db.sqlError', '"prepare" in "queryRow" failed -- ' . $sql);
				return;
			}

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', '"queryRow" failed -- ' . $sql);
				return;
			}

			$result = $sth->fetch(PDO::FETCH_ASSOC);
			$sth->closeCursor();
			return $result;
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}

	public function queryOne($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if ($sth === false) {
				$this->poplr->fatalError('db.sqlError', '"prepare" in "queryOne" failed -- ' . $sql);
				return;
			}

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', '"queryOne" failed -- ' . $sql);
				return;
			}

			$result = $sth->fetchColumn();
			$sth->closeCursor();
			return $result;
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}

	public function execute($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if ($sth === false) {
				$this->poplr->fatalError('db.sqlError', '"prepare" in "execute" failed -- ' . $sql);
				return;
			}

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', '"execute" failed -- ' . $sql);
				return;
			}

			return $sth->rowCount();
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}

	public function insert($sql, $params=array()) {
		try {
			$sth = $this->dbh->prepare($sql);

			if ($sth === false) {
				$this->poplr->fatalError('db.sqlError', '"prepare" in "insert" failed -- ' . $sql);
				return;
			}

			if (!@$sth->execute($params)) {
				$this->poplr->fatalError('db.sqlError', '"insert" failed -- ' . $sql);
				return;
			}

			return $this->dbh->lastInsertId();
		} catch (PDOException $e) {
			$this->poplr->fatalError('db.sqlError', $e->getMessage());
		}
	}
}
