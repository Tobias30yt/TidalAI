@echo off
setlocal

title TidalAI - Code Counter

echo ==========================================
echo          TidalAI CODE COUNTER
echo ==========================================
echo.
echo Counting Java source files...
echo.

set "PROJECT_ROOT=%~dp0"
set "JAVA_ROOT=%PROJECT_ROOT%src"

if not exist "%JAVA_ROOT%" (
    echo ERROR: src folder not found.
    echo.
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
"$files = Get-ChildItem -Path '%JAVA_ROOT%' -Recurse -Filter '*.java' -File; $totalLines = 0; $codeLines = 0; foreach ($file in $files) { $lines = Get-Content -LiteralPath $file.FullName; $totalLines += $lines.Count; $codeLines += ($lines ^| Where-Object { $_.Trim() -ne '' }).Count }; Write-Host ('Java files        : ' + $files.Count); Write-Host ('Total lines       : ' + $totalLines); Write-Host ('Non-empty lines   : ' + $codeLines); Write-Host ('Project path      : ' + '%PROJECT_ROOT%')"

echo.
echo ==========================================
echo.

pause
