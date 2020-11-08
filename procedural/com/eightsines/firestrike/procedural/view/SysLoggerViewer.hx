package com.eightsines.firestrike.procedural.view;

import com.eightsines.firestrike.procedural.generator.Processor;

class SysLoggerViewer extends Viewer {
    public function new(processorCb : () -> Processor) {
        super(processorCb);
    }

    override public function log(message : String) : Void {
        Sys.println(message);
    }
}
