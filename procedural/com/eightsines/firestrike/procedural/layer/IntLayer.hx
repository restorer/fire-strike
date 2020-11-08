package com.eightsines.firestrike.procedural.layer;

import com.eightsines.firestrike.procedural.geom.EdgePoint;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Polybox;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.Layer; // {LayerReducer}
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.Pair;

using Lambda;
using Safety;
using com.eightsines.firestrike.procedural.util.Tools;

class IntLayer extends Layer<Int> {
    private static var EMPTY_CREATOR = () -> 0;

    public static var COND_IS_EMPTY = (entry) -> (entry == 0);
    public static var REDUCE_ALL_EMPTY = new LayerReducer<Int, Bool>(true, (_, _, entry, carry) -> (carry && entry == 0));

    public static inline var CARRY_ALL_EMPTY : Bool = true;

    public var currentEntry : Int = 0;
    private var visited : BoolLayer;

    public function new(width : Int, height : Int) {
        super(width, height, EMPTY_CREATOR);
        visited = new BoolLayer(width, height);
    }

    public function toString() : String {
        return __toString("IntLayer");
    }

    // Override to force use inline
    override public inline function set(row : Int, col : Int, entry : Int) : Void {
        #if php
            php.Syntax.code("{0}->entries->arr[{1}]->arr[{2}] = {3}", this, row, col, entry);
        #else
            entries[row][col] = entry;
        #end
    }

    // Override to force use inline
    override public function clear(bbox : Null<Rect> = null) : Void {
        if (bbox == null) {
            for (row in 0 ... height) {
                for (col in 0 ... width) {
                    set(row, col, emptyValue);
                }
            }
        } else {
            var fromRow = bbox.row;
            var boundRow = bbox.row + bbox.height;
            var fromCol = bbox.col;
            var boundCol = bbox.col + bbox.width;

            if (boundRow <= fromRow || boundCol <= fromCol) {
                return;
            }

            if (fromRow < 0) {
                fromRow = 0;
            }

            if (boundRow > height) {
                boundRow = height;
            }

            if (fromCol < 0) {
                fromCol = 0;
            }

            if (boundCol > width) {
                boundCol = width;
            }

            for (row in fromRow ... boundRow) {
                for (col in fromCol ... boundCol) {
                    set(row, col, emptyValue);
                }
            }
        }
    }

    public function equals(other : IntLayer) : Bool {
        if (width != other.width || height != other.height) {
            return false;
        }

        for (row in 0 ... height) {
            for (col in 0 ... width) {
                if (entries[row][col] != other.entries[row][col]) {
                    return false;
                }
            }
        }

        return true;
    }

    public function reset() : Void {
        clear();
        currentEntry = 0;
    }

    public function copy() : IntLayer {
        return cast copyInto(new IntLayer(width, height));
    }

    public function nextEntry() : Int {
        return ++currentEntry;
    }

    public function partition(entry : Int) : Int {
        var partitions : Int = 0;
        visited.clear();

        for (row in 0 ... height) {
            for (col in 0 ... width) {
                if (!visited.get(row, col) && get(row, col) == entry) {
                    floodFill(row, col, entry, (partitions == 0) ? entry : nextEntry());
                    partitions++;
                }
            }
        }

        return partitions;
    }

    public function isSinglePartition(entry : Int, bbox : Null<Rect> = null) : Bool {
        var isFound : Bool = false;
        visited.clear(bbox);

        for (row in (bbox == null ? (0 ... height) : bbox.rows())) {
            for (col in (bbox == null ? (0 ... width) : bbox.columns())) {
                if (!visited.get(row, col) && get(row, col) == entry) {
                    if (isFound) {
                        return false;
                    }

                    floodFill(row, col, entry, entry);
                    isFound = true;
                }
            }
        }

        return isFound;
    }

    public function markLeftovers(kernRows : Int, kernColumns : Int, leftoverEntry : Int) : Bool {
        var hasLeftovers = false;

        for (row in 0 ... height) {
            for (col in 0 ... width) {
                var entry = get(row, col);

                if (entry == 0) {
                    continue;
                }

                var isKernFound = false;
                var rowPair = clampToKern(row, kernRows, height);
                var colPair = clampToKern(col, kernColumns, width);

                for (r in rowPair.first ... rowPair.second) {
                    for (c in colPair.first ... colPair.second) {
                        if (findKernEntry(r, c, kernRows, kernColumns) == entry) {
                            isKernFound = true;
                            break;
                        }
                    }

                    if (isKernFound) {
                        break;
                    }
                }

                if (!isKernFound) {
                    set(row, col, leftoverEntry);
                    hasLeftovers = true;
                }
            }
        }

        return hasLeftovers;
    }

