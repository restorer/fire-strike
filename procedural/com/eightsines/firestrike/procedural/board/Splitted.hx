package com.eightsines.firestrike.procedural.board;

import haxe.ds.Option;

using Safety;

class Splitted<T> {
    public var tl : Null<T>;
    public var tr : Null<T>;
    public var bl : Null<T>;
    public var br : Null<T>;

    public function new(?tl : T, ?tr : T, ?bl : T, ?br : T) {
        this.tl = tl;
        this.tr = tr;
        this.bl = bl;
        this.br = br;
    }

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    public function toString() : String {
        var sb = new StringBuf();
        sb.add("Splitted(tl=");
        sb.add(tl == null ? "NULL" : Std.string(tl.unsafe()));
        sb.add(", tr=");
        sb.add(tr == null ? "NULL" : Std.string(tr.unsafe()));
        sb.add(", bl=");
        sb.add(bl == null ? "NULL" : Std.string(bl.unsafe()));
        sb.add(", br=");
        sb.add(br == null ? "NULL" : Std.string(br.unsafe()));
        sb.add(")");
        return sb.toString();
    }

    public static function createEmpty<T>() : Splitted<Option<T>> {
        return new Splitted(Option.None, Option.None, Option.None, Option.None);
    }

    @SuppressWarnings("checkstyle:HiddenField")
    public static function createFloor(tl : Floor, tr : Floor, bl : Floor, br : Floor) : Splitted<Option<Floor>> {
        return new Splitted<Option<Floor>>(Some(tl), Some(tr), Some(bl), Some(br));
    }

    @SuppressWarnings("checkstyle:HiddenField")
    public static function createCeiling(tl : Ceiling, tr : Ceiling, bl : Ceiling, br : Ceiling) : Splitted<Option<Ceiling>> {
        return new Splitted<Option<Ceiling>>(Some(tl), Some(tr), Some(bl), Some(br));
    }
}
