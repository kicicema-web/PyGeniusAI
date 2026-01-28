@echo off
chcp 65001 >nul
title PyGenius AI Desktop
echo ==========================================
echo    PyGenius AI Desktop Edition
echo ==========================================
echo.

:: Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python is not installed!
    echo.
    echo Please install Python 3.8 or higher from:
    echo https://www.python.org/downloads/
    echo.
    echo Make sure to check "Add Python to PATH" during installation.
    echo.
    pause
    exit /b 1
)

echo [OK] Python found
echo.

:: Check if requests is installed
echo Checking dependencies...
python -c "import requests" 2>nul
if errorlevel 1 (
    echo Installing required packages...
    python -m pip install requests -q
    if errorlevel 1 (
        echo [ERROR] Failed to install dependencies.
        echo Please run: pip install requests
        pause
        exit /b 1
    )
)

echo [OK] Dependencies ready
echo.
echo Starting PyGenius AI Desktop...
echo ==========================================
python pygenius_desktop.py

if errorlevel 1 (
    echo.
    echo [ERROR] Application crashed!
    pause
)
