package com.eightsines.firestrike.procedural.util;

class Sequence {
    private static var seqId : Int = 0;

    public static function nextId() : Int {
        return ++seqId;
    }
}
