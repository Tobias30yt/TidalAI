#!/usr/bin/env bash

set -euo pipefail

echo
echo "=========================================="
echo "       TIDALAI LINUX RELEASE BUILDER"
echo "=========================================="
echo

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"

VERSION="0.1.0"
APP_NAME="TidalAI"
MAIN_CLASS="com.tidalai.Main"
APP_JAR="tidalai-${VERSION}.jar"

RELEASE_DIR="$PROJECT_ROOT/release"
LINUX_DIR="$RELEASE_DIR/linux"

INPUT_DIR="$PROJECT_ROOT/target/linux-input"
JPACKAGE_DIR="$PROJECT_ROOT/target/linux-jpackage"
APP_IMAGE="$JPACKAGE_DIR/$APP_NAME"

APPDIR="$PROJECT_ROOT/target/TidalAI.AppDir"

ICON_PNG="$PROJECT_ROOT/src/main/resources/iconig.png"

APPIMAGE="$LINUX_DIR/TidalAI-$VERSION.AppImage"

# ------------------------------------------------------------

# CHECK TOOLS

# ------------------------------------------------------------

command -v java >/dev/null 2>&1 || {
echo "[ERROR] Java was not found in WSL."
echo "Install Java 27 in your Linux/WSL environment."
exit 1
}

command -v javac >/dev/null 2>&1 || {
echo "[ERROR] javac was not found."
echo "A full JDK is required."
exit 1
}

command -v mvn >/dev/null 2>&1 || {
echo "[ERROR] Maven was not found in WSL."
exit 1
}

command -v jpackage >/dev/null 2>&1 || {
echo "[ERROR] jpackage was not found."
echo "Use a JDK that contains jpackage."
exit 1
}

command -v curl >/dev/null 2>&1 || {
echo "[ERROR] curl was not found."
echo "Install curl in WSL."
exit 1
}

echo "Java:"
java -version
echo

echo "jpackage:"
jpackage --version
echo

echo "Maven:"
mvn -version
echo

# ------------------------------------------------------------

# ARCHITECTURE

# ------------------------------------------------------------

ARCH="$(uname -m)"

if [[ "$ARCH" != "x86_64" ]]; then
echo "[ERROR] This release script currently targets x86_64."
echo "Detected architecture: $ARCH"
exit 1
fi

# ------------------------------------------------------------

# ICON

# ------------------------------------------------------------

if [[ ! -f "$ICON_PNG" ]]; then
echo "[ERROR] PNG icon not found:"
echo "  $ICON_PNG"
exit 1
fi

# ------------------------------------------------------------

# CLEAN

# ------------------------------------------------------------

echo "Cleaning previous Linux release..."

rm -rf "$LINUX_DIR"
rm -rf "$INPUT_DIR"
rm -rf "$JPACKAGE_DIR"
rm -rf "$APPDIR"

mkdir -p "$LINUX_DIR"
mkdir -p "$INPUT_DIR"
mkdir -p "$JPACKAGE_DIR"

# ------------------------------------------------------------

# MAVEN

# ------------------------------------------------------------

echo
echo "=========================================="
echo "          BUILDING TIDALAI"
echo "=========================================="
echo

cd "$PROJECT_ROOT"

mvn clean package

echo
echo "Maven build successful."
echo

# ------------------------------------------------------------

# DEPENDENCIES

# ------------------------------------------------------------

echo "Preparing Linux runtime dependencies..."

mvn dependency:copy-dependencies 
-DincludeScope=runtime 
-DoutputDirectory="$INPUT_DIR"

if [[ ! -f "$PROJECT_ROOT/target/$APP_JAR" ]]; then
echo
echo "[ERROR] Application JAR not found:"
echo "  target/$APP_JAR"
exit 1
fi

cp 
"$PROJECT_ROOT/target/$APP_JAR" 
"$INPUT_DIR/$APP_JAR"

# ------------------------------------------------------------

