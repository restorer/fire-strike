package com.eightsines.firestrike.procedural.section;

import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Polybox;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.SectionAppearance; // {SectionAppearanceKind}
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.Sequence;

using Lambda;
using Safety;
using com.eightsines.firestrike.procedural.util.Tools;

typedef SectionScenarioObject = SectionObject<SectionScenarioAction>;
typedef SectionEnemy = SectionObject<Int>;
typedef SectionItem = SectionObject<SectionItemType>;
typedef SectionWallLamp = SectionObject<Bool>;

class Section {
    public static inline var MIN_SIZE : Int = 3;

    public static inline var VAL_OUTER_AVAIL = 1;
    public static inline var VAL_OUTER_INNERSPACE = 2;
    public static inline var VAL_OUTER_UNAVAIL = 3;
    public static inline var VAL_OUTER_USED = 4;

    public static inline var VAL_INNER_AVAIL = 1;
    public static inline var VAL_INNER_INNERSPACE = 2;
    public static inline var VAL_INNER_BORDER = 3;
    public static inline var VAL_INNER_DECORATION = 4;
    public static inline var VAL_INNER_KEYPOINT = 5;
    public static inline var VAL_INNER_PASSABLE = 6;
    public static inline var VAL_INNER_BOX = 7;

    public var geometry : SafeArray<Polybox>;
    public var entryValue : Int;
    public var connections : SafeArray<SectionConnection> = [];
    public var fences : SafeArray<SectionFence> = [];
    public var scenario : Int = 0;
    public var scenarioOpener : Int = 0;
    public var scenarioAction : Null<SectionScenarioAction> = null;
    public var scenarioObjects : SafeArray<SectionScenarioObject> = [];
    public var decorations : SafeArray<SectionDecoration> = [];
    public var grass : SafeArray<Point> = [];
    public var lamps : SafeArray<Point> = [];
    public var ceilingLamps : SafeArray<Point> = [];
    public var wallLamps : SafeArray<SectionWallLamp> = [];
    public var player : Null<Point> = null;
    public var enemies : SafeArray<SectionEnemy> = [];
    public var items : SafeArray<SectionItem> = [];
    public var appearanceKind : Null<SectionAppearanceKind> = null;
    public var appearance : Null<SectionAppearance> = null;
    public var noTransPoints : SafeArray<Point> = [];
    public var __id : Int;

    private var __bbox : Null<Rect> = null;

    public function new(polybox : Null<Polybox>, entryValue : Int, ?__id : Int) {
        this.geometry = (polybox == null ? [] : [polybox]);
        this.entryValue = entryValue;
        this.__id = (__id == null ? Sequence.nextId() : __id);
    }

    public static function copyAll(source : SafeArray<Section>) : SafeArray<Section> {
        for (section in source) {
            for (connection in section.connections) {
                connection.__copySectionId = connection.ensureSection().__id;

                if (connection.otherConnection != null) {
                    connection.__copyOtherConnectionId = connection.otherConnection.unsafe().__id;
                }
            }

            for (fence in section.fences) {
                if (fence.otherFence != null) {
                    fence.__copyOtherFenceId = fence.otherFence.unsafe().__id;
                }
            }
        }

        var destination = source.map((section) -> section.copyWithoutOtherSize());
        var sectionsMap = new Map<Int, Section>();
        var connectionsMap = new Map<Int, SectionConnection>();
        var fencesMap = new Map<Int, SectionFence>();

        for (section in destination) {
            sectionsMap[section.__id] = section;
        }

        for (section in destination) {
            for (connection in section.connections) {
                if (!sectionsMap.exists(connection.__copySectionId)) {
                    throw new GeneratorException("copyAll failed: section for connection was not found");
                }

                connection.section = sectionsMap[connection.__copySectionId];
                connectionsMap[connection.__id] = connection;
            }

            for (fence in section.fences) {
                fencesMap[fence.__id] = fence;
            }
        }

        for (section in destination) {
            for (connection in section.connections) {
                if (connection.__copyOtherConnectionId != 0) {
                    if (!connectionsMap.exists(connection.__copyOtherConnectionId)) {
                        throw new GeneratorException("copyAll failed: other-side connection was not found");
                    }

                    connection.otherConnection = connectionsMap[connection.__copyOtherConnectionId];
                }
            }

            for (fence in section.fences) {
                if (fence.__copyOtherFenceId != 0) {
                    if (!fencesMap.exists(fence.__copyOtherFenceId)) {
                        throw new GeneratorException("copyAll failed: other-size fence was not found");
                    }

                    fence.otherFence = fencesMap[fence.__copyOtherFenceId];
                }
            }
        }

        return destination;
    }

