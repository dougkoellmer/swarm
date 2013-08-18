#!/bin/bash

IN=$1
OUT=$2

echo "Minifying HTML..."

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

COMPRESSOR_FILE="htmlcompressor-1.5.3.jar"

cd $CURRENT_DIR

COMPRESSOR_PATH=$(realpath ../../tools)

cd -

TEMP_COMPRESSOR_PATH="./temp"

mkdir $TEMP_COMPRESSOR_PATH

cp $COMPRESSOR_PATH/$COMPRESSOR_FILE $TEMP_COMPRESSOR_PATH/$COMPRESSOR_FILE

java -jar $TEMP_COMPRESSOR_PATH/$COMPRESSOR_FILE --remove-intertag-spaces -t html -o $OUT $IN

rm -rf $TEMP_COMPRESSOR_PATH

echo "Done!"