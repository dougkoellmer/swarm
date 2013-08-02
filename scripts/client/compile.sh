#!/bin/bash

export SHELLOPTS
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is needed

WAR="$BH_HOME/project/war"


cd $BH_HOME/project/src

java -cp "$GWT_HOME/gwt-dev.jar;$GWT_HOME/*" com.google.gwt.dev.Compiler -war $BH_HOME/war com.b33hive.B33hive

cd -