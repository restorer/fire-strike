package com.eightsines.firestrike.procedural.board;

import com.eightsines.firestrike.procedural.layer.Layer;

using Safety;

class Board extends Layer<Entry> {
    private static var EMPTY_CREATOR = () -> Entry.createEmpty();

    public var script : SafeArray<String> = [];

    public function new(width : Int, height : Int) {
        super(width, height, EMPTY_CREATOR);
    }

    public function toString() : String {
        return __toString("Board");
    }

    override public function set(row : Int, col : Int, entry : Entry) : Void {
        var existingEntry = entries[row][col];

        var _floor = entry.floor;
        var _ceiling = entry.ceiling;

        if (_floor != null) {
            if (_floor.tl != null) {
                existingEntry.floor.unsafe().tl = _floor.tl;
            }

            if (_floor.tr != null) {
                existingEntry.floor.unsafe().tr = _floor.tr;
            }

            if (_floor.bl != null) {
                existingEntry.floor.unsafe().bl = _floor.bl;
            }

            if (_floor.br != null) {
                existingEntry.floor.unsafe().br = _floor.br;
            }
        }

        if (entry.arrow != null) {
            existingEntry.arrow = entry.arrow;
        }

        if (entry.cell != null) {
            existingEntry.cell = entry.cell;
        }

        if (_ceiling != null) {
            if (_ceiling.tl != null) {
                existingEntry.ceiling.unsafe().tl = _ceiling.tl;
            }

            if (_ceiling.tr != null) {
                existingEntry.ceiling.unsafe().tr = _ceiling.tr;
            }

            if (_ceiling.bl != null) {
                existingEntry.ceiling.unsafe().bl = _ceiling.bl;
            }

            if (_ceiling.br != null) {
                existingEntry.ceiling.unsafe().br = _ceiling.br;
            }
        }

        if (entry.mark != null) {
            existingEntry.mark = entry.mark;
        }

        if (entry.noTrans != null) {
            existingEntry.noTrans = entry.noTrans;
        }
    }

    public function copy() : Board {
        var result = new Board(width, height);
        result.script = script;

        return cast copyInto(result);
    }
}
