package com.eightsines.firestrike.procedural.generator;

import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;

class AbstractGeometryGenerator {
    private var random : Random;
    private var layer : IntLayer;
    private var viewer : Viewer;

    public function new(random : Random, layer : IntLayer, viewer : Viewer) {
        this.random = random;
        this.layer = layer;
        this.viewer = viewer;
    }
}
