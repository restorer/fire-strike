package com.eightsines.firestrike.procedural.util;

import haxe.Int32;
import haxe.ds.ArraySort;
import haxe.ds.Option;

#if macro
    import haxe.macro.Context;
    import haxe.macro.Expr;
#end

class Tools {
    public static inline function also<T>(object : T, block : (T) -> Void) : T {
        block(object);
        return object;
    }

    public static inline function let<T, R>(object : T, block : (T) -> R) : R {
        return block(object);
    }

    public static inline function pushAll<T>(a : Array<T>, b : Array<T>) : Array<T> {
        for (v in b) {
            a.push(v);
        }

        return a;
    }

    public static inline function isEmpty<T>(a : Array<T>) : Bool {
        return (a.length == 0);
    }

    public static inline function first<T>(a : Array<T>) : T {
        return a[0];
    }

    public static inline function last<T>(a : Array<T>) : T {
        return a[a.length - 1];
    }

    public static inline function contains<T>(a : Array<T>, v : T) : Bool {
        return (a.indexOf(v) >= 0);
    }

    public static inline function stableSort<T>(a : Array<T>, cmp : (T, T) -> Int) : Void {
        ArraySort.sort(a, cmp);
    }

    public static inline function toInt32(v : Int) : Int32 {
        // force clamp by adding zero
        return (cast v : haxe.Int32) + 0;
    }

    public static inline function optionate<T>(v : Null<T>) : Option<T> {
        return (v == null ? None : Some(v));
    }

    public static inline function runOr<T>(value : Null<T>, onNonNullCallback : (T) -> Void, onNullCallback : () -> Void) : Void {
        if (value != null) {
            onNonNullCallback(value);
        } else {
            onNullCallback();
        }
    }

    macro public static function arrayOfAny(value : Expr, extra : Array<Expr>) : Expr {
        var extraExprs: Array<Expr> = [];

        for (expr in extra) {
            extraExprs.push(macro @:pos(Context.currentPos()) {
                a.push($e{expr});
            });
        }

        return macro @:pos(Context.currentPos()) {
            var a = new Array<Any>();
            a.push($e{value});
            $b{extraExprs};
            a;
        };
    }
}
