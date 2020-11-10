package com.eightsines.firestrike.procedural.layer;

import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Polybox;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.util.Unit;

using Safety;

typedef LayerEmptyCreator<T> = () -> T;
typedef LayerCondition<T> = (T) -> Bool;

class LayerReducer<T, R> {
    public var carry(default, null) : R;
    public var cb(default, null) : (Int, Int, T, R) -> R;

    public function new(carry : R, cb : (Int, Int, T, R) -> R) {
        this.carry = carry;
        this.cb = cb;
    }
}

class Layer<T> {
    private var entries : Array<Array<T>>;
    private var emptyValue : T;

    public var width(default, null) : Int;
    public var height(default, null) : Int;
    public var maxRow(default, null) : Int;
    public var maxCol(default, null) : Int;

    public function new(width : Int, height : Int, emptyCreator : LayerEmptyCreator<T>) {
        this.width = width;
        this.height = height;

        maxRow = height - 1;
        maxCol = width - 1;

        entries = [for (row in 0 ... height) [for (col in 0 ... width) emptyCreator()]];
        emptyValue = emptyCreator();
    }

    public inline function get(row : Int, col : Int) : T {
        #if php
            return php.Syntax.code("{0}->entries->arr[{1}]->arr[{2}]", this, row, col);
        #else
            return entries[row][col];
        #end
    }

    public inline function safeGet(row : Int, col : Int, def : T) : T {
        return ((row >= 0 && col >= 0 && row < height && col < width) ? get(row, col) : def);
    }

    public inline function getAt(point : Point) : T {
        return get(point.row, point.col);
    }

    public inline function safeGetAt(point : Point, def : T) : T {
        return safeGet(point.row, point.col, def);
    }

    // Must not be inline, because it is overriden in Board
    public function set(row : Int, col : Int, entry : T) : Void {
        #if php
            php.Syntax.code("{0}->entries->arr[{1}]->arr[{2}] = {3}", this, row, col, entry);
        #else
            entries[row][col] = entry;
        #end
    }

    public inline function setAt(point : Point, entry : T) : Void {
        set(point.row, point.col, entry);
    }

    public inline function empty(row : Int, col : Int) : Void {
        set(row, col, emptyValue);
    }

