#!/bin/bash

DIR=../../src/swarm/client/js
HISTORY_DIR="$DIR/history/"

# Compiling history.js twice here...there was a decent reason, but I forgot why...something with extra metadata being kept after only one compile?
java -jar ../../tools/closure_compiler.jar --js $HISTORY_DIR/native.history.js --js_output_file $HISTORY_DIR/native.history_min.js
cp $HISTORY_DIR/native.history_min.js $HISTORY_DIR/temp.js
java -jar ../../tools/closure_compiler.jar --js $HISTORY_DIR/temp.js --js_output_file $HISTORY_DIR/native.history_min.js
rm $HISTORY_DIR/temp.js