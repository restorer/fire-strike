package com.eightsines.firestrike.procedural.config;

import com.eightsines.firestrike.procedural.board.CellItem;

using Safety;

// additionalLoss: если игрок далеко от врага, то он тратит больше патронов (и ещё игрок может промахиваться).
// Для разного вида оружия эта поправка тоже разная (например, для dblpist - это 2, т.к. за один цикл уходит не менее 2х патронов).

class WeaponConfig {
    public var ammo(default, null) : Int;
    public var dps(default, null) : Int; // damage per second
    public var spd(default, null) : Int; // shoots per second (== ammo per second)
    public var stun(default, null) : Float;
    public var additionalLoss(default, null) : Int;
    public var cellItem(default, null) : Null<CellItem>;

    public function new(ammo : Int, dps : Int, spd : Int, stun : Float, ?additionalLoss : Int, ?cellItem : CellItem) {
        this.ammo = ammo;
        this.dps = dps;
        this.spd = spd;
        this.stun = stun;
        this.additionalLoss = additionalLoss.or(0);
        this.cellItem = cellItem;
    }

    public function toString() : String {
        return 'WeaponConfig(ammo=${ammo}, dps=${dps}, spd=${spd}, stun=${stun}, additionalLoss=${additionalLoss}, cellItem=${cellItem == null ? "NULL" : Std.string(cellItem.unsafe())})';
    }
}
