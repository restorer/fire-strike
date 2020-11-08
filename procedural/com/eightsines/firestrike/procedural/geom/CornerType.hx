package com.eightsines.firestrike.procedural.geom;

@:enum
abstract CornerType(Int) to Int {
    var None = 0;
    var Inner = 1;
    var Outer = 2;
}
