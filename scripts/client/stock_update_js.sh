#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed

APP_ID=$1

APP_JS=$(realpath ../../project/war/r.app/r.app.nocache.js)
JS_MIN_OUT=min.js
JSP=$(realpath ../../project/war/index.jsp)
MODULE=$(realpath ../../project/war/r.app)

BH_SCRIPTS="../../project/lib/b33hive/scripts/client"
WAR="../../../../war"

cd $BH_SCRIPTS
sh update_js.sh $APP_JS $JS_MIN_OUT $JSP $MODULE
sh minify_html.sh $WAR/index.jsp $WAR/index.min.jsp
cd -


