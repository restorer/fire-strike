package com.eightsines.firestrike.procedural.section;

import com.eightsines.firestrike.procedural.board.Ceiling;
import com.eightsines.firestrike.procedural.board.CellDecor;
import com.eightsines.firestrike.procedural.board.CellDoor;
import com.eightsines.firestrike.procedural.board.CellPass;
import com.eightsines.firestrike.procedural.board.CellTWall;
import com.eightsines.firestrike.procedural.board.CellWall;
import com.eightsines.firestrike.procedural.board.CellWindow;
import com.eightsines.firestrike.procedural.board.Floor;
import com.eightsines.firestrike.procedural.util.Pair;
import com.eightsines.firestrike.procedural.util.Random;

using Safety;

@:enum
abstract SectionAppearanceKind(Int) {
    var Outdoor = 0;
    var Indoor = 1;
    var Exit = 2;
}

class SectionAppearance {
    private static var AVAIL_CEILING : SafeArray<SafeArray<Ceiling>> = [
        [ Ceiling.A_Regular, Ceiling.A_Lamp ],
        [ Ceiling.B_Regular, Ceiling.B_Lamp ],
        [ Ceiling.C_Regular, Ceiling.C_Lamp ],
    ];

    private static var AVAIL_WALL : SafeArray<Pair<SafeArray<CellWall>, SafeArray<CellWindow>>> = [
        new Pair(
            [ A_Regular, A_LampOff, A_LampOn, A_Switch ],
            [ A_Open ]
        ),
        new Pair(
            [ B_Regular, B_LampOff, B_LampOn, B_Switch ],
            [ B_Open1, B_Open2 ]
        ),
        new Pair(
            [ C_Regular, C_LampOn1, C_LampOn2, C_Switch ],
            [ C_Open1, C_Open2 ]
        ),
        new Pair(
            [ D_Regular, D_LampOff, D_LampOn, D_Switch ],
            [ D_Open ]
        ),
        new Pair(
            [ F_Regular, F_LampOff, F_LampOn1, F_Switch ],
            [ F_Open ]
        ),
    ];

    private static var AVAIL_LATTICES : SafeArray<SafeArray<CellTWall>> = [
        [ Lattice1 ],
        [ Fence1, Fence2, Fence3 ],
    ];

    private static var OUTDOOR_PILLARS : SafeArray<CellDecor> = [
        Tree,
    ];

    private static var OUTDOOR_ROCKS : SafeArray<CellDecor> = [
        FloorLamp,
        Rock,
    ];

    private static var OUTDOOR_PASSABLES : SafeArray<CellPass> = [
        Grass,
    ];

    private static var INDOOR_PILLARS : SafeArray<CellDecor> = [
        Column1,
        Column2,
        Column3,
        Column4,
        Column5,
    ];

    private static var INDOOR_PASSABLES : SafeArray<CellPass> = [
        Lamp,
    ];

    private static var REGULAR_DOORS : SafeArray<CellDoor> = [
        Regular1,
        Regular2,
    ];

    private static var CODE_DOORS : SafeArray<CellDoor> = [
        Code1,
        Code2,
        Code3,
    ];

