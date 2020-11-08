package com.eightsines.firestrike.procedural.section;

import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.util.Sequence;

using Safety;

class SectionFence implements SectionDivider {
    public var from : Point;
    public var to : Point;
    public var normal : Point;
    public var rect : Rect;
    public var size : Int;
    public var otherFence : Null<SectionFence> = null;
    public var gates : SafeArray<SectionGate> = [];
    public var __passable : Bool = false;
    public var __id : Int;
    public var __copyOtherFenceId : Int = 0;

    public function new(from : Point, to : Point, normal : Point, ?rect : Rect, ?size : Int, ?__id : Int) {
        this.from = from;
        this.to = to;
        this.normal = normal;
        this.rect = (rect == null ? Rect.fromPoints(from, to) : rect);
        this.size = (size == null ? from.lineSizeTo(to) : size);
        this.__id = (__id == null ? Sequence.nextId() : __id);
    }

    public function copyWithoutOtherSide() : SectionFence {
        var result = new SectionFence(from.copy(), to.copy(), normal.copy(), rect.copy(), size, __id);

        result.gates = gates.map((gate) -> gate.copy());
        result.__passable = __passable;
        result.__copyOtherFenceId = __copyOtherFenceId;

        return result;
    }

    public function getOtherDivider() : SectionDivider {
        return otherFence.sure();
    }

    public static function fromConnection(connection : SectionConnection) : SectionFence {
        var result = new SectionFence(connection.from, connection.to, connection.normal);
        result.__passable = connection.__passable;
        return result;
    }
}
