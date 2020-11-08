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

function convertSound {
    echo "$2 ..."
    convertOne "$1" "./sounds-hq/${2}.wav" "../android/src/main/assets/sounds/${2}.mp3"
}

cd "$(dirname "$0")"
mkdir -p "../android/src/main/assets/sounds"

convertSound 1 custom_mon_5_death
convertSound 1 diforb_achievement_unlocked
convertSound 0.95 diforb_boom
convertSound 1 diforb_btn_press
convertSound 1 diforb_door_close
convertSound 1 diforb_door_open
convertSound 1 diforb_level_end
convertSound 1 diforb_level_start
convertSound 1 diforb_mark
convertSound 1 diforb_mon_1_ready
convertSound 0.95 diforb_mon_2_ready
convertSound 1 diforb_mon_3_ready
convertSound 1 diforb_mon_4_ready
convertSound 1 diforb_mon_5_ready
convertSound 0.95 custom_no_way
convertSound 1 diforb_pick_item
convertSound 0.95 diforb_shoot_grenade
convertSound 1 diforb_shoot_knife
convertSound 1 diforb_switch
convertSound 1 freesfx_death_hero
convertSound 0.95 freesfx_mon_1_death
convertSound 1 freesfx_mon_2_death
convertSound 0.9 freesfx_mon_3_death
convertSound 1 freesfx_mon_4_death
convertSound 0.95 freesfx_pick_ammo
convertSound 0.95 freesfx_pick_weapon
convertSound 0.9 freesfx_shoot_ak47
convertSound 1 freesfx_shoot_dblpistol
convertSound 1 freesfx_shoot_pistol
convertSound 1 freesfx_shoot_tmp
convertSound 1 freesfx_shoot_winchester
