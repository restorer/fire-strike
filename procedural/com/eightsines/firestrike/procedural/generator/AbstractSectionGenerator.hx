package com.eightsines.firestrike.procedural.generator;

import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section; // {SectionEnemy, SectionItem, SectionScenarioObject, SectionPassable, SectionWallLamp}
import com.eightsines.firestrike.procedural.section.SectionConnection;
import com.eightsines.firestrike.procedural.section.SectionDecoration;
import com.eightsines.firestrike.procedural.section.SectionDivider;
import com.eightsines.firestrike.procedural.section.SectionGate;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

@:enum
abstract AbstractSectionGeneratorDumpKind(Int) {
    var Regular = 0;
    var Scenario = 1;
    var Gates = 2;
    var Actions = 3;
}

class AbstractSectionGeneratorDumpOptions {
    public var kind : AbstractSectionGeneratorDumpKind = Regular;
    public var selected : SafeArray<Section> = [];
    public var connectorsFor : Null<Section> = null;
    public var only : SafeArray<Section> = [];

    public function new() {}
}

// 0x0000x - квадратик (стена)
// 0x1000x - рамка (окно)
// 0x2000x - маленький квадратик (дверь)
// 0x3000x - четыре квадратика (ящик)
// 0x4000x - горизонтальная тонкая палка
// 0x5000x - вертикальная толстая палка
// 0x6000x - крестик
// 0x7000x - шахматка

@SuppressWarnings("checkstyle:MagicNumber")
class AbstractSectionGenerator {
    private var random : Random;
    private var layer : IntLayer;
    private var viewer : Viewer;
    private var sections : SafeArray<Section>;

    public function new(random : Random, layer : IntLayer, viewer : Viewer, sections : SafeArray<Section>) {
        this.random = random;
        this.layer = layer;
        this.viewer = viewer;
        this.sections = sections;
    }

    private function dump(
        ?kind : AbstractSectionGeneratorDumpKind,
        ?selected : SafeArray<Section>,
        ?connectorsFor : Section,
        ?only : SafeArray<Section>,
        ?data : String
    ) : Void {
        var options = new AbstractSectionGeneratorDumpOptions();

        if (kind != null) {
            options.kind = kind;
        }

        if (selected != null) {
            options.selected = selected;
        }

        if (connectorsFor != null) {
            options.connectorsFor = connectorsFor;
        }

        if (only != null) {
            options.only = only;
        }

        layer.clear();
        var availSections = (options.only.length != 0 ? options.only : sections);

        for (section in availSections) {
            var fillEntryValue = getSectionFillDumpEntry(section, options);
            var outlineEntryValue = getSectionOutlineDumpEntry(section, options);

            if (fillEntryValue != 0) {
                for (polybox in section.geometry) {
                    layer.fillPolybox(polybox, fillEntryValue);
                }
            }

            for (polybox in section.geometry) {
                layer.outlinePolybox(polybox, outlineEntryValue);
            }
        }

        for (section in availSections) {
            for (divider in section.getDividers()) {
                var currentSection : Section;
                var scenarioGate : Null<Int>;

                if (Std.is(divider, SectionConnection)) {
                    var connection : SectionConnection = cast divider;

                    currentSection = (options.connectorsFor == section || options.connectorsFor == connection.ensureSection())
                        ? connection.ensureSection()
                        : section;

                    scenarioGate = connection.scenarioGate;
                } else {
                    currentSection = section;
                    scenarioGate = null;
                }

                if (divider.__passable || divider.gates.length == 0) {
                    layer.fillRect(divider.rect, getDividerDumpEntry(divider, currentSection, scenarioGate, options));
                } else {
                    for (gate in divider.gates) {
                        layer.fillRect(gate.rect, getGateDumpEntry(gate, currentSection, scenarioGate, options));
                    }
                }
            }
        }

        for (section in availSections) {
            for (object in section.scenarioObjects) {
                layer.setAt(object.position, getObjectDumpEntry(object, section, options));
            }

            for (decoration in section.decorations) {
                layer.plot(decoration.points, getDecorationDumpEntry(decoration, section, options));
            }

            for (position in section.grass) {
                layer.setAt(position, 4 << 20);
            }

            for (position in section.lamps) {
                layer.setAt(position, 6 << 20);
            }

            for (position in section.ceilingLamps) {
                layer.setAt(position, 7 << 20);
            }

            for (wallLamp in section.wallLamps) {
                layer.setAt(wallLamp.position, getWallLampDumpEntry(wallLamp, section, options));
            }

            if (section.player != null) {
                // Белая вертикальная палка
                layer.setAt(section.player.unsafe(), 0x50007);
            }

            for (enemy in section.enemies) {
                // Синяя вертикальная палка
                layer.setAt(enemy.position, 0x50001);
            }

            for (item in section.items) {
                layer.setAt(item.position, getItemDumpEntry(item, section, options));
            }
        }

        viewer.dumpIntLayer(layer, data);
    }

