package com.eightsines.firestrike.procedural.board;

@:enum
abstract CellWall(Int) to Int {
    var A_Regular = 6;
    var A_LampOff = 40;
    var A_LampOn = 7;
    var A_Switch = 34;

    var B_Regular = 42;
    var B_LampOff = 11;
    var B_LampOn = 12;
    var B_Switch = 20;

    var C_Regular = 8;
    var C_LampOn1 = 9;
    var C_LampOn2 = 10;
    var C_Switch = 30;

    var D_Regular = 5;
    var D_LampOff = 41;
    var D_LampOn = 26;
    var D_Switch = 27;
    var D_Shutters1 = 18;
    var D_Shutters2 = 19;

    var E_Regular = 22;
    var E_Switch = 23;

    var F_Regular = 1;
    var F_LampOff = 43;
    var F_LampOn1 = 25;
    var F_Switch = 32;
    var F_LampOn2 = 29;

    var GateL = 15;
    var GateC = 14;
    var GateR = 13;

    var Thanks1 = 2;
    var Thanks2 = 3;

    var Bricks1 = 16;
    var Bricks2 = 17;

    var Box1 = 36;
    var Box2 = 37;
    var Box3 = 38;
    var Box4 = 39;

    var Mold = 4;
}
