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

// DOING:
// - https://play.google.com/about/developer-content-policy/#!?modal_active=none
//
// SOMEDAY:
// - Если ещё нет согласия на отправку крэшей, спрашивать в отдельной активити (а то мало ли, создание текстур отвалится)
// - Некоторые текстуры стен не стыкуются
// - Всё-таки сделать отдельный экран end-of-the-game (который появляется после проигрыша на e99m99)

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
