@echo off
rem Copyright 2011 Google Inc. All Rights Reserved.

rem Google SQL Service command line tool.
rem Example:
rem   %0 <instance> [database]

java -jar "%~dp0\.\google_sql.jar" %*
