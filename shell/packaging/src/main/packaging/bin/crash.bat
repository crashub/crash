@echo off
setlocal

REM init variables
set CMD_LINE_ARGS=

REM Get standard environment variables
set PRGDIR=%~dp0

REM Only set CRASH_HOME if not already set
if not ".%CRASH_HOME%" == "." goto setupArgs
pushd %PRGDIR%..
set CRASH_HOME=%CD%
popd

:setupArgs
REM copy args from the command line
set CMD_LINE_ARGS=%*

:setClasspath
REM if JAVA_HOME is set, add tools.jar to classpath
if ".%JAVA_HOME%" == "."  goto addCrashJar
set CLASSPATH=%JAVA_HOME%\lib\tools.jar
set TOOLS_JAR=%JAVA_HOME%\lib\tools.jar

:addCrashJar
for %%F in (%CRASH_HOME%\bin\crsh.cli*.jar) do set JARNAME=%%~nxF
set CLASSPATH=%CLASSPATH%;%CRASH_HOME%\bin\%JARNAME%

:setJars
REM add all jars from the lib directory to the classpath
for %%F in (%CRASH_HOME%\lib\*.jar) do set CLASSPATH=%CLASSPATH%;%%F

REM Create tmp dir if it does not exist
mkdir  %CRASH_HOME%\tmp

REM start the application with all parameters. Add tools.jar to the bootclasspath, otherwise it cannot be found
java -Xbootclasspath/a:"%TOOLS_JAR%" -classpath "%CLASSPATH%" -Djava.util.logging.config.file="%CRASH_HOME%\conf\logging.properties" -jar "%CRASH_HOME%\bin\%JARNAME%" --conf "%CRASH_HOME%\conf" --cmd "%CRASH_HOME%\cmd" %CMD_LINE_ARGS%

set ERROR_CODE=%ERRORLEVEL%
endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
