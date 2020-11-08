package com.eightsines.firestrike.procedural.view.browser;

import com.eightsines.firestrike.procedural.board.Arrow;
import com.eightsines.firestrike.procedural.board.Board;
import com.eightsines.firestrike.procedural.board.Ceiling;
import com.eightsines.firestrike.procedural.board.Cell;
import com.eightsines.firestrike.procedural.board.Entry;
import com.eightsines.firestrike.procedural.board.Floor;
import com.eightsines.firestrike.procedural.generator.Processor;
import com.eightsines.firestrike.procedural.layer.BoolLayer;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.layer.Layer;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.Tools;
import com.eightsines.firestrike.procedural.view.Viewer;
import de.polygonal.Printf;
import js.Browser;
import js.html.CanvasElement;
import js.html.CanvasRenderingContext2D;
import js.html.DOMElement;
import js.html.InputElement;
import js.html.Storage;
import js.html.TextAreaElement;
import js.html.URL;

using Safety;
using StringTools;

class BrowserViewer extends Viewer {
    private static inline var ENTRY_SIZE : Int = 64;
    private static inline var ENTRY_HALF_SIZE : Int = Std.int(ENTRY_SIZE / 2);
    private static inline var GIF_FRAME_SIZE : Int = 512;

    private static inline var LOCAL_STORAGE_PREFIX : String = "com.eightsines.firestrike.procedural:";
    private static inline var LOCAL_STORAGE_ARGS : String = LOCAL_STORAGE_PREFIX + "args";

    private static var INT_CELL_PALETTE : SafeArray<Int> = [0, 255, 192, 128];

    private var graphicsUrl : String;
    private var argsElement : InputElement;
    private var viewportContainerElement : DOMElement;
    private var viewportElement : CanvasElement;
    private var fullLogElement : TextAreaElement;
    private var attachedNoteElement : InputElement;
    private var viewportContext : CanvasRenderingContext2D;
    private var localStorage : Null<Storage>;
    private var preloadedImages = new Map<String, PreloadedImage>();
    private var requestRenderTimeoutId : Null<Int> = null;

    private var entries : SafeArray<BrowserViewerEntry> = [];
    private var entryIndex : Int = 0;

    public function new(processorCb : () -> Processor) {
        super(processorCb);

        graphicsUrl = untyped Browser.window.GRAPHICS_URL;
        argsElement = cast Browser.document.getElementById("args");
        viewportContainerElement = cast Browser.document.getElementById("viewportcontainer");
        viewportElement = cast Browser.document.getElementById("viewport");
        fullLogElement = cast Browser.document.getElementById("fulllog");
        attachedNoteElement = cast Browser.document.getElementById("attachednote");
        viewportContext = viewportElement.getContext2d();
        localStorage = Browser.getLocalStorage();

        initialize();
    }

    private function initialize() : Void {
        Browser.document.getElementById("gen").addEventListener("click", () -> {
            if (localStorage != null) {
                localStorage.unsafe().setItem(LOCAL_STORAGE_ARGS, argsElement.value.trim());
            }

            processorCb().process(argsElement.value.trim().split(" "));
        });

        Browser.document.getElementById("regen").addEventListener("click", () -> processorCb().process([]));

        Browser.document.getElementById("prev").addEventListener("click", () -> {
            entryIndex--;
            requestRender();
        });

        Browser.document.getElementById("next").addEventListener("click", () -> {
            entryIndex++;
            requestRender();
        });

        Browser.document.getElementById("fastprev").addEventListener("click", () -> {
            entryIndex -= 16;
            requestRender();
        });

        Browser.document.getElementById("fastnext").addEventListener("click", () -> {
            entryIndex += 16;
            requestRender();
        });

        Browser.document.getElementById("gifall").addEventListener("click", () -> makeGif());
        Browser.document.getElementById("gifbrd").addEventListener("click", () -> makeGif(true));

        Browser.document.getElementById("tglog").addEventListener("click", () -> {
            viewportContainerElement.classList.toggle("viewport-container--hidden");
            fullLogElement.classList.toggle("full-log--visible");
        });

        viewportElement.addEventListener("click", () -> viewportElement.classList.toggle("viewport--restricted"));

        if (localStorage != null) {
            argsElement.value = localStorage.unsafe().getItem(LOCAL_STORAGE_ARGS);
        }
    }

