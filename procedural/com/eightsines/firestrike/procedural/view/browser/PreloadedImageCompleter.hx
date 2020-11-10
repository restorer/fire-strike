package com.eightsines.firestrike.procedural.view.browser;

class PreloadedImageCompleter {
    public var callback : () -> Void;
    public var ensureSources : Array<String>;

    public function new(callback : () -> Void, ensureSources : Array<String>) {
        this.callback = callback;
        this.ensureSources = ensureSources;
    }
}
