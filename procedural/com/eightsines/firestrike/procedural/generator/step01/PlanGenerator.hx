package com.eightsines.firestrike.procedural.generator.step01;

import com.eightsines.firestrike.procedural.generator.AbstractGeometryGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Polybox;
import com.eightsines.firestrike.procedural.geom.PolyboxEdge;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

using Safety;

class PlanGeneratorGrowSpec {
    public var check : (Rect, IntLayer) -> Bool;
    public var size : (Rect) -> Int;
    public var apply : (Rect) -> Void;

    public function new(check : (Rect, IntLayer) -> Bool, size : (Rect) -> Int, apply : (Rect) -> Void) {
        this.check = check;
        this.size = size;
        this.apply = apply;
    }
}

class PlanGeneratorSplitSpec {
    public var edge : PolyboxEdge;
    public var from : Point;
    public var to : Point;

    public function new(edge : PolyboxEdge, from : Point, to : Point) {
        this.edge = edge;
        this.from = from;
        this.to = to;
    }
}

class PlanGeneratorRoom {
    public var rect : Rect;
    public var polybox : Null<Polybox> = null;
    public var entryValue : Int;
    public var cornerEntryValue : Int = 0;

    public function new(rect : Rect, entryValue : Int) {
        this.rect = rect;
        this.entryValue = entryValue;
    }
}

class PlanGenerator extends AbstractGeometryGenerator implements Generator {
    private static inline final SEED_ROOM_SIZE : Int = 5;

    private static var GROW_SPECS : Array<PlanGeneratorGrowSpec> = [
        new PlanGeneratorGrowSpec(
            (rect, layer) -> ((rect.row > 0)
                && layer.reduceBoxFilled(rect.row - 1, rect.col, 1, rect.width, IntLayer.REDUCE_ALL_EMPTY)
            ),
            (rect) -> rect.width,
            (rect) -> { rect.row--; rect.height++; }
        ),
        new PlanGeneratorGrowSpec(
            (rect, layer) -> ((rect.col + rect.width < layer.width)
                && layer.reduceBoxFilled(rect.row, rect.col + rect.width, rect.height, 1, IntLayer.REDUCE_ALL_EMPTY)
            ),
            (rect) -> rect.height,
            (rect) -> { rect.width++; }
        ),
        new PlanGeneratorGrowSpec(
            (rect, layer) -> ((rect.row + rect.height < layer.height)
                && layer.reduceBoxFilled(rect.row + rect.height, rect.col, 1, rect.width, IntLayer.REDUCE_ALL_EMPTY)
            ),
            (rect) -> rect.width,
            (rect) -> { rect.height++; }
        ),
        new PlanGeneratorGrowSpec(
            (rect, layer) -> ((rect.col > 0)
                && layer.reduceBoxFilled(rect.row, rect.col - 1, rect.height, 1, IntLayer.REDUCE_ALL_EMPTY)
            ),
            (rect) -> rect.height,
            (rect) -> { rect.col--; rect.width++; }
        ),
    ];

    private var rooms : Array<PlanGeneratorRoom> = [];

    public function new(random : Random, layer : IntLayer, viewer : Viewer) {
        super(random, layer, viewer);
    }

    public function generate() : Array<Section> {
        drawOutline(false);
        seedRooms();

        if (rooms.length < 2) {
            drawOutline(true);
            seedRooms();

            if (rooms.length < 2) {
                layer.fill(0);
                seedRooms();
            }
        }

        growRoomsSimple();
        convertRectsToPolyboxes();
        growRoomsSplitted();
        fillPossibleHoles();
        GeneratorUtils.ensureMinSectionSize(layer, viewer);

        return GeneratorUtils.traceSections(layer);
    }

    private function drawOutline(clearMore : Bool) : Void {
        layer.fill(-1);
        viewer.dumpIntLayer(layer);

        for (i in 0 ... (clearMore ? random.nextIntRangeIn(15, 30) : random.nextIntRangeIn(5, 15))) {
            var layerCopy = layer.copy();

            layer.fillRect(Rect.fromCoords(
                random.nextIntRangeIn(0, Std.int(layer.height / 2) + SEED_ROOM_SIZE),
                random.nextIntRangeIn(0, Std.int(layer.width / 2) + SEED_ROOM_SIZE),
                random.nextIntRangeIn(Std.int(layer.height / 2) - SEED_ROOM_SIZE, layer.height - 1),
                random.nextIntRangeIn(Std.int(layer.width / 2) - SEED_ROOM_SIZE, layer.width - 1)
            ), 0);

            if (!layer.equals(layerCopy)) {
                viewer.dumpIntLayer(layer);
            }
        }
    }

    private function seedRooms() : Void {
        rooms = [];

        // TODO: мин/макс кол-во комнат рассчитать исходя их кол-ва пустых клеток
        // (в этом поможет layer.collect())

        for (i in 0 ... random.nextIntRangeIn(10, 20)) {
            var rect;
            var attemptsLeft = 100;

            do {
                attemptsLeft--;

                rect = new Rect(
                    random.nextIntRangeIn(0, layer.height - SEED_ROOM_SIZE),
                    random.nextIntRangeIn(0, layer.width - SEED_ROOM_SIZE),
                    SEED_ROOM_SIZE,
                    SEED_ROOM_SIZE
                );
            } while (!layer.reduceRectFilled(rect, IntLayer.REDUCE_ALL_EMPTY) && attemptsLeft > 0);

            if (attemptsLeft <= 0) {
                break;
            }

            var room = new PlanGeneratorRoom(rect, layer.nextEntry());
            layer.fillRect(room.rect, room.entryValue);
            rooms.push(room);
        }

        viewer.dumpIntLayer(layer);
    }

