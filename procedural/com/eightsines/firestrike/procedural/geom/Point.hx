package com.eightsines.firestrike.procedural.geom;

import com.eightsines.firestrike.procedural.util.IntMath;

class Point {
    public static var UP : Point = new Point(-1, 0);
    public static var RIGHT : Point = new Point(0, 1);
    public static var DOWN : Point = new Point(1, 0);
    public static var LEFT : Point = new Point(0, -1);

    // Clockwise, starting from up
    public static var DIRECTIONS : SafeArray<Point> = [
        UP,
        RIGHT,
        DOWN,
        LEFT,
    ];

    // Clockwise, starting from upper-right
    public static var CORNERS : SafeArray<Point> = [
        new Point(-1, 1),
        new Point(1, 1),
        new Point(1, -1),
        new Point(-1, -1),
    ];

    public var row : Int;
    public var col : Int;

    public function new(row : Int, col : Int) {
        this.row = row;
        this.col = col;
    }

    public inline function copy() : Point {
        return new Point(row, col);
    }

    public inline function set(_row : Int, _col : Int) : Void {
        this.row = _row;
        this.col = _col;
    }

    public inline function setTo(other : Point) : Void {
        row = other.row;
        col = other.col;
    }

    public inline function add(_row : Int, _col : Int) : Point {
        return new Point(this.row + _row, this.col + _col);
    }

    public inline function addTo(other : Point) : Point {
        return new Point(row + other.row, col + other.col);
    }

    public inline function sub(other : Point) : Point {
        return new Point(row - other.row, col - other.col);
    }

    public inline function lineSizeTo(other : Point) : Int {
        return IntMath.abs(other.row - row) + IntMath.abs(other.col - col) + 1;
    }

    public inline function mul(_row : Int, _col : Int) : Point {
        return new Point(this.row * _row, this.col * _col);
    }

    public inline function scalar(v : Int) : Point {
        return mul(v, v);
    }

    public inline function divScalar(v : Int) : Point {
        return new Point(Std.int(row / v), Std.int(col / v));
    }

    public inline function rotateLeft() : Point {
        return new Point(-col, row);
    }

    public inline function equals(other : Point) : Bool {
        return (row == other.row && col == other.col);
    }

    public inline function key() : String {
        return keyFor(row, col);
    }

    public function toString() : String {
        return 'Point(row=${row}, col=${col})';
    }

    @SuppressWarnings("checkstyle:HiddenField")
    public static inline function keyFor(row : Int, col : Int) : String {
        return '${row}:${col}';
    }

    public static function directionFor(point : Point) : Int {
        for (i in 0 ... DIRECTIONS.length) {
            if (DIRECTIONS[i].equals(point)) {
                return i;
            }
        }

        return -1;
    }
}
