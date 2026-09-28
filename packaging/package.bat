@echo off
setlocal EnableExtensions EnableDelayedExpansion

title TidalAI Release Builder

echo.
echo ==========================================
echo          TIDALAI RELEASE BUILDER
echo ==========================================
echo.

REM ------------------------------------------------------------
REM VERSION
REM ------------------------------------------------------------

set "VERSION=0.1.0"
set "APP_NAME=TidalAI"
set "MAIN_CLASS=com.tidalai.Main"
set "APP_JAR=tidalai-0.1.0.jar"

REM ------------------------------------------------------------
REM PROJECT ROOT
REM ------------------------------------------------------------

cd /d "%~dp0.."

set "PROJECT_ROOT=%CD%"
set "PACKAGING_DIR=%PROJECT_ROOT%\packaging"
set "RELEASE_DIR=%PROJECT_ROOT%\release"
set "WINDOWS_DIR=%RELEASE_DIR%\windows"
set "INPUT_DIR=%PROJECT_ROOT%\target\release-input"
set "ICON_PNG=%PROJECT_ROOT%\src\main\resources\iconig.png"
set "ICON_ICO=%PACKAGING_DIR%\TidalAI.ico"

echo Project:
echo   %PROJECT_ROOT%
echo.

REM ------------------------------------------------------------
REM JAVA 27
REM ------------------------------------------------------------

if exist "C:\Program Files\Java\jdk-27" (

    set "JAVA_HOME=C:\Program Files\Java\jdk-27"

)

if not defined JAVA_HOME (

    echo [ERROR] JAVA_HOME is not set.
    echo.
    echo Expected Java 27 at:
    echo   C:\Program Files\Java\jdk-27
    echo.
    pause
    exit /b 1

)

set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Java:
java -version
echo.

REM ------------------------------------------------------------
REM CHECK TOOLS
REM ------------------------------------------------------------

where java >nul 2>&1

if errorlevel 1 (

    echo [ERROR] Java was not found.
    pause
    exit /b 1

)

where mvn >nul 2>&1

if errorlevel 1 (

    echo [ERROR] Maven was not found.
    pause
    exit /b 1

)

where jpackage >nul 2>&1

if errorlevel 1 (

    echo [ERROR] jpackage was not found.
    echo.
    echo jpackage should be included with JDK 27.
    echo.
    pause
    exit /b 1

)

REM ------------------------------------------------------------
REM ICON
REM ------------------------------------------------------------

if not exist "%ICON_PNG%" (

    echo [ERROR] Could not find:
    echo   %ICON_PNG%
    echo.
    pause
    exit /b 1

)

if not exist "%ICON_ICO%" (

    echo Creating Windows icon...

    powershell.exe ^
        -NoProfile ^
        -ExecutionPolicy Bypass ^
        -File "%PACKAGING_DIR%\make-icon.ps1"

    if errorlevel 1 (

        echo.
        echo [ERROR] Icon creation failed.
        pause
        exit /b 1

    )

) else (

    echo Existing Windows icon found:
    echo   %ICON_ICO%

)

echo.

REM ------------------------------------------------------------
REM CLEAN RELEASE
REM ------------------------------------------------------------

echo Cleaning previous release...

if exist "%RELEASE_DIR%" (
    rmdir /s /q "%RELEASE_DIR%"
)

mkdir "%WINDOWS_DIR%"

if exist "%INPUT_DIR%" (
    rmdir /s /q "%INPUT_DIR%"
)

mkdir "%INPUT_DIR%"

echo.

REM ------------------------------------------------------------
REM MAVEN BUILD
REM ------------------------------------------------------------

echo ==========================================
echo          BUILDING TIDALAI
echo ==========================================
echo.

call mvn clean package

if errorlevel 1 (

    echo.
    echo [ERROR] Maven build failed.
    echo.
    pause
    exit /b 1

)

echo.
echo Maven build successful.
echo.

REM ------------------------------------------------------------
REM COPY RUNTIME DEPENDENCIES
REM ------------------------------------------------------------

echo Preparing runtime dependencies...

call mvn dependency:copy-dependencies ^
    -DincludeScope=runtime ^
    -DoutputDirectory="%INPUT_DIR%"

if errorlevel 1 (

    echo.
    echo [ERROR] Could not copy runtime dependencies.
    echo.
    pause
    exit /b 1

)

