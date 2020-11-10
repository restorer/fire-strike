package com.eightsines.firestrike.procedural.generator.step04;

import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.generator.AbstractSectionGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;
import org.zamedev.lib.ds.HashSet;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

class ScenarioGenerator extends AbstractSectionGenerator implements Generator {
    private var settings : Settings;

    public function new(settings : Settings, random : Random, layer : IntLayer, viewer : Viewer, sections : Array<Section>) {
        super(random, layer, viewer, sections);
        this.settings = settings;
    }

    public function generate() : Array<Section> {
        chooseSecrets();
        createScenarios();

        return sections;
    }

    private function chooseSecrets() : Void {
        var candidates : Array<Section> = [];

        for (section in sections) {
            if (section.connections.length == 1) {
                candidates.push(section);
            }
        }

        if (candidates.length == 0) {
            return;
        }

        dump(Scenario, candidates);
        candidates.sort((a, b) -> (a.getArea() - b.getArea()));

        var secretScenario : Int = 0;

        // Leave at least 2 sections (one for the start point, second for the finish point)
        var maxSecrets : Int = IntMath.min(sections.length - 2, 1 + random.nextIntExPow(candidates.length, 4));

        if (maxSecrets <= 0) {
            return;
        }

        // Do not generate more than 3 secrets per level
        var selectedCandidates = candidates.slice(0, IntMath.min(3, maxSecrets));

        if (selectedCandidates.length != candidates.length) {
            dump(Scenario, selectedCandidates);
        }

        for (section in selectedCandidates) {
            secretScenario--;
            section.scenario = secretScenario;
            setOuterScenarioGates(section, secretScenario);
        }

        dump(Scenario);
    }

    private function createScenarios() : Void {
        var currentScenario = 0;

        while (true) {
            var availableSections = sections.filter((section) -> (section.scenario == 0));

            if (availableSections.length == 0) {
                throw new GeneratorException("createScenarios failed: no available sections");
            }

            var candidates : Array<Section> = [];

            for (section in availableSections) {
                if (canUseSection(section)) {
                    candidates.push(section);
                }
            }

            if (candidates.length == 0) {
                if (availableSections.length == 1) {
                    // this is last section
                    candidates = availableSections;
                } else {
                    throw new GeneratorException("createScenarios failed: no candidates");
                }
            }

            dump(Scenario, candidates);
            candidates.sort((a, b) -> (getOuterFreeSections(a).length - getOuterFreeSections(b).length));

            var originalSection = candidates[Std.int(candidates.length * Math.pow(random.nextFloatEx(), 4))];
            var scenarioSections : Array<Section> = [originalSection];
            var probability = 0.2;

            originalSection.scenario = ++currentScenario;

            GeneratorUtils.walkNeighbors(
                originalSection,
                null,
                (section) -> {
                    if (section.scenario != 0
                        || !canUseSection(section)
                        || random.nextFloatEx() >= (probability / getOuterFreeSections(section).length)
                    ) {
                        return false;
                    }

                    probability /= 1.25;
                    section.scenario = currentScenario;
                    scenarioSections.push(section);

                    return true;
                }
            );

            if (settings.argVerboseLevel >= 1) {
                dump(Scenario, scenarioSections, null, null, 'scenario = ${currentScenario}');
                dump(Scenario, null, null, null, 'scenario = ${currentScenario}');
            } else if (candidates.length > 1 || scenarioSections.length > 1) {
                dump(Scenario, scenarioSections);
            }

            if (currentScenario > 1) {
                random.nextFromArray(scenarioSections).sure().scenarioOpener = currentScenario - 1;
            }

            var sectionsLeft = availableSections.count((section) -> !scenarioSections.contains(section));

            if (sectionsLeft == 0) {
                for (section in scenarioSections) {
                    section.scenario = 0;
                }

                dump(Scenario);
                break;
            }

            setOuterScenarioGates(originalSection, currentScenario);
            dump(Scenario);
        }
    }

    private function setOuterScenarioGates(section : Section, scenario : Int) : Void {
        GeneratorUtils.walkNeighbors(
            section,
            (connection) -> {
                if (connection.scenarioGate == 0
                    && (connection.ensureSection().scenario != scenario
                        || connection.otherConnection.sure().ensureSection().scenario != scenario
                    )
                ) {
                    connection.setBothScenarioGates(scenario);
                }

                return true;
            },
            (section) -> {
                return section.scenario == scenario;
            }
        );
    }

    private function getOuterFreeSections(section : Section) : Array<Section> {
        // Can't use LinkedSet, because LinkedObjectSet is not currently implemented
        var sectionSet = new HashSet<Section>();

        for (connection in section.connections) {
            if (connection.ensureSection().scenario == 0) {
                sectionSet.add(connection.ensureSection());
            }
        }

        var result = sectionSet.keys().array();
        result.sort((a, b) -> a.__id - b.__id);

        return result;
    }

    private function canUseSection(checkSection : Section) : Bool {
        // Если "исключить" нужную секцию из графа, то должно получиться ровно два подграфа - первый,
        // в котором будет только эта секция, и второй - со всеми остальными.
        //
        // Иначе (больше 2 подграфов, или, ну вдруг, 1) эту секцию использовать нельзя.

        return GeneratorUtils.computeConnectedRoots(
            sections.filter((section) -> (section.scenario == 0)),
            (section) -> ((section == checkSection)
                ? []
                : getOuterFreeSections(section).filter((outerSection) -> (outerSection != checkSection))
            )
        ).length == 2;
    }
}
