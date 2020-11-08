package com.eightsines.firestrike.procedural.generator.step05;

import com.eightsines.firestrike.procedural.generator.AbstractSectionGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.section.SectionConnection;
import com.eightsines.firestrike.procedural.section.SectionDivider;
import com.eightsines.firestrike.procedural.section.SectionFence;
import com.eightsines.firestrike.procedural.section.SectionGate;
import com.eightsines.firestrike.procedural.section.SectionGateType;
import com.eightsines.firestrike.procedural.section.SectionScenarioAction;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

@:enum
abstract GatesGeneratorAppendGateKind(Int) from Int to Int {
    var OnlySecretWall = -2;
    var OnlyWindow = -1;
    var Door = 0;
    var WindowOrDoor = 1;
    var Window = 2;
    var DoorOrWindow = 3;
}

class GatesGenerator extends AbstractSectionGenerator implements Generator {
    public function new(random : Random, layer : IntLayer, viewer : Viewer, sections : SafeArray<Section>) {
        super(random, layer, viewer, sections);
    }

    public function generate() : SafeArray<Section> {
        choosePassableSide();
        createGates();
        chooseScenarioType();
        humanizePassableSide();

        return sections;
    }

    private function choosePassableSide() : Void {
        for (section in sections) {
            for (connection in section.connections) {
                if (connection.__passable || connection.otherConnection.sure().__passable) {
                    continue;
                }

                if (random.nextBool()) {
                    connection.__passable = true;
                } else {
                    connection.otherConnection.sure().__passable = true;
                }
            }

            for (fence in section.fences) {
                if (fence.__passable || fence.otherFence.sure().__passable) {
                    continue;
                }

                if (random.nextBool()) {
                    fence.__passable = true;
                } else {
                    fence.otherFence.sure().__passable = true;
                }
            }
        }

        dump(Scenario);
    }

    private function createGates() : Void {
        dump(Gates);

        for (section in sections) {
            var hasGates : Bool = false;
            dump(Gates, [section]);

            for (divider in section.getDividers()) {
                if (!divider.__passable) {
                    var isGateBetweenSameScenario : Bool = false;

                    var kind = if (Std.is(divider, SectionFence)) {
                        OnlyWindow;
                    } else if (Std.is(divider, SectionConnection)) {
                        var connection : SectionConnection = cast divider;

                        if (section.scenario < 0 || connection.ensureSection().scenario < 0) {
                            OnlySecretWall;
                        } else {
                            isGateBetweenSameScenario = (section.scenario == connection.ensureSection().scenario);
                            Door;
                        }
                    } else {
                        throw new GeneratorException('Unknown divider type: ${Type.typeof(divider)}');
                    }

                    appendGates(
                        divider.gates,
                        divider.rect.getPoint(),
                        divider.rect.getLineDirection(),
                        divider.normal.scalar(-1),
                        divider.size,
                        kind,
                        isGateBetweenSameScenario
                    );

                    hasGates = true;
                }
            }

            if (hasGates) {
                dump(Gates, [section]);
            }
        }

        dump(Gates);
    }

    private function chooseScenarioType() : Void {
        dump(Actions);

        var scenarioMap = new Map<Int, SafeArray<Section>>();
        GeneratorUtils.fillScenarioMaps(sections, scenarioMap);

        var hasChanges : Bool = false;
        var candidatesForKeys : SafeArray<Section> = [];
        var candidatesForStart : SafeArray<Section> = [];
        var candidatesForFinal : SafeArray<Section> = [];

        for (section in sections) {
            if (section.scenario < 0) {
                continue;
            }

            if (section.scenario == 0) {
                candidatesForStart.push(section);
                continue;
            }

            if (section.scenario == 1) {
                candidatesForFinal.push(section);
                continue;
            }

            // Если в сценарии учавствует несколько секций, opener-ом должна быть только одна из них
            var sectionsToOpen = scenarioMap[section.scenarioOpener];

            if (sectionsToOpen == null) {
                continue;
            }

            if (sectionsToOpen.exists((sectionToOpen) -> sectionToOpen.hasOuterWideDoor())) {
                section.scenarioAction = SectionScenarioAction.Switch;
                hasChanges = true;
            } else if (section.getArea() <= 9) {
                // If it is 3x3 section, do not place key here (because in case that this will be start section, there will no be place for player).
                // Also do not use JustDoor scenario action, because this will ruin all "PathTo" logic.
                section.scenarioAction = SectionScenarioAction.Switch;
                hasChanges = true;
            } else {
                candidatesForKeys.push(section);
            }
        }

        if (hasChanges) {
            dump(Actions);
        }

        if (candidatesForStart.length == 0) {
            throw new GeneratorException("chooseScenarioType failed: start section is not found");
        }

        if (candidatesForFinal.length == 0) {
            throw new GeneratorException("chooseScenarioType failed: final section is not found");
        }

        dump(Actions, candidatesForStart);
        dump(Actions, candidatesForFinal);

        if (candidatesForStart.length > 1) {
            candidatesForStart.sort((a, b) -> (a.connections.length - b.connections.length));
        }

        if (candidatesForFinal.length > 1) {
            candidatesForFinal.sort((a, b) -> (a.connections.length - b.connections.length));
        }

        candidatesForStart[0].scenarioAction = candidatesForStart.exists((section) -> section.hasOuterWideDoor())
            ? SectionScenarioAction.Switch
            : SectionScenarioAction.JustDoor;

        candidatesForFinal[0].scenarioAction = SectionScenarioAction.EndLevel;
        dump(Actions);

        if (candidatesForKeys.length != 0) {
            dump(Actions, candidatesForKeys);
            var currentKey : Int = 0;

            random.shuffleArray(candidatesForKeys).slice(0, random.nextIntIn(3)).iter((section) -> {
                section.scenarioAction = SectionScenarioAction.Key(++currentKey);
            });

            dump(Actions);
        }

        hasChanges = false;

        for (section in sections) {
            if (section.scenario >= 0 && section.scenarioAction == null && scenarioMap[section.scenarioOpener] != null) {
                // Do not use JustDoor scenario action, because this will ruin all "PathTo" logic.
                section.scenarioAction = SectionScenarioAction.Switch;
                hasChanges = true;
            }
        }

        if (hasChanges) {
            dump(Actions);
        }
    }

