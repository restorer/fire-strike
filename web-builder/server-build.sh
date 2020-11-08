#!/bin/bash

echo "This script must be corrected so that it can be used."
exit 1

# ---

SELF="$(dirname "$0")"
TEMP="<PROJECTS DIR>/fsr-mapper/build"
BUILD="fsr-srvbuild.apk"

if [ ! -e "$TEMP/$BUILD" ] ; then
	echo "Apk not found (\"$TEMP/$BUILD\")"
	exit
fi

if [ -e "$TEMP/.lock" ] ; then
	echo "Lock file exists (\"$TEMP/.lock\")"
fi

echo "lock" > "$TEMP/.lock"

if [ ! -e "$TEMP/.lock" ] ; then
	echo "Can't create lock file (\"$TEMP/.lock\")"
fi

[ -e "$TEMP/.srvbuild" ] && rm -r "$TEMP/.srvbuild"
mkdir "$TEMP/.srvbuild"

[ -e "$TEMP/logs" ] && rm -r "$TEMP/logs"
mkdir "$TEMP/logs"

date '+Built at %Y-%m-%d %H:%M MSK' > "$TEMP/logs/builder.log"
echo >> "$TEMP/logs/builder.log"

ruby "$SELF/make-levels.rb" builder 2>&1 >> "$TEMP/logs/builder.log"
ruby "$SELF/make-texmap.rb" builder 2>&1 >> "$TEMP/logs/builder.log"
echo >> "$TEMP/logs/builder.log"

pushd "$TEMP/.srvbuild"
zip -d "../$BUILD" META-INF/*  2>&1 >> "$TEMP/logs/builder.log"
zip -r "../$BUILD" assets  2>&1 >> "$TEMP/logs/builder.log"
zip -r "../$BUILD" res  2>&1 >> "$TEMP/logs/builder.log"
popd

jarsigner \
	-sigalg SHA1withRSA \
	-digestalg SHA1 \
	-keystore /home/admin/.android/debug.keystore \
	-storepass android \
	"$TEMP/$BUILD" \
	androiddebugkey 2>&1 >> "$TEMP/logs/builder.log"

jarsigner -verify "$TEMP/$BUILD" 2>&1 >> "$TEMP/logs/builder.log"

rm -r "$TEMP/.srvbuild"
rm "$TEMP/.lock"
