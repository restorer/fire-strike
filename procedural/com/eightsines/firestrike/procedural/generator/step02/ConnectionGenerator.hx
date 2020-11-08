package com.eightsines.firestrike.procedural.generator.step02;

import com.eightsines.firestrike.procedural.generator.AbstractSectionGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.section.SectionConnection;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;
import org.zamedev.lib.ds.HashSet;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

class ConnectionGenerator extends AbstractSectionGenerator implements Generator {
    public function new(random : Random, layer : IntLayer, viewer : Viewer, sections : SafeArray<Section>) {
        super(random, layer, viewer, sections);
    }

    public function generate() : SafeArray<Section> {
        dump();

        removeInvalidSections();
        findConnections();
        removeUnconnectedSections();
        removeExcessConnections();

        return sections;
    }

    // Каждая точка входит в квадрат 3x3, но пройти нельзя будет, из-за того, что по краям будут стены
    //
    // ###
    // #.O##
    // ##O.#
    //   ###
    private function removeInvalidSections() : Void {
        var tempLayer = new IntLayer(layer.width, layer.height);

        var newSections = sections.filter((section) -> {
            tempLayer.clear();

            for (row in 0 ... layer.height) {
                for (col in 0 ... layer.width) {
                    if (section.__getPolybox().isInside(row, col, false)) {
                        tempLayer.set(row, col, 1);
                    }
                }
            }

            return tempLayer.isSinglePartition(1);
        });

        if (newSections.length != sections.length) {
            sections = newSections;
            dump();
        }
    }

    private function findConnections() : Void {
        var sectionMap = new Map<Int, Section>();

        for (section in sections) {
            sectionMap[section.entryValue] = section;
        }

        for (section in sections) {
            for (edge in section.__getPolybox().getEdges()) {
                if (!layer.pointInside(edge.from.addTo(edge.normal)) || !layer.pointInside(edge.to.addTo(edge.normal))) {
                    continue;
                }

                var connectFrom : Null<Point> = null;
                var connectSection : Null<Section> = null;
                var connectTo = new Point(0, 0);

                edge.walk((point, isLastPoint) -> {
                    var otherPoint = point.addTo(edge.normal);
                    var otherSection = sectionMap[layer.getAt(otherPoint)];
                    var hasConnection = (otherSection != null && !otherSection.__getPolybox().isCornerPoint(otherPoint));

                    if (hasConnection) {
                        if (connectFrom == null) {
                            connectFrom = point.copy();
                            connectSection = otherSection;
                        }

                        connectTo.setTo(point);
                    }

                    if (connectFrom != null && (!hasConnection || isLastPoint)) {
                        var _connectFrom = connectFrom.unsafe();

                        var connection = new SectionConnection(
                            connectSection,
                            _connectFrom.copy(),
                            connectTo.copy(),
                            edge.normal
                        );

                        var otherConnection = new SectionConnection(
                            section,
                            _connectFrom.addTo(edge.normal),
                            connectTo.addTo(edge.normal),
                            edge.normal.scalar(-1)
                        );

                        connection.otherConnection = otherConnection;
                        otherConnection.otherConnection = connection;

                        section.connections.push(connection);
                        connectSection.unsafe().connections.push(otherConnection);

                        layer.fillRect(connection.rect, -1);
                        layer.fillRect(otherConnection.rect, -1);
                        viewer.dumpIntLayer(layer);

                        connectFrom = null;
                        connectSection = null;
                    }
                }, false);
            }
        }
    }

    private function removeUnconnectedSections() : Void {
        var connectedRoots = GeneratorUtils.computeConnectedRoots(
            sections,
            (section) -> section.connections.safeMap((connection) -> connection.ensureSection())
        );

        if (connectedRoots.length == 0) {
            dump();
            throw new GeneratorException("removeUnconnectedSections failed: connectedRoots are empty");
        }

        connectedRoots.sort((a, b) -> (b.length - a.length));
        sections = connectedRoots[0];

        dump(); // dump in any case
    }

    private function removeExcessConnections() : Void {
        // TODO: use more optimal method:
        //
        // 1. Clear set of visited sections
        // 2. Select initial section, append it to current sections
        // 3. Append this section to set of visited sections
        // 4. Visit every section in current sections list (if this section isn't already visited)
        // 5. Visit every connection of section
        // 6. If connected section isn't visited already, append it to set of visited sections and to current sections list
        //    (in other case do nothing)
        // 7. If there is some sections left in current sections list, go to 4

        while (true) {
            var excessConnections : SafeArray<SectionConnection> = [];
            var visitedSet = new HashSet<SectionConnection>();

            for (section in sections) {
                for (connection in section.connections) {
                    if (!connection.__removed && !visitedSet.exists(connection)) {
                        connection.__setBothRemoved(true);

                        visitedSet.add(connection);
                        visitedSet.add(connection.otherConnection.sure());

                        if (isEverySectionReachable()) {
                            excessConnections.push(connection);
                        }

                        connection.__setBothRemoved(false);
                    }
                }
            }

            if (excessConnections.length == 0) {
                break;
            }

            random.nextFromArray(excessConnections).sure().__setBothRemoved(true);
            dump();
        }
    }

    private function isEverySectionReachable() : Bool {
        if (sections.length == 0) {
            return false;
        }

        var visitedSet = new HashSet<Section>();
        var currentSections : SafeArray<Section> = [sections[0]];

        visitedSet.add(sections[0]);

        while (true) {
            var newSections : SafeArray<Section> = [];

            for (section in currentSections) {
                for (connection in section.connections) {
                    if (!connection.__removed && !visitedSet.exists(connection.ensureSection())) {
                        visitedSet.add(connection.ensureSection());
                        newSections.push(connection.ensureSection());
                    }
                }
            }

            if (newSections.length == 0) {
                break;
            }

            currentSections = newSections;
        }

        for (section in sections) {
            if (!visitedSet.exists(section)) {
                return false;
            }
        }

        return true;
    }
}
