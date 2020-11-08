package com.eightsines.firestrike.procedural.generator;

import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.board.Entry;
import com.eightsines.firestrike.procedural.board.Splitted;
import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.Layer; // {LayerReducer}
import com.eightsines.firestrike.procedural.util.BoardUtils;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.IntMath;
import haxe.Json;
import haxe.ds.Option;
import org.zamedev.lib.DynamicExt;

using Lambda;
using Safety;
using org.zamedev.lib.DynamicTools;

class Exporter {
    private static inline var T_HERO = 1;
    private static inline var T_WALL = 2;
    private static inline var T_TWALL = 3;
    // private static inline var T_TPASS = 4;
    private static inline var T_TWIND = 5;
    private static inline var T_DOOR = 6;
    private static inline var T_DITEM = 7;
    private static inline var T_DLAMP = 8;
    private static inline var T_OBJ = 9;
    private static inline var T_MON = 10;

    public static var REDUCE_ALL_EMPTY = new LayerReducer<Entry, Bool>(true, (_, _, entry, carry) -> (carry && BoardUtils.isEmptyEntry(entry)));

    private var settings : Settings;
    private var board : Board;
    private var minRow : Int = 0;
    private var minCol : Int = 0;
    private var maxRow : Int = 0;
    private var maxCol : Int = 0;

    public function new(settings : Settings, board : Board) {
        this.settings = settings;
        this.board = board;
    }

    public function export() : String {
        var bounds = computeBounds();

        return Json.stringify({
            version : 1,
            format : 4,
            xpos : minCol,
            ypos : minRow,
            map : exportMap(bounds),
            graphics : "set-1",
            ensureLevel : settings.availWeapons.fold((v, c) -> IntMath.max(v, c), 0),
            difficultyLevel : settings.difficulty,
            actions : board.script.join("\n"),
            _generator : settings.argumentsLine,
        });
    }

    private function computeBounds() : Rect {
        minRow = 0;
        minCol = 0;
        maxRow = board.height - 1;
        maxCol = board.width - 1;

        while (minRow < (board.height - 1) && board.reduceBoxFilled(minRow, 0, 1, board.width, REDUCE_ALL_EMPTY)) {
            minRow++;
        }

        while (minCol < (board.width - 1) && board.reduceBoxFilled(0, minCol, board.height, 1, REDUCE_ALL_EMPTY)) {
            minCol++;
        }

        while (maxRow > 0 && board.reduceBoxFilled(maxRow, 0, 1, board.width, REDUCE_ALL_EMPTY)) {
            maxRow--;
        }

        while (maxCol > 0 && board.reduceBoxFilled(0, minCol, board.height, 1, REDUCE_ALL_EMPTY)) {
            maxCol--;
        }

        if (minRow > maxRow || minCol > maxCol) {
            throw new GeneratorException("Exporter failed: couldn't compute bounds");
        }

        return Rect.fromCoords(minRow, minCol, maxRow, maxCol);
    }

    private function exportMap(bounds : Rect) : SafeArray<SafeArray<DynamicExt>> {
        return [ for (row in 0 ... bounds.height)
            [ for (col in 0 ... bounds.width)
                exportEntry(board.get(bounds.row + row, bounds.col + col))
            ]
        ];
    }

    private function exportEntry(entry : Entry) : DynamicExt {
        var resType : Int = 0;
        var resValue : Int = 0;

        switch (entry.cell.sure()) {
            case Some(cell):
                switch (cell) {
                    case Wall(value):
                        resType = T_WALL;
                        resValue = value;

                    case TWall(value):
                        resType = T_TWALL;
                        resValue = value;

                    case Window(value):
                        resType = T_TWIND;
                        resValue = value;

                    case Door(value):
                        resType = T_DOOR;
                        resValue = value;

                    case Decor(value):
                        resType = T_DITEM;
                        resValue = value;

                    case Pass(value):
                        resType = T_DLAMP;
                        resValue = value;

                    case Item(value):
                        resType = T_OBJ;
                        resValue = value;

                    case Enemy(value):
                        resType = T_MON;
                        resValue = value;

                    case Player(direction):
                        resType = T_HERO;
                        resValue = direction;
                }

            case None:
        }

        return {
            floor : exportSplitted(entry.floor.sure()),
            arrow : switch (entry.arrow.sure()) {
                case None: 0;
                case Some(value): value;
            },
            type : resType,
            value : resValue,
            ceil : exportSplitted(entry.ceiling.sure()),
            mark : switch (entry.mark.sure()) {
                case None: "";
                case Some(value): value;
            },
            noTrans : switch (entry.noTrans.sure()) {
                case None: false;
                case Some(value): value;
            },
        };
    }

    private function exportSplitted<T : Int>(splitted : Splitted<Option<T>>) : SafeArray<Int> {
        return [
            switch (splitted.tl.sure()) {
                case None: 0;
                case Some(value): value;
            },
            switch (splitted.tr.sure()) {
                case None: 0;
                case Some(value): value;
            },
            switch (splitted.bl.sure()) {
                case None: 0;
                case Some(value): value;
            },
            switch (splitted.br.sure()) {
                case None: 0;
                case Some(value): value;
            },
        ];
    }
}
