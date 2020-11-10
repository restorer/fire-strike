package com.eightsines.firestrike.procedural.section;

enum abstract SectionDecorationType(Int) to Int {
    var Box = 1; // ящики
    var Pillar = 2; // колонны, деревья, высокие декорации
    var Rock = 3; // камни, низкие декорации
    var Lattice = 4; // любые виды решёток
    var Barrel = 5; // бочка
}
