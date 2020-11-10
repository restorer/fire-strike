package com.eightsines.firestrike.procedural.geom;

import com.eightsines.firestrike.procedural.util.IntMath;

class Extend {
    private static inline final CALC_AREA_CENTER : Int = 256; // Can be any pretty big number

    public var tl : Int = 0;
    public var tr : Int = 0;
    public var bl : Int = 0;
    public var br : Int = 0;

    public function new() {}

    public function getMinSize() : Int {
        return IntMath.min(tl, IntMath.min(tr, IntMath.min(bl, br)));
    }

    public function getRect(row : Int, col : Int, spacing : Int = 1) : Rect {
        return Rect.fromCoords(
            row - IntMath.min(tl, tr) + spacing,
            col - IntMath.min(tl, bl) + spacing,
            row + IntMath.min(bl, br) - spacing,
            col + IntMath.min(tr, br) - spacing
        );
    }

    public function getArea() : Int {
        return getRect(CALC_AREA_CENTER, CALC_AREA_CENTER).getArea();
    }

    public function toString() : String {
        return 'Extend(tl=${tl}, tr=${tr}, bl=${bl}, br=${br})';
    }
}
