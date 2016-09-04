#!/bin/bash

DIR=../../src/swarm/shared/css/
OUT_DIR=../../bin/


cat $DIR/iframe_sandbox.css $DIR/tooltip.css $DIR/cell.css $DIR/buttons.css $DIR/icons.css $DIR/magnifier.css $DIR/account.css $DIR/dialog.css $DIR/general.css $DIR/codemirror.css $DIR/caja.css  > $DIR/temp.css

mkdir -p $OUT_DIR

java -jar ../../tools/yuicompressor-2.4.8.jar $DIR/temp.css -o $OUT_DIR/min.css

rm $DIR/temp.css