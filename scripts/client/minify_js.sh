#!/bin/bash

DIR=../../src/swarm/client/js
CM_DIR="$DIR/codemirror/"
CAJA_DIR="$DIR/caja/"
HISTORY_DIR="$DIR/history/"
MODERNIZR_DIR="$DIR/modernizr"
FASTCLICK_DIR="$DIR/fastclick"
UTILS_DIR="$DIR/utils"
IMAGES_LOADED_DIR="$DIR/images_loaded"

OUT_DIR="../../bin"

sh ./minify_codemirror.sh
sh ./minify_history.sh
sh ./minify_fastclick_js.sh
sh ./minify_utils_js.sh

echo $IMAGES_LOADED

echo "HERERERERER"

cat $IMAGES_LOADED_DIR/images_loaded.min.js $UTILS_DIR/utils.min.js $FASTCLICK_DIR/fastclick.min.js $HISTORY_DIR/native.history_min.js $MODERNIZR_DIR/modernizr.custom.90450.js $CM_DIR/cm_min.js $CAJA_DIR/caja-minified.js > $OUT_DIR/dependencies.min.js