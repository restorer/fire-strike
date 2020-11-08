package com.eightsines.firestrike.procedural.util;

import com.eightsines.firestrike.procedural.board.Entry;

using Safety;

class BoardUtils {
    public static function isEmptyEntry(entry : Entry) : Bool {
        var _floor = entry.floor.sure();
        var _ceiling = entry.ceiling.sure();

        return (_floor.tl.sure().match(None)
            && _floor.tr.sure().match(None)
            && _floor.bl.sure().match(None)
            && _floor.br.sure().match(None)
            && entry.arrow.sure().match(None)
            && entry.cell.sure().match(None)
            && _ceiling.tl.sure().match(None)
            && _ceiling.tr.sure().match(None)
            && _ceiling.bl.sure().match(None)
            && _ceiling.br.sure().match(None)
            && entry.mark.sure().match(None)
        );
    }
}
