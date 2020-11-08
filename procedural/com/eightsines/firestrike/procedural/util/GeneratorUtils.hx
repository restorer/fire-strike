package com.eightsines.firestrike.procedural.util;

import com.eightsines.firestrike.procedural.geom.Extend;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.section.SectionConnection;
import com.eightsines.firestrike.procedural.view.Viewer;
import org.zamedev.lib.ds.HashSet;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

class GeneratorUtils {
    public static function ensureMinSectionSize(layer : IntLayer, viewer : Viewer) : Void {
        if (layer.markLeftovers(Section.MIN_SIZE, Section.MIN_SIZE, -1)) {
            viewer.dumpIntLayer(layer);

            if (layer.mergeLeftovers(Section.MIN_SIZE, Section.MIN_SIZE, -1)) {
                viewer.dumpIntLayer(layer);
            }

            if (layer.cleanupLeftovers(-1)) {
                viewer.dumpIntLayer(layer);
            }
        }
    }

    public static function traceSections(layer : IntLayer) : SafeArray<Section> {
        var existingEntries = new HashSet<Int>();

        return layer.tracePolyboxes().map((pair) -> {
            while (existingEntries.exists(pair.second)) {
                pair.second = layer.nextEntry();
            }

            existingEntries.add(pair.second);
            return new Section(pair.first, pair.second);
        });
    }

    public static function walkNeighbors(
        originalSection : Section,
        connectionPredicate : Null<(SectionConnection) -> Bool>,
        sectionPredicate : (Section) -> Bool,
        ?visitedSet : HashSet<Section>
    ) : Void {
        var neighbors : SafeArray<Section> = [originalSection];

        if (visitedSet == null) {
            visitedSet = new HashSet<Section>();
        }

        visitedSet.add(originalSection);

        while (true) {
            var newNeighbors : SafeArray<Section> = [];

            for (section in neighbors) {
                for (connection in section.connections) {
                    if (connectionPredicate != null && !connectionPredicate(connection)) {
                        continue;
                    }

                    if (!visitedSet.exists(connection.ensureSection())) {
                        visitedSet.add(connection.ensureSection());

                        if (sectionPredicate(connection.ensureSection())) {
                            newNeighbors.push(connection.ensureSection());
                        }
                    }
                }
            }

            if (newNeighbors.isEmpty()) {
                break;
            }

            neighbors = newNeighbors;
        }
    }

    public static function computeConnectedRoots(
        inputSections : SafeArray<Section>,
        outerSectionsCb : (Section) -> SafeArray<Section>
    ) : SafeArray<SafeArray<Section>> {
        var connectedRoots : SafeArray<SafeArray<Section>> = [];

        for (section in inputSections) {
            // Can't use LinkedSet, because LinkedObjectSet is not currently implemented
            var connectedSet = new HashSet<Section>();
            connectedSet.add(section);

            for (outerSection in outerSectionsCb(section)) {
                connectedSet.add(outerSection);
            }

            var newConnectedRoots : SafeArray<SafeArray<Section>> = [];

            for (connected in connectedRoots) {
                var hasIntersection : Bool = false;

                for (other in connected) {
                    if (connectedSet.exists(other)) {
                        hasIntersection = true;
                        break;
                    }
                }

                if (hasIntersection) {
                    for (other in connected) {
                        connectedSet.add(other);
                    }
                } else {
                    newConnectedRoots.push(connected);
                }
            }

            var subConnectedRoots = connectedSet.keys().array();
            subConnectedRoots.sort((a, b) -> a.__id - b.__id);

            newConnectedRoots.push(subConnectedRoots);
            connectedRoots = newConnectedRoots;
        }

        return connectedRoots;
    }

