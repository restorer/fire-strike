package com.eightsines.firestrike.procedural.config;

import com.eightsines.firestrike.procedural.util.Pair;
import haxe.Int64;

@SuppressWarnings("checkstyle:MagicNumber")
class Settings {
    public static inline var ARG_SIZE_MAX : Int = 5;
    public static inline var ARG_DIFFICULTY_MAX : Int = 5;

    // Can be specified:

    public var argSize : Int = 1;
    public var argWeaponLevel : Int = 1;
    public var argEnemyLevel : Int = 1;
    public var argDifficulty : Int = 3;
    public var outputName : Null<String> = null;
    public var argSeed : Null<String> = null;
    public var argVerboseLevel : Int = 0;

    // Call prepare() to fill following:

    @:safety(unsafe) public var argumentsLine(default, null) : String;
    @:safety(unsafe) public var desiredSizes(default, null) : SafeArray<Pair<Int, Int>>;
    @:safety(unsafe) public var availWeapons(default, null) : SafeArray<Int>;
    @:safety(unsafe) public var availEnemies(default, null) : SafeArray<Int>;
    @:safety(unsafe) public var difficulty(default, null) : Float;

    public function new() {}

    public function prepare(seed : Int64) : Void {
        desiredSizes = [];

        if (argSize < 2) {
            desiredSizes.push(new Pair(16, 16));
        } else if (argSize < 3) {
            desiredSizes.push(new Pair(16, 32));
            desiredSizes.push(new Pair(16, 48));
        } else if (argSize < 4) {
            desiredSizes.push(new Pair(16, 64));
            desiredSizes.push(new Pair(32, 32));
            desiredSizes.push(new Pair(32, 48));
        } else if (argSize < 5) {
            desiredSizes.push(new Pair(32, 64));
            desiredSizes.push(new Pair(48, 48));
            desiredSizes.push(new Pair(48, 64));
        } else {
            desiredSizes.push(new Pair(64, 64));
        }

        difficulty = switch (argDifficulty) {
            case 1: 0.25;
            case 2: 0.5;
            case 3: 0.75;
            case 4: 1.0;
            case 5: 1.25;
            default: throw 'Invalid argDifficulty = ${argDifficulty}';
        }

        availWeapons = [ for (i in 0 ... argWeaponLevel) i ];
        availEnemies = [ for (i in 0 ... argEnemyLevel) i ];

        argumentsLine = '-s ${argSize}'
            + ' -w ${argWeaponLevel}'
            + ' -e ${argEnemyLevel}'
            + ' -d ${argDifficulty}'
            + ' -r ${Int64.toStr(seed)}';
    }
}
