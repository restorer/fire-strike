package com.eightsines.firestrike.procedural.section;

enum SectionScenarioAction {
    EndLevel;
    Switch;
    Key(type : Int); // type can be 1 (blue), 2 (red) or 3 (green)
    JustDoor;
}