    public function copyWithoutOtherSize() : Section {
        var result = new Section(null, entryValue, __id);

        result.geometry = geometry.map((polybox) -> polybox.deepCopy());
        result.connections = connections.map((connection) -> connection.copyWithoutOtherSide());
        result.fences = fences.map((fence) -> fence.copyWithoutOtherSide());
        result.scenario = scenario;
        result.scenarioOpener = scenarioOpener;
        result.scenarioAction = scenarioAction;
        result.scenarioObjects = scenarioObjects.map((scenarioObject) -> scenarioObject.copy());
        result.decorations = decorations.map((decoration) -> decoration.copy());
        result.grass = grass.map((point) -> point.copy());
        result.lamps = lamps.map((point) -> point.copy());
        result.ceilingLamps = ceilingLamps.map((point) -> point.copy());
        result.wallLamps = wallLamps.map((wallLamp) -> wallLamp.copy());
        result.player = player!.copy();
        result.enemies = enemies.map((enemy) -> enemy.copy());
        result.items = items.map((item) -> item.copy());
        result.appearanceKind = appearanceKind;
        result.appearance = appearance!.copy();
        result.noTransPoints = noTransPoints.map((point) -> point.copy());

        return result;
    }

    public function __getPolybox() : Polybox {
        return geometry[0];
    }

    public function getArea() : Int {
        return geometry.fold((polybox, carry) -> (carry + polybox.getBbox().sure().getArea()), 0);
    }

    public function getDividers() : SafeArray<SectionDivider> {
        return fences
            .map((fence : SectionFence) -> (cast fence : SectionDivider))
            .pushAll(
                connections
                    .filter((connection) -> !connection.__removed)
                    .map((connection) -> (cast connection : SectionDivider))
            );
    }

