package com.eightsines.firestrike.procedural.util;

#if use_recorder

import com.eightsines.firestrike.procedural.section.Section;
import com.eightsines.firestrike.procedural.view.Viewer;
import haxe.Json;
import org.zamedev.lib.ds.HashSet;

class Recorder {
    private static var viewer : Viewer;
    private static var recordsMap = new Map<String, Array<Array<Int>>>();

    @SuppressWarnings("checkstyle:MagicNumber")
    public static function initialize(_viewer : Viewer) : Void {
        viewer = _viewer;
        recordsMap = new Map<String, Array<Array<Int>>>();

        for (line in RECORDED.split("\n")) {
            if (line.indexOf("[rec] ") != 0) {
                continue;
            }

            var parsed : { key : String, sections : Array<Int> } = cast Json.parse(line.substr(6));

            if (!recordsMap.exists(parsed.key)) {
                recordsMap[parsed.key] = [];
            }

            recordsMap[parsed.key].push(parsed.sections);
        }
    }

    public static function replay(key : String, sections : Array<Section>) : Void {
        var records = recordsMap[key];

        if (records != null) {
            var record = records.shift();

            if (record != null) {
                var sectionMap = new Map<Int, Section>();

                for (section in sections) {
                    if (sectionMap.exists(section.entryValue)) {
                        throw "Duplicated entryValue while replaying";
                    }

                    sectionMap[section.entryValue] = section;
                }

                while (sections.length > 0) {
                    sections.pop();
                }

                for (entryValue in record) {
                    if (!sectionMap.exists(entryValue)) {
                        throw "Non-existing entryValue while replaying";
                    }

                    sections.push(sectionMap[entryValue]);
                }

                return;
            }
        }

        record(key, sections);
    }

    private static function record(key : String, sections : Array<Section>) : Void {
        var existingValues = new HashSet<Int>();

        for (section in sections) {
            if (existingValues.exists(section.entryValue)) {
                throw "Duplicated entryValue while recording";
            }

            existingValues.add(section.entryValue);
        }

        viewer.log("[rec] " + Json.stringify({
            key : key,
            sections : sections.map((s) -> s.entryValue)
        }));
    }

    private static var RECORDED : String = "";
}

#end
