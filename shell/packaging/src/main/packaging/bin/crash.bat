@echo off
REM init variables
set CMD_LINE_ARGS=

REM Get standard environment variables
set PRGDIR=%~dp0

REM Only set CRASH_HOME if not already set
if not ".%CRASH_HOME%" == "." goto setupArgs
pushd 
cd %~dp0\..
set CRASH_HOME=%CD%
popd

:setupArgs
REM copy args one by one from the command line
if "%1%a"=="a" goto setClasspath
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setupArgs

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
for %%F in (%CRASH_HOME%\lib\*.jar) do (
	call :concat %%F
)

REM Create tmp dir if it does not exist
mkdir  %CRASH_HOME%\tmp

set CLASSPATH=%CLASSPATH%;%LIB%

REM start the application with all parameters. Add tools.jar to the bootclasspath, otherwise it cannot be found
java -Xbootclasspath/a:"%TOOLS_JAR%" -classpath "%CLASSPATH%" org.crsh.cli.impl.bootstrap.Main -jar "%CRASH_HOME%\bin\%JARNAME%" --conf "%CRASH_HOME%\conf" --cmd "%CRASH_HOME%\cmd" %CMD_LINE_ARGS%

:concat
if "%LIB%" == "" (
	set LIB=%1
) else (
	set LIB=%LIB%;%1;
)
goto :eof