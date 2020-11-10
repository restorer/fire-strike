package com.eightsines.firestrike.procedural.board;

enum abstract CellTWall(Int) to Int {
    var Fence1 = 1;
    var Fence2 = 2;
    var Fence3 = 3;

    var Lattice1 = 4;
    var Lattice2 = 5;

    var ArrowL = 6;
    var ArrowR = 7;
    var ArrowD = 8;
    var ArrowU = 9;
}