    public static function computeExtends(layer : IntLayer, bbox : Rect, entries : SafeArray<Int>, viewer : Viewer) : SafeArray<SafeArray<Extend>> {
        var result : SafeArray<SafeArray<Extend>> = [ for (row in 0 ... bbox.height) [ for (col in 0 ... bbox.width) new Extend() ] ];

        for (row in 0 ... bbox.height) {
            var irow = bbox.height - row - 1;

            for (col in 0 ... bbox.width) {
                var icol = bbox.width - col - 1;

                result[row][col].tl = if (entries.contains(layer.get(bbox.row + row, bbox.col + col))) {
                    IntMath.min(
                        ((row == 0 || col == 0) ? -1 : result[row - 1][col - 1].tl),
                        IntMath.min(
                            (row == 0 ? -1 : result[row - 1][col].tl),
                            (col == 0 ? -1 : result[row][col - 1].tl)
                        )
                    ) + 1;
                } else {
                    -1;
                }

                result[row][icol].tr = if (entries.contains(layer.get(bbox.row + row, bbox.col + icol))) {
                    IntMath.min(
                        ((row == 0 || col == 0) ? -1 : result[row - 1][icol + 1].tr),
                        IntMath.min(
                            (row == 0 ? -1 : result[row - 1][icol].tr),
                            (col == 0 ? -1 : result[row][icol + 1].tr)
                        )
                    ) + 1;
                } else {
                    -1;
                }

                result[irow][col].bl = if (entries.contains(layer.get(bbox.row + irow, bbox.col + col))) {
                    IntMath.min(
                        ((row == 0 || col == 0) ? -1 : result[irow + 1][col - 1].bl),
                        IntMath.min(
                            (row == 0 ? -1 : result[irow + 1][col].bl),
                            (col == 0 ? -1 : result[irow][col - 1].bl)
                        )
                    ) + 1;
                } else {
                    -1;
                }

                result[irow][icol].br = if (entries.contains(layer.get(bbox.row + irow, bbox.col + icol))) {
                    IntMath.min(
                        ((row == 0 || col == 0) ? -1 : result[irow + 1][icol + 1].br),
                        IntMath.min(
                            (row == 0 ? -1 : result[irow + 1][icol].br),
                            (col == 0 ? -1 : result[irow][icol + 1].br)
                        )
                    ) + 1;
                } else {
                    -1;
                }
            }
        }

        return result;
    }

    /**
        Выбирает случайную точку из массива, но отдаёт предпочтение тем, вокруг которых больше пустого места.
    **/
    public static function selectRandomFreestPoint(random : Random, layer : IntLayer, points : SafeArray<Point>) : Null<Point> {
        if (points.isEmpty()) {
            return null;
        }

        if (points.length == 1) {
            return points[0];
        }

        points.stableSort((a, b) -> {
            var cntA : Int = 0;
            var cntB : Int = 0;

            for (i in -1 ... 2) {
                for (j in -1 ... 2) {
                    if (i == 0 && j == 0) {
                        continue;
                    }

                    if (layer.safeGet(a.row + i, a.col + j, 0) == Section.VAL_INNER_AVAIL) {
                        cntA++;
                    }

                    if (layer.safeGet(b.row + i, b.col + j, 0) == Section.VAL_INNER_AVAIL) {
                        cntB++;
                    }
                }
            }

            return cntB - cntA;
        });

        return points[Std.int(points.length * Math.pow(random.nextFloatEx(), 5))];
    }

    public static function computeScenarioSections(sections : SafeArray<Section>) : SafeArray<SafeArray<Section>> {
        var result : SafeArray<SafeArray<Section>> = [];
        var maxIterations = 500;
        var currentSection = sections.find((section) -> (section.scenario == 0)).sure();

        while (maxIterations > 0) {
            var availSections = sections.filter((section) -> (section.scenario == currentSection.scenario));
            var scenarioOpenerSection = availSections.find((section) -> (section.scenarioOpener != 0));

            result.push(availSections);

            if (scenarioOpenerSection == null) {
                break;
            }

            var _scenarioOpenerSection = scenarioOpenerSection.unsafe();

            currentSection = sections.find((section) -> (section.scenario == _scenarioOpenerSection.scenarioOpener)).sure();
            maxIterations--;

            if (maxIterations <= 0) {
                throw new GeneratorException("computeSectionsPath failed: too much iterations");
            }
        }

        return result;
    }

    public static function fillScenarioMaps(
        sections : SafeArray<Section>,
        scenarioMap : Null<Map<Int, SafeArray<Section>>>,
        scenarioOpenerMap : Null<Map<Int, Section>> = null
    ) : Void {
        for (section in sections) {
            if (scenarioMap != null) {
                if (section.scenario != 0) {
                    var list = scenarioMap[section.scenario];

                    if (list != null) {
                        list.push(section);
                    } else {
                        scenarioMap[section.scenario] = [section];
                    }
                }
            }

            if (scenarioOpenerMap != null) {
                if (section.scenarioOpener != 0) {
                    if (scenarioOpenerMap.exists(section.scenarioOpener)) {
                        throw new GeneratorException("fillScenarioMaps failed: several sections with the same scenarioOpener");
                    }

                    scenarioOpenerMap[section.scenarioOpener] = section;
                }
            }
        }
    }
}
