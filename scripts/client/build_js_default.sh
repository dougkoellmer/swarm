#!/bin/bash

#--- DRK > Calls build_js.sh with a common configuration.
#---		This script should be invoked from the project directory.

APP_JS=$(realpath ./war/r.app/r.app.nocache.js)
JS_MIN_OUT=min.js
JSP=$(realpath ./war/index.jsp)
MODULE=$(realpath ./war/r.app)

BH_SCRIPTS="./lib/swarm/scripts/client"

cd $BH_SCRIPTS
sh build_js.sh $APP_JS $JS_MIN_OUT $JSP $MODULE
#sh minify_html.sh $WAR/index.jsp $WAR/index.min.jsp
cd -


