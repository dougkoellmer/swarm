#!/bin/bash

DIR=../../src/swarm/client/js
CM_DIR="$DIR/codemirror/"
CAJA_DIR="$DIR/caja/"
HISTORY_DIR="$DIR/history/"
MODERNIZR_DIR="$DIR/modernizr"

OUT_DIR="../../bin/"

cat $CM_DIR/codemirror.js $CM_DIR/css.js $CM_DIR/javascript.js $CM_DIR/xml.js $CM_DIR/htmlmixed.js > $CM_DIR/cm_temp.js
java -jar ../../tools/closure_compiler.jar --js $CM_DIR/cm_temp.js --js_output_file $CM_DIR/cm_min.js
rm $CM_DIR/cm_temp.js

# Compiling history.js twice here...there was a decent reason, but I forgot why...something with extra metadata being kept after only one compile?
java -jar ../../tools/closure_compiler.jar --js $HISTORY_DIR/native.history.js --js_output_file $HISTORY_DIR/native.history_min.js
cp $HISTORY_DIR/native.history_min.js $HISTORY_DIR/temp.js
java -jar ../../tools/closure_compiler.jar --js $HISTORY_DIR/temp.js --js_output_file $HISTORY_DIR/native.history_min.js
rm $HISTORY_DIR/temp.js

cat $HISTORY_DIR/native.history_min.js $MODERNIZR_DIR/modernizr.custom.90450.js $CM_DIR/cm_min.js $CAJA_DIR/caja-minified.js > $OUT_DIR/dependencies.min.js

rm $CM_DIR/cm_min.js
rm $HISTORY_DIR/native.history_min.js