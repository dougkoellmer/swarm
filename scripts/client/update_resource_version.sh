#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed

#! Note the backquote here.
TIME=`date +%s`

#../../project/war/b33hive.jsp

PAGE=$1
RESOURCE=$2

sed -i -E "s/$RESOURCE\?v\=([0-9]+)/$RESOURCE\?v\=$TIME/g" $PAGE

#! Necessary because sed messes up file permissions under cygwin.
chmod 777 "$PAGE"