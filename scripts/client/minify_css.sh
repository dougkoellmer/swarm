#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed


DIR=../../src/b33hive/shared/css/
OUT_DIR=../../bin/

echo "Concating css files."
cat $DIR/cell.css $DIR/buttons.css $DIR/icons.css $DIR/magnifier.css $DIR/account.css $DIR/dialog.css $DIR/general.css $DIR/codemirror.css $DIR/caja.css  > $DIR/temp.css
echo "Minifying css files."
java -jar ../../tools/yuicompressor-2.4.8.jar $DIR/temp.css -o $OUT_DIR/bh_min.css
rm $DIR/temp.css