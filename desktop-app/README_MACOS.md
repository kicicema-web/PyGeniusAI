# PyGenius AI - macOS Edition

## Quick Start

### Option 1: Run with Python (Easiest)

1. Make sure you have Python 3.8+ installed:
   ```bash
   python3 --version
   ```
   If not installed, get it from https://www.python.org/downloads/mac-osx/

2. Open Terminal and navigate to the PyGeniusAI folder:
   ```bash
   cd /path/to/PyGeniusAI
   ```

3. Run the launcher:
   ```bash
   ./run_macos.sh
   ```

   Or run directly with Python:
   ```bash
   python3 pygenius_desktop.py
   ```

### Option 2: Build macOS App Bundle (Recommended for Distribution)

1. Open Terminal in the PyGeniusAI folder

2. Run the setup script:
   ```bash
   python3 setup_macos.py
   ```

3. The script will:
   - Install py2app (if not present)
   - Build the .app bundle
   - Place it in `dist/PyGenius AI.app`

4. To install:
   - Open the `dist` folder
   - Drag "PyGenius AI.app" to your Applications folder
   - Double-click to launch

5. (Optional) Create DMG for distribution:
   ```bash
   chmod +x create_dmg.sh
   ./create_dmg.sh
   ```

## Requirements

- macOS 10.13 (High Sierra) or later
- Python 3.8 or higher (for Option 1)
- Internet connection (for AI features)

## First Launch

If you see "Cannot open because the developer cannot be verified":

1. Right-click (or Control-click) on PyGenius AI.app
2. Select "Open" from the menu
3. Click "Open" in the dialog that appears

This only needs to be done once.

## Troubleshooting

### "Python is not installed"
Install Python from https://www.python.org/downloads/mac-osx/ or use Homebrew:
```bash
brew install python
```

### "Permission denied" when running run_macos.sh
Make it executable:
```bash
chmod +x run_macos.sh
```

### App crashes on startup
Check if `requests` is installed:
```bash
pip3 install requests
```

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| ⌘+N | New file |
| ⌘+O | Open file |
| ⌘+S | Save file |
| F5 | Run code |

## Features

Same as other platforms:
- 📝 Code editor with syntax highlighting
- 💻 Console for running Python code
- 🤖 AI Tutor powered by OpenRouter (GPT-3.5)
- 💡 Code explanation and optimization
- 🐛 Bug detection
- 🎨 Dark theme
- 📁 File operations

## Uninstall

To remove the app:
1. Drag PyGenius AI.app from Applications to Trash
2. Empty Trash

No other files are installed on your system.