if not exist "target\%APP_JAR%" (

    echo.
    echo [ERROR] Application JAR not found:
    echo   target\%APP_JAR%
    echo.
    pause
    exit /b 1

)

copy /Y ^
    "target\%APP_JAR%" ^
    "%INPUT_DIR%\%APP_JAR%" ^
    >nul

echo Runtime input prepared.
echo.

REM ------------------------------------------------------------
REM WINDOWS APP IMAGE
REM ------------------------------------------------------------

echo ==========================================
echo        CREATING WINDOWS APP IMAGE
echo ==========================================
echo.

jpackage ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --app-version "%VERSION%" ^
    --input "%INPUT_DIR%" ^
    --main-jar "%APP_JAR%" ^
    --main-class "%MAIN_CLASS%" ^
    --icon "%ICON_ICO%" ^
    --dest "%WINDOWS_DIR%" ^
    --vendor "TidalAI" ^
    --description "TidalAI local AI workspace" ^
    --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (

    echo.
    echo [ERROR] Windows app-image creation failed.
    echo.
    pause
    exit /b 1

)

echo.
echo Windows app-image created.
echo.

REM ------------------------------------------------------------
REM WINDOWS EXE
REM ------------------------------------------------------------

echo ==========================================
echo          CREATING WINDOWS EXE
echo ==========================================
echo.

jpackage ^
    --type exe ^
    --name "%APP_NAME%" ^
    --app-version "%VERSION%" ^
    --input "%INPUT_DIR%" ^
    --main-jar "%APP_JAR%" ^
    --main-class "%MAIN_CLASS%" ^
    --icon "%ICON_ICO%" ^
    --dest "%WINDOWS_DIR%" ^
    --vendor "TidalAI" ^
    --description "TidalAI local AI workspace" ^
    --win-per-user-install ^
    --win-menu ^
    --win-menu-group "TidalAI" ^
    --win-shortcut ^
    --win-dir-chooser ^
    --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (

    echo.
    echo [ERROR] Windows EXE creation failed.
    echo.
    echo If jpackage reports a Windows installer/WiX
    echo dependency error, install the required Windows
    echo packaging prerequisite and run this script again.
    echo.
    pause
    exit /b 1

)

echo.
echo ==========================================
echo       WINDOWS RELEASE SUCCESSFUL
echo ==========================================
echo.
echo EXE:
echo   %WINDOWS_DIR%\TidalAI-%VERSION%.exe
echo.
echo APP IMAGE:
echo   %WINDOWS_DIR%\TidalAI\
echo.

REM ------------------------------------------------------------
REM LINUX VIA WSL
REM ------------------------------------------------------------

echo ==========================================
echo             LINUX RELEASE
echo ==========================================
echo.

where wsl.exe >nul 2>&1

if errorlevel 1 (

    echo [INFO] WSL was not found.
    echo Linux AppImage will not be built now.
    echo.
    echo Windows release is still complete.
    goto DONE

)

wsl.exe --status >nul 2>&1

if errorlevel 1 (

    echo [INFO] WSL is installed but not ready.
    echo Linux AppImage will not be built now.
    echo.
    echo Windows release is still complete.
    goto DONE

)

for /f "delims=" %%I in ('
    wsl.exe wslpath -a "%PROJECT_ROOT%"
') do (

    set "WSL_PROJECT=%%I"

)

if not defined WSL_PROJECT (

    echo [INFO] Could not resolve project path inside WSL.
    echo Linux AppImage will not be built now.
    echo.
    goto DONE

)

echo Starting Linux package build in WSL...
echo.

wsl.exe bash -lc ^
    "cd '!WSL_PROJECT!' && chmod +x packaging/package-linux.sh && ./packaging/package-linux.sh"

if errorlevel 1 (

    echo.
    echo [WARNING] Linux AppImage build failed.
    echo Windows release is still available.
    echo.

) else (

    echo.
    echo Linux AppImage build completed.
    echo.

)

:DONE

echo ==========================================
echo           TIDALAI RELEASE 0.1.0
echo ==========================================
echo.
echo Release directory:
echo   %RELEASE_DIR%
echo.
echo Files:
echo   release\windows\TidalAI-%VERSION%.exe
echo   release\windows\TidalAI\
echo   release\linux\TidalAI-%VERSION%.AppImage
echo.
echo ==========================================
echo.

pause
exit /b 0
