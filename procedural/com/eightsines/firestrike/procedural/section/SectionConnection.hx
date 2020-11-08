package com.eightsines.firestrike.procedural.section;

import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.util.Sequence;

using Safety;

class SectionConnection implements SectionDivider {
    public var section : Null<Section>;
    public var from : Point;
    public var to : Point;
    public var normal : Point;
    public var rect : Rect;
    public var size : Int;
    public var otherConnection : Null<SectionConnection> = null;
    public var scenarioGate : Int = 0;
    public var gates : SafeArray<SectionGate> = [];
    public var __removed : Bool = false;
    public var __passable : Bool = false;
    public var __id : Int;
    public var __copySectionId : Int = 0;
    public var __copyOtherConnectionId : Int = 0;

    public function new(section : Null<Section>, from : Point, to : Point, normal : Point, ?rect : Rect, ?size : Int, ?__id : Int) {
        this.section = section;
        this.from = from;
        this.to = to;
        this.normal = normal;
        this.rect = (rect == null ? Rect.fromPoints(from, to) : rect);
        this.size = (size == null ? from.lineSizeTo(to) : size);
        this.__id = (__id == null ? Sequence.nextId() : __id);
    }

    public function copyWithoutOtherSide() : SectionConnection {
        var result = new SectionConnection(null, from.copy(), to.copy(), normal.copy(), rect.copy(), size, __id);

        result.scenarioGate = scenarioGate;
        result.gates = gates.map((gate) -> gate.copy());
        result.__removed = __removed;
        result.__passable = __passable;
        result.__copySectionId = __copySectionId;
        result.__copyOtherConnectionId = __copyOtherConnectionId;

        return result;
    }

    public inline function ensureSection() : Section {
        return section.sure();
    }

    public function getOtherDivider() : SectionDivider {
        return otherConnection.sure();
    }

    public function __setBothPassable(value : Bool) : Void {
        __passable = value;
        otherConnection.sure().__passable = value;
    }

    public function __setBothRemoved(value : Bool) : Void {
        __removed = value;
        otherConnection.sure().__removed = value;
    }

    public function setBothScenarioGates(value : Int) : Void {
        scenarioGate = value;
        otherConnection.sure().scenarioGate = value;
    }
}
