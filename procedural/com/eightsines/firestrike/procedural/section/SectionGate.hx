package com.eightsines.firestrike.procedural.section;

import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Rect;

class SectionGate {
    public var type : SectionGateType;
    public var from : Point;
    public var to : Point;
    public var normal : Point;
    public var rect : Rect;
    public var size : Int;

    public function new(type : SectionGateType, from : Point, to : Point, normal : Point, ?rect : Rect, ?size : Int) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.normal = normal;
        this.rect = (rect == null ? Rect.fromPoints(from, to) : rect);
        this.size = (size == null ? from.lineSizeTo(to) : size);
    }

    public function copy() : SectionGate {
        return new SectionGate(type, from.copy(), to.copy(), normal.copy(), rect.copy(), size);
    }
}
