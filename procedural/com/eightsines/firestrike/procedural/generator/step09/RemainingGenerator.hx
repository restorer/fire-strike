package com.eightsines.firestrike.procedural.generator.step09;

import com.eightsines.firestrike.procedural.config.Config;
import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.generator.AbstractSectionGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.geom.Rect;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section; // {SectionItem, SectionWallLamp}
import com.eightsines.firestrike.procedural.section.SectionItemType;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

using com.eightsines.firestrike.procedural.util.Tools;

class RemainingGenerator extends AbstractSectionGenerator implements Generator {
    private static inline var MAX_SECRET_ITEMS : Int = 4;
    private static inline var LAMPS_SUBDIVISION : Int = 6;
    private static inline var GRASS_MIN_FACTOR : Float = 0.2;
    private static inline var GRASS_MAX_FACTOR : Float = 0.6;

    private var settings : Settings;
    private var availSecretItems : SafeArray<SectionItemType> = [];

    public function new(
        settings : Settings,
        random : Random,
        layer : IntLayer,
        viewer : Viewer,
        sections : SafeArray<Section>
    ) {
        super(random, layer, viewer, sections);
        this.settings = settings;
    }

    public function generate() : SafeArray<Section> {
        prepareAvailSecretItems();
        placeSecretItems();
        placeLampsAndGrass();

        return sections;
    }

    private function prepareAvailSecretItems() : Void {
        availSecretItems.push(SectionItemType.Backpack);
        availSecretItems.push(SectionItemType.Armor(false));
        availSecretItems.push(SectionItemType.Armor(true));
        availSecretItems.push(SectionItemType.Health(false));
        availSecretItems.push(SectionItemType.Health(true));
        availSecretItems.push(SectionItemType.OpenMap);

        for (i in 0 ... Config.weapons.length) {
            var weaponConfig = Config.weapons[i];

            if (weaponConfig.cellItem != null) {
                availSecretItems.push(SectionItemType.Weapon(i));
            }
        }

        for (i in 0 ... Config.ammo.length) {
            var ammoIndex = Config.ammo.length - i - 1;
            var ammoConfig = Config.ammo[ammoIndex];

            if (!ammoConfig.infinite) {
                availSecretItems.push(SectionItemType.Ammo(ammoIndex, true));
                availSecretItems.push(SectionItemType.Ammo(ammoIndex, false));
            }
        }
    }

    private function placeSecretItems() : Void {
        for (section in sections) {
            if (section.scenario >= 0) {
                continue;
            }

            if (settings.argVerboseLevel >= 1) {
                dump(Actions, [section]);
            }

            section.renderAvailInnerCells(layer, true);

            if (settings.argVerboseLevel >= 1) {
                viewer.dumpIntLayer(layer);
            }

            var points : SafeArray<Point> = random.shuffleArray(layer.collect([Section.VAL_INNER_AVAIL], section.getBbox()));

            if (points.length != 0) {
                var totalSecretItems = IntMath.min(MAX_SECRET_ITEMS, random.nextIntRangeInPow(1, points.length, 2.0));

                var currentSecretItems = availSecretItems.copy();
                random.shuffleArray(currentSecretItems);

                for (i in 0 ... totalSecretItems) {
                    section.items.push(new SectionItem(points[i], currentSecretItems[i]));
                }
            }

            if (points.length != 0 || settings.argVerboseLevel >= 1) {
                dump(Actions, [section]);
            }
        }
    }

    private function placeLampsAndGrass() : Void {
        for (section in sections) {
            if (settings.argVerboseLevel >= 1) {
                dump(Actions, [section]);
            }

            section.renderAvailInnerCells(layer, true);

            if (settings.argVerboseLevel >= 1) {
                viewer.dumpIntLayer(layer);
            }

            var wasPlaced : Bool = false;

            if (section.appearanceKind != Outdoor) {
                var innerBbox = section.getBbox().expand(-1);
                var vertSubdivisions : Int = Math.ceil(innerBbox.height / LAMPS_SUBDIVISION);
                var horSubdivisions : Int = Math.ceil(innerBbox.width / LAMPS_SUBDIVISION);
                var vertSize : Int = Math.floor(innerBbox.height / vertSubdivisions);
                var horSize : Int = Math.floor(innerBbox.width / horSubdivisions);

                for (i in 0 ... vertSubdivisions) {
                    var fromRow = innerBbox.row + Math.floor((vertSize - 0.5) * (i + 0.5));
                    var toRow = innerBbox.row + Math.floor(vertSize * (i + 0.5));

                    for (j in 0 ... horSubdivisions) {
                        var rect = Rect.fromCoords(
                            fromRow,
                            innerBbox.col + Math.floor((horSize - 0.5) * (j + 0.5)),
                            toRow,
                            innerBbox.col + Math.floor(horSize * (j + 0.5))
                        );

                        var availPoints : SafeArray<Point> = [];

                        for (point in rect.points()) {
                            if ([Section.VAL_INNER_AVAIL,
                                Section.VAL_INNER_PASSABLE,
                                Section.VAL_INNER_DECORATION
                            ].contains(layer.getAt(point))) {
                                availPoints.push(point);
                            }
                        }

                        switch (availPoints.length) {
                            case 0: // do nothing

                            case 1:
                                if (layer.getAt(availPoints[0]) == Section.VAL_INNER_AVAIL && random.nextBool()) {
                                    section.lamps.push(availPoints[0]);
                                    wasPlaced = true;
                                } else {
                                    section.ceilingLamps.push(availPoints[0]);
                                }

                            default:
                                section.ceilingLamps.pushAll(availPoints);
                        }
                    }
                }

                for (row in section.getBbox().rows()) {
                    var vertVal = (row - innerBbox.row + vertSize) % vertSize;
                    var isVert = (vertVal == Math.floor((vertSize - 0.5) * 0.5) || vertVal == Math.floor(vertSize * 0.5));

                    for (col in section.getBbox().columns()) {
                        var horVal = (col - innerBbox.col + horSize) % horSize;
                        var isHor = (horVal == Math.floor((horSize - 0.5) * 0.5) || horVal == Math.floor(horSize * 0.5));

                        if ((isVert || isHor) && layer.get(row, col) == Section.VAL_INNER_BORDER) {
                            var isVertLighter = ((row - section.getBbox().row) % 2) != 0;
                            var isHorLighter = ((col - section.getBbox().col) % 2) != 0;

                            section.wallLamps.push(new SectionWallLamp(
                                new Point(row, col),
                                (isVertLighter && !isHorLighter) || (!isVertLighter && isHorLighter)
                            ));
                        }
                    }
                }
            }

            if (section.appearanceKind == Outdoor) {
                var points : SafeArray<Point> = random.shuffleArray(layer.collect([Section.VAL_INNER_AVAIL], section.getBbox()));
                var totalGrassCount = random.nextIntRangeIn(Std.int(points.length * GRASS_MIN_FACTOR), Std.int(points.length * GRASS_MAX_FACTOR));

                for (i in 0 ... totalGrassCount) {
                    section.grass.push(points[i]);
                    wasPlaced = true;
                }
            }

            if (wasPlaced || settings.argVerboseLevel >= 1) {
                dump(Actions, [section]);
            }
        }
    }
}
