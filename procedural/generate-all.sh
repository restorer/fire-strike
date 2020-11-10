#!/bin/bash

echo "The levels have already been generated and reworked,"
exit "so let's get out of this script to avoid messing up the final levels."
exit

function generate () {
    NAME=$1
    shift

    echo "Generating $NAME ..."
    ./build/cpp/Main -o "build/generated/${NAME}.gd2l" $@
    echo
}

cd "$(dirname "$0")"

if [ ! -e build/cpp/Main ] ; then
    echo "CPP build is not generated yet"
    exit
fi

mkdir -p build/generated

# generate e01m01 -s 1 -w 2 -e 1 -d 1 -r 120491858328382 [done, qa]
# generate e01m02 -s 1 -w 2 -e 1 -d 2 -r 183205592702244 [done, qa]
# generate e01m03 -s 1 -w 2 -e 2 -d 2 -r 211463260779576 # new enemy type, should find new weapon [done, qa]
# generate e01m04 -s 2 -w 3 -e 2 -d 3 -r 13023262613063 [done, qa]
# generate e01m05 -s 1 -w 3 -e 2 -d 3 -r 198608760638973 [done, qa]
# generate e01m06 -s 2 -w 3 -e 2 -d 3 -r 85685144255627 # should find new weapon [done, qa]
# generate e01m07 -s 2 -w 4 -e 3 -d 4 -r 164674977662617 # new enemy type [done, qa]
# generate e01m08 -s 2 -w 4 -e 3 -d 3 -r 78922190533781 [done, qa]
# generate e01m09 -s 3 -w 4 -e 3 -d 3 -r 264813932906777 # should find new weapon [done, qa]
# generate e01m10 -s 2 -w 5 -e 3 -d 3 -r 33536743164150 [done, qa]
# generate e02m01 -s 3 -w 5 -e 4 -d 2 -r 13566485544383 # new enemy type [done, qa]
# generate e02m02 -s 3 -w 5 -e 4 -d 3 -r 31781772733630 # should find new weapon [done, qa]
# generate e02m03 -s 3 -w 6 -e 4 -d 3 -r 22758459502 [done, qa]
# generate e02m04 -s 4 -w 6 -e 4 -d 4 -r 169923031808296 [done, qa]
# generate e02m05 -s 3 -w 6 -e 4 -d 4 -r 249518825680787 [done, qa]
# generate e02m06 -s 4 -w 6 -e 5 -d 4 -r 155669706820510 # new enemy type [done, qa]
# generate e02m07 -s 4 -w 7 -e 5 -d 5 -r 226373706209135 [done, qa]
# generate e02m08 -s 4 -w 7 -e 5 -d 4 -r 213893203021469 [done, qa]
# generate e02m09 -s 5 -w 7 -e 5 -d 4 -r 126611372679188 [done, qa]
# generate e02m10 -s 4 -w 7 -e 5 -d 4 -r 217565207058629 [done, qa]
# generate e03m01 -s 5 -w 7 -e 5 -d 4 -r 47972477089578 [done, qa]
# generate e03m02 -s 5 -w 7 -e 5 -d 4 -r 224464577964169 [done, qa]
# generate e03m03 -s 5 -w 7 -e 5 -d 5 -r 180977265404325 [done, qa]
# generate e03m04 -s 5 -w 7 -e 5 -d 5 -r 75312906883678 [done, qa]
# generate e03m05 -s 5 -w 7 -e 5 -d 4 -r 213558906458804 [done, qa]

echo "Done"

if [ "$1" = "--upload" ] ; then
    echo "Copying locally ..." \
    && chmod a+rw build/generated/*.gd2l \
    && cp build/generated/*.gd2l ../materials/levels/ \
    && echo "Done" \
    && echo "Compiling ..." \
    && pushd ../materials/ \
    && ruby make-levels.rb \
    && popd \
    && echo "Done"

    # && echo "Uploading to the server ..." \
    # && scp -r -P <PORT> build/generated/*.gd2l <USER>@<SERVER>:<PROJECTS DIR>/fsr-mapper/levels/ \
    # && echo "Done"
fi