    public function mergeLeftovers(kernRows : Int, kernColumns : Int, leftoverEntry : Int) : Bool {
        var hasAtLeastOneChange = false;
        var hasChanges : Bool;

        do {
            hasChanges = false;

            for (row in 0 ... height) {
                for (col in 0 ... width) {
                    var entry = get(row, col);

                    if (entry != leftoverEntry) {
                        continue;
                    }

                    var mergedEntry = 0;
                    var mergedPoint : Null<Point> = null;
                    var rowPair = clampToKern(row, kernRows, height);
                    var colPair = clampToKern(col, kernColumns, width);

                    for (r in rowPair.first ... rowPair.second) {
                        for (c in colPair.first ... colPair.second) {
                            mergedEntry = findKernEntry(r, c, kernRows, kernColumns, leftoverEntry);

                            if (mergedEntry != 0) {
                                mergedPoint = new Point(r, c);
                                break;
                            }
                        }

                        if (mergedEntry != 0) {
                            break;
                        }
                    }

                    if (mergedEntry != 0) {
                        for (r in 0 ... kernRows) {
                            for (c in 0 ... kernColumns) {
                                set(mergedPoint.row + r, mergedPoint.col + c, mergedEntry);
                            }
                        }

                        hasChanges = true;
                        hasAtLeastOneChange = true;
                    }
                }
            }
        } while (hasChanges);

        return hasAtLeastOneChange;
    }

    public function cleanupLeftovers(leftoverEntry : Int) : Bool {
        var hasChanges = false;

        for (row in 0 ... height) {
            for (col in 0 ... width) {
                var entry = get(row, col);

                if (entry == leftoverEntry) {
                    empty(row, col);
                    hasChanges = true;
                }
            }
        }

        return hasChanges;
    }

    public function tracePolyboxes() : SafeArray<Pair<Polybox, Int>> {
        var result : SafeArray<Pair<Polybox, Int>> = [];
        visited.clear();

        for (row in 0 ... height) {
            for (col in 0 ... width) {
                var entry = get(row, col);

                if (entry != 0 && !visited.get(row, col)) {
                    var polybox = Polybox.fromBorderPoints(traceBorderPoints(new Point(row, col), [entry]));

                    if (polybox != null) {
                        visited.fillPolybox(polybox, true);
                        result.push(new Pair(polybox.unsafe(), entry));
                    }
                }
            }
        }

        return result;
    }

    public function traceEdgePoints(innerEntries : SafeArray<Int>, ?bbox : Rect, outsideEntry : Int = 0) : SafeArray<EdgePoint> {
        for (row in (bbox == null ? 0 ... height : bbox.rows())) {
            for (col in (bbox == null ? 0 ... width : bbox.columns())) {
                var entry = get(row, col);

                if (!innerEntries.contains(entry)) {
                    continue;
                }

                var points = traceBorderPoints(new Point(row, col), innerEntries);
                var edgePoints = points.safeMap((point) -> new EdgePoint(point));

                for (i in 0 ... edgePoints.length) {
                    edgePoints[i].setConnections(
                        edgePoints[(i + edgePoints.length - 1) % edgePoints.length],
                        edgePoints[(i + 1) % edgePoints.length]
                    );
                }

                return edgePoints;
            }
        }

        return [];
    }

