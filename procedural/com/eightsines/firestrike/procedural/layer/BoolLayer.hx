package com.eightsines.firestrike.procedural.layer;

import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.Layer; // {LayerReducer}

class BoolLayer extends Layer<Bool> {
    private static final EMPTY_CREATOR = () -> false;
    public static final REDUCE_ALL_FALSE = new LayerReducer<Bool, Bool>(true, (_, _, entry, carry) -> (carry && !entry));

    public function new(width : Int, height : Int) {
        super(width, height, EMPTY_CREATOR);
    }

    public function toString() : String {
        return dumpToString("BoolLayer");
    }

    public function copy() : BoolLayer {
        return cast copyInto(new BoolLayer(width, height));
    }

    // Override to force use inline
    override public inline function set(row : Int, col : Int, entry : Bool) : Void {
        #if php
            php.Syntax.code("{0}->entries->arr[{1}]->arr[{2}] = {3}", this, row, col, entry);
        #else
            entries[row][col] = entry;
        #end
    }

    // Override to force use inline
    override public function clear(?bbox : Null<Rect>) : Void {
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
}
