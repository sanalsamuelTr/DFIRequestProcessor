@rem echo off
@rem

@echo Copyright 2018 Thomson Reuters. All rights reserved.

@SET PROJECT_HOME=%CD%
@SET JARFILE=./dfi-request-processor-0.0.1.jar

@rem Java memory options.
@SET MEMORY_OPTS=-Xms1024M -Xmx1024M

java -classpath %MEMORY_OPTS% -jar %JARFILE% %*
