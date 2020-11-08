package com.eightsines.firestrike.procedural.view.browser;

class PreloadedImageCompleter {
    public var callback : () -> Void;
    public var ensureSources : SafeArray<String>;

    public function new(callback : () -> Void, ensureSources : SafeArray<String>) {
        this.callback = callback;
        this.ensureSources = ensureSources;
    }
}