# JPACKAGE APP IMAGE

# ------------------------------------------------------------

echo
echo "=========================================="
echo "      CREATING LINUX JPACKAGE IMAGE"
echo "=========================================="
echo

jpackage 
--type app-image 
--name "$APP_NAME" 
--app-version "$VERSION" 
--input "$INPUT_DIR" 
--main-jar "$APP_JAR" 
--main-class "$MAIN_CLASS" 
--icon "$ICON_PNG" 
--dest "$JPACKAGE_DIR" 
--vendor "TidalAI" 
--description "TidalAI local AI workspace" 
--java-options "-Dfile.encoding=UTF-8" 
--java-options "--enable-native-access=ALL-UNNAMED"

if [[ ! -d "$APP_IMAGE" ]]; then
echo
echo "[ERROR] jpackage did not create:"
echo "  $APP_IMAGE"
exit 1
fi

echo
echo "Linux jpackage image created."
echo

# ------------------------------------------------------------

# CREATE APPDIR

# ------------------------------------------------------------

echo "Creating AppImage AppDir..."

mkdir -p "$APPDIR"

cp -a 
"$APP_IMAGE/." 
"$APPDIR/"

# ------------------------------------------------------------

# APPRUN

# ------------------------------------------------------------

cat > "$APPDIR/AppRun" <<'EOF'
#!/usr/bin/env bash

set -e

HERE="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

exec "$HERE/bin/TidalAI" "$@"
EOF

chmod +x "$APPDIR/AppRun"

# ------------------------------------------------------------

# DESKTOP FILE

# ------------------------------------------------------------

cat > "$APPDIR/TidalAI.desktop" <<'EOF'
[Desktop Entry]
Name=TidalAI
Comment=TidalAI local AI workspace
Exec=TidalAI
Icon=TidalAI
Terminal=false
Type=Application
Categories=Development;Utility;
StartupNotify=true
EOF

mkdir -p 
"$APPDIR/usr/share/applications"

cp 
"$APPDIR/TidalAI.desktop" 
"$APPDIR/usr/share/applications/TidalAI.desktop"

# ------------------------------------------------------------

# ICON

# ------------------------------------------------------------

mkdir -p 
"$APPDIR/usr/share/icons/hicolor/256x256/apps"

cp 
"$ICON_PNG" 
"$APPDIR/TidalAI.png"

cp 
"$ICON_PNG" 
"$APPDIR/usr/share/icons/hicolor/256x256/apps/TidalAI.png"

# ------------------------------------------------------------

# APPIMAGETOOL

# ------------------------------------------------------------

CACHE_DIR="$HOME/.cache/tidalai"
APPIMAGETOOL="$CACHE_DIR/appimagetool-x86_64.AppImage"

mkdir -p "$CACHE_DIR"

if [[ ! -f "$APPIMAGETOOL" ]]; then
echo "Downloading appimagetool..."


curl \
    -L \
    --fail \
    --retry 3 \
    -o "$APPIMAGETOOL" \
    "https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage"

fi

chmod +x "$APPIMAGETOOL"

# ------------------------------------------------------------

# BUILD APPIMAGE

# ------------------------------------------------------------

echo
echo "=========================================="
echo "        CREATING TIDALAI APPIMAGE"
echo "=========================================="
echo

"$APPIMAGETOOL" 
"$APPDIR" 
"$APPIMAGE"

# ------------------------------------------------------------

# RESULT

# ------------------------------------------------------------

if [[ ! -f "$APPIMAGE" ]]; then
echo
echo "[ERROR] AppImage was not created."
exit 1
fi

chmod +x "$APPIMAGE"

echo
echo "=========================================="
echo "       LINUX RELEASE SUCCESSFUL"
echo "=========================================="
echo
echo "AppImage:"
echo "  $APPIMAGE"
echo
ls -lh "$APPIMAGE"
echo
echo "=========================================="
echo
