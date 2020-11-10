package com.eightsines.firestrike.procedural.generator.step11;

import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.board.Entry;
import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.generator.AbstractPainter;
import com.eightsines.firestrike.procedural.generator.Painter;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.layer.Layer; // {LayerReducer}
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.util.Unit;
import com.eightsines.firestrike.procedural.view.Viewer;
import com.eightsines.natcmp.NatCmp;
import org.zamedev.lib.ds.HashSet;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

class ScriptPainterAction {
    public var mark : String;
    public var openDoor : Bool = false;
    public var buryWall : Bool = false;
    public var pathToMark : Null<String> = null;

    public function new(mark : String) {
        this.mark = mark;
    }
}

class ScriptPainter extends AbstractPainter implements Painter {
    private var keyScriptAddedSet = new HashSet<Int>();
    private var closeScriptAddedSet = new HashSet<Int>();
    private var secretBuryScriptAddedSet = new HashSet<Int>();
    private var secretInsideScriptAddedSet = new HashSet<Int>();
    private var actionsMap = new Map<Int, ScriptPainterAction>();

    public function new(settings : Settings, random : Random, layer : IntLayer, board : Board, viewer : Viewer, sections : Array<Section>) {
        super(settings, random, layer, board, viewer, sections);
    }

    public function paint() : Board {
        markScenarioActions();
        walkGates();
        markSecrets();
        generatePathTo();
        generateActions();

        viewer.dumpBoard(board, 1);
        viewer.dumpBoard(board, -1);

        return board;
    }

    private function markScenarioActions() : Void {
        var isExitFound : Bool = false;

        for (section in sections) {
            for (object in section.scenarioObjects) {
                switch (object.type) {
                    case EndLevel:
                        if (isExitFound) {
                            throw new GeneratorException("ScriptPainter failed: more than one end level switches found");
                        }

                        isExitFound = true;
                        board.setAt(object.position, Entry.createMark("E"));
                        board.script.push("E : Exit");

                    case Switch:
                        board.setAt(object.position, Entry.createMark('O${section.scenarioOpener}'));
                        actionsMap[section.scenarioOpener] = new ScriptPainterAction('O${section.scenarioOpener}');

                    case Key(type):
                        board.setAt(object.position, Entry.createMark('K${getKeyLetter(type)}'));
                        actionsMap[section.scenarioOpener] = new ScriptPainterAction('K${getKeyLetter(type)}');

                    case JustDoor:
                        // do nothing
                }
            }
        }

        if (!isExitFound) {
            throw new GeneratorException("ScriptPainter failed: end level switch not found");
        }

        viewer.dumpBoard(board);
    }

    private function walkGates() : Void {
        var wasChanged : Bool = false;

        for (section in sections) {
            for (connection in section.connections) {
                if (connection.scenarioGate == 0) {
                    continue;
                }

                for (gate in connection.gates) {
                    switch (gate.type) {
                        case Door: {
                            var scenarioOpenerSection = scenarioOpenerMap[connection.scenarioGate];

                            if (scenarioOpenerSection == null) {
                                throw new GeneratorException("ScriptPainter failed: gate door without scenario opener");
                            }

                            switch (scenarioOpenerSection.scenarioAction.sure()) {
                                case Switch: {
                                    if (gate.size == 1) {
                                        board.fillRect(gate.rect, Entry.createMark('D${connection.scenarioGate}'));
                                        guardedAppendScript(closeScriptAddedSet, connection.scenarioGate, ': Close D${connection.scenarioGate}');
                                        actionsMap[connection.scenarioGate].sure().openDoor = true;
                                    } else {
                                        board.fillRect(gate.rect, Entry.createMark('W${connection.scenarioGate}'));
                                        actionsMap[connection.scenarioGate].sure().buryWall = true;
                                    }
                                }

                                case Key(type): {
                                    if (gate.size != 1) {
                                        throw new GeneratorException('ScriptPainter failed: key\'ed door with gate size = ${gate.size}');
                                    }

                                    var keyLetter = getKeyLetter(type);

                                    board.fillRect(gate.rect, Entry.createMark('D${keyLetter}'));
                                    guardedAppendScript(keyScriptAddedSet, type, ': ${keyLetter}Key D${keyLetter}');
                                }

                                case EndLevel:
                                    throw new GeneratorException("ScriptPainter failed: gate door with opener action = EndLevel");

                                case JustDoor:
                                    if (gate.size != 1) {
                                        throw new GeneratorException('ScriptPainter failed: JustDoor\'ed door with size = ${gate.size}');
                                    }
                            }

                            wasChanged = true;
                        }

                        case SecretWall:
                            board.fillRect(gate.rect, Entry.createMark('H${- connection.scenarioGate}'));
                            guardedAppendScript(secretBuryScriptAddedSet, connection.scenarioGate, 'H${- connection.scenarioGate} : BuryThis');
                            wasChanged = true;

                        case Window | Passable:
                    }
                }
            }
        }

        if (wasChanged) {
            viewer.dumpBoard(board);
        }
    }