    public function clear(?bbox : Null<Rect>) : Void {
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

    public function copyInto(result : Layer<T>) : Layer<T> {
        for (row in 0 ... height) {
            for (col in 0 ... width) {
                result.set(row, col, entries[row][col]);
            }
        }

        return result;
    }

    public inline function pointInside(point : Point) : Bool {
        return (point.row >= 0 && point.col >= 0 && point.row < height && point.col < width);
    }

    public function reduceAll<R>(reducer : LayerReducer<T, R>) : R {
        var carry = reducer.carry;

        for (row in 0 ... height) {
            for (col in 0 ... width) {
                carry = callReducer(row, col, reducer, carry);
            }
        }

        return carry;
    }

    public function reducePoints<R>(points : Array<Point>, reducer : LayerReducer<T, R>) : R {
        var carry = reducer.carry;

        for (point in points) {
            carry = callReducer(point.row, point.col, reducer, carry);
        }

        return carry;
    }

    public function reduceBoxOutline<R>(row : Int, col : Int, rows : Int, columns : Int, reducer : LayerReducer<T, R>) : R {
        var carry = reducer.carry;

        for (i in 0 ... columns) {
            carry = callReducer(row, col + i, reducer, carry);
            carry = callReducer(row + rows - 1, col + i, reducer, carry);
        }

        for (i in 1 ... rows - 1) {
            carry = callReducer(row + i, col, reducer, carry);
            carry = callReducer(row + i, col + columns - 1, reducer, carry);
        }

        return carry;
    }

    public inline function reduceRectOutline<R>(rect : Rect, reducer : LayerReducer<T, R>) : R {
        return reduceBoxOutline(rect.row, rect.col, rect.height, rect.width, reducer);
    }

    public function reducePolyboxOutline<R>(polybox : Polybox, reducer : LayerReducer<T, R>) : R {
        var carry = reducer.carry;

        for (point in polybox.getBorderPoints()) {
            carry = callReducer(point.row, point.col, reducer, carry);
        }

        return carry;
    }

    public function reduceBoxFilled<R>(row : Int, col : Int, rows : Int, columns : Int, reducer : LayerReducer<T, R>) : R {
        var carry = reducer.carry;

        for (r in 0 ... rows) {
            for (c in 0 ... columns) {
                carry = callReducer(row + r, col + c, reducer, carry);
            }
        }

        return carry;
    }

    public inline function reduceRectFilled<R>(rect : Rect, reducer : LayerReducer<T, R>) : R {
        return reduceBoxFilled(rect.row, rect.col, rect.height, rect.width, reducer);
    }

    public function reducePolyboxFilled<R>(polybox : Polybox, reducer : LayerReducer<T, R>) : R {
        var carry = reducer.carry;
        var bbox = polybox.getBbox();

        if (bbox == null) {
            return carry;
        }

        for (row in 0 ... bbox.height) {
            for (col in 0 ... bbox.width) {
                if (polybox.isInside(bbox.row + row, bbox.col + col)) {
                    carry = callReducer(bbox.row + row, bbox.col + col, reducer, carry);
                }
            }
        }

        return carry;
    }

    public function fill(entry : T, ?condition : LayerCondition<T>) : Bool {
        var result = false;

        for (row in 0 ... height) {
            for (col in 0 ... width) {
                if (condition == null || condition(get(row, col))) {
                    set(row, col, entry);
                    result = true;
                }
            }
        }

        return result;
    }

    public function plot(points : Array<Point>, entry : T, ?condition : LayerCondition<T>) : Void {
        reducePoints(points, createConditionalSetReducer(entry, condition));
    }

    public function outlineBox(row : Int, col : Int, rows : Int, columns : Int, entry : T, ?condition : LayerCondition<T>) : Void {
        reduceBoxFilled(row, col, rows, columns, createConditionalSetReducer(entry, condition));
    }

    public function outlineRect(rect : Rect, entry : T, ?condition : LayerCondition<T>) : Void {
        reduceRectOutline(rect, createConditionalSetReducer(entry, condition));
    }

    public function outlinePolybox(polybox : Polybox, entry : T, ?condition : LayerCondition<T>) : Void {
        reducePolyboxOutline(polybox, createConditionalSetReducer(entry, condition));
    }

    public function fillBox(row : Int, col : Int, rows : Int, columns : Int, entry : T, ?condition : LayerCondition<T>) : Void {
        reduceBoxFilled(row, col, rows, columns, createConditionalSetReducer(entry, condition));
    }

    public function fillRect(rect : Rect, entry : T, ?condition : LayerCondition<T>) : Void {
        reduceRectFilled(rect, createConditionalSetReducer(entry, condition));
    }

    public function fillPolybox(polybox : Polybox, entry : T, ?condition : LayerCondition<T>) : Void {
        reducePolyboxFilled(polybox, createConditionalSetReducer(entry, condition));
    }

    private inline function callReducer<R>(row : Int, col : Int, reducer : LayerReducer<T, R>, carry : R) : R {
        return reducer.cb(row, col, get(row, col), carry);
    }

    private function createConditionalSetReducer(entry : T, condition : Null<LayerCondition<T>>) : LayerReducer<T, Unit> {
        if (condition == null) {
            return new LayerReducer<T, Unit>(null, (row, col, _, _) -> {
                set(row, col, entry);
                return null;
            });
        } else {
            var _condition = condition.unsafe();

            return new LayerReducer<T, Unit>(null, (row, col, existingEntry, _) -> {
                if (_condition(existingEntry)) {
                    set(row, col, entry);
                }

                return null;
            });
        }
    }

    private function dumpToString(layerName : String) : String {
        var sb = new StringBuf();
        sb.add(layerName);
        sb.add('(width=${width}, height=${height}, entries=[\n');

        for (row in 0 ... height) {
            for (col in 0 ... width) {
                if (col != 0) {
                    sb.add(" ");
                }

                sb.add(Std.string(entries[row][col]));
            }

            sb.add("\n");
        }

        sb.add("])");
        return sb.toString();
    }
}
