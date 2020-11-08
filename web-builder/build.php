<?php

die('This script must be corrected so that it can be used.');
define('TOOLS_DIR', '<TOOLS DIR>');

?>
<!doctype html>
<html>
<head>
	<meta charset="utf-8" />
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
	<meta name="viewport" content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=no" />
	<title>Build</title>
	<style>
body {
	font-size: 12px;
	line-height: 16px;
	font-family: sans-serif;
	margin: 10px;
	padding: 0;
}

form {
	margin: 0;
	padding: 0;
}

pre {
	padding: 0;
	margin: 0;
}

.output {
	margin: 10px 0;
	background: #EEE;
}

.output .header {
	padding: 10px;
	background: #080;
	color: #FFF;
	font-weight: bold;
}

.output .content {
	padding: 10px;
}
	</style>
</head>
<body>
<?php if (isset($_GET['solver'])) : ?>
	<?php $name = preg_replace('/[^a-z0-9\-_.]/i', '', $_GET['solver']) ?>
	<a href="?r=<?php echo time() . '"' ?>>&larr; Back</a>
	<div class="output">
		<div class="header">
			<?php echo htmlspecialchars($name) ?>
		</div>
		<div class="content">
			<?php if (file_exists(dirname(__FILE__) . "/build/logs/solver.{$name}.log")) : ?>
				<pre><?php echo file_get_contents(dirname(__FILE__) . "/build/logs/solver.{$name}.log") ?></pre>
			<?php else : ?>
				<b>Not found</b>
			<?php endif ?>
		</div>
	</div>
<?php elseif (file_exists(dirname(__FILE__) . '/build/.lock') || isset($_POST['build'])) : ?>
	<?php
		if (isset($_POST['build'])) {
			if (exec(TOOLS_DIR . '/fsr/server-build.sh > /dev/null &') != '') {
?>
<b>Mystical error happened.</b><br /><br />
<a href="?r=<?php echo time() . '"' ?>>&larr; Back</a>
<?php
				exit();
			}
		}
	?>

	Building. <span id="refresh">Refresh in <span id="seconds">5</span> seconds.</span>

	<script type="text/javascript">
		function doit() {
			var val = Number(document.getElementById('seconds').innerHTML);

			if (val <= 1) {
				document.getElementById('refresh').innerHTML = 'Refreshing now.';
				location.href = '?r=' + (new Date()).valueOf();
				return;
			}

			document.getElementById('seconds').innerHTML = val - 1;
			setTimeout(doit, 1000);
		}

		setTimeout(doit, 1000);
	</script>
<?php else : ?>
	<form method="post">
		<?php if (file_exists(dirname(__FILE__) . '/build/logs/builder.log')) : ?>
			<input type="submit" name="build" value="Re-build" />
		<?php else : ?>
			<input type="submit" name="build" value="Build" />
		<?php endif ?>
	</form>
	<?php if (file_exists(dirname(__FILE__) . '/build/fsr-srvbuild.apk')) : ?>
		<div class="output">
			<div class="header">Download</div>
			<div class="content">
				<a href="build/fsr-srvbuild.apk">fsr-srvbuild.apk</a>
			</div>
		</div>
	<?php endif ?>
	<?php if (file_exists(dirname(__FILE__) . '/build/logs/builder.log')) : ?>
		<div class="output">
			<div class="header">Build log</div>
			<div class="content">
				<?php echo nl2br(htmlspecialchars(file_get_contents(dirname(__FILE__) . '/build/logs/builder.log'))) ?>
			</div>
		</div>
	<?php endif ?>
	<?php
		$list = array();
		$dh = opendir(dirname(__FILE__) . '/build/logs/');

		if ($dh) {
			while (($name = readdir($dh)) !== false) {
				if (preg_match('/^solver\.([^.]+)\.log$/', $name, $mt)) {
					$list[] = $mt[1];
				}
			}

			closedir($dh);
		}

		natsort($list);
		$rand = time();
	?>
	<?php if (count($list)) : ?>
		<div class="output">
			<div class="header">Solver logs</div>
			<div class="content">
				<?php foreach ($list as $name) : ?>
					<a href="?solver=<?php
						echo htmlspecialchars($name) . '&r=' . $rand . '"'
					?>><?php echo htmlspecialchars($name) ?></a><br />
				<?php endforeach ?>
			</div>
		</div>
	<?php endif ?>
<?php endif ?>
</body>
</html>
