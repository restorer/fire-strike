package com.eightsines.firestrike.procedural.section;

import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Rect;

interface SectionDivider {
    var normal : Point;
    var rect : Rect;
    var size : Int;
    var gates : SafeArray<SectionGate>;
    var __passable : Bool;

    function getOtherDivider() : SectionDivider;
}
