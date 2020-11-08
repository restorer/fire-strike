package com.eightsines.firestrike.procedural.board;

enum Cell {
    Wall(value : CellWall);
    TWall(value : CellTWall);
    Window(value : CellWindow);
    Door(value : CellDoor);
    Decor(value : CellDecor);
    Pass(value : CellPass);
    Item(value : CellItem);
    Enemy(value : CellEnemy);
    Player(direction : Int);
}