    public function isFreeRect(rect : Rect) : Bool {
        for (row in rect.rows()) {
            for (col in rect.columns()) {
                if (get(row, col) != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public function collect(entries : SafeArray<Int>, ?rect : Rect) : SafeArray<Point> {
        var result : SafeArray<Point> = [];

        if (rect == null) {
            rect = new Rect(0, 0, height, width);
        }

        for (row in rect.rows()) {
            for (col in rect.columns()) {
                if (entries.contains(get(row, col))) {
                    result.push(new Point(row, col));
                }
            }
        }

        return result;
    }

    private function traceBorderPoints(startPoint : Point, entries : SafeArray<Int>) : SafeArray<Point> {
        var points : SafeArray<Point> = [];
        var point = startPoint;
        var directionIndex : Int = 0;

        while (true) {
            points.push(point);

            if (points.length > width * height) {
                // Shouldn't happen, but check for the great justice
                throw new GeneratorException("traceBorderPoints failed");
            }

            var leftRotDirectionIndex = (directionIndex + Point.DIRECTIONS.length - 1) % Point.DIRECTIONS.length;
            var direction = Point.DIRECTIONS[leftRotDirectionIndex];
            var nextPoint = point.addTo(direction);
            var normalPoint = nextPoint.addTo(direction.rotateLeft());

            if (pointInside(nextPoint)
                && entries.contains(getAt(nextPoint))
                && (!pointInside(normalPoint) || !entries.contains(getAt(normalPoint)))
            ) {
                directionIndex = leftRotDirectionIndex;
                point = nextPoint;
            } else {
                var initialDirectionIndex = directionIndex;

                while (true) {
                    direction = Point.DIRECTIONS[directionIndex];
                    nextPoint = point.addTo(direction);
                    normalPoint = point.addTo(direction.rotateLeft());

                    if (pointInside(nextPoint)
                        && entries.contains(getAt(nextPoint))
                        && (!pointInside(normalPoint) || !entries.contains(getAt(normalPoint)))
                    ) {
                        point = nextPoint;
                        break;
                    }

                    directionIndex = (directionIndex + 1) % Point.DIRECTIONS.length;

                    if (directionIndex == initialDirectionIndex) {
                        // Can happen when polybox contains only one point
                        return points;
                    }
                }
            }

            if (startPoint.equals(point)) {
                break;
            }
        }

        return points;
    }

    // value = 5, kern = 3, max = 10, 3 4 5 (6)
    // value = 0, kern = 3, max = 10, 0 (1)
    // value = 9, kern = 3, max = 10, 7 (8)

    private function clampToKern(value : Int, kern : Int, max : Int) : Pair<Int, Int> {
        var from = value - kern + 1;
        var to = value;

        if (from < 0) {
            from = 0;
        }

        if (to + kern > max) {
            to = max - kern;
        }

        if (to < from) {
            throw new GeneratorException('clampToKern failed value = ${value}, kern = ${kern}, max = ${max}, from = ${from}, to = ${to}');
        }

        return new Pair(from, to + 1);
    }

    private function findKernEntry(row : Int, col : Int, rows : Int, columns : Int, entryToIgnore : Int = 0) : Int {
        var result = 0;

        for (r in 0 ... rows) {
            for (c in 0 ... columns) {
                var entry = get(row + r, col + c);

                if (entry == 0) {
                    return 0;
                }

                if (entry == entryToIgnore) {
                    continue;
                }

                if (result == 0) {
                    result = entry;
                    continue;
                }

                if (entry != result) {
                    return 0;
                }
            }
        }

        return result;
    }

    private function floodFill(row : Int, col : Int, src : Int, dst : Int) : Void {
        if (get(row, col) != src) {
            return;
        }

        var points : SafeArray<Point> = [new Point(row, col)];
        visited.set(row, col, true);

        while (true) {
            var newPoints : SafeArray<Point> = [];

            for (point in points) {
                if (src != dst) {
                    set(point.row, point.col, dst);
                }

                if (point.row > 1) {
                    floodFillCheckAndVisit(point.row - 1, point.col, src, newPoints);
                }

                if (point.col > 1) {
                    floodFillCheckAndVisit(point.row, point.col - 1, src, newPoints);
                }

                if (point.row < maxRow) {
                    floodFillCheckAndVisit(point.row + 1, point.col, src, newPoints);
                }

                if (point.col < maxCol) {
                    floodFillCheckAndVisit(point.row, point.col + 1, src, newPoints);
                }
            }

            if (newPoints.length == 0) {
                break;
            }

            points = newPoints;
        }
    }

    private function floodFillCheckAndVisit(row : Int, col : Int, src : Int, newPoints : SafeArray<Point>) : Void {
        if (!visited.get(row, col) && get(row, col) == src) {
            newPoints.push(new Point(row, col));
            visited.set(row, col, true);
        }
    }

    public static function createAllowReducer(allow : SafeArray<Int>) : LayerReducer<Int, Bool> {
        return new LayerReducer<Int, Bool>(true, (_, _, entry, carry) -> (carry && allow.contains(entry)));
    }
}
