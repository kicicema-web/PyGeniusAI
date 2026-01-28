#!/bin/bash
# Build IPA for PyGenius AI iOS
# Run this script on macOS with Xcode installed

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "PyGenius AI - iOS IPA Builder"
echo "=========================================="
echo ""

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo -e "${RED}[ERROR] This script must be run on macOS!${NC}"
    echo "iOS apps can only be built on macOS with Xcode."
    exit 1
fi

# Check if Xcode is installed
if ! command -v xcodebuild &> /dev/null; then
    echo -e "${RED}[ERROR] Xcode is not installed!${NC}"
    echo "Please install Xcode from the App Store."
    exit 1
fi

XCODE_VERSION=$(xcodebuild -version | head -1)
echo -e "${GREEN}[OK] $XCODE_VERSION found${NC}"

# Check if we're in the right directory
if [ ! -f "PyGeniusAI.xcodeproj/project.pbxproj" ]; then
    echo -e "${RED}[ERROR] Not in PyGeniusAI iOS project directory!${NC}"
    echo "Please run this script from the ios-app/PyGeniusAI directory."
    exit 1
fi

echo ""
echo "Select build type:"
echo "1. Debug (for testing on simulator)"
echo "2. Release Archive (for device distribution)"
echo ""
read -p "Enter choice (1-2): " choice

PROJECT_NAME="PyGeniusAI"
BUILD_DIR="build"

mkdir -p "$BUILD_DIR"

case $choice in
    1)
        echo ""
        echo "Building Debug version..."
        echo ""
        
        xcodebuild build \
            -project "${PROJECT_NAME}.xcodeproj" \
            -scheme "${PROJECT_NAME}" \
            -destination 'platform=iOS Simulator,name=iPhone 15' \
            -configuration Debug \
            -derivedDataPath "$BUILD_DIR/DerivedData"
        
        if [ $? -eq 0 ]; then
            echo ""
            echo -e "${GREEN}Build successful!${NC}"
            echo "App is ready for testing in iOS Simulator"
        else
            echo -e "${RED}Build failed!${NC}"
            exit 1
        fi
        ;;
        
    2)
        echo ""
        echo "Building Release Archive..."
        echo ""
        
        # Check for signing configuration
        echo -e "${YELLOW}[WARNING] Make sure you have configured signing in Xcode!${NC}"
        echo "Go to: Project Settings → Signing & Capabilities"
        echo ""
        read -p "Continue? (y/N): " confirm
        
        if [[ $confirm != [yY] ]]; then
            echo "Build cancelled."
            exit 0
        fi
        
        # Create archive
        echo "Creating archive..."
        xcodebuild archive \
            -project "${PROJECT_NAME}.xcodeproj" \
            -scheme "${PROJECT_NAME}" \
            -destination 'generic/platform=iOS' \
            -archivePath "$BUILD_DIR/${PROJECT_NAME}.xcarchive" \
            -configuration Release
        
        if [ $? -ne 0 ]; then
            echo -e "${RED}Archive creation failed!${NC}"
            exit 1
        fi
        
        echo -e "${GREEN}Archive created!${NC}"
        echo ""
        
        # Create export options plist if it doesn't exist
        if [ ! -f "$BUILD_DIR/exportOptions.plist" ]; then
            echo "Creating exportOptions.plist..."
            cat > "$BUILD_DIR/exportOptions.plist" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>development</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>uploadSymbols</key>
    <true/>
    <key>compileBitcode</key>
    <false/>
</dict>
</plist>
EOF
            echo -e "${YELLOW}[WARNING] Please update $BUILD_DIR/exportOptions.plist with your Team ID${NC}"
        fi
        
        echo ""
        read -p "Export IPA now? (requires valid signing) (y/N): " export_confirm
        
        if [[ $export_confirm == [yY] ]]; then
            echo "Exporting IPA..."
            xcodebuild -exportArchive \
                -archivePath "$BUILD_DIR/${PROJECT_NAME}.xcarchive" \
                -exportOptionsPlist "$BUILD_DIR/exportOptions.plist" \
                -exportPath "$BUILD_DIR/IPA"
            
            if [ $? -eq 0 ]; then
                echo ""
                echo -e "${GREEN}==========================================${NC}"
                echo -e "${GREEN}IPA Export successful!${NC}"
                echo -e "${GREEN}==========================================${NC}"
                echo ""
                echo "IPA location:"
                ls -la "$BUILD_DIR/IPA/"*.ipa 2>/dev/null || echo "Check $BUILD_DIR/IPA/"
                echo ""
                echo "To install on device:"
                echo "  - Use Apple Configurator 2"
                echo "  - Use AltStore"
                echo "  - Upload to App Store Connect"
            else
                echo -e "${RED}IPA export failed!${NC}"
                echo "Common issues:"
                echo "  - Missing signing certificate"
                echo "  - Invalid Team ID in exportOptions.plist"
                echo "  - Device not registered in provisioning profile"
            fi
        fi
        ;;
        
    *)
        echo "Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
echo "Done!"
