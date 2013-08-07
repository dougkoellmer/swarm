#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed

IN=$1
OUT=$2

echo "Minifying HTML..."

java -jar ../../tools/htmlcompressor-1.5.3.jar --remove-intertag-spaces -t html -o $OUT $IN

echo "Done!"