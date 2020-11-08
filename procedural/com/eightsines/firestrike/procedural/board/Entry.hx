package com.eightsines.firestrike.procedural.board;

import haxe.ds.Option;

using Safety;

class Entry {
    public var floor : Null<Splitted<Option<Floor>>> = null;
    public var arrow : Null<Option<Arrow>> = null;
    public var cell : Null<Option<Cell>> = null;
    public var ceiling : Null<Splitted<Option<Ceiling>>> = null;
    public var mark : Null<Option<String>> = null;
    public var noTrans : Null<Option<Bool>> = null;

    public function new() {}

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    public function toString() : String {
        var sb = new StringBuf();
        sb.add("Entry(floor=");
        sb.add(floor == null ? "NULL" : floor.unsafe().toString());
        sb.add(", arrow=");
        sb.add(arrow == null ? "NULL" : Std.string(arrow.unsafe()));
        sb.add(", cell=");
        sb.add(cell == null ? "NULL" : Std.string(cell.unsafe()));
        sb.add(", ceiling=");
        sb.add(ceiling == null ? "NULL" : ceiling.unsafe().toString());
        sb.add(", mark=");
        sb.add(mark == null ? "NULL" : Std.string(mark.unsafe()));
        sb.add(", noTrans=");
        sb.add(noTrans == null ? "NULL" : Std.string(noTrans.unsafe()));
        sb.add(")");
        return sb.toString();
    }

    public static function createEmpty() : Entry {
        var entry = new Entry();

        entry.floor = Splitted.createEmpty();
        entry.arrow = None;
        entry.cell = None;
        entry.ceiling = Splitted.createEmpty();
        entry.mark = None;
        entry.noTrans = None;

        return entry;
    }

    public static function createFloor(tl : Floor, tr : Floor, bl : Floor, br : Floor) : Entry {
        var entry = new Entry();
        entry.floor = Splitted.createFloor(tl, tr, bl, br);
        return entry;
    }

    public static function createFloorEntry(
        tl : Option<Floor>,
        tr : Option<Floor>,
        bl : Option<Floor>,
        br : Option<Floor>
    ) : Entry {
        var entry = new Entry();
        entry.floor = new Splitted(tl, tr, bl, br);
        return entry;
    }

    public static function createCeiling(tl : Ceiling, tr : Ceiling, bl : Ceiling, br : Ceiling) : Entry {
        var entry = new Entry();
        entry.ceiling = Splitted.createCeiling(tl, tr, bl, br);
        return entry;
    }

    public static function createCeilingEntry(
        tl : Option<Ceiling>,
        tr : Option<Ceiling>,
        bl : Option<Ceiling>,
        br : Option<Ceiling>
    ) : Entry {
        var entry = new Entry();
        entry.ceiling = new Splitted(tl, tr, bl, br);
        return entry;
    }

    public static function createEmptyCell() : Entry {
        var entry = new Entry();
        entry.cell = None;
        return entry;
    }

    public static function createCell(value : Cell) : Entry {
        var entry = new Entry();
        entry.cell = Some(value);
        return entry;
    }

    public static function createEmptyArrow() : Entry {
        var entry = new Entry();
        entry.arrow = None;
        return entry;
    }

    public static function createArrow(value : Arrow) : Entry {
        var entry = new Entry();
        entry.arrow = Some(value);
        return entry;
    }

    public static function createMark(value : String) : Entry {
        var entry = new Entry();
        entry.mark = Some(value);
        return entry;
    }

    public static function createNoTrans(value : Bool = true) : Entry {
        var entry = new Entry();
        entry.noTrans = Some(value);
        return entry;
    }
}