    override public function dumpBoolLayer(layer : BoolLayer, ?data : String) : Void {
        super.dumpBoolLayer(layer, data);
        pushEntry(BrowserViewerEntry.BoolLayer(layer.copy(), data));
    }

    override public function dumpIntLayer(layer : IntLayer, ?data : String) : Void {
        super.dumpIntLayer(layer, data);
        pushEntry(BrowserViewerEntry.IntLayer(layer.copy(), data));
    }

    override public function dumpBoard(board : Board, renderMode : Int = 0, ?data : String) : Void {
        super.dumpBoard(board, renderMode, data);
        pushEntry(BrowserViewerEntry.Board(board.copy(), renderMode, data));
    }

    override public function log(message : String) : Void {
        Browser.console.log(message);
        fullLogElement.value += message + "\n";
    }

    private function makeGif(onlyBoardEntries : Bool = false) : Void {
        trace("Adding frames...");

        var gif : Dynamic = untyped __js__(
            'new GIF({ background: "#000000", width: {0}, height: {1}, workerScript: "lib/gif.worker.js" })',
            GIF_FRAME_SIZE,
            GIF_FRAME_SIZE
        );

        var canvas : CanvasElement = cast Browser.document.createElement('canvas');
        var context = canvas.getContext2d();

        canvas.width = GIF_FRAME_SIZE;
        canvas.height = GIF_FRAME_SIZE;

        for (i in 0 ... entries.length) {
            switch (entries[i]) {
                case BoolLayer(_, _) | IntLayer(_, _):
                    if (onlyBoardEntries) {
                        continue;
                    }

                case Board(_, _):
                    // do nothing
            }

            renderEntry(entries[i]);
            context.drawImage(viewportElement, 0, 0, GIF_FRAME_SIZE, GIF_FRAME_SIZE);

            gif.addFrame(context, {
                copy : true,
                delay : (i == entries.length - 1 ? 5000 : 200),
            });
        }

        gif.on("finished", (blob : Any) -> {
            trace("Finished");
            Browser.window.open(URL.createObjectURL(blob));
        });

        trace("Encoding...");
        gif.render();
    }

    private function pushEntry(entry : BrowserViewerEntry) : Void {
        entries.push(entry);
        entryIndex = entries.length - 1;
        requestRender();
    }

    private function requestRender() : Void {
        if (entryIndex >= entries.length - 1) {
            entryIndex = entries.length - 1;
        }

        if (entryIndex <= 0) {
            entryIndex = 0;
        }

        if (requestRenderTimeoutId != null) {
            Browser.window.clearTimeout(requestRenderTimeoutId);
        }

        requestRenderTimeoutId = Browser.window.setTimeout(() -> {
            requestRenderTimeoutId = null;

            if (entryIndex >= 0 && entryIndex < entries.length) {
                renderEntry(entries[entryIndex]);
            }
        }, 1);
    }

    private function renderEntry(entry : BrowserViewerEntry) : Void {
        switch (entry) {
            case BoolLayer(layer, data):
                render(layer, renderBoolCell, 0, data);

            case IntLayer(layer, data):
                render(layer, renderIntCell, 0, data);

            case Board(layer, renderMode, data):
                render(layer, renderBoardCell, renderMode, data);
        }
    }

    private function render<T>(layer : Layer<T>, cellRenderer : (Int, Int, T, Int) -> Void, renderMode : Int, data : Null<String>) : Void {
        var size = IntMath.max(layer.width, layer.height);

        viewportElement.height = size * ENTRY_SIZE;
        viewportElement.width = size * ENTRY_SIZE;

        viewportContext.font = 'bold 24px Tahoma, Arial';
        viewportContext.textAlign = 'center';
        viewportContext.textBaseline = 'middle';

        for (row in 0 ... layer.height) {
            for (col in 0 ... layer.width) {
                cellRenderer(col * ENTRY_SIZE, row * ENTRY_SIZE, layer.get(row, col), renderMode);
            }
        }

        attachedNoteElement.value = (data == null ? "" : data);
    }

