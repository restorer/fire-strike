package com.eightsines.firestrike.procedural.geom;

import org.zamedev.lib.ds.HashSet;

using Lambda;

class Polybox {
    public var points(default, null) : Array<Point> = [];

    public function new(?points : Array<Point>) {
        if (points != null) {
            this.points = points.copy();
        }
    }

    public function copy() : Polybox {
        return new Polybox(points);
    }

    public function deepCopy() : Polybox {
        return new Polybox(points.map((point) -> point.copy()));
    }

    public function isEmpty() : Bool {
        return (points.length == 0);
    }

    public function getBbox() : Null<Rect> {
        if (points.length == 0) {
            return null;
        }

        var minRow = points[0].row;
        var maxRow = points[0].row;
        var minCol = points[0].col;
        var maxCol = points[0].col;

        for (i in 1 ... points.length) {
            var point = points[i];

            if (point.row < minRow) {
                minRow = point.row;
            }

            if (point.row > maxRow) {
                maxRow = point.row;
            }

            if (point.col < minCol) {
                minCol = point.col;
            }

            if (point.col > maxCol) {
                maxCol = point.col;
            }
        }

        return new Rect(minRow, minCol, maxRow - minRow + 1, maxCol - minCol + 1);
    }

    public function isCornerPoint(point : Point) : Bool {
        for (p in points) {
            if (p.equals(point)) {
                return true;
            }
        }

        return false;
    }

    public inline function isPointInside(point : Point, includeBorder : Bool = true) : Bool {
        return isInside(point.row, point.col, includeBorder);
    }

    public function isInside(row : Int, col : Int, includeBorder : Bool = true) : Bool {
        var result = false;

        for (i in 0 ... points.length) {
            var firstPoint = points[i];
            var secondPoint = points[(i + 1) % points.length];

            if (((firstPoint.row >= row && secondPoint.row <= row) || (secondPoint.row >= row && firstPoint.row <= row))
                && ((firstPoint.col >= col && secondPoint.col <= col) || (secondPoint.col >= col && firstPoint.col <= col))
            ) {
                return includeBorder;
            }

            if (((firstPoint.row <= row && secondPoint.row > row) || (secondPoint.row <= row && firstPoint.row > row))
                && firstPoint.col < col
            ) {
                result = !result;
            }
        }

        return result;
    }

    public function getBorderPoints() : Array<Point> {
        var result : Array<Point> = [];

        if (points.length != 0) {
            var point = points[0].copy();
            var existingPoints = new HashSet<String>();

            result.push(point.copy());
            existingPoints.add(point.key());

            for (i in 1 ... points.length) {
                appendBorderPoints(result, existingPoints, point, points[i]);
            }

            appendBorderPoints(result, existingPoints, point, points[0]);
        }

        return result;
    }

    public function getEdges() : Array<PolyboxEdge> {
        var result : Array<PolyboxEdge> = [];

        for (i in 0 ... points.length) {
            var toIndex = (i + 1) % points.length;

            var from = points[i];
            var to = points[toIndex];

            result.push(new PolyboxEdge(i, toIndex, from.copy(), to.copy()));
        }

        return result;
    }

    private function appendBorderPoints(
        result : Array<Point>,
        existingPoints : HashSet<String>,
        point : Point,
        nextPoint : Point
    ) : Void {
        while (!point.equals(nextPoint)) {
            if (point.row < nextPoint.row) {
                point.row++;
            } else if (point.row > nextPoint.row) {
                point.row--;
            } else if (point.col < nextPoint.col) {
                point.col++;
            } else {
                point.col--;
            }

            if (!existingPoints.exists(point.key())) {
                result.push(point.copy());
                existingPoints.add(point.key());
            }
        }
    }

    public static function fromBorderPoints(points : Array<Point>) : Null<Polybox> {
        points = optimizePoints(points);

        if (points.length == 0) {
            return null;
        }

        var firstPoint = points[0];
        var lastPoint = points[points.length - 1];

        if (firstPoint.row != lastPoint.row && firstPoint.col != lastPoint.col) {
            return null;
        }

        return new Polybox(points);
    }

    public static function makeRect(row : Int, col : Int, width : Int, height : Int) : Polybox {
        return new Polybox([
            new Point(row, col),
            new Point(row, col + width - 1),
            new Point(row + height - 1, col + width - 1),
            new Point(row + height - 1, col),
        ]);
    }

    public static function fromRect(rect : Rect) : Polybox {
        return makeRect(rect.row, rect.col, rect.width, rect.height);
    }

    public static function optimizePoints(points : Array<Point>) : Array<Point> {
        var result : Array<Point> = [];

        for (i in 0 ... points.length) {
            var point = points[i];

            if (points.length == 1) {
                result.push(point);
                break;
            }

            var prevPoint = points[(i - 1 + points.length) % points.length];
            var nextPoint = points[(i + 1) % points.length];

            if (point.equals(prevPoint)) {
                continue;
            }

            if (point.row == prevPoint.row && prevPoint.row == nextPoint.row
                && ((prevPoint.col < point.col && point.col < nextPoint.col)
                    || (prevPoint.col > point.col && point.col > nextPoint.col)
                )
            ) {
                continue;
            }

            if (point.col == prevPoint.col && prevPoint.col == nextPoint.col
                && ((prevPoint.row < point.row && point.row < nextPoint.row)
                    || (prevPoint.row > point.row && point.row > nextPoint.row)
                )
            ) {
                continue;
            }

            result.push(point);
        }

        return result;
    }
}
