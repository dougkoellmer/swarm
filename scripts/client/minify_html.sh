#!/bin/bash

IN=$1
OUT=$2

echo "Minifying HTML..."

java -jar ../../tools/htmlcompressor-1.5.3.jar --remove-intertag-spaces -t html -o $OUT $IN

echo "Done!"