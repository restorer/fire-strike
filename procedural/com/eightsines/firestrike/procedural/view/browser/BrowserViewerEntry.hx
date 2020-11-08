package com.eightsines.firestrike.procedural.view.browser;

import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.layer.BoolLayer;
import com.eightsines.firestrike.procedural.layer.IntLayer;

enum BrowserViewerEntry {
    BoolLayer(layer : BoolLayer, data : Null<String>);
    IntLayer(layer : IntLayer, data : Null<String>);
    Board(layer : Board, renderMode : Int, data : Null<String>);
}
