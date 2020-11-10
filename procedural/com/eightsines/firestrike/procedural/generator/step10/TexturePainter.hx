package com.eightsines.firestrike.procedural.generator.step10;

import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.board.Cell;
import com.eightsines.firestrike.procedural.board.CellDoor;
import com.eightsines.firestrike.procedural.board.CellTWall;
import com.eightsines.firestrike.procedural.board.CellWall;
import com.eightsines.firestrike.procedural.board.Entry;
import com.eightsines.firestrike.procedural.board.Splitted;
import com.eightsines.firestrike.procedural.config.Config;
import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.generator.AbstractPainter;
import com.eightsines.firestrike.procedural.generator.Painter;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.section.SectionAppearance;
import com.eightsines.firestrike.procedural.section.SectionConnection;
import com.eightsines.firestrike.procedural.section.SectionGate;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;
import haxe.ds.Option;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

class TexturePainter extends AbstractPainter implements Painter {
    private static var AVAIL_BOXES : Array<CellWall> = [
        Box1,
        Box2,
        Box3,
        Box4,
    ];

    private static var AVAIL_SECRET_WALLS : Array<CellWall> = [
        Bricks1,
        Bricks2,
        Mold,
    ];

    public function new(settings : Settings, random : Random, layer : IntLayer, board : Board, viewer : Viewer, sections : Array<Section>) {
        super(settings, random, layer, board, viewer, sections);
    }

    public function paint() : Board {
        assignAppearance();
        paintEverything();
        viewer.dumpBoard(board, 1);

        return board;
    }

    private function assignAppearance() : Void {
        for (section in sections) {
            section.appearance = SectionAppearance.generate(random, section.appearanceKind.sure());
        }
    }

