<?php

define('GRAPHICS_PATH', __DIR__ . '/graphics/');

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-type');

if (isset($_SERVER['REQUEST_METHOD']) && strtoupper($_SERVER['REQUEST_METHOD']) === 'OPTIONS') {
    return;
}

if (realpath(GRAPHICS_PATH) === false) {
    echo json_encode(array(
        'error' => 'Внутренняя ошибка (invalid GRAPHICS_PATH)',
    ));

    return;
}

$path = isset($_POST['path']) ? $_POST['path'] : null;

if (!is_string($path) || trim($path) === '') {
    echo json_encode(array(
        'error' => 'Внутренняя ошибка (no path)',
    ));

    return;
}

$texturePath = realpath(GRAPHICS_PATH . trim($path));

if ($texturePath === false || strpos($texturePath, realpath(GRAPHICS_PATH)) !== 0 || !is_file($texturePath) || !is_writable($texturePath)) {
    echo json_encode(array(
        'error' => 'Внутренняя ошибка (invalid path)',
        'texturePath' => $texturePath,
        'GRAPHICS_PATH' => realpath(GRAPHICS_PATH),
        'is_file' => is_file($texturePath),
        'is_writable' => is_writable($texturePath),
    ));

    return;
}

$textureFile = isset($_FILES['texture']) ? $_FILES['texture'] : null;

if (!$textureFile || $textureFile['error'] !== UPLOAD_ERR_OK || !is_uploaded_file($textureFile['tmp_name'])) {
    echo json_encode(array(
        'error' => 'Ошибка загрузки',
    ));

    return;
}

if ($textureFile['size'] === 0) {
    echo json_encode(array(
        'error' => 'Пустой файл',
    ));

    return;
}

if ($textureFile['size'] > 1024 * 1024 * 10) {
    echo json_encode(array(
        'error' => 'Файл больше чем 10 мб',
    ));

    return;
}

$size = @getimagesize($textureFile['tmp_name']);

if (!$size || !in_array($size[2], array(IMAGETYPE_GIF, IMAGETYPE_PNG, IMAGETYPE_JPEG), true)) {
    echo json_encode(array(
        'error' => 'Можно загружать только .gif, .png, .jpg и .jpeg',
    ));

    return;
}

switch ($size[2]) {
    case IMAGETYPE_GIF:
        $im = @imagecreatefromgif($textureFile['tmp_name'] /* , $bgcolor */);
        break;

    case IMAGETYPE_PNG:
        $im = @imagecreatefrompng($textureFile['tmp_name']);
        imagesavealpha($im, true);
        break;

    case IMAGETYPE_JPEG:
        $im = @imagecreatefromjpeg($textureFile['tmp_name']);
        break;
}

if (!$im) {
    echo json_encode(array(
        'error' => 'Не удалось декодировать изображение',
    ));

    return;
}

if (!@imagepng($im, $texturePath)) {
    @imagedestroy($im);

    echo json_encode(array(
        'error' => 'Внутренняя ошибка (save)',
    ));

    return;
}

@chmod($texturePath, 0666);
@imagedestroy($im);

echo json_encode(array(
    'error' => null,
));
