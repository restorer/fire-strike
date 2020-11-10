package com.eightsines.firestrike.procedural;

import com.eightsines.firestrike.procedural.generator.Processor;

#if js
    import com.eightsines.firestrike.procedural.view.browser.BrowserViewer;
#elseif sys
    import com.eightsines.firestrike.procedural.generator.Exporter;
    import com.eightsines.firestrike.procedural.view.SysLoggerViewer;
    import sys.io.File;
#end

using Safety;

// https://habr.com/post/332832/
// https://habr.com/post/184818/
// http://jnordberg.github.io/gif.js/

@:expose
class Main {
    public static function main() : Void {
        var processor : Null<Processor> = null;
        var viewer = #if js new BrowserViewer(() -> processor.sure()) #elseif sys new SysLoggerViewer(() -> processor.sure()) #end ;

        #if use_recorder
            com.eightsines.firestrike.procedural.util.Recorder.initialize(viewer);
        #end

        processor = new Processor(viewer);

        #if sys
            if (processor.process(Sys.args())) {
                var data = (new Exporter(processor.settings.sure(), processor.board.sure())).export();
                File.saveContent(processor.settings.sure().outputName.sure(), data);
            }
        #end
    }
}
