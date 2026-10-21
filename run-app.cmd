@echo off
setlocal
cd /d "%~dp0"
set "JAVA_RUN=java"
if exist "%ProgramFiles%\Java\jdk-17\bin\java.exe" set "JAVA_RUN=%ProgramFiles%\Java\jdk-17\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_RUN=%JAVA_HOME%\bin\java.exe"
if not exist "%~dp0task-tracker.jar" (
  echo The source-only copy does not include the runnable JAR.
  echo Build with mvnw.cmd package, then run mvnw.cmd spring-boot:run.
  pause
  exit /b 1
)
echo Starting Task Tracker. Once started, open http://localhost:8080
echo Keep this window open. Press Ctrl+C to stop.
"%JAVA_RUN%" -jar "%~dp0task-tracker.jar"
if errorlevel 1 (
  echo Check that JDK 17 or newer is installed and JAVA_HOME is correct.
  pause
)
