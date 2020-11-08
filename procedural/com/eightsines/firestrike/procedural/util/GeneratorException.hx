package com.eightsines.firestrike.procedural.util;

class GeneratorException {
    public var message(default, null) : String;

    public function new(message : String) {
        this.message = message;
    }
}
