package com.eightsines.firestrike.procedural.geom;

import com.eightsines.firestrike.procedural.util.IntMath;

class Rect {
    public var row : Int;
    public var col : Int;
    public var height : Int;
    public var width : Int;

    public function new(row : Int, col : Int, height : Int, width : Int) {
        this.row = row;
        this.col = col;
        this.height = height;
        this.width = width;
    }

    public function copy() : Rect {
        return new Rect(row, col, height, width);
    }

    public function equals(other : Rect) : Bool {
        return (row == other.row && col == other.col && height == other.height && width == other.width);
    }

    public function expand(value : Int = 1) : Rect {
        return new Rect(row - value, col - value, height + value * 2, width + value * 2);
    }

    public function getArea() : Int {
        return width * height;
    }

    public function getMinSize() : Int {
        return IntMath.min(width, height);
    }

    public function getPoint(lineOffset : Int = 0) : Point {
        var pointRow = row;
        var pointCol = col;

        if (height > 1 && width == 1) {
            pointRow += lineOffset;
        } else if (width > 1 && height == 1) {
            pointCol += lineOffset;
        }

        return new Point(pointRow, pointCol);
    }

    public function getMaxLineOffset() : Int {
        return (height - 1) + (width - 1);
    }

    public function getLineDirection() : Point {
        return new Point(IntMath.sign(height - 1), IntMath.sign(width - 1));
    }

    public function union(other : Rect) : Rect {
        var startRow = IntMath.min(row, other.row);
        var startCol = IntMath.min(col, other.col);
        var postEndRow = IntMath.max(row + height, other.row + other.height);
        var postEndCol = IntMath.max(col + width, other.col + other.width);

        return new Rect(startRow, startCol, postEndRow - startRow, postEndCol - startCol);
    }

    public function addPoint(point : Point) : Rect {
        return new Rect(row + point.row, col + point.col, height, width);
    }

    public inline function rows() : IntIterator {
        return new IntIterator(row, row + height);
    }

    public inline function columns() : IntIterator {
        return new IntIterator(col, col + width);
    }

    public function points() : SafeArray<Point> {
        var points : SafeArray<Point> = [];

        for (r in 0 ... height) {
            for (c in 0 ... width) {
                points.push(new Point(row + r, col + c));
            }
        }

        return points;
    }

    public function borderPoints() : SafeArray<Point> {
        var points : SafeArray<Point> = [];

        for (c in 0 ... width) {
            points.push(new Point(row, col + c));
        }

        for (r in 1 ... height) {
            points.push(new Point(row + r, col + width - 1));
        }

        if (height > 1) {
            for (c in 1 ... width) {
                points.push(new Point(row + height - 1, col + width - c - 1));
            }
        }

        if (width > 1) {
            for (r in 1 ... height - 1) {
                points.push(new Point(row + height - r - 1, col));
            }
        }

        return points;
    }

    public inline function getFrom() : Point {
        return new Point(row, col);
    }

    public inline function getTo() : Point {
        return new Point(row + height - 1, col + width - 1);
    }

    public function toString() : String {
        return 'Rect(row=${row}, col=${col}, height=${height}, width=${width})';
    }

    public static inline function fromCoords(rowA : Int, colA : Int, rowB : Int, colB : Int) : Rect {
        return new Rect(IntMath.min(rowA, rowB), IntMath.min(colA, colB), IntMath.abs(rowB - rowA) + 1, IntMath.abs(colB - colA) + 1);
    }

    public static inline function fromPoints(a : Point, b : Point) : Rect {
        return fromCoords(a.row, a.col, b.row, b.col);
    }

    public static inline function fromOnePoint(point : Point) : Rect {
        return new Rect(point.row, point.col, 1, 1);
    }
}