    private function renderBoolCell(px : Int, py : Int, entry : Bool, _ : Int) : Void {
        viewportContext.fillStyle = "#000000";
        viewportContext.fillRect(px, py, ENTRY_SIZE, ENTRY_SIZE);

        if (entry) {
            viewportContext.fillStyle = "#ffffff";
            viewportContext.fillRect(px + 4, py + 4, ENTRY_SIZE - 8, ENTRY_SIZE - 8);
        }
    }

    private function renderIntCell(px : Int, py : Int, entry : Int, _ : Int) : Void {
        var additionalEntry = (entry < 0) ? 0 : (entry >> 20);

        if (entry > 0) {
            entry &= 0xfffff;
        }

        var b = INT_CELL_PALETTE[(entry & 0x01) | ((entry & 0x08) >> 2)];
        var r = INT_CELL_PALETTE[((entry & 0x02) >> 1) | ((entry & 0x10) >> 3)];
        var g = INT_CELL_PALETTE[((entry & 0x04) >> 2) | ((entry & 0x20) >> 4)];

        viewportContext.fillStyle = "#000000";
        viewportContext.fillRect(px, py, ENTRY_SIZE, ENTRY_SIZE);
        viewportContext.fillStyle = 'rgb(${r},${g},${b})';

        if (entry >= 0x70000) {
            viewportContext.fillRect(px + 4, py + 4, ENTRY_HALF_SIZE - 4, ENTRY_HALF_SIZE - 4);
            viewportContext.fillRect(px + ENTRY_HALF_SIZE, py + ENTRY_HALF_SIZE, ENTRY_HALF_SIZE - 4, ENTRY_HALF_SIZE - 4);
        } else if (entry >= 0x60000) {
            viewportContext.fillRect(px + ENTRY_HALF_SIZE - 8, py + 4, 16, ENTRY_SIZE - 8);
            viewportContext.fillRect(px + 4, py + ENTRY_HALF_SIZE - 8, ENTRY_SIZE - 8, 16);
        } else if (entry >= 0x50000) {
            viewportContext.fillRect(px + ENTRY_HALF_SIZE - 16, py + 4, 32, ENTRY_SIZE - 8);
        } else if (entry >= 0x40000) {
            viewportContext.fillRect(px + 4, py + ENTRY_HALF_SIZE - 8, ENTRY_SIZE - 8, 16);
        } else if (entry >= 0x30000) {
            viewportContext.fillRect(px + 4, py + 4, ENTRY_HALF_SIZE - 8, ENTRY_HALF_SIZE - 8);
            viewportContext.fillRect(px + ENTRY_HALF_SIZE + 4, py + 4, ENTRY_HALF_SIZE - 8, ENTRY_HALF_SIZE - 8);
            viewportContext.fillRect(px + 4, py + ENTRY_HALF_SIZE + 4, ENTRY_HALF_SIZE - 8, ENTRY_HALF_SIZE - 8);
            viewportContext.fillRect(px + ENTRY_HALF_SIZE + 4, py + ENTRY_HALF_SIZE + 4, ENTRY_HALF_SIZE - 8, ENTRY_HALF_SIZE - 8);
        } else if (entry < -0x10000 || entry >= 0x20000) {
            viewportContext.fillRect(px + 16, py + 16, ENTRY_SIZE - 32, ENTRY_SIZE - 32);
        } else {
            viewportContext.fillRect(px + 4, py + 4, ENTRY_SIZE - 8, ENTRY_SIZE - 8);

            if (entry < 0 || entry >= 0x10000) {
                viewportContext.fillStyle = "#000000";
                viewportContext.fillRect(px + 16, py + 16, ENTRY_SIZE - 32, ENTRY_SIZE - 32);
            }
        }

        if (additionalEntry != 0) {
            var b = INT_CELL_PALETTE[(additionalEntry & 0x01) | ((additionalEntry & 0x08) >> 2)];
            var r = INT_CELL_PALETTE[((additionalEntry & 0x02) >> 1) | ((additionalEntry & 0x10) >> 3)];
            var g = INT_CELL_PALETTE[((additionalEntry & 0x04) >> 2) | ((additionalEntry & 0x20) >> 4)];

            viewportContext.fillStyle = 'rgb(${r},${g},${b})';
            viewportContext.fillRect(px + 24, py + 24, ENTRY_SIZE - 48, ENTRY_SIZE - 48);
        }
    }

