package com.eightsines.firestrike.procedural.generator;

import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

class AbstractPainter {
    private var settings : Settings;
    private var random : Random;
    private var layer : IntLayer;
    private var board : Board;
    private var viewer : Viewer;
    private var sections : Array<Section>;
    private var scenarioOpenerMap : Map<Int, Section> = new Map<Int, Section>();

    public function new(settings : Settings, random : Random, layer : IntLayer, board : Board, viewer : Viewer, sections : Array<Section>) {
        this.settings = settings;
        this.random = random;
        this.layer = layer;
        this.board = board;
        this.viewer = viewer;
        this.sections = sections;

        GeneratorUtils.fillScenarioMaps(sections, null, scenarioOpenerMap);
    }
}
