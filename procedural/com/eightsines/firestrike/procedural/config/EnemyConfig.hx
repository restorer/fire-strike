package com.eightsines.firestrike.procedural.config;

class EnemyConfig {
    public var type(default, null) : Int;
    public var health(default, null) : Int;
    public var dps(default, null) : Int;
    public var ammo(default, null) : Int;

    public function new(type : Int, health : Int, dps : Int, ammo : Int) {
        this.type = type;
        this.health = health;
        this.dps = dps;
        this.ammo = ammo;
    }

    public function toString() : String {
        // htype -- human type = type + 1
        return 'EnemyConfig(htype=${type + 1}, health=${health}, dps=${dps}, ammo=${ammo})';
    }
}
