#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed


DIR=../../project/src/com/b33hive/shared/css/
OUT_DIR=../../project/war/r.bh/

echo "Concating css files."
cat $DIR/cell.css $DIR/buttons.css $DIR/icons.css $DIR/magnifier.css $DIR/account.css $DIR/dialog.css $DIR/general.css $DIR/codemirror.css $DIR/caja.css  > $DIR/temp.css
echo "Minifying css files."
java -jar ../../tools/yuicompressor-2.4.8.jar $DIR/temp.css -o $OUT_DIR/bh_min.css
rm $DIR/temp.css


echo "Setting new version..."


#! Note the backquote here.
TIME=`date +%s`

PAGE=../../project/war/b33hive.jsp

sed -i -E "s/bh_min.css\?v\=([0-9]+)/bh_min.css\?v\=$TIME/g" $PAGE

#! Necessary because sed messes up file permissions under cygwin.
chmod 777 $PAGE

sh minify_html.sh