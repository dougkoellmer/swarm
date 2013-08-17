#!/bin/bash

#! Note the backquote here.
TIME=`date +%s`

PAGE=$1
RESOURCE=$2

sed -i -E "s/$RESOURCE\?v\=([0-9]+)/$RESOURCE\?v\=$TIME/g" $PAGE

#! Necessary because sed messes up file permissions under cygwin.
chmod 777 "$PAGE"