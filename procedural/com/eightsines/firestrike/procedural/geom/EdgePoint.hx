package com.eightsines.firestrike.procedural.geom;

import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.Sequence;

using Safety;

class EdgePoint {
    public var point(default, null) : Point;
    public var prevConnected(default, null) : Null<EdgePoint> = null;
    public var nextConnected(default, null) : Null<EdgePoint> = null;
    public var cornerType(default, null) : CornerType;
    public var __id : Int;
    public var __copyPrevConnectedId : Int = 0;
    public var __copyNextConnectedId : Int = 0;

    public function new(point : Point, ?cornerType : CornerType, ?__id : Int) {
        this.point = point;
        this.cornerType = (cornerType == null ? CornerType.None : cornerType);
        this.__id = (__id == null ? Sequence.nextId() : __id);
    }

    public static function copyAll(source : SafeArray<EdgePoint>) : SafeArray<EdgePoint> {
        for (edgePoint in source) {
            edgePoint.__copyPrevConnectedId = edgePoint.prevConnected!.__id.or(0);
            edgePoint.__copyNextConnectedId = edgePoint.nextConnected!.__id.or(0);
        }

        var destination = source.map((section) -> section.copyWithoutOtherSize());
        var edgePointMap = new Map<Int, EdgePoint>();

        for (edgePoint in destination) {
            edgePointMap[edgePoint.__id] = edgePoint;
        }

        for (edgePoint in destination) {
            if (edgePoint.__copyPrevConnectedId != 0) {
                if (!edgePointMap.exists(edgePoint.__copyPrevConnectedId)) {
                    throw new GeneratorException("copyAll failed: prevConnected was not found");
                }

                edgePoint.prevConnected = edgePointMap[edgePoint.__copyPrevConnectedId];
            }

            if (edgePoint.__copyNextConnectedId != 0) {
                if (!edgePointMap.exists(edgePoint.__copyNextConnectedId)) {
                    throw new GeneratorException("copyAll failed: nextConnected was not found");
                }

                edgePoint.nextConnected = edgePointMap[edgePoint.__copyNextConnectedId];
            }
        }

        return destination;
    }

    public function destroyConnections() : Void {
        if (prevConnected != null) {
            prevConnected.unsafe().nextConnected = null;
            prevConnected = null;
        }

        if (nextConnected != null) {
            nextConnected.unsafe().prevConnected = null;
            nextConnected = null;
        }

        cornerType = CornerType.None;
    }

    public function setConnections(prevConnected : Null<EdgePoint>, nextConnected : Null<EdgePoint>) : Void {
        this.prevConnected = prevConnected;
        this.nextConnected = nextConnected;

        if (prevConnected == null || nextConnected == null) {
            cornerType = Inner;
            return;
        }

        var prevDirection = Point.directionFor(point.sub(prevConnected.unsafe().point));
        var nextDirection = Point.directionFor(nextConnected.unsafe().point.sub(point));

        if (prevDirection < 0 || nextDirection < 0) {
            // Shouldn't happen, but check for the great justice
            cornerType = Inner;
            return;
        }

        cornerType = switch (((nextDirection - prevDirection) + Point.DIRECTIONS.length) % Point.DIRECTIONS.length) {
            case 0: None;
            case 3: Outer;
            default: Inner;
        }
    }

    private function copyWithoutOtherSize() : EdgePoint {
        var result = new EdgePoint(point, cornerType, __id);
        result.__copyPrevConnectedId = __copyPrevConnectedId;
        result.__copyNextConnectedId = __copyNextConnectedId;
        return result;
    }
}
