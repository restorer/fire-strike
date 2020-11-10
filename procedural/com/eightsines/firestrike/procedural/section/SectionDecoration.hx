package com.eightsines.firestrike.procedural.section;

import com.eightsines.firestrike.procedural.geom.Point;

class SectionDecoration {
    public var type : SectionDecorationType;
    public var points : Array<Point>;
    public var size : Int;

    public function new(type : SectionDecorationType, points : Array<Point>) {
        this.type = type;
        this.points = points;

        size = points.length;
    }

    public function copy() : SectionDecoration {
        return new SectionDecoration(type, points.map((point) -> point.copy()));
    }
}
