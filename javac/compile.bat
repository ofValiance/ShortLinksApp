@echo off
echo ========================================
echo Compiling short-links-app-1.0...

cd /d %~dp0src
javac *.java
if %errorlevel% neq 0 (
    echo Compilation error!
    pause
    exit /b
)
echo Success!
echo ========================================

pause