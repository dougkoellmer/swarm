#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed


DIR=../../project/war/

echo "Minifying HTML..."

java -jar ../../tools/htmlcompressor-1.5.3.jar --remove-intertag-spaces -t html -o $DIR/b33hive.min.jsp $DIR/b33hive.jsp

echo "Done!"