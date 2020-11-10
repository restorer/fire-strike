package com.eightsines.firestrike.procedural.generator.step06;

import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.generator.AbstractSectionGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.geom.CornerType;
import com.eightsines.firestrike.procedural.geom.EdgePoint;
import com.eightsines.firestrike.procedural.geom.Extend;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section; // {SectionScenarioObject}
import com.eightsines.firestrike.procedural.section.SectionDecoration;
import com.eightsines.firestrike.procedural.section.SectionDecorationType;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.Pair;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

class DecorationsGenerator extends AbstractSectionGenerator implements Generator {
    private static inline final MIN_ROCKS : Int = 3;

    private var settings : Settings;

    public function new(settings : Settings, random : Random, layer : IntLayer, viewer : Viewer, sections : Array<Section>) {
        super(random, layer, viewer, sections);
        this.settings = settings;
    }

    public function generate() : Array<Section> {
        assignAppearanceKind();
        placeSwitches();
        placeBlockyDecorations();
        placeKeys();
        placeDecorationsNearBorder();

        return sections;
    }

    private function assignAppearanceKind() : Void {
        for (section in sections) {
            section.appearanceKind = (section.scenario == 1) ? Exit : (random.nextBool() ? Indoor : Outdoor);
        }
    }

    private function placeSwitches() : Void {
        for (section in sections) {
            switch (section.scenarioAction) {
                case EndLevel | Switch:
                    if (!placeSwitchForSection(section, false) && !placeSwitchForSection(section, true)) {
                        throw new GeneratorException("placeSwitches failed: no avail points");
                    }

                default:
                    // do not place switch if section has no scenario action, also do not place keys yet
            }
        }
    }

    private function placeSwitchForSection(section : Section, allowPlaceNearGates : Bool) : Bool {
        section.renderAvailOuterCells(layer, allowPlaceNearGates);

        if (settings.argVerboseLevel >= 1) {
            viewer.dumpIntLayer(layer);
        }

        var availMidPoints : Array<Point> = [];

        for (polybox in section.geometry) {
            for (edge in polybox.getEdges()) {
                var midPoint = edge.from.addTo(edge.to).divScalar(2);

                if (layer.getAt(midPoint) == Section.VAL_INNER_AVAIL) {
                    availMidPoints.push(midPoint);
                }
            }
        }

        var point = random.nextFromArray(availMidPoints);

        if (point == null) {
            point = random.nextFromArray(layer.collect([Section.VAL_INNER_AVAIL], section.getBbox()));

            if (point == null) {
                return false;
            }
        }

        section.scenarioObjects.push(new SectionScenarioObject(point, section.scenarioAction.sure()));
        dump(Actions);

        return true;
    }

    private function placeBlockyDecorations() : Void {
        for (section in sections) {
            dump(Actions, [section]);

            while (true) {
                section.renderAvailInnerCells(layer, false);

                if (settings.argVerboseLevel >= 1) {
                    viewer.dumpIntLayer(layer);
                }

                var bbox = section.getBbox();
                var availExtends : Array<Pair<Point, Extend>> = [];
                var maxArea : Int = -1;

                var extMap = GeneratorUtils.computeExtends(
                    layer,
                    bbox,
                    [ Section.VAL_INNER_AVAIL, Section.VAL_INNER_PASSABLE, Section.VAL_INNER_KEYPOINT ],
                    viewer
                );

                if (settings.argVerboseLevel >= 2) {
                    for (row in 0 ... bbox.height) {
                        for (col in 0 ... bbox.width) {
                            layer.set(row + bbox.row, col + bbox.col, extMap[row][col].getMinSize());
                        }
                    }

                    viewer.dumpIntLayer(layer);
                }

                for (row in 0 ... bbox.height) {
                    for (col in 0 ... bbox.width) {
                        var ext = extMap[row][col];
                        var area = ext.getArea();

                        if (ext.getMinSize() < 1 || area < maxArea) {
                            continue;
                        }

                        if (area > maxArea) {
                            availExtends = [];
                            maxArea = area;
                        }

                        availExtends.push(new Pair(new Point(row + bbox.row, col + bbox.col), ext));
                    }
                }

                if (maxArea < 0) {
                    break;
                }

                var pair = random.nextFromArray(availExtends).sure();

                var rect = pair.second.getRect(
                    pair.first.row,
                    pair.first.col,
                    pair.second.getMinSize() > 1 ? 2 : 1
                );

                if (rect.getMinSize() < 2 && random.nextBool()) {
                    break;
                }

                if (rect.getArea() == 1) {
                    section.decorations.push(new SectionDecoration(
                        random.nextBool() ? SectionDecorationType.Pillar : SectionDecorationType.Barrel,
                        rect.points()
                    ));
                } else if (rect.getMinSize() < 3) {
                    section.decorations.push(new SectionDecoration(
                        (random.nextFloatEx() > 0.75 && section.appearanceKind == Outdoor)
                            ? SectionDecorationType.Rock
                            : SectionDecorationType.Box,
                        rect.points()
                    ));
                } else {
                    section.decorations.push(new SectionDecoration(SectionDecorationType.Lattice, rect.borderPoints()));

                    var innerPoints = rect.expand(-1).points();
                    section.decorations.push(new SectionDecoration(SectionDecorationType.Pillar, innerPoints));
                    section.noTransPoints.pushAll(innerPoints);
                }

                dump(Actions, [section], null, null, pair.toString());
            }
        }

        dump(Actions);
    }

