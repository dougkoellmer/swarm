#!/bin/bash

DIR=../../src/swarm/client/js
FASTCLICK_DIR="$DIR/fastclick/"

# Compiling history.js twice here...there was a decent reason, but I forgot why...something with extra metadata being kept after only one compile?
java -jar ../../tools/closure_compiler.jar --js $FASTCLICK_DIR/fastclick.js --js_output_file $FASTCLICK_DIR/fastclick.min.js