package com.eightsines.firestrike.procedural.generator.step03;

import com.eightsines.firestrike.procedural.generator.AbstractSectionGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.layer.BoolLayer;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.section.SectionConnection;
import com.eightsines.firestrike.procedural.section.SectionFence;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;
import org.zamedev.lib.ds.HashSet;

using Lambda;
using Safety;
using com.eightsines.firestrike.procedural.util.Tools;

class HumanizerGenerator extends AbstractSectionGenerator implements Generator {
    public function new(random : Random, layer : IntLayer, viewer : Viewer, sections : Array<Section>) {
        super(random, layer, viewer, sections);
    }

    public function generate() : Array<Section> {
        var sectionsBackup = Section.copyAll(sections);

        randomizeConnections();
        cleanupRemovedConnections();
        mergePassableSections();

        if (sections.length < 2) {
            sections = sectionsBackup;

            // Connections can be removed at ConnectionGenerator step, so clean up it after restoring from backup
            cleanupRemovedConnections();
        }

        checkSectionsAndConnections();
        return sections;
    }

    private function randomizeConnections() : Void {
        var hasChanges : Bool = false;

        for (section in sections) {
            for (connection in section.connections) {
                if (connection.__removed) {
                    if (random.nextFloatEx() > 0.8) {
                        connection.__setBothRemoved(false);
                        hasChanges = true;
                    }
                } else if (random.nextFloatEx() > 0.9) {
                    connection.__setBothPassable(true);
                    hasChanges = true;
                }
            }
        }

        if (hasChanges) {
            dump();
        }
    }

    private function cleanupRemovedConnections() : Void {
        for (section in sections) {
            section.connections = section.connections.filter((connection) -> !connection.__removed);
        }
    }

    private function mergePassableSections() : Void {
        var visitedSet = new HashSet<Section>();

        for (originalSection in sections.copy()) {
            if (visitedSet.exists(originalSection)) {
                continue;
            }

            var mergeWithSections : Array<Section> = [];

            GeneratorUtils.walkNeighbors(
                originalSection,
                (connection) -> connection.__passable,
                (section) -> {
                    mergeWithSections.push(section);
                    return true;
                },
                visitedSet
            );

            if (mergeWithSections.length == 0) {
                continue;
            }

            dump(null, [originalSection].concat(mergeWithSections));

            for (connection in originalSection.connections.copy()) {
                if (connection.__removed) {
                    throw new GeneratorException("mergePassableSections failed: connection in originalSection.connections was already removed");
                }

                if (mergeWithSections.contains(connection.ensureSection())) {
                    createFences(originalSection, connection);
                    originalSection.connections.remove(connection);
                } else if (connection.__passable) {
                    throw new GeneratorException("mergePassableSections failed: connection in originalSection.connections is passable, but not in merge list");
                }
            }

            for (connection in originalSection.connections) {
                if (connection.__removed) {
                    throw new GeneratorException("mergePassableSections failed: connection in originalSection is removed");
                }

                if (connection.__passable) {
                    throw new GeneratorException("mergePassableSections failed: connection in originalSection is passable");
                }
            }

            for (section in mergeWithSections) {
                originalSection.geometry.push(section.__getPolybox());
                sections.remove(section);

                for (connection in section.connections) {
                    if (connection.__removed) {
                        continue;
                    }

                    if (mergeWithSections.contains(connection.ensureSection())) {
                        createFences(originalSection, connection);
                    } else {
                        connection.otherConnection.sure().section = originalSection;
                        originalSection.connections.push(connection);
                    }
                }
            }

            dump(null, [originalSection], originalSection);
            dump();
        }
    }

    private function checkSectionsAndConnections() : Void {
        var overlapLayer = new BoolLayer(layer.width, layer.height);

        for (section in sections) {
            for (polybox in section.geometry) {
                if (!overlapLayer.reducePolyboxFilled(polybox, BoolLayer.REDUCE_ALL_FALSE)) {
                    throw new GeneratorException("checkSectionsAndConnections failed: section polybox overlaps with other polybox or section");
                }

                overlapLayer.fillPolybox(polybox, true);
            }
        }

        overlapLayer.clear();

        for (section in sections) {
            for (connection in section.connections) {
                if (connection.__passable) {
                    throw new GeneratorException("checkSectionsAndConnections failed: connection is passable");
                }

                if (connection.__removed) {
                    throw new GeneratorException("checkSectionsAndConnections failed: connection is removed");
                }

                if (!sections.contains(connection.ensureSection())) {
                    throw new GeneratorException("checkSectionsAndConnections failed: connection points to not existing section");
                }

                if (connection.otherConnection.sure().ensureSection() != section) {
                    throw new GeneratorException("checkSectionsAndConnections failed: other side connections it not points to current section");
                }

                if (!overlapLayer.reduceRectFilled(connection.rect, BoolLayer.REDUCE_ALL_FALSE)) {
                    throw new GeneratorException("checkSectionsAndConnections failed: connection rect overlaps with some other connection");
                }

                overlapLayer.fillRect(connection.rect, true);
            }
        }
    }

    private function createFences(section : Section, connection : SectionConnection) : Void {
        var fence = SectionFence.fromConnection(connection);
        var otherFence = SectionFence.fromConnection(connection.otherConnection.sure());

        fence.otherFence = otherFence;
        otherFence.otherFence = fence;

        section.fences.push(fence);
        section.fences.push(otherFence);

        connection.__setBothRemoved(true);
    }
}