    private function renderBoardCell(px : Int, py : Int, entry : Entry, renderMode : Int) : Void {
        var drawList : SafeArray<BrowserViewerTile> = [];
        var preloadList : SafeArray<String> = [];

        if (renderMode > 0) {
            switch (entry.ceiling.sure().tl.sure()) {
                case Some(value):
                    fillPreloadedImage(
                        getCeilingSrc(value),
                        new BrowserViewerTile(0, 0, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE),
                        drawList,
                        preloadList
                    );

                case None:
            }

            switch (entry.ceiling.sure().tr.sure()) {
                case Some(value):
                    fillPreloadedImage(
                        getCeilingSrc(value),
                        new BrowserViewerTile(ENTRY_HALF_SIZE, 0, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE),
                        drawList,
                        preloadList
                    );

                case None:
            }

            switch (entry.ceiling.sure().bl.sure()) {
                case Some(value):
                    fillPreloadedImage(
                        getCeilingSrc(value),
                        new BrowserViewerTile(0, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE),
                        drawList,
                        preloadList
                    );

                case None:
            }

            switch (entry.ceiling.sure().br.sure()) {
                case Some(value):
                    fillPreloadedImage(
                        getCeilingSrc(value),
                        new BrowserViewerTile(ENTRY_HALF_SIZE, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE),
                        drawList,
                        preloadList
                    );

                case None:
            }
        } else if (renderMode == 0) {
            switch (entry.floor.sure().tl.sure()) {
                case Some(value):
                    fillPreloadedImage(
                        getFloorSrc(value),
                        new BrowserViewerTile(0, 0, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE),
                        drawList,
                        preloadList
                    );

                case None:
            }

            switch (entry.floor.sure().tr.sure()) {
                case Some(value):
                    fillPreloadedImage(
                        getFloorSrc(value),
                        new BrowserViewerTile(ENTRY_HALF_SIZE, 0, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE),
                        drawList,
                        preloadList
                    );

                case None:
            }

            switch (entry.floor.sure().bl.sure()) {
                case Some(value):
                    fillPreloadedImage(
                        getFloorSrc(value),
                        new BrowserViewerTile(0, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE),
                        drawList,
                        preloadList
                    );

                case None:
            }

            switch (entry.floor.sure().br.sure()) {
                case Some(value):
                    fillPreloadedImage(
                        getFloorSrc(value),
                        new BrowserViewerTile(ENTRY_HALF_SIZE, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE, ENTRY_HALF_SIZE),
                        drawList,
                        preloadList
                    );

                case None:
            }
        }

        switch (entry.arrow.sure()) {
            case Some(value):
                fillPreloadedImage(getArrowSrc(value), new BrowserViewerTile(0, 0, ENTRY_SIZE, ENTRY_SIZE), drawList, preloadList);

            case None:
        }

        switch (entry.cell.sure()) {
            case Some(value):
                fillPreloadedImage(getCellSrc(value), new BrowserViewerTile(0, 0, ENTRY_SIZE, ENTRY_SIZE), drawList, preloadList);

            case None:
        }

        switch (entry.noTrans.sure()) {
            case Some(true):
                fillPreloadedImage(getNoTransSrc(), new BrowserViewerTile(0, 0, ENTRY_SIZE, ENTRY_SIZE), drawList, preloadList);

            case Some(false) | None:
        }

        if (preloadList.length != 0) {
            viewportContext.fillStyle = "#440000";
            viewportContext.fillRect(px, py, ENTRY_SIZE, ENTRY_SIZE);

            renderMark(px, py, entry);
            preloadImages(preloadList, () -> renderBoardCell(px, py, entry, renderMode));

            return;
        }

        viewportContext.fillStyle = "#000000";
        viewportContext.fillRect(px, py, ENTRY_SIZE, ENTRY_SIZE);

        for (tile in drawList) {
            var xm = tile.iw.sure() / ENTRY_SIZE;
            var ym = tile.ih.sure() / ENTRY_SIZE;

            viewportContext.drawImage(
                tile.image.sure(),
                tile.x * xm,
                tile.y * ym,
                tile.w * xm,
                tile.h * ym,
                px + tile.x,
                py + tile.y,
                tile.w,
                tile.h
            );
        }

        renderMark(px, py, entry);
    }

