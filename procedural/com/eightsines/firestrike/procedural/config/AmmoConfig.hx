package com.eightsines.firestrike.procedural.config;

class AmmoConfig {
    public var ensured(default, null) : Int;
    public var max(default, null) : Int;
    public var pickOne(default, null) : Int;
    public var pickBox(default, null) : Int;
    public var inBackpack(default, null) : Bool;
    public var infinite(default, null) : Bool;

    public function new(ensured : Int, max : Int, pickOne : Int, pickBox : Int, inBackpack : Bool = true, infinite : Bool = false) {
        this.ensured = ensured;
        this.max = max;
        this.pickOne = pickOne;
        this.pickBox = pickBox;
        this.inBackpack = inBackpack;
        this.infinite = infinite;
    }

    public function toString() : String {
        return 'AmmoConfig(ensured=${ensured}, max=${max}, pickOne=${pickOne}, pickBox=${pickBox}, inBackpack=${inBackpack}, infinite=${infinite})';
    }
}