    private function placeKeys() : Void {
        for (section in sections) {
            switch (section.scenarioAction) {
                case Key(_):
                    dump(Actions, [section]);
                    section.renderAvailInnerCells(layer, true);

                default:
                    // do not place key if section has no scenario action, also ignore switches (they are placed earlier)
                    continue;
            }

            if (settings.argVerboseLevel >= 1) {
                viewer.dumpIntLayer(layer);
            }

            var point = GeneratorUtils.selectRandomFreestPoint(random, layer, layer.collect([Section.VAL_INNER_AVAIL], section.getBbox()));

            if (point == null) {
                throw new GeneratorException("placeKeys failed: no avail points");
            }

            section.scenarioObjects.push(new SectionScenarioObject(point, section.scenarioAction.sure()));
            dump(Actions);
        }
    }

    private function placeDecorationsNearBorder() : Void {
        for (section in sections) {
            section.renderAvailInnerCells(layer, false);

            if (settings.argVerboseLevel >= 1) {
                viewer.dumpIntLayer(layer, 'scenario = ${section.scenario}');
            }

            var layerCopy = (settings.argVerboseLevel >= 1 ? layer.copy() : null);

            var edgePoints = layer.traceEdgePoints([Section.VAL_INNER_AVAIL, Section.VAL_INNER_PASSABLE], section.getBbox()).filter((edgePoint) -> {
                if (!canPlaceDecorationAt(edgePoint.point)) {
                    edgePoint.destroyConnections();
                    return false;
                }

                layerCopy!.setAt(edgePoint.point, switch (edgePoint.cornerType) {
                    case None: -1;
                    case Inner: -2;
                    case Outer: -4;
                });

                return true;
            });

            if (edgePoints.length == 0) {
                continue;
            }

            if (layerCopy != null) {
                viewer.dumpIntLayer(layerCopy, 'scenario = ${section.scenario}');
            }

            var placedCount = placeDecorationsNearBorderInternal(
                section,
                EdgePoint.copyAll(edgePoints),
                [CornerType.Outer, CornerType.Inner, CornerType.None],
                SectionDecorationType.Lattice,
                Math.floor(edgePoints.length * 0.5)
            );

            placedCount += placeDecorationsNearBorderInternal(
                section,
                EdgePoint.copyAll(edgePoints),
                [CornerType.Inner],
                SectionDecorationType.Pillar,
                Math.floor((edgePoints.length - placedCount) * 0.25)
            );

            placedCount += placeDecorationsNearBorderInternal(
                section,
                EdgePoint.copyAll(edgePoints),
                [CornerType.Inner, CornerType.None],
                SectionDecorationType.Box,
                Math.floor((edgePoints.length - placedCount) * 0.5)
            );

            if (section.appearanceKind == Outdoor) {
                placeDecorationsNearBorderInternal(
                    section,
                    EdgePoint.copyAll(edgePoints),
                    [CornerType.Inner, CornerType.None],
                    SectionDecorationType.Rock,
                    Math.floor((edgePoints.length - placedCount) * 0.25)
                );
            }
        }

        dump(Actions);
    }

