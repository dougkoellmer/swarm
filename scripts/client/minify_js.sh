#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed

DIR=../../project/war/r.js
CM_DIR="$DIR/cm/2/"
CAJA_DIR="$DIR/caja/2/"
HISTORY_DIR="$DIR/history/1"
MODERNIZR_DIR="$DIR/modernizr"
PLACEHOLDER_DIR="$DIR/placeholder"
BH_DIR="$DIR/../r.bh/"
OUT_DIR="$BH_DIR/"

cat $CM_DIR/codemirror.js $CM_DIR/css.js $CM_DIR/javascript.js $CM_DIR/xml.js $CM_DIR/htmlmixed.js > $CM_DIR/cm_temp.js
java -jar ../../tools/closure_compiler.jar --js $CM_DIR/cm_temp.js --js_output_file $CM_DIR/cm_min.js
rm $CM_DIR/cm_temp.js

java -jar ../../tools/closure_compiler.jar --js $HISTORY_DIR/native.history.js --js_output_file $HISTORY_DIR/native.history_min.js
cp $HISTORY_DIR/native.history_min.js $HISTORY_DIR/temp.js
java -jar ../../tools/closure_compiler.jar --js $HISTORY_DIR/temp.js --js_output_file $HISTORY_DIR/native.history_min.js
rm $HISTORY_DIR/temp.js

cat $HISTORY_DIR/native.history_min.js $MODERNIZR_DIR/modernizr.custom.90450.js $CM_DIR/cm_min.js $CAJA_DIR/caja-minified.js $BH_DIR/r.bh.nocache.js > $OUT_DIR/bh_min.js


echo "Setting new version..."


#! Note the backquote here.
TIME=`date +%s`

PAGE=../../project/war/b33hive.jsp

sed -i -E "s/bh_min.js\?v\=([0-9]+)/bh_min.js\?v\=$TIME/g" $PAGE

#! Necessary because sed messes up file permissions under cygwin.
chmod 777 $PAGE

sh minify_html.sh