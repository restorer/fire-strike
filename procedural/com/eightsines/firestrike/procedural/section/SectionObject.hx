package com.eightsines.firestrike.procedural.section;

import com.eightsines.firestrike.procedural.geom.Point;

class SectionObject<T> {
    public var position : Point;
    public var type : T;

    public function new(position : Point, type : T) {
        this.position = position;
        this.type = type;
    }

    public function copy(?copiedType : T) : SectionObject<T> {
        return new SectionObject<T>(position.copy(), copiedType == null ? type : copiedType);
    }
}
