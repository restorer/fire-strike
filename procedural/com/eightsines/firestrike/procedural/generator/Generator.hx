package com.eightsines.firestrike.procedural.generator;

import com.eightsines.firestrike.procedural.section.Section;

interface Generator {
    function generate() : Array<Section>;
}
