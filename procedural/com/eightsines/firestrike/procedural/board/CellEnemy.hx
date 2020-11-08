package com.eightsines.firestrike.procedural.board;

@:enum
abstract CellEnemy(Int) to Int {
    var SoldierWithPistol = 1;
    var SoldierWithRifle = 2;
    var SoldierWithKnife = 3;
    var SoldierWithGrenade = 4;
    var Zombie = 5;
}
