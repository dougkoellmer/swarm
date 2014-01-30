#!/bin/bash

DIR=../../src/swarm/client/js
UTILS_DIR="$DIR/utils/"

# Compiling history.js twice here...there was a decent reason, but I forgot why...something with extra metadata being kept after only one compile?
java -jar ../../tools/closure_compiler.jar --js $UTILS_DIR/utils.js --js_output_file $UTILS_DIR/utils.min.js