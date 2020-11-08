package com.eightsines.firestrike.procedural.view.browser;

import js.html.Image;

using Safety;

class PreloadedImage {
    private var completers : SafeArray<PreloadedImageCompleter> = [];
    private var onLoadCb : Null<() -> Void> = null;
    private var onErrorCb : Null<() -> Void> = null;

    public var isLoaded(default, null) : Bool = false;
    public var image(default, null) : Null<Image> = null;
    public var width(default, null) : Int = 0;
    public var height(default, null) : Int = 0;

    public function new(callback : () -> Void, ensureSources : SafeArray<String>) {
        completers.push(new PreloadedImageCompleter(callback, ensureSources));
    }

    public function getAndClearCompleters() : SafeArray<PreloadedImageCompleter> {
        var result = completers;
        completers = [];
        return result;
    }

    public function addCompleter(completer : PreloadedImageCompleter) : Void {
        for (otherCompleter in completers) {
            if (Reflect.compareMethods(otherCompleter.callback, completer.callback)) {
                return;
            }
        }

        completers.push(completer);
    }

    public function preload(src : String, onLoadCb : () -> Void, onErrorCb : () -> Void) : Void {
        this.onLoadCb = onLoadCb;
        this.onErrorCb = onErrorCb;

        image = (new Image()).apply((image) -> {
            image.addEventListener("load", onLoadListener);
            image.addEventListener("error", onErrorListener);
            image.src = src;
        });
    }

    private function onLoadListener() : Void {
        isLoaded = true;
        width = image.sure().width;
        height = image.sure().height;
        onLoadCb();
    }

    private function onErrorListener() : Void {
        image = null;
        onErrorCb();
    }
}