    private function getSectionFillDumpEntry(section : Section, options : AbstractSectionGeneratorDumpOptions) : Int {
        return switch (options.kind) {
            case Regular | Scenario:
                if (options.selected.contains(section)) {
                    -0x10001;
                } else {
                    (section.scenarioOpener << 20) | if (section.scenario < 0) {
                        0x10038;
                    } else if (section.scenario != 0) {
                        0x10000 + section.scenario;
                    } else {
                        0;
                    }
                }

            case Gates:
                0;

            case Actions:
                if (options.selected.contains(section)) {
                    -0x10001;
                } else if (section.scenarioAction == null || section.scenarioObjects.length != 0 || section.decorations.length != 0) {
                    0;
                } else {
                    switch (section.scenarioAction.sure()) {
                        case EndLevel:
                            0x10007;

                        case Switch:
                            0x100ff | (section.scenarioOpener << 20);

                        case Key(type):
                            (section.scenarioOpener << 20) | switch (type) {
                                case 1: 0x10001;
                                case 2: 0x10002;
                                case 3: 0x10004;
                                default: 0x10006;
                            }

                        case JustDoor:
                            0x10006;
                    }
                }
        };
    }

    private function getSectionOutlineDumpEntry(section : Section, options : AbstractSectionGeneratorDumpOptions) : Int {
        return switch (options.kind) {
            case Regular | Scenario:
                section.entryValue;

            case Gates:
                if (options.selected.contains(section)) {
                    0xff;
                } else {
                    section.entryValue;
                }

            case Actions:
                if (section.scenario < 0) {
                    0xff;
                } else if (section.scenario != 0) {
                    section.scenario;
                } else {
                    0x38;
                }
        };
    }

    private function getDividerDumpEntry(
        divider : SectionDivider,
        section : Section,
        scenarioGate : Null<Int>,
        options : AbstractSectionGeneratorDumpOptions
    ) : Int {
        return switch (options.kind) {
            case Regular | Scenario:
                if (divider.__passable) {
                    getSectionFillDumpEntry(section, options);
                } else if (scenarioGate == null || scenarioGate == 0) {
                    0x10000 + section.entryValue;
                } else if (scenarioGate.unsafe() < 0) {
                    0x20038;
                } else {
                    0x20000 + scenarioGate.unsafe();
                }

            case Gates:
                if (divider.__passable) {
                    0;
                } else if (options.selected.contains(section)) {
                    -1;
                } else {
                    0x10000 + section.entryValue;
                }

            case Actions:
                if (divider.__passable) {
                    getSectionFillDumpEntry(section, options);
                } else {
                    getSectionOutlineDumpEntry(section, options);
                }
        };
    }

    private function getGateDumpEntry(
        gate : SectionGate,
        section : Section,
        scenarioGate : Null<Int>,
        options : AbstractSectionGeneratorDumpOptions
    ) : Int {
        return switch (options.kind) {
            case Regular | Scenario:
                0;

            case Gates:
                switch (gate.type) {
                    case Door | SecretWall:
                        if (options.selected.contains(section)) {
                            -0x10001;
                        } else {
                            0x20000 + section.entryValue;
                        }

                    case Window:
                        if (options.selected.contains(section)) {
                            -1;
                        } else {
                            0x10000 + section.entryValue;
                        }

                    case Passable:
                        0;
                }

            case Actions:
                var entry = if (scenarioGate != null) {
                    if (scenarioGate < 0) {
                        0xff;
                    } else if (scenarioGate != 0) {
                        scenarioGate.unsafe();
                    } else {
                        0x38;
                    }
                } else {
                    getSectionOutlineDumpEntry(section, options);
                };

                switch (gate.type) {
                    case Door:
                        0x20000 + entry;

                    case Window:
                        0x10000 + entry;

                    case SecretWall:
                        0x10000 + (0xff << 20) + entry;

                    case Passable:
                        getSectionFillDumpEntry(section, options);
                }
        };
    }

    private function getObjectDumpEntry(
        object : SectionScenarioObject,
        section : Section,
        options : AbstractSectionGeneratorDumpOptions
    ) : Int {
        return switch (object.type) {
            case EndLevel:
                return 0x10001 | (1 << 20);

            case Switch:
                return (0x10000 + getSectionOutlineDumpEntry(section, options)) | (section.scenarioOpener << 20);

            case Key(type):
                (section.scenarioOpener << 20) | switch (type) {
                    case 1: 0x10001;
                    case 2: 0x10002;
                    case 3: 0x10004;
                    default: 0x10006;
                }

            case JustDoor:
                0x10006;
        }
    }

    private function getDecorationDumpEntry(
        decoration : SectionDecoration,
        section : Section,
        options : AbstractSectionGeneratorDumpOptions
    ) : Int {
        return switch (decoration.type) {
            case Box:
                return 0x30000 + getSectionOutlineDumpEntry(section, options);

            case Pillar | Rock | Barrel:
                return 0x50000 + getSectionOutlineDumpEntry(section, options);

            case Lattice:
                return 0x40000 + getSectionOutlineDumpEntry(section, options);
        }
    }

    private function getItemDumpEntry(item : SectionItem, section : Section, options : AbstractSectionGeneratorDumpOptions) : Int {
        switch (item.type) {
            case Health(_):
                // Красный крестик
                return 0x60002;

            case Armor(_):
                // Синий крестик
                return 0x60001;

            case Ammo(_, _):
                // Зелёный крестик
                return 0x60004;

            case Backpack:
                // Голубой крестик
                return 0x60005;

            case OpenMap:
                // Желтый крестик
                return 0x60006;

            case Weapon(_):
                // Фиолетовый крестик
                return 0x60003;
        }
    }

    private function getWallLampDumpEntry(wallLamp : SectionWallLamp, section : Section, options : AbstractSectionGeneratorDumpOptions) : Int {
        return getSectionOutlineDumpEntry(section, options) | ((wallLamp.type ? 7 : 0x100) << 20);
    }
}