    private function markSecrets() : Void {
        var hasSecrets : Bool = false;

        for (section in sections) {
            if (section.scenario >= 0) {
                continue;
            }

            section.renderAvailInnerCells(layer, true);

            if (settings.argVerboseLevel >= 1) {
                viewer.dumpIntLayer(layer);
            }

            layer.reduceRectFilled(section.getBbox(), new LayerReducer<Int, Unit>(null, (row, col, value, _) -> {
                if (value == Section.VAL_INNER_AVAIL || value == Section.VAL_INNER_PASSABLE) {
                    board.set(row, col, Entry.createMark('S${- section.scenario}'));
                }

                return null;
            }));

            guardedAppendScript(secretInsideScriptAddedSet, section.scenario, 'S${- section.scenario} : Secret ${- section.scenario}');
            hasSecrets = true;
        }

        if (hasSecrets) {
            viewer.dumpBoard(board);
        }
    }

    private function generatePathTo() : Void {
        var startSection = sections.find((section) -> (section.player != null));

        if (startSection == null) {
            throw new GeneratorException("generatePathTo failed: start section not found");
        }

        for (availSections in GeneratorUtils.computeScenarioSections(sections)) {
            var pathToMark : String;
            var scenarioOpenerSection = availSections.find((section) -> (section.scenarioOpener != 0));

            if (scenarioOpenerSection == null) {
                pathToMark = "E";
            } else if (!actionsMap.exists(scenarioOpenerSection.scenarioOpener)) {
                // This section(s) has JustDoor type
                continue;
            } else {
                pathToMark = actionsMap[scenarioOpenerSection.scenarioOpener].unsafe().mark;
            }

            var maxIterations = 500;
            var scenario = availSections[0].scenario;

            while (true) {
                if (scenario == 0) {
                    board.script.push(': PathTo ${pathToMark}');
                    break;
                }

                if (actionsMap.exists(scenario)) {
                    actionsMap[scenario].unsafe().pathToMark = pathToMark;
                    break;
                }

                if (!scenarioOpenerMap.exists(scenario)) {
                    throw new GeneratorException('generatePathTo failed: can\'t walk back for scenario = ${scenario}');
                } else {
                    scenario = scenarioOpenerMap[scenario].unsafe().scenario;
                }

                maxIterations--;

                if (maxIterations <= 0) {
                    throw new GeneratorException("generatePathTo failed: too much iterations while walking back");
                }
            }
        }
    }

    private function generateActions() : Void {
        for (scenario => action in actionsMap) {
            var actionScripts : Array<String> = [];

            if (action.openDoor) {
                actionScripts.push('Open D${scenario}');
            }

            if (action.buryWall) {
                actionScripts.push('Bury W${scenario}');
            }

            action.pathToMark.run((pathToMark) -> actionScripts.push('PathTo ${pathToMark}'));

            if (actionScripts.length != 0) {
                board.script.push('${action.mark} : ${actionScripts.join(', ')}');
            }
        }

        board.script.sort(NatCmp.natCmp);
    }

    private function guardedAppendScript(guardSet : HashSet<Int>, guardKey : Int, scriptLine : String) : Void {
        if (guardSet.exists(guardKey)) {
            return;
        }

        guardSet.add(guardKey);
        board.script.push(scriptLine);
    }

    private function getKeyLetter(type : Int) : String {
        return switch (type) {
            case 1: "B";
            case 2: "R";
            case 3: "G";
            default: throw new GeneratorException('ScriptPainter failed: Invalid key type = ${type}');
        }
    }
}
