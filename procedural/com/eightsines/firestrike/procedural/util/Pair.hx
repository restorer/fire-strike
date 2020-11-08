package com.eightsines.firestrike.procedural.util;

class Pair<F, S> {
    public var first : F;
    public var second : S;

    public function new(first : F, second : S) {
        this.first = first;
        this.second = second;
    }

    public function toString() : String {
        return 'Pair(first=${first}, second=${second})';
    }
}
