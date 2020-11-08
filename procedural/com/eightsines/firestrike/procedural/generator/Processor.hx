package com.eightsines.firestrike.procedural.generator;

import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.config.Config;
import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;
import haxe.CallStack;
import haxe.Int64;

using Safety;
using StringTools;

class Processor {
    private var viewer : Viewer;
    private var random : Null<Random> = null;

    public var settings : Null<Settings> = null;
    public var board : Null<Board> = null;

    public function new(viewer : Viewer) {
        this.viewer = viewer;
    }

    public function getVerboseLevel() : Int {
        return settings!.argVerboseLevel.or(0);
    }

    public function process(args : SafeArray<String>) : Bool {
        var argHandler = createArgHandler(() -> settings.sure());

        try {
            settings = new Settings();
            argHandler.parse(args.stdArray());
        } catch (e : Any) {
            if (Std.is(e, GeneratorException)) {
                viewer.log((cast e : GeneratorException).message);
            } else {
                viewer.log(e);
            }

            return false;
        }

        #if sys
            if (settings.unsafe().outputName == null || settings.unsafe().outputName.unsafe().length == 0) {
                if (args.length != 0) {
                    viewer.log("Output name is not set.");
                }

                viewer.log(argHandler.getDoc());
                return false;
            }
        #end

        if (settings.unsafe().argSeed != null) {
            random = new Random(Int64.parseString(settings.unsafe().argSeed.unsafe()));
        } else if (random == null) {
            random = new Random(Random.makeSeed());
        }

        try {
            board = (new Orchestrator(settings.unsafe(), viewer, random.unsafe())).generate();
        } catch (e : Any) {
            if (Std.is(e, GeneratorException)) {
                viewer.log((cast e : GeneratorException).message);
            } else if (Std.is(e, String) && (cast e : String) == "STOP") {
                viewer.log("STOP");
            } else {
                #if js
                    throw e;
                #else
                    viewer.log("Uncaught exception: " + Std.string(e) + "\n" + CallStack.toString(CallStack.exceptionStack()));
                #end
            }

            return false;
        }

        return true;
    }

    private static function createArgHandler(settingsCb : () -> Settings) : hxargs.Args.ArgHandler {
        return hxargs.Args.generate([
            #if sys
                @doc("Output name, required")
                ["-o", "--output"] => (output : String) -> {
                    settingsCb().outputName = output.trim();
                },
            #end

            @doc('Desired size, 1 by default')
            ["-s", "--size"] => (size : String) -> {
                settingsCb().argSize = parseIntArg(size, "size", 1, Settings.ARG_SIZE_MAX);
            },

            @doc('Weapon level, 1 by default')
            ["-w", "--weapon"] => (weapon : String) -> {
                settingsCb().argWeaponLevel = parseIntArg(weapon, "weapon level", 1, Config.weapons.length);
            },

            @doc('Enemy level, 1 by default')
            ["-e", "--enemy"] => (enemy : String) -> {
                settingsCb().argEnemyLevel = parseIntArg(enemy, "enemy level", 1, Config.enemies.length);
            },

            @doc('Difficulty level, 3 by default')
            ["-d", "--difficulty"] => (difficulty : String) -> {
                settingsCb().argDifficulty = parseIntArg(
                    difficulty,
                    "difficulty",
                    1,
                    Settings.ARG_DIFFICULTY_MAX
                );
            },

            @doc("Seed for random, not specified by default")
            ["-r", "--random"] => (seed : String) -> {
                settingsCb().argSeed = seed;
            },

            @doc("Be verbose")
            ["-v", "--verbose"] => () -> {
                settingsCb().argVerboseLevel = IntMath.max(1, settingsCb().argVerboseLevel);
            },

            @doc("Be more verbose")
            ["-vv", "--verbose-more"] => () -> {
                settingsCb().argVerboseLevel = IntMath.max(2, settingsCb().argVerboseLevel);
            },

            @doc("Be super verbose")
            ["-vvv", "--verbose-super"] => () -> {
                settingsCb().argVerboseLevel = IntMath.max(3, settingsCb().argVerboseLevel);
            },

            @doc("Be puper verbose")
            ["-vvvv", "--verbose-puper"] => () -> {
                settingsCb().argVerboseLevel = IntMath.max(4, settingsCb().argVerboseLevel);
            },

            _ => (arg : String) -> {
                throw new GeneratorException('Unknown command: "${arg}"');
            }
        ]);
    }

    private static function parseIntArg(str : String, name : String, min : Int, max : Int) : Int {
        var val = Std.parseInt(str);

        if (val == null) {
            throw new GeneratorException('Can\'t parse ${name}: "${str}"');
        }

        if (val < min || val > max) {
            throw new GeneratorException('Invalid ${name}: value must be >= ${min} and <= ${max}, but "${val}" given');
        }

        return val;
    }
}
