#!/bin/bash


DIR=../../src/swarm/client/js
CM_DIR="$DIR/codemirror/"

cat $CM_DIR/codemirror.js $CM_DIR/css.js $CM_DIR/javascript.js $CM_DIR/xml.js $CM_DIR/htmlmixed.js > $CM_DIR/cm_temp.js
java -jar ../../tools/closure_compiler.jar --js $CM_DIR/cm_temp.js --js_output_file $CM_DIR/cm_min.js
rm $CM_DIR/cm_temp.js