    public function hasOuterWideDoor() : Bool {
        for (connection in connections) {
            for (gate in (connection.__passable ? connection.otherConnection.sure() : connection).gates) {
                if (gate.type == Door && gate.size > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    public function getBbox() : Rect {
        if (__bbox == null) {
            for (polybox in geometry) {
                if (__bbox == null) {
                    __bbox = polybox.getBbox().sure();
                } else {
                    __bbox = __bbox.unsafe().union(polybox.getBbox().sure());
                }
            }

            if (__bbox == null) {
                throw new GeneratorException("getBbox failed: section has no geometry");
            }
        }

        return __bbox.unsafe();
    }

    public function renderAvailOuterCells(layer : IntLayer, allowPlaceNearGates : Bool) : Void {
        layer.clear(getBbox().expand(1));

        for (polybox in geometry) {
            layer.fillPolybox(polybox, VAL_OUTER_INNERSPACE);
        }

        for (polybox in geometry) {
            layer.outlinePolybox(polybox, VAL_OUTER_AVAIL);
        }

        for (divider in getDividers()) {
            if (divider.__passable) {
                layer.fillRect(divider.rect, VAL_OUTER_INNERSPACE);
            } else {
                for (gate in divider.gates) {
                    layer.fillRect(gate.rect, (gate.type == Passable) ? VAL_OUTER_INNERSPACE : VAL_OUTER_USED);
                }
            }
        }

        for (object in scenarioObjects) {
            layer.setAt(object.position, VAL_OUTER_USED);
        }

        if (!allowPlaceNearGates) {
            for (row in getBbox().rows()) {
                for (col in getBbox().columns()) {
                    if (layer.get(row, col) == VAL_OUTER_AVAIL
                        && !(([VAL_OUTER_AVAIL, VAL_OUTER_UNAVAIL].contains(layer.safeGet(row - 1, col, 0))
                                && [VAL_OUTER_AVAIL, VAL_OUTER_UNAVAIL].contains(layer.safeGet(row + 1, col, 0))
                            )
                            || ([VAL_OUTER_AVAIL, VAL_OUTER_UNAVAIL].contains(layer.safeGet(row, col - 1, 0))
                                && [VAL_OUTER_AVAIL, VAL_OUTER_UNAVAIL].contains(layer.safeGet(row, col + 1, 0))
                            )
                        )
                    ) {
                        layer.set(row, col, VAL_OUTER_UNAVAIL);
                    }
                }
            }
        }

        for (row in getBbox().rows()) {
            for (col in getBbox().columns()) {
                if (layer.get(row, col) == VAL_OUTER_AVAIL
                    && !(layer.safeGet(row - 1, col, 0) == VAL_OUTER_INNERSPACE
                        || layer.safeGet(row + 1, col, 0) == VAL_OUTER_INNERSPACE
                        || layer.safeGet(row, col - 1, 0) == VAL_OUTER_INNERSPACE
                        || layer.safeGet(row, col + 1, 0) == VAL_OUTER_INNERSPACE
                    )
                ) {
                    layer.set(row, col, VAL_OUTER_UNAVAIL);
                }
            }
        }
    }

    public function renderAvailInnerCells(layer : IntLayer, forPassableObjects : Bool, onlyNearBorder : Bool = false) : Void {
        layer.clear(getBbox().expand(1));

        for (polybox in geometry) {
            layer.fillPolybox(polybox, onlyNearBorder ? VAL_INNER_INNERSPACE : VAL_INNER_AVAIL);
        }

        for (polybox in geometry) {
            layer.outlinePolybox(polybox, VAL_INNER_BORDER);
        }

        for (divider in getDividers()) {
            if (divider.__passable) {
                layer.fillRect(divider.rect, onlyNearBorder ? VAL_INNER_INNERSPACE : VAL_INNER_AVAIL);

                // Коннектор может быть проходимым, но с другой стороны этого коннектора может быть дверь.
                if (!forPassableObjects && !divider.getOtherDivider().__passable) {
                    for (gate in divider.getOtherDivider().gates) {
                        layer.fillRect(gate.rect.addPoint(gate.normal.scalar(-1)), VAL_INNER_PASSABLE);
                    }
                }
            } else {
                for (gate in divider.gates) {
                    layer.fillRect(
                        gate.rect,
                        if (gate.type == Passable) {
                            (forPassableObjects ? VAL_INNER_AVAIL : VAL_INNER_PASSABLE);
                        } else {
                            VAL_INNER_KEYPOINT;
                        }
                    );
                }
            }
        }

        for (object in scenarioObjects) {
            switch (object.type) {
                case EndLevel | Switch:
                    layer.setAt(object.position, VAL_INNER_KEYPOINT);

                case Key(_):
                    layer.setAt(object.position, VAL_INNER_PASSABLE);

                case JustDoor:
                    throw new GeneratorException("renderAvailInnerCells failed: scenario object with type JustDoor");
            }
        }

        if (player != null) {
            layer.setAt(player.unsafe(), VAL_INNER_PASSABLE);
        }

        for (enemy in enemies) {
            layer.setAt(enemy.position, VAL_INNER_PASSABLE);
        }

        for (item in items) {
            layer.setAt(item.position, VAL_INNER_PASSABLE);
        }

        for (decoration in decorations) {
            layer.plot(decoration.points, switch (decoration.type) {
                case Box:
                    VAL_INNER_BOX;

                case Pillar | Lattice | Rock | Barrel:
                    VAL_INNER_DECORATION;
            });
        }

        if (onlyNearBorder) {
            for (row in getBbox().rows()) {
                for (col in getBbox().columns()) {
                    if (layer.get(row, col) != VAL_INNER_INNERSPACE) {
                        continue;
                    }

                    if ([VAL_INNER_BORDER, VAL_INNER_BOX, VAL_INNER_DECORATION].contains(layer.safeGet(row - 1, col, 0))
                        || [VAL_INNER_BORDER, VAL_INNER_BOX, VAL_INNER_DECORATION].contains(layer.safeGet(row + 1, col, 0))
                        || [VAL_INNER_BORDER, VAL_INNER_BOX, VAL_INNER_DECORATION].contains(layer.safeGet(row, col - 1, 0))
                        || [VAL_INNER_BORDER, VAL_INNER_BOX, VAL_INNER_DECORATION].contains(layer.safeGet(row, col + 1, 0))
                    ) {
                        layer.set(row, col, VAL_INNER_AVAIL);
                    }
                }
            }
        }
    }
}
