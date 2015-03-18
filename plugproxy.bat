@ECHO OFF
@REM
@REM A batch file for running the PlugProxy UI
@REM
@REM  Brad Wellington 06/27/2002
@REM

set params1=%1 %2 %3 %4 %5 %6 %7 %8 %9
shift
shift
shift
shift
shift
shift
shift
shift
shift
set params2=%1 %2 %3 %4 %5 %6 %7 %8 %9
shift
shift
shift
shift
shift
shift
shift
shift
shift
set params3=%1 %2 %3 %4 %5 %6 %7 %8 %9

java -jar plugproxy.jar %params1% %params2% %params%