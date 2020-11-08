package com.eightsines.firestrike.procedural.util;

class IntMath {
    public static inline function min(a : Int, b : Int) : Int {
        return (a < b ? a : b);
    }

    public static inline function max(a : Int, b : Int) : Int {
        return (a > b ? a : b);
    }

    public static inline function abs(v : Int) : Int {
        return (v > 0 ? v : -v);
    }

    public static inline function sign(v : Int) : Int {
        return (v == 0 ? 0 : (v < 0 ? -1 : 1));
    }

    public static inline function square(v : Int) : Int {
        return (v * v);
    }

    public static function select(probability : Float, probabilities : SafeArray<Float>) : Int {
        for (i in 0 ... probabilities.length) {
            if (probability < probabilities[i]) {
                return i;
            }
        }

        return probabilities.length;
    }
}
