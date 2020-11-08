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

    public static inline function pushAll<T>(a : SafeArray<T>, b : SafeArray<T>) : SafeArray<T> {
        for (v in b) {
            a.push(v);
        }

        return a;
    }

    public static inline function isEmpty<T>(a : SafeArray<T>) : Bool {
        return (a.length == 0);
    }

    public static inline function first<T>(a : SafeArray<T>) : T {
        return a[0];
    }

    public static inline function last<T>(a : SafeArray<T>) : T {
        return a[a.length - 1];
    }

    public static inline function contains<T>(a : SafeArray<T>, v : T) : Bool {
        return (a.indexOf(v) >= 0);
    }

    public static inline function safeConcat<T>(a : SafeArray<T>, b : SafeArray<T>) : SafeArray<T> {
        return cast a.concat(b.stdArray());
    }

    public static inline function safeMap<T, S>(a : SafeArray<T>, f : (T) -> S) : SafeArray<S> {
        return cast a.map(f);
    }

    public static inline function stableSort<T>(a : SafeArray<T>, cmp : (T, T) -> Int) : Void {
        ArraySort.sort(a.stdArray(), cmp);
    }

    public static inline function toInt32(v : Int) : Int32 {
        // force clamp by adding zero
        return (cast v : haxe.Int32) + 0;
    }

    public static inline function optionate<T>(v : Null<T>) : Option<T> {
        return (v == null ? None : Some(v));
    }

    @:safety(unsafe)
    public static function instanceExt<T : {}, S : T>(value : T, c : Class<S>) : Null<S> {
        return Std.instance(value, c);
    }

    public static inline function runOr<T>(value : Null<T>, onNonNullCallback : (T) -> Void, onNullCallback : () -> Void) : Void {
        if (value != null) {
            onNonNullCallback(value);
        } else {
            onNullCallback();
        }
    }

    public static macro function arrayOfAny(value : Expr, extra : Array<Expr>) : Expr {
        var extraExprs = new Array<Expr>();

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
