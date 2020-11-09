#!/bin/bash

if [[ "$1" = "" ]] ; then
	echo "Usage: z-build <debug|release> [install]"
	exit
fi

if [[ "$2" = "install" ]] ; then
	PREFIX="install"
else
	PREFIX="assemble"
fi

case "$1" in
	debug)
		SUFFIX="Debug"
		;;

	release)
		SUFFIX="Release"
		;;

	*)
		echo "Unknown build type: $1"
		exit
esac

cd "$(dirname "$0")"
./gradlew "${PREFIX}${SUFFIX}"

if [[ "$1" = "release" && -e "../_dist" ]] ; then
	VERSION_NAME="$(cat build.gradle | grep ' versionName ' | sed -e "s/^[ ]*versionName[ ]*'//" | sed -e "s/'[ ]*$//")"

	SRC_APK="build/outputs/apk/release/android-release.apk"
	SRC_MAPPING="build/outputs/mapping/release/mapping.txt"
	DST_BASENAME="fire-strike-retro-${VERSION_NAME}"

	[ -e "../_dist/${DST_BASENAME}.apk" ] && rm "../_dist/${DST_BASENAME}.apk"
	[ -e "../_dist/${DST_BASENAME}.txt" ] && rm "../_dist/${DST_BASENAME}.txt"
	[ -e "$SRC_APK" ] && cp "$SRC_APK" "../_dist/${DST_BASENAME}.apk"
	[ -e "$SRC_MAPPING" ] && cp "$SRC_MAPPING" "../_dist/${DST_BASENAME}.txt"
fi
