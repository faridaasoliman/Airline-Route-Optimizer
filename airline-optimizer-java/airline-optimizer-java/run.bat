@echo off
echo ============================================
echo   SkyRoute AI — Airline Optimizer (Java)
echo ============================================
echo.

:: Create output folder
if not exist "out" mkdir out

echo [1/2] Compiling Java files...
javac -d out -sourcepath src src\airline\Main.java

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Compilation failed. Make sure Java JDK is installed.
    echo Download from: https://adoptium.net
    pause
    exit /b 1
)

echo [2/2] Launching application...
echo.
java -cp out airline.Main

pause
