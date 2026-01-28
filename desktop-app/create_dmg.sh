#!/bin/bash
# Create a DMG installer for PyGenius AI on macOS
# Run this script on a Mac after building the app

APP_NAME="PyGenius AI"
APP_BUNDLE="dist/${APP_NAME}.app"
DMG_NAME="PyGeniusAI-macOS.dmg"
VOLUME_NAME="PyGenius AI Installer"

# Check if app bundle exists
if [ ! -d "$APP_BUNDLE" ]; then
    echo "Error: $APP_BUNDLE not found!"
    echo "Please build the app first with: python3 setup_macos.py"
    exit 1
fi

echo "Creating DMG for ${APP_NAME}..."

# Remove old DMG if exists
if [ -f "$DMG_NAME" ]; then
    rm "$DMG_NAME"
fi

# Create a temporary directory for the DMG contents
TMP_DIR=$(mktemp -d)

# Copy app bundle
cp -r "$APP_BUNDLE" "$TMP_DIR/"

# Create Applications folder symlink
ln -s /Applications "$TMP_DIR/Applications"

# Create the DMG
hdiutil create -srcfolder "$TMP_DIR" -volname "$VOLUME_NAME" -fs HFS+ -fsargs "-c c=64,a=16,e=16" -format UDRW -size 100m temp.dmg

# Convert to compressed read-only DMG
hdiutil convert temp.dmg -format UDZO -o "$DMG_NAME"

# Clean up
rm temp.dmg
rm -rf "$TMP_DIR"

echo ""
echo "=========================================="
echo "DMG created successfully!"
echo "=========================================="
echo ""
echo "File: $DMG_NAME"
echo ""
echo "To distribute:"
echo "  1. Upload $DMG_NAME to GitHub releases"
echo "  2. Users can download and double-click to mount"
echo "  3. Drag PyGenius AI.app to Applications folder"
echo ""
