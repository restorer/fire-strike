package com.eightsines.firestrike.procedural.generator.step01;

import com.eightsines.firestrike.procedural.generator.AbstractGeometryGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

using Lambda;
using Safety;
using com.eightsines.firestrike.procedural.util.Tools;

class BspGeneratorNode {
    private static inline final MIN_NODE_SIZE : Int = 6;
    private static inline final MIN_ROOM_SIZE : Int = 5;

    public var bbox : Rect;
    public var entryValue : Int;
    public var leftChild : Null<BspGeneratorNode> = null;
    public var rightChild : Null<BspGeneratorNode> = null;
    public var room : Null<Rect> = null;

    public function new(row : Int, col : Int, height : Int, width : Int, entryValue : Int) {
        bbox = new Rect(row, col, height, width);
        this.entryValue = entryValue;
    }

    public function isLeaf() : Bool {
        return (leftChild == null && rightChild == null);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public function split(random : Random, layer : IntLayer) : Bool {
        if (leftChild != null && rightChild != null) {
            return false;
        }

        var splitVertical : Bool;

        if (bbox.width > bbox.height && bbox.width * 1.25 >= bbox.height) {
            splitVertical = false;
        } else if (bbox.height > bbox.width && bbox.height * 1.25 >= bbox.width) {
            splitVertical = true;
        } else {
            splitVertical = random.nextBool();
        }

        var max = (splitVertical ? bbox.height : bbox.width) - MIN_NODE_SIZE;

        if (max <= MIN_NODE_SIZE) {
            return false;
        }

        var split = random.nextIntRangeIn(MIN_NODE_SIZE, max);

        if (splitVertical) {
            leftChild = new BspGeneratorNode(bbox.row, bbox.col, split, bbox.width, entryValue);
            rightChild = new BspGeneratorNode(bbox.row + split, bbox.col, bbox.height - split, bbox.width, layer.nextEntry());
        } else {
            leftChild = new BspGeneratorNode(bbox.row, bbox.col, bbox.height, split, entryValue);
            rightChild = new BspGeneratorNode(bbox.row, bbox.col + split, bbox.height, bbox.width - split, layer.nextEntry());
        }

        return true;
    }

    public function createRoom(random : Random) : Void {
        var width = random.nextIntRangeIn(MIN_ROOM_SIZE, bbox.width);
        var height = random.nextIntRangeIn(MIN_ROOM_SIZE, bbox.height);

        room = new Rect(
            random.nextIntRangeIn(bbox.row, bbox.row + bbox.height - height),
            random.nextIntRangeIn(bbox.col, bbox.col + bbox.width - width),
            height,
            width
        );
    }

    public function findRoom(random : Random) : Null<Rect> {
        if (room != null) {
            return room;
        }

        var leftRoom = leftChild!.findRoom(random);
        var rightRoom = rightChild!.findRoom(random);

        if (leftRoom == null && rightRoom == null) {
            return null;
        }

        if (leftRoom == null) {
            return rightRoom;
        }

        if (rightRoom == null) {
            return leftRoom;
        }

        return (random.nextBool() ? leftRoom : rightRoom);
    }
}

class BspGenerator extends AbstractGeometryGenerator implements Generator {
    private var nodes : Array<BspGeneratorNode> = [];

    public function new(random : Random, layer : IntLayer, viewer : Viewer) {
        super(random, layer, viewer);
    }

    public function generate() : Array<Section> {
        createNodes();
        createRooms();
        drawHalls();
        GeneratorUtils.ensureMinSectionSize(layer, viewer);

        return GeneratorUtils.traceSections(layer);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private function createNodes() : Void {
        nodes = [new BspGeneratorNode(0, 0, layer.height, layer.width, layer.nextEntry())];

        var maxNodeSize = Std.int(Math.min(layer.width, layer.height) * 0.75);
        var currentNodes = nodes;

        while (true) {
            dumpNodes();
            var nextNodes : Array<BspGeneratorNode> = [];

            for (node in currentNodes) {
                if (node.isLeaf()
                    && (node.bbox.width > maxNodeSize || node.bbox.height > maxNodeSize || random.nextFloatEx() > 0.25)
                    && node.split(random, layer)
                ) {
                    nextNodes.push(node.leftChild.sure());
                    nextNodes.push(node.rightChild.sure());
                }
            }

            if (nextNodes.length == 0) {
                break;
            }

            nodes.pushAll(nextNodes);
            currentNodes = nextNodes;
        }
    }

    private function createRooms() : Void {
        for (node in nodes) {
            if (node.isLeaf()) {
                node.createRoom(random);
            }
        }

        dumpNodes();
    }

    private function drawHalls() : Void {
        for (node in nodes) {
            if (!node.isLeaf()) {
                drawHall(node.leftChild.sure().findRoom(random).sure(), node.rightChild.sure().findRoom(random).sure());
            }
        }
    }

    private function drawHall(roomA : Rect, roomB : Rect) : Void {
        var commonMinSize = IntMath.min(IntMath.min(roomA.height, roomA.width), IntMath.min(roomB.height, roomB.width));
        var maxExpand = Std.int((commonMinSize - 1) / 2);
        var expand = (maxExpand <= 1) ? 1 : random.nextIntRangeIn(1, maxExpand);

        var pointA = new Point(
            random.nextIntRangeIn(roomA.row + expand, roomA.row + roomA.height - 1 - expand),
            random.nextIntRangeIn(roomA.col + expand, roomA.col + roomA.width - 1 - expand)
        );

        var pointB = new Point(
            random.nextIntRangeIn(roomB.row + expand, roomB.row + roomB.height - 1 - expand),
            random.nextIntRangeIn(roomB.col + expand, roomB.col + roomB.width - 1 - expand)
        );

        var entry = layer.nextEntry();

        if (random.nextBool()) {
            layer.fillRect(
                Rect.fromCoords(pointA.row, pointA.col, pointB.row, pointA.col).expand(expand),
                entry,
                IntLayer.COND_IS_EMPTY
            );

            layer.fillRect(
                Rect.fromCoords(pointB.row, pointA.col, pointB.row, pointB.col).expand(expand),
                entry,
                IntLayer.COND_IS_EMPTY
            );
        } else {
            layer.fillRect(
                Rect.fromCoords(pointA.row, pointA.col, pointA.row, pointB.col).expand(expand),
                entry,
                IntLayer.COND_IS_EMPTY
            );

            layer.fillRect(
                Rect.fromCoords(pointA.row, pointB.col, pointB.row, pointB.col).expand(expand),
                entry,
                IntLayer.COND_IS_EMPTY
            );
        }

        if (layer.partition(entry) != 0) {
            viewer.dumpIntLayer(layer);
        }
    }

    private function dumpNodes() : Void {
        layer.clear();

        for (node in nodes) {
            if (node.isLeaf()) {
                node.room.runOr(
                    (room) -> layer.fillRect(room, node.entryValue),
                    () -> layer.outlineRect(node.bbox, node.entryValue)
                );
            }
        }

        viewer.dumpIntLayer(layer);
    }
}
