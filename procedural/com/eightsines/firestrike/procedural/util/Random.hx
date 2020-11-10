package com.eightsines.firestrike.procedural.util;

import haxe.Int32;
import haxe.Int64;

using com.eightsines.firestrike.procedural.util.Tools;

// http://developer.classpath.org/doc/java/util/Random-source.html

@SuppressWarnings("checkstyle:MagicNumber")
class Random {
    private static var MULTIPLIER : Int64 = Int64.make(0x05, 0xdeece66d);
    private static var ADDEND : Int64 = Int64.ofInt(0x0b);
    private static var MASK : Int64 = Int64.make(0xffff, 0xffffffff);

    public var seed : Int64;

    public function new(seed : Int64) {
        this.seed = seed;
    }

    private function next(bits : Int) : Int {
        seed = (seed * MULTIPLIER + ADDEND) & MASK;
        return (seed >>> (48 - bits)).low;
    }

    /**
        Returns the next pseudorandom, uniformly distributed boolean value.
    **/
    public inline function nextBool() : Bool {
        return (next(1) != 0);
    }

    /**
        Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the specified value (exclusive).
        If bound is not specified, returns the next pseudorandom, uniformly distributed int value.
    **/
    public function nextIntEx(?bound : Int) : Int {
        if (bound == null) {
            return next(32);
        }

        if (bound <= 0) {
            return 0;
        }

        var _bound = bound.toInt32();

        // i.e., bound is a power of 2
        if ((_bound & (-_bound)) == _bound) {
            return ((Int64.make(0, _bound) * next(31)) >>> 31).low;
        }

        var bits : Int32;
        var result : Int32;

        do {
            bits = next(31);
            result = bits % _bound;
        } while (bits - result + (_bound - 1) < 0);

        return result;
    }

    /**
        Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the specified value (inclusive).
    **/
    public inline function nextIntIn(bound : Int32) : Int {
        return nextIntEx(bound + 1);
    }

    public inline function nextIntExPow(bound : Int32, pow : Float) : Int {
        return Math.floor(nextFloatExPow(pow) * bound);
    }

    public inline function nextIntInPow(bound : Int32, pow : Float) : Int {
        return Math.floor(nextFloatExPow(pow) * (bound + 1));
    }

    /**
        Returns a pseudorandom float value between 0.0 (invlusive) and the specified value (exclusive).
        If bound is not specified, returns uniformly distributed float value between 0.0 (inclusive) and 1.0 (exclusive).
    **/
    public function nextFloatEx(?bound : Float) : Float {
        if (bound != null && bound <= 0.0) {
            return 0.0;
        }

        var result : Float = next(24) / 16777216.0; // (1 << 24) == 16777216
        return (bound == null) ? result : (result * bound);
    }

    /**
        Returns a pseudorandom float value between 0.0 (invlusive) and the specified value (incluside).
        If bound is not specified, returns uniformly distributed float value between 0.0 (inclusive) and 1.0 (incluside).
    **/
    public function nextFloatIn(?bound : Float) : Float {
        if (bound != null && bound <= 0.0) {
            return 0.0;
        }

        var result : Float = next(24) / 16777215.0; // ((1 << 24) - 1) == 16777215
        return (bound == null) ? result : (result * bound);
    }

    public inline function nextFloatExPow(pow : Float) : Float {
        return Math.pow(nextFloatEx(), pow);
    }

    /**
        Return a random integer between "from" (inclusive) and "to" (exclusive).
    **/
    public inline function nextIntRangeEx(from : Int, to : Int) : Int {
        return from + nextIntEx(to - from);
    }

    /**
        Return a random integer between "from" and "to", inclusive.
    **/
    public inline function nextIntRangeIn(from : Int, to : Int) : Int {
        return from + nextIntIn(to - from);
    }

    public inline function nextIntRangeExPow(from : Int, to : Int, pow : Float) : Int {
        return from + nextIntExPow(to - from, pow);
    }

    public inline function nextIntRangeInPow(from : Int, to : Int, pow : Float) : Int {
        return from + nextIntInPow(to - from, pow);
    }

    /**
        Return a random float between "from" (inclusive) and "to" (exclusive).
    **/
    public inline function nextFloatRangeEx(from : Float, to : Float) : Float {
        return from + nextFloatEx(to - from);
    }

    /**
        Return a random float between "from" and "to", inclusive.
    **/
    public inline function nextFloatRangeIn(from : Float, to : Float) : Float {
        return from + nextFloatIn(to - from);
    }

    /**
        Return a random item from an array. Will return null if the array is null or empty.
    **/
    public function nextFromArray<T>(list : Array<T>) : Null<T> { // "inline" removed due to bug in Haxe 4 preview
        // Can't inline this function due to but in haxe and / or hashlink
        return ((list == null || list.length == 0) ? null : list[nextIntEx(list.length)]);
    }

    public function nextFromArrayPow<T>(list : Array<T>, pow : Float) : Null<T> { // "inline" removed due to bug in Haxe 4 preview
        // Can't inline this function due to but in haxe and / or hashlink
        return ((list == null || list.length == 0) ? null : list[nextIntExPow(list.length, pow)]);
    }

    /**
        Shuffle an Array. This operation affects the array in place, and returns that array.
    **/
    public function shuffleArray<T>(list : Array<T>) : Array<T> {
        if (list != null && list.length != 0) {
            for (i in 0 ... list.length - 1) {
                var j = nextIntRangeEx(i, list.length);

                var tmp = list[i];
                list[i] = list[j];
                list[j] = tmp;
            }
        }

        return list;
    }

    public static function makeSeed(?value : Int) : Int64 {
        if (value == null) {
            value = Std.int(Date.now().getTime() % 4294967296.0); // 0x100000000 == 4294967296
        }

        return (Int64.make(0, value) ^ MULTIPLIER) & MASK;
    }
}