    private function placeDecorationsNearBorderInternal(
        section : Section,
        edgePoints : Array<EdgePoint>,
        cornerTypes : Array<CornerType>,
        decorationType : SectionDecorationType,
        maxCount : Int
    ) : Int {
        var placedCount = 0;

        while (true) {
            if (placedCount >= maxCount || random.nextFloatEx() > (decorationType == Rock ? 0.95 : 0.75)) {
                break;
            }

            var layerCopy = (settings.argVerboseLevel >= 2 ? layer.copy() : null);

            edgePoints = edgePoints.filter((edgePoint) -> {
                if (!cornerTypes.contains(edgePoint.cornerType) || !canPlaceDecorationAt(edgePoint.point)) {
                    edgePoint.destroyConnections();
                    return false;
                }

                layerCopy!.setAt(edgePoint.point, switch (edgePoint.cornerType) {
                    case None: -1;
                    case Inner: -2;
                    case Outer: -4;
                });

                return true;
            });

            if (edgePoints.length == 0) {
                break;
            }

            if (decorationType == Rock) {
                var isRockPlaced = false;
                random.shuffleArray(edgePoints);

                for (edgePoint in edgePoints) {
                    if (layerCopy != null) {
                        viewer.dumpIntLayer(layerCopy, 'decorationType = ${decorationType}');
                        layerCopy.setAt(edgePoint.point, -14);
                        viewer.dumpIntLayer(layerCopy, 'decorationType = ${decorationType}, point = ${edgePoint.point}');
                    }

                    var placedRocks = generateRock(section, edgePoint.point);

                    if (placedRocks != 0) {
                        placedCount += placedRocks;
                        isRockPlaced = true;
                        break;
                    }
                }

                if (!isRockPlaced || random.nextBool()) {
                    break;
                }
            } else {
                var edgePoint = random.nextFromArray(edgePoints).sure();

                if (layerCopy != null) {
                    viewer.dumpIntLayer(layerCopy, 'decorationType = ${decorationType}');
                    layerCopy.setAt(edgePoint.point, -14);
                    viewer.dumpIntLayer(layerCopy, 'decorationType = ${decorationType}, point');
                }

                switch (decorationType) {
                    case Box | Pillar | Barrel:
                        section.decorations.push(new SectionDecoration(decorationType, [edgePoint.point]));
                        layer.setAt(edgePoint.point, Section.VAL_INNER_DECORATION);
                        placedCount++;

                    case Lattice:
                        placedCount += generateLattice(section, edgePoint);

                    case Rock:
                }
            }

            if (settings.argVerboseLevel >= 2) {
                viewer.dumpIntLayer(layer, 'decorationType = ${decorationType}, placed');
            }
        }

        return placedCount;
    }

    private function generateRock(section : Section, point : Point) : Int {
        var availPoints : Array<Point> = [];

        for (i in -1 ... 2) {
            for (j in -1 ... 2) {
                if (i == 0 && j == 0) {
                    continue;
                }

                var rockPoint = point.add(i, j);

                if (layer.pointInside(rockPoint) && canPlaceDecorationAt(rockPoint)) {
                    availPoints.push(rockPoint);
                }
            }
        }

        if (availPoints.length < MIN_ROCKS - 1) {
            return 0;
        }

        random.shuffleArray(availPoints);
        availPoints = availPoints.slice(0, random.nextIntRangeIn(MIN_ROCKS - 1, IntMath.min(4, availPoints.length)));

        var realPoints = [point];
        layer.setAt(point, Section.VAL_INNER_DECORATION);

        for (rockPoint in availPoints) {
            if (!canPlaceDecorationAt(rockPoint)) {
                continue;
            }

            layer.setAt(rockPoint, Section.VAL_INNER_DECORATION);
            realPoints.push(rockPoint);
        }

        section.decorations.push(new SectionDecoration(SectionDecorationType.Rock, realPoints));
        return realPoints.length;
    }

