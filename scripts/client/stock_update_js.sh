#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed

APP_ID=$1

APP_JS=$(realpath ../../project/war/r.$APP_ID/r.$APP_ID.nocache.js)
JS_MIN_OUT=${APP_ID}_min.js
JSP=$(realpath ../../project/war/$APP_ID.jsp)
MODULE=$(realpath ../../project/war/r.$APP_ID)

BH_SCRIPTS="../../project/lib/b33hive/scripts/client"
WAR="../../../../war"

cd $BH_SCRIPTS
sh update_js.sh $APP_JS $JS_MIN_OUT $JSP $MODULE
sh minify_html.sh $WAR/$APP_ID.jsp $WAR/$APP_ID.min.jsp
cd -


