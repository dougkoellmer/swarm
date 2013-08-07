#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed

APP_JS=$1
JS_MIN_OUT=$2
JSP=$3
MODULE=$4

sh minify_js.sh

SUPPORT_JS="../../bin/bh_support_lib.min.js"

cat $SUPPORT_JS $APP_JS > "$MODULE/$JS_MIN_OUT"

sh update_resource_version.sh $JSP $JS_MIN_OUT