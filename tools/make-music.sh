#!/bin/bash

# 11025, 16000, 22050, 44100
RATE=22050

function convertOne {
    if [ "$1" != "1" ] ; then
        sox -v "$1" "$2" -r "$RATE" "$3" silence 1 0.1 0.1% reverse silence 1 0.1 0.1% reverse
    else
        sox "$2" -r "$RATE" "$3" silence 1 0.1 0.1% reverse silence 1 0.1 0.1% reverse
    fi
}

function convertMusic {
    echo "$2 ..."
    convertOne "$1" "./music-hq/$2" "../src/main/assets/music/$2"
}

cd `dirname "$0"`

convertMusic 0.8 bensound-high-octane.mp3
convertMusic 1 incompetech-motherlode.mp3
convertMusic 0.95 incompetech-ready-aim-fire.mp3
convertMusic 0.95 incompetech-jet-fueled-vixen.mp3
convertMusic 0.9 incompetech-exhilarate.mp3
convertMusic 0.85 dj2puredigital-intro-nice-flyer.mp3