    private function growRoomsSimple() : Void {
        var canGrowMore : Bool;

        do {
            canGrowMore = false;

            for (room in rooms) {
                var maxSize : Int = 0;
                var availGrowSpecs : Array<PlanGeneratorGrowSpec> = [];

                for (spec in GROW_SPECS) {
                    if (!spec.check(room.rect, layer)) {
                        continue;
                    }

                    var size = spec.size(room.rect);

                    if (size < maxSize) {
                        continue;
                    }

                    if (size > maxSize) {
                        maxSize = size;
                        availGrowSpecs = [spec];
                    } else {
                        availGrowSpecs.push(spec);
                    }
                }

                if (availGrowSpecs.length == 0) {
                    continue;
                }

                canGrowMore = true;
                random.nextFromArray(availGrowSpecs).sure().apply(room.rect);
                layer.outlineRect(room.rect, room.entryValue);
            }

            if (canGrowMore) {
                viewer.dumpIntLayer(layer);
            }
        } while (canGrowMore);
    }

    private function convertRectsToPolyboxes() : Void {
        for (room in rooms) {
            room.polybox = Polybox.fromRect(room.rect);
            room.cornerEntryValue = layer.nextEntry();
        }

        dumpOutlinePolyboxes();
    }

    private function growRoomsSplitted() : Void {
        var canGrowMore : Bool;

        do {
            canGrowMore = false;

            for (room in rooms) {
                var maxSize : Int = 0;
                var splitSpecs : Array<PlanGeneratorSplitSpec> = [];

                for (edge in room.polybox.sure().getEdges()) {
                    if (!layer.pointInside(edge.from.addTo(edge.normal)) || !layer.pointInside(edge.to.addTo(edge.normal))) {
                        continue;
                    }

                    var splitFrom : Null<Point> = null;
                    var splitTo = new Point(0, 0);

                    edge.walk((point, isLastPoint) -> {
                        var entry = layer.getAt(point.addTo(edge.normal));

                        if (entry == 0) {
                            if (splitFrom == null) {
                                splitFrom = point.copy();
                            }

                            splitTo.setTo(point);
                        }

                        if (splitFrom != null && (entry != 0 || isLastPoint)) {
                            var size = splitFrom.sure().lineSizeTo(splitTo);

                            if (size >= maxSize) {
                                if (size > maxSize) {
                                    maxSize = size;
                                    splitSpecs = [];
                                }

                                splitSpecs.push(new PlanGeneratorSplitSpec(edge, splitFrom.sure().copy(), splitTo.copy()));
                            }

                            splitFrom = null;
                        }
                    });
                }

                if (splitSpecs.length == 0) {
                    continue;
                }

                var spec = random.nextFromArray(splitSpecs).sure();
                var splitFrom = spec.from.copy();
                var splitTo = spec.to.copy();

                while (true) {
                    var from = splitFrom.addTo(spec.edge.normal);
                    var to = splitTo.addTo(spec.edge.normal);

                    if (!layer.pointInside(from)
                        || !layer.pointInside(to)
                        || !layer.reduceRectFilled(Rect.fromPoints(from, to), IntLayer.REDUCE_ALL_EMPTY)
                    ) {
                        break;
                    }

                    splitFrom = from;
                    splitTo = to;
                }

                var points = room.polybox.sure().points;
                var toIndex = spec.edge.toIndex;

                if (spec.from.equals(spec.edge.from)) {
                    points[spec.edge.fromIndex].setTo(splitFrom);
                } else {
                    points.insert(toIndex, splitFrom);
                    points.insert(toIndex, spec.from);
                    toIndex += 2;
                }

                if (spec.to.equals(spec.edge.to)) {
                    points[toIndex].setTo(splitTo);
                } else {
                    points.insert(toIndex, spec.to);
                    points.insert(toIndex, splitTo);
                }

                canGrowMore = true;
                dumpOutlinePolyboxes();
            }
        } while (canGrowMore);
    }

    private function fillPossibleHoles() : Void {
        layer.fill(0, (entry) -> (entry != -1));

        for (room in rooms) {
            layer.fillPolybox(room.polybox.sure(), room.entryValue);
        }

        layer.fill(layer.nextEntry(), (entry : Int) -> (entry == 0));
        layer.cleanupLeftovers(-1);

        viewer.dumpIntLayer(layer);
    }

    private function dumpOutlinePolyboxes() : Void {
        layer.fill(0, (entry) -> (entry != -1));

        for (room in rooms) {
            layer.outlinePolybox(room.polybox.sure(), room.entryValue);
            layer.plot(room.polybox.sure().points, room.cornerEntryValue);
        }

        viewer.dumpIntLayer(layer);
    }
}