    private function humanizePassableSide() : Void {
        var candidates : SafeArray<SectionDivider> = [];

        for (section in sections) {
            for (divider in section.getDividers()) {
                if (divider.__passable && !divider.getOtherDivider().__passable) {
                    candidates.push(divider);
                }
            }
        }

        for (divider in candidates) {
            if (divider.size < 2 || random.nextBool()) {
                continue;
            }

            divider.__passable = false;

            for (gate in divider.getOtherDivider().gates) {
                var normal = gate.normal.scalar(-1);

                divider.gates.push(new SectionGate(
                    SectionGateType.Passable,
                    gate.from.addTo(normal),
                    gate.to.addTo(normal),
                    normal
                ));
            }
        }

        dump(Actions);
    }

    private function appendGates(
        gates : SafeArray<SectionGate>,
        start : Point,
        direction : Point,
        normal : Point,
        avail : Int,
        kind : GatesGeneratorAppendGateKind = 0,
        isGateBetweenSameScenario : Bool
    ) : Void {
        var type = switch (kind) {
            case OnlySecretWall:
                SectionGateType.SecretWall;

            case Door:
                SectionGateType.Door;

            case WindowOrDoor:
                (random.nextFloatEx() > 0.25) ? SectionGateType.Window : SectionGateType.Door;

            case Window | OnlyWindow:
                SectionGateType.Window;

            case DoorOrWindow:
                random.nextBool() ? SectionGateType.Window : SectionGateType.Door;
        }

        var size = switch (type) {
            case Window:
                // от 1 до 3 может быть как обычное окно, так и решётка. более 3 - только решётка
                IntMath.min(avail, 1 + IntMath.select(random.nextFloatEx(), [0.85, 0.9, 0.95, 0.975]));

            case SecretWall:
                1;

            case Door:
                if (isGateBetweenSameScenario) {
                    1;
                } else {
                    IntMath.min(avail, 1 + IntMath.select(random.nextFloatEx(), [0.9, 0.975]));
                }

            case Passable:
                throw new GeneratorException("appendGates failed: type == Passable");
        }

        var position : Int = Math.floor((avail - size) * Math.sin(random.nextFloatEx() * Math.PI));

        gates.push(new SectionGate(
            type,
            start.addTo(direction.scalar(position)),
            start.addTo(direction.scalar(position + size - 1)),
            normal
        ));

        var nextKind = switch (kind) {
            case Door | WindowOrDoor | Window | DoorOrWindow:
                (kind + 3) % 3 + 1;

            default:
                kind;
        }

        var mustPlace = Std.int(IntMath.min(layer.width, layer.height) / 5);
        var leftBefore = (position - 2) + 1;

        if (leftBefore > 0 && (leftBefore >= mustPlace || random.nextFloatEx() > 0.25)) {
            appendGates(gates, start, direction, normal, leftBefore, nextKind, isGateBetweenSameScenario);
        }

        var after = position + size + 1;
        var leftAfter = avail - after;

        if (leftAfter > 0 && (leftAfter >= mustPlace || random.nextFloatEx() > 0.25)) {
            appendGates(
                gates,
                start.addTo(direction.scalar(after)),
                direction,
                normal,
                leftAfter,
                nextKind,
                isGateBetweenSameScenario
            );
        }
    }
}