    private function paintEverything() : Void {
        board.clear();

        for (section in sections) {
            var appearance = section.appearance.sure();
            var entry = Entry.createFloor(appearance.floor, appearance.floor, appearance.floor, appearance.floor);

            appearance.ceiling.run((ceiling) -> {
                entry.ceiling = Splitted.createCeiling(ceiling, ceiling, ceiling, ceiling);
            });

            for (polybox in section.geometry) {
                board.fillPolybox(polybox, entry);
            }

            appearance.ceilingLamp.run((ceilingLamp) -> {
                var ceilingLampEntry = Entry.createCeiling(
                    ceilingLamp,
                    ceilingLamp,
                    ceilingLamp,
                    ceilingLamp
                );

                for (point in section.ceilingLamps) {
                    board.setAt(point, ceilingLampEntry);
                }
            });

            for (polybox in section.geometry) {
                board.outlinePolybox(polybox, Entry.createCell(Wall(appearance.wall)));
            }

            if (appearance.wallLampOn != null || appearance.wallLampOff != null) {
                var wallLights : Array<CellWall> = if (appearance.wallLampOn != null && appearance.wallLampOff != null) {
                    [appearance.wallLampOff, appearance.wallLampOn];
                } else if (appearance.wallLampOn != null) {
                    [appearance.wallLampOn, appearance.wallLampOn];
                } else {
                    @:nullSafety(Off) [appearance.wallLampOff, appearance.wallLampOff];
                }

                for (wallLamp in section.wallLamps) {
                    board.setAt(wallLamp.position, Entry.createCell(Wall(wallLights[wallLamp.type ? 1 : 0])));
                }
            }
        }

        for (section in sections) {
            var appearance = section.appearance.sure();
            var secretWall : Null<CellWall> = null;

            for (divider in section.getDividers()) {
                if (divider.__passable) {
                    board.fillRect(divider.rect, Entry.createEmptyCell());
                } else if (divider.gates.length == 0) {
                    throw new GeneratorException("TexturePainter failed: non-passable divider with no gates");
                } else {
                    var connection : Null<SectionConnection> = Std.downcast(cast divider, SectionConnection);

                    for (gate in divider.gates) {
                        switch (gate.type) {
                            case Door:
                                switch (gate.size) {
                                    case 1:
                                        board.fillRect(gate.rect, Entry.createCell(Door(getDoorType(appearance, gate, connection.sure()))));
                                        subdivideFloorAndCeiling(gate.rect, connection);

                                    case 2:
                                        board.setAt(gate.rect.getPoint(), Entry.createCell(Wall(GateL)));
                                        board.setAt(gate.rect.getPoint(1), Entry.createCell(Wall(GateR)));

                                    case 3:
                                        board.setAt(gate.rect.getPoint(), Entry.createCell(Wall(GateL)));
                                        board.setAt(gate.rect.getPoint(1), Entry.createCell(Wall(GateC)));
                                        board.setAt(gate.rect.getPoint(2), Entry.createCell(Wall(GateR)));

                                    default:
                                        throw new GeneratorException('TexturePainter failed: door with size = ${gate.size}');
                                }

                            case Window:
                                if (appearance.windows.length != 0
                                    && (
                                        (gate.size == 1 && random.nextFloatEx() > 0.25)
                                        || (gate.size > 1 && random.nextBool())
                                    )
                                ) {
                                    board.fillRect(gate.rect, Entry.createCell(Window(random.nextFromArray(appearance.windows).sure())));
                                    subdivideFloorAndCeiling(gate.rect, connection);
                                } else {
                                    paintLattice(gate.rect.points(), gate.size, appearance);
                                }

                            case SecretWall:
                                if (secretWall == null) {
                                    secretWall = random.nextFromArray(AVAIL_SECRET_WALLS);
                                }

                                board.fillRect(gate.rect, Entry.createCell(Wall(secretWall.sure())));

                            case Passable:
                                board.fillRect(gate.rect, Entry.createEmptyCell());
                        }
                    }
                }
            }
        }

        for (section in sections) {
            var appearance = section.appearance.sure();

            for (object in section.scenarioObjects) {
                board.setAt(object.position, Entry.createCell(switch (object.type) {
                    case EndLevel:
                        Cell.Wall(E_Switch);

                    case Switch:
                        Cell.Wall(appearance.wallSwitch);

                    case Key(type):
                        switch (type) {
                            case 1:
                                Cell.Item(Key_Blue);

                            case 2:
                                Cell.Item(Key_Red);

                            case 3:
                                Cell.Item(Key_Green);

                            default:
                                throw new GeneratorException('TexturePainter failed: key with type = ${type}');
                        }

                    case JustDoor:
                        throw new GeneratorException("paintEverything failed: scenario object with type JustDoor");
                }));
            }

            for (decoration in section.decorations) {
                switch (decoration.type) {
                    case Box:
                        board.plot(decoration.points, Entry.createCell(Cell.Wall(random.nextFromArray(AVAIL_BOXES).sure())));

                    case Pillar:
                        board.plot(decoration.points, Entry.createCell(Cell.Decor(appearance.pillar)));

                    case Rock:
                        board.plot(decoration.points, Entry.createCell(Cell.Decor(random.nextFromArray(appearance.rocks).sure())));

                    case Lattice:
                        paintLattice(decoration.points, decoration.size, appearance);

                    case Barrel:
                        board.plot(decoration.points, Entry.createCell(Cell.Decor(Barrel)));
                }
            }

            for (point in section.noTransPoints) {
                board.setAt(point, Entry.createNoTrans());
            }

            if (appearance.canHasGrass) {
                for (position in section.grass) {
                    board.setAt(position, Entry.createCell(Pass(Grass)));
                }
            }

            if (appearance.canHasLamps) {
                for (position in section.lamps) {
                    board.setAt(position, Entry.createCell(Pass(Lamp)));
                }
            }

            section.player.run((player) -> {
                board.setAt(player, Entry.createCell(Player(3)));
            });

            for (enemy in section.enemies) {
                board.setAt(enemy.position, Entry.createCell(switch (enemy.type) {
                    case 0:
                        Cell.Enemy(SoldierWithPistol);

                    case 1:
                        Cell.Enemy(SoldierWithRifle);

                    case 2:
                        Cell.Enemy(SoldierWithKnife);

                    case 3:
                        Cell.Enemy(SoldierWithGrenade);

                    case 4:
                        Cell.Enemy(Zombie);

                    default:
                        throw new GeneratorException('TexturePainter failed: enemy with type = ${enemy.type}');
                }));
            }

            for (item in section.items) {
                board.setAt(item.position, Entry.createCell(switch (item.type) {
                    case Health(box):
                        Cell.Item(box ? Medi : Stim);

                    case Armor(box):
                        Cell.Item(box ? Armor_Red : Armor_Green);

                    case Ammo(ammo, box):
                        switch (ammo) {
                            case 1:
                                Cell.Item(box ? CBox : Clip);

                            case 2:
                                Cell.Item(box ? SBox : Shell);

                            case 3:
                                Cell.Item(box ? GBox : Grenade);

                            default:
                                throw new GeneratorException('TexturePainter failed: item with ammo = ${ammo}');
                        };

                    case Backpack:
                        Cell.Item(BPack);

                    case OpenMap:
                        Cell.Item(OpenMap);

                    case Weapon(weapon): {
                        var weaponConfig = Config.weapons[weapon];

                        if (weaponConfig.cellItem == null) {
                            throw new GeneratorException('TexturePainter failed: weapon without callItem = ${weapon}');
                        }

                        Cell.Item(weaponConfig.cellItem.unsafe());
                    }
                }));
            }
        }

        viewer.dumpBoard(board);
    }