    private function renderMark(px : Int, py : Int, entry : Entry) : Void {
        switch (entry.mark.sure()) {
            case Some(value):
                viewportContext.strokeStyle = '#000000';
                viewportContext.lineWidth = 4;
                viewportContext.strokeText(value, px + 32, py + 32, 64);
                viewportContext.lineWidth = 1;

                viewportContext.fillStyle = '#ff0000';
                viewportContext.fillText(value, px + 32, py + 32, 64);

            case None:
        }
    }

    private function getFloorSrc(value : Floor) : String {
        return Printf.format("%s/set-1/floor/floor_%02d.png", Tools.arrayOfAny(graphicsUrl, value));
    }

    private function getArrowSrc(value : Arrow) : String {
        return Printf.format("%s/common/misc/arrow_%02d.png", Tools.arrayOfAny(graphicsUrl, value));
    }

    private function getCellSrc(cell : Cell) : String {
        return '${graphicsUrl}/' + switch (cell) {
            case Wall(value):
                Printf.format("set-1/walls/wall_%02d.png", [value].stdArray());

            case TWall(value):
                Printf.format("set-1/twall/twall_%02d.png", [value].stdArray());

            case Window(value):
                Printf.format("set-1/twind/twind_%02d.png", [value].stdArray());

            case Door(value):
                Printf.format("set-1/doors/door_%02d_f.png", [value].stdArray());

            case Decor(value):
                Printf.format("set-1/ditem/ditem_%02d.png", [value].stdArray());

            case Pass(value):
                Printf.format("set-1/dlamp/dlamp_%02d.png", [value].stdArray());

            case Item(value):
                Printf.format("common/objects/obj_%02d.png", [value].stdArray());

            case Enemy(value):
                Printf.format("common/monsters/mon_%02d_a3.png", [value].stdArray());

            case Player(direction):
                Printf.format("common/misc/hero_%02d.png", [direction].stdArray());
        }
    }

    private function getCeilingSrc(value : Ceiling) : String {
        return Printf.format("%s/set-1/ceil/ceil_%02d.png", Tools.arrayOfAny(graphicsUrl, value));
    }

    private function getNoTransSrc() : String {
        return Printf.format("%s/common/misc/no_trans.png", Tools.arrayOfAny(graphicsUrl));
    }

    private function fillPreloadedImage(
        src : String,
        tile : BrowserViewerTile,
        drawList : SafeArray<BrowserViewerTile>,
        preloadList : SafeArray<String>
    ) : Void {
        var preloadedImage = preloadedImages[src];

        if (preloadedImage == null || !preloadedImage.isLoaded) {
            preloadList.push(src);
            return;
        }

        var _preloadedImage = preloadedImage.sure();

        tile.image = _preloadedImage.image;
        tile.iw = _preloadedImage.width;
        tile.ih = _preloadedImage.height;

        drawList.push(tile);
    }

    @:safety(unsafe)
    private function preloadImages(sources : SafeArray<String>, callback : () -> Void) : Void {
        for (src in sources) {
            var preloadedImage = preloadedImages[src];

            if (preloadedImage != null) {
                preloadedImage.addCompleter(new PreloadedImageCompleter(callback, sources));
                continue;
            }

            preloadedImage = new PreloadedImage(callback, sources);
            preloadedImages[src] = preloadedImage;

            function onLoadCb() {
                for (completer in preloadedImage.getAndClearCompleters()) {
                    var hasNoErrors = true;

                    for (ensureSrc in completer.ensureSources) {
                        var ensurePreloadedImage = preloadedImages[ensureSrc];

                        if (ensurePreloadedImage == null) {
                            hasNoErrors = false;
                            trace("Preload error");
                        } else if (!ensurePreloadedImage.isLoaded) {
                            ensurePreloadedImage.addCompleter(completer);
                        }
                    }

                    if (hasNoErrors) {
                        completer.callback();
                    }
                }
            }

            function onErrorCb() {
                // "Safety: Cannot assign nullable value to not-nullable variable." due to using onErrorCb inside onErrorCb.
                Browser.window.setTimeout(() -> preloadedImage.preload(src, onLoadCb, onErrorCb), 1000);
            }

            preloadedImage.preload(src, onLoadCb, onErrorCb);
        }
    }
}
