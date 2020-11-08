package com.eightsines.firestrike.procedural.geom;

import com.eightsines.firestrike.procedural.util.IntMath;

class PolyboxEdge {
    public var fromIndex : Int;
    public var toIndex : Int;
    public var from : Point;
    public var to : Point;
    public var direction : Point;
    public var normal : Point; // TODO: invert normal, so it will be pointing outside

    public function new(fromIndex : Int, toIndex : Int, from : Point, to : Point) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.from = from;
        this.to = to;

        direction = new Point(IntMath.sign(to.row - from.row), IntMath.sign(to.col - from.col));
        normal = new Point(IntMath.sign(from.col - to.col), IntMath.sign(to.row - from.row));
    }

    public function walk(cb : (Point, Bool) -> Void, includeCorners : Bool = true) : Void {
        var row = from.row;
        var col = from.col;

        if (includeCorners) {
            while (true) {
                var isLastPoint = (row == to.row && col == to.col);
                cb(new Point(row, col), isLastPoint);

                if (isLastPoint) {
                    break;
                }

                row += direction.row;
                col += direction.col;
            }
        } else {
            if (row == to.row && col == to.col) {
                return;
            }

            while (true) {
                row += direction.row;
                col += direction.col;

                if (row == to.row && col == to.col) {
                    break;
                }

                cb(new Point(row, col), (row + direction.row == to.row && col + direction.col == to.col));
            }
        }
    }
}
