package com.eightsines.firestrike.procedural.view;

import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.generator.Processor;
import com.eightsines.firestrike.procedural.layer.BoolLayer;
import com.eightsines.firestrike.procedural.layer.IntLayer;

class Viewer {
    private var processorCb : () -> Processor;

    public function new(processorCb : () -> Processor) {
        this.processorCb = processorCb;
    }

    public function dumpBoolLayer(layer : BoolLayer, ?data : String) : Void {
        if (processorCb().getVerboseLevel() > 0 && data != null) {
            log(data);
        }

        if (processorCb().getVerboseLevel() >= 4) {
            log(layer.toString());
        }
    }

    public function dumpIntLayer(layer : IntLayer, ?data : String) : Void {
        if (processorCb().getVerboseLevel() > 0 && data != null) {
            log(data);
        }

        if (processorCb().getVerboseLevel() >= 4) {
            log(layer.toString());
        }
    }

    public function dumpBoard(board : Board, renderMode : Int = 0, ?data : String) : Void {
        if (processorCb().getVerboseLevel() > 0 && data != null) {
            log(data);
        }

        if (processorCb().getVerboseLevel() >= 4) {
            log(board.toString());
        }
    }

    public function log(message : String) : Void {}
}
