#!/bin/bash
# Copyright 2009 Google Inc. All Rights Reserved.
#
# Launches the AppCfg utility, which allows Google App Engine
# developers to deploy their application to the cloud.
#
[ -z "${DEBUG}" ] || set -x  # trace if $DEBUG env. var. is non-zero
#SDK_BIN="`dirname "$0" | sed -e "s#^\\([^/]\\)#${PWD}/\\1#"`" # sed makes absolute
#SDK_LIB="$SDK_BIN/../lib"
JAR_FILE="$GAE_HOME/lib/appengine-tools-api.jar"

if [ ! -e "$JAR_FILE" ]; then
    echo "$JAR_FILE not found"
    exit 1
fi

echo $JAR_FILE

"$JAVA_HOME_64"/bin/java -Xmx1100m -cp "$JAR_FILE" com.google.appengine.tools.admin.AppCfg "$@"