    private function generateLattice(section : Section, edgePoint : EdgePoint) : Int {
        var startEdgePoint = edgePoint;
        var finishEdgePoint = edgePoint;

        while (startEdgePoint.prevConnected != null
            && (startEdgePoint.prevConnected.unsafe().cornerType == None || random.nextFloatEx() >= 0.9)
        ) {
            startEdgePoint = startEdgePoint.prevConnected.unsafe();
        }

        if (startEdgePoint.prevConnected!.cornerType == Inner) {
            startEdgePoint = startEdgePoint.prevConnected.unsafe();
        }

        while (finishEdgePoint.nextConnected != null
            && (finishEdgePoint.nextConnected.unsafe().cornerType == None || random.nextFloatEx() >= 0.9)
        ) {
            finishEdgePoint = finishEdgePoint.nextConnected.unsafe();
        }

        if (finishEdgePoint.nextConnected!.cornerType == Inner) {
            finishEdgePoint = finishEdgePoint.nextConnected.unsafe();
        }

        var points = [];
        var placedCount = 0;

        while (true) {
            if (canPlaceDecorationAt(startEdgePoint.point)) {
                points.push(startEdgePoint.point);
                layer.setAt(startEdgePoint.point, Section.VAL_INNER_DECORATION);
            } else if (points.length != 0) {
                if (points.length > 2) {
                    section.decorations.push(new SectionDecoration(SectionDecorationType.Lattice, points));
                    placedCount += points.length;
                }

                points = [];
            }

            if (startEdgePoint == finishEdgePoint) {
                break;
            }

            startEdgePoint = startEdgePoint.nextConnected.sure();
        }

        if (points.length > 2) {
            section.decorations.push(new SectionDecoration(SectionDecorationType.Lattice, points));
            placedCount += points.length;
        }

        return placedCount;
    }

    private function canPlaceDecorationAt(point : Point) : Bool {
        if (layer.getAt(point) != Section.VAL_INNER_AVAIL) {
            return false;
        }

        var reachableCount : Int = 0;

        for (direction in Point.DIRECTIONS) {
            if (shouldBeReachableNearDecoration(point.addTo(direction))) {
                reachableCount++;
            }
        }

        if (reachableCount == 4) {
            var cornersPassCount : Int = 0;

            for (corner in Point.CORNERS) {
                if (isPassableNearDecoration(point.addTo(corner))) {
                    cornersPassCount++;
                }
            }

            return (cornersPassCount >= 3);
        }

        if (reachableCount == 3) {
            for (i in 0 ... Point.DIRECTIONS.length) {
                if (shouldBeReachableNearDecoration(point.addTo(Point.DIRECTIONS[i]))
                    && shouldBeReachableNearDecoration(point.addTo(Point.DIRECTIONS[(i + 1) % Point.DIRECTIONS.length]))
                    && !isPassableNearDecoration(point.addTo(Point.CORNERS[i]))
                ) {
                    return false;
                }
            }

            return true;
        }

        if (reachableCount == 2) {
            for (i in 0 ... Point.DIRECTIONS.length) {
                if (shouldBeReachableNearDecoration(point.addTo(Point.DIRECTIONS[i]))
                    && shouldBeReachableNearDecoration(point.addTo(Point.DIRECTIONS[(i + 1) % Point.DIRECTIONS.length]))
                    && isPassableNearDecoration(point.addTo(Point.CORNERS[i]))
                ) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    private function isPassableNearDecoration(point : Point) : Bool {
        var entry = layer.getAt(point);
        return entry == Section.VAL_INNER_AVAIL || entry == Section.VAL_INNER_PASSABLE;
    }

    private function shouldBeReachableNearDecoration(point : Point) : Bool {
        var entry = layer.getAt(point);
        return entry == Section.VAL_INNER_AVAIL || entry == Section.VAL_INNER_PASSABLE || entry == Section.VAL_INNER_KEYPOINT;
    }
}
