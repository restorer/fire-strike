package com.eightsines.firestrike.procedural.generator.step07;

import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.generator.AbstractSectionGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section; // {SectionScenarioObject}
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

class PlayerGenerator extends AbstractSectionGenerator implements Generator {
    private var settings : Settings;
    private var sectionsMap = new Map<Int, Section>();
    private var sectionsMapLayer : IntLayer;
    private var pathWaveLayer : IntLayer;

    public function new(settings : Settings, random : Random, layer : IntLayer, viewer : Viewer, sections : Array<Section>) {
        super(random, layer, viewer, sections);
        this.settings = settings;

        sectionsMapLayer = new IntLayer(layer.width, layer.height);
        pathWaveLayer = new IntLayer(layer.width, layer.height);
    }

    public function generate() : Array<Section> {
        placePlayer();
        return sections;
    }

    private function placePlayer() : Void {
        var section = sections.find((section) -> (section.scenario == 0)).sure();

        dump(Actions, [section]);
        section.renderAvailInnerCells(layer, true /* , true */);

        if (settings.argVerboseLevel >= 1) {
            viewer.dumpIntLayer(layer);
        }

        section.player = GeneratorUtils.selectRandomFreestPoint(random, layer, layer.collect([Section.VAL_INNER_AVAIL], section.getBbox()));

        if (section.player == null) {
            throw new GeneratorException("placePlayer failed: no free cells for player");
        }

        dump(Actions);
    }
}
