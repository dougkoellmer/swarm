#!/bin/bash

# "Builds" all the JS, taking minified GWT output, and cat-ing that with minified native support JS
# like History.JS, CodeMirror, Modernizr, etc. Then updates resource version in HTML (or JSP) file.

APP_JS=$1
JS_MIN_OUT=$2
HTML_FILE=$3
MODULE=$4

sh minify_js.sh

SUPPORT_JS="../../bin/dependencies.min.js"

cat $SUPPORT_JS $APP_JS > "$MODULE/$JS_MIN_OUT"

sh update_resource_version.sh $HTML_FILE $JS_MIN_OUT