    private function getDoorType(
        appearance : SectionAppearance,
        gate : SectionGate,
        connection : SectionConnection
    ) : CellDoor {
        if (connection == null || connection.scenarioGate == 0) {
            return appearance.doorRegular;
        }

        var scenarioOpenerSection = scenarioOpenerMap[connection.scenarioGate];

        if (scenarioOpenerSection == null) {
            return appearance.doorRegular;
        }

        return switch (scenarioOpenerSection.scenarioAction.sure()) {
            case Switch:
                appearance.doorCode;

            case Key(type):
                switch (type) {
                    case 1:
                        return KeyB;

                    case 2:
                        return KeyR;

                    case 3:
                        return KeyG;

                    default:
                        throw new GeneratorException('TexturePainter failed: key\'ed door with key type = ${type}');
                }

            case EndLevel:
                throw new GeneratorException("TexturePainter failed: gate door with opener action = EndLevel");

            case JustDoor:
                appearance.doorRegular;
        }
    }

    private function subdivideFloorAndCeiling(rect : Rect, connection : SectionConnection) : Void {
        if (connection == null) {
            return;
        }

        var section = connection.ensureSection();
        var otherSection = connection.otherConnection.sure().ensureSection();

        if (section.appearance.sure().floor != otherSection.appearance.sure().floor) {
            subdivideInternal(
                connection.normal,
                Option.Some(section.appearance.sure().floor),
                Option.Some(otherSection.appearance.sure().floor),
                (tl, tr, bl, br) -> {
                    board.fillRect(rect, Entry.createFloorEntry(tl, tr, bl, br));
                }
            );
        }

        if (section.appearance.sure().ceiling != otherSection.appearance.sure().ceiling) {
            subdivideInternal(
                connection.normal,
                section.appearance!.ceiling.optionate(),
                otherSection.appearance!.ceiling.optionate(),
                (tl, tr, bl, br) -> {
                    board.fillRect(rect, Entry.createCeilingEntry(tl, tr, bl, br));
                }
            );
        }
    }

    private function subdivideInternal<T>(
        normal : Point,
        entry : Option<T>,
        otherEntry : Option<T>,
        cb : (Option<T>, Option<T>, Option<T>, Option<T>) -> Void
    ) : Void {
        if (Point.UP.equals(normal)) {
            cb(entry, entry, otherEntry, otherEntry);
        } else if (Point.RIGHT.equals(normal)) {
            cb(otherEntry, entry, otherEntry, entry);
        } else if (Point.DOWN.equals(normal)) {
            cb(otherEntry, otherEntry, entry, entry);
        } else if (Point.LEFT.equals(normal)) {
            cb(entry, otherEntry, entry, otherEntry);
        } else {
            throw new GeneratorException('TexturePainter failed: invalid subdivide normal = ${normal}');
        }
    }

    private function paintLattice(points : Array<Point>, size : Int, appearance : SectionAppearance) : Void {
        if (appearance.lattices.length == 1) {
            board.plot(points, Entry.createCell(TWall(appearance.lattices[0])));
            return;
        }

        switch (size) {
            case 1:
                board.plot(points, Entry.createCell(TWall(appearance.lattices[1])));

            case 2:
                board.setAt(points[0], Entry.createCell(TWall(getFenceType(points[0], points[1], appearance.lattices))));
                board.setAt(points[1], Entry.createCell(TWall(getFenceType(points[1], points[0], appearance.lattices))));

            default:
                if (points.first().lineSizeTo(points.last()) <= 2) {
                    board.plot(points, Entry.createCell(TWall(appearance.lattices[1])));
                } else {
                    board.setAt(points[0], Entry.createCell(TWall(getFenceType(points[0], points[1], appearance.lattices))));

                    for (off in 1 ... (points.length - 1)) {
                        board.setAt(points[off], Entry.createCell(TWall(appearance.lattices[1])));
                    }

                    board.setAt(
                        points[points.length - 1],
                        Entry.createCell(TWall(getFenceType(points[points.length - 1], points[points.length - 2], appearance.lattices)))
                    );
                }
        }
    }

    private function getFenceType(startPoint : Point, endPoint : Point, lattices : Array<CellTWall>) : CellTWall {
        return (startPoint.row < endPoint.row || startPoint.col < endPoint.col) ? lattices[0] : lattices[2];
    }
}
