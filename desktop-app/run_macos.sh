#!/bin/bash
# PyGenius AI Desktop - macOS Launcher

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "=========================================="
echo "   PyGenius AI Desktop Edition"
echo "=========================================="
echo ""

# Check if Python 3 is installed
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}[ERROR] Python 3 is not installed!${NC}"
    echo ""
    echo "Please install Python 3.8 or higher:"
    echo "1. Install Homebrew: /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
    echo "2. Install Python: brew install python"
    echo ""
    echo "Or download from: https://www.python.org/downloads/mac-osx/"
    echo ""
    read -p "Press Enter to exit..."
    exit 1
fi

echo -e "${GREEN}[OK]$(python3 --version) found${NC}"
echo ""

# Check if requests is installed
echo "Checking dependencies..."
if ! python3 -c "import requests" 2>/dev/null; then
    echo -e "${YELLOW}Installing required packages...${NC}"
    pip3 install requests --user
    if [ $? -ne 0 ]; then
        echo -e "${RED}[ERROR] Failed to install dependencies.${NC}"
        echo "Please run: pip3 install requests"
        read -p "Press Enter to exit..."
        exit 1
    fi
fi

echo -e "${GREEN}[OK] Dependencies ready${NC}"
echo ""
echo "Starting PyGenius AI Desktop..."
echo "=========================================="
echo ""

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Run the application
python3 pygenius_desktop.py

if [ $? -ne 0 ]; then
    echo ""
    echo -e "${RED}[ERROR] Application crashed!${NC}"
    read -p "Press Enter to exit..."
fi
