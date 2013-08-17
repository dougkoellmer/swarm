#!/bin/bash

#NOTE: If getting error about AppCfg, may have to explicitly set JAVA_HOME in appcfg.sh

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed

WAR="$BH_HOME/project/war/"

#OLD_JAVA_HOME=$JAVA_HOME
#export JAVA_HOME=$JAVA_HOME_32

cd $APP_ENGINE_HOME
sh ../appengine-java-sdk-1.8.0/bin/appcfg.sh update $WAR
cd -

#export JAVA_HOME=$OLD_JAVA_HOME