    public var floor : Floor;
    public var ceiling : Null<Ceiling>;
    public var ceilingLamp : Null<Ceiling>;
    public var wall : CellWall;
    public var wallLampOff : Null<CellWall>;
    public var wallLampOn : Null<CellWall>;
    public var wallSwitch : CellWall;
    public var windows : SafeArray<CellWindow>;
    public var pillar : CellDecor;
    public var rocks : SafeArray<CellDecor>;
    public var passables : SafeArray<CellPass>;
    public var doorRegular : CellDoor;
    public var doorCode : CellDoor;
    public var canHasLamps : Bool;
    public var canHasGrass : Bool;
    public var lattices : SafeArray<CellTWall>;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public function new(
        floor : Floor,
        ceiling : Null<Ceiling>,
        ceilingLamp : Null<Ceiling>,
        wall : CellWall,
        wallLampOff : Null<CellWall>,
        wallLampOn : Null<CellWall>,
        wallSwitch : CellWall,
        windows : SafeArray<CellWindow>,
        pillar : CellDecor,
        rocks : SafeArray<CellDecor>,
        passables : SafeArray<CellPass>,
        doorRegular : CellDoor,
        doorCode : CellDoor,
        canHasLamps : Bool,
        canHasGrass : Bool,
        lattices : SafeArray<CellTWall>
    ) {
        this.floor = floor;
        this.ceiling = ceiling;
        this.ceilingLamp = ceilingLamp;
        this.wall = wall;
        this.wallLampOff = wallLampOff;
        this.wallLampOn = wallLampOn;
        this.wallSwitch = wallSwitch;
        this.windows = windows;
        this.pillar = pillar;
        this.rocks = rocks;
        this.passables = passables;
        this.doorRegular = doorRegular;
        this.doorCode = doorCode;
        this.canHasLamps = canHasLamps;
        this.canHasGrass = canHasGrass;
        this.lattices = lattices;
    }

    public function copy() : SectionAppearance {
        return new SectionAppearance(
            floor,
            ceiling,
            ceilingLamp,
            wall,
            wallLampOff,
            wallLampOn,
            wallSwitch,
            windows.copy(),
            pillar,
            rocks.copy(),
            passables.copy(),
            doorRegular,
            doorCode,
            canHasLamps,
            canHasGrass,
            lattices
        );
    }

    @SuppressWarnings("checkstyle:HiddenField")
    public static function generate(random : Random, kind : SectionAppearanceKind) : SectionAppearance {
        var doorRegular = random.nextFromArray(REGULAR_DOORS).sure();
        var doorCode = random.nextFromArray(CODE_DOORS).sure();
        var lattices = random.nextFromArray(AVAIL_LATTICES).sure();

        return switch (kind) {
            case Outdoor:
                var wall = random.nextFromArray(AVAIL_WALL).sure();

                new SectionAppearance(
                    random.nextFromArray([Sand1, Sand2, Sand3, Bricks1, Bricks2]).unsafe(),
                    null,
                    null,
                    wall.first[0],
                    wall.first[1],
                    wall.first[2],
                    wall.first[3],
                    wall.second,
                    random.nextFromArray(OUTDOOR_PILLARS).sure(),
                    OUTDOOR_ROCKS,
                    OUTDOOR_PASSABLES,
                    doorRegular,
                    doorCode,
                    false,
                    true,
                    lattices
                );

            case Indoor:
                var ceiling = random.nextFromArray(AVAIL_CEILING).sure();
                var wall = random.nextFromArray(AVAIL_WALL).sure();

                new SectionAppearance(
                    random.nextFromArray([Indoor1, Indoor2, Indoor3]).unsafe(),
                    ceiling[0],
                    ceiling[1],
                    wall.first[0],
                    wall.first[1],
                    wall.first[2],
                    wall.first[3],
                    wall.second,
                    random.nextFromArray(INDOOR_PILLARS).sure(),
                    [],
                    INDOOR_PASSABLES,
                    doorRegular,
                    doorCode,
                    true,
                    false,
                    lattices
                );

            case Exit:
                var ceiling = random.nextFromArray(AVAIL_CEILING).sure();

                new SectionAppearance(
                    random.nextFromArray([Indoor1, Indoor2, Indoor3]).unsafe(),
                    ceiling[0],
                    ceiling[1],
                    E_Regular,
                    null,
                    null,
                    E_Switch,
                    [],
                    random.nextFromArray(INDOOR_PILLARS).sure(),
                    [],
                    INDOOR_PASSABLES,
                    doorRegular,
                    doorCode,
                    true,
                    false,
                    lattices
                );
        }
    }
}
