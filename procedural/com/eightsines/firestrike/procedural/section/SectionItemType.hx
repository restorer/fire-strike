package com.eightsines.firestrike.procedural.section;

enum SectionItemType {
    Health(box : Bool); // Stim, Medi
    Armor(box : Bool); // Armor_Green, Armor_Red
    Ammo(ammo : Int, box : Bool); // Clip, CBox, Shell, SBox, Grenade, GBox
    Backpack; // BPack
    OpenMap; // OpenMap
    Weapon(weapon : Int); // Weapon_DblPist, Weapon_AK47, Weapon_Tmp, Weapon_Winchester
}
