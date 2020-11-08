package com.eightsines.firestrike.procedural.view.browser;

import js.html.ImageElement;

class BrowserViewerTile {
    public var x : Int;
    public var y : Int;
    public var w : Int;
    public var h : Int;
    public var image : Null<ImageElement> = null;
    public var iw : Null<Int> = null;
    public var ih : Null<Int> = null;

    public function new(x : Int, y : Int, w : Int, h : Int) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}
