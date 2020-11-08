package com.eightsines.firestrike.procedural.generator;

import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.generator.step01.BspGenerator;
import com.eightsines.firestrike.procedural.generator.step01.PlanGenerator;
import com.eightsines.firestrike.procedural.generator.step02.ConnectionGenerator;
import com.eightsines.firestrike.procedural.generator.step03.HumanizerGenerator;
import com.eightsines.firestrike.procedural.generator.step04.ScenarioGenerator;
import com.eightsines.firestrike.procedural.generator.step05.GatesGenerator;
import com.eightsines.firestrike.procedural.generator.step06.DecorationsGenerator;
import com.eightsines.firestrike.procedural.generator.step07.PlayerGenerator;
import com.eightsines.firestrike.procedural.generator.step08.EnemyGenerator;
import com.eightsines.firestrike.procedural.generator.step09.RemainingGenerator;
import com.eightsines.firestrike.procedural.generator.step10.TexturePainter;
import com.eightsines.firestrike.procedural.generator.step11.ScriptPainter;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.NoFreeSpaceGeneratorException;
import com.eightsines.firestrike.procedural.util.Pair;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

using Safety;

class Orchestrator {
    private var settings : Settings;
    private var viewer : Viewer;
    private var random : Random;

    public function new(settings : Settings, viewer : Viewer, random : Random) {
        this.settings = settings;
        this.viewer = viewer;
        this.random = random;
    }

    public function generate() : Board {
        settings.prepare(random.seed);

        #if js
            return generateInternal();
        #else
            for (iteration in 0 ... 10) {
                try {
                    return generateInternal();
                } catch (e : GeneratorException) {
                    viewer.log(e.message);
                }
            }
        #end

        throw new GeneratorException("Orchestrator failed: can't generate level after several tries");
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private function generateInternal() : Board {
        var desiredSize : Pair<Int, Int> = random.nextFromArray(settings.desiredSizes).sure();
        var desiredWidth : Int = desiredSize.first;
        var desiredHeight : Int = desiredSize.second;

        if (random.nextBool()) {
            var tmp = desiredWidth;
            desiredWidth = desiredHeight;
            desiredHeight = tmp;
        }

        viewer.log('Using "${settings.argumentsLine}", chosen size is ${desiredHeight} x ${desiredWidth}');

        var layer = new IntLayer(desiredWidth, desiredHeight);

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> Geometry");
        }

        var sections = getGeometryGenerator(layer).generate();

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> ConnectionGenerator");
        }

        sections = (new ConnectionGenerator(random, layer, viewer, sections)).generate();

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> HumanizerGenerator");
        }

        sections = (new HumanizerGenerator(random, layer, viewer, sections)).generate();

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> ScenarioGenerator");
        }

        sections = (new ScenarioGenerator(settings, random, layer, viewer, sections)).generate();

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> GatesGenerator");
        }

        sections = (new GatesGenerator(random, layer, viewer, sections)).generate();

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> DecorationsGenerator");
        }

        sections = (new DecorationsGenerator(settings, random, layer, viewer, sections)).generate();

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> PlayerGenerator");
        }

        sections = (new PlayerGenerator(settings, random, layer, viewer, sections)).generate();

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> EnemyGenerator");
        }

        sections = (new EnemyGenerator(settings, random, layer, viewer, sections)).generate();

        if (settings.argVerboseLevel >= 3) {
            viewer.log(">>>> RemainingGenerator");
        }

        sections = (new RemainingGenerator(settings, random, layer, viewer, sections)).generate();

        var board : Null<Board> = null;

        while (true) {
            try {
                board = new Board(desiredWidth, desiredHeight);

                if (settings.argVerboseLevel >= 3) {
                    viewer.log(">>>> TexturePainter");
                }

                board = (new TexturePainter(settings, random, layer, board, viewer, sections)).paint();

                if (settings.argVerboseLevel >= 3) {
                    viewer.log(">>>> ScriptPainter");
                }

                board = (new ScriptPainter(settings, random, layer, board, viewer, sections)).paint();

                break;
            } catch (e : NoFreeSpaceGeneratorException) {
                if (desiredWidth >= 64 && desiredHeight >= 64) {
                    throw e;
                }

                desiredWidth = IntMath.min(64, desiredWidth + 4);
                desiredHeight = IntMath.min(64, desiredHeight + 4);
            }
        }

        if (#if js true #else settings.argVerboseLevel >= 1 #end) {
            viewer.log(board.script.join("\n"));
        }

        return board;
    }

    private function getGeometryGenerator(layer : IntLayer) : Generator {
        return random.nextBool() ? new BspGenerator(random, layer, viewer) : new PlanGenerator(random, layer, viewer);
    }
}
