# PyGenius AI - Desktop Edition

A desktop version of PyGenius AI for Windows, Linux, and macOS.

## Features

- 📝 **Code Editor** - Syntax highlighting, line numbers, file operations
- 💻 **Console** - Run Python code and see output in real-time
- 🤖 **AI Tutor** - Ask questions about Python programming
- 💡 **Code Explanation** - Get AI-powered explanations of your code
- 🐛 **Bug Detection** - Find and fix issues in your code
- ⚡ **Code Optimization** - Get suggestions to improve your code
- 🎨 **Dark Theme** - Easy on the eyes for long coding sessions

## Quick Start

### Windows

#### Option 1: Run with Python (Easiest)
1. Make sure you have Python 3.8+ installed: https://www.python.org/downloads/
2. Extract `PyGeniusAI-Windows.zip`
3. Double-click `run_windows.bat`

#### Option 2: Build Executable (Advanced)
```bash
pip install pyinstaller requests
pyinstaller --onefile --windowed pygenius_desktop.py
```
The executable will be in `dist/PyGeniusAI.exe`

### Linux

```bash
# Install dependencies
pip3 install requests

# Run directly
python3 pygenius_desktop.py

# Or use the pre-built executable
./PyGeniusAI
```

### macOS

```bash
# Install dependencies
pip3 install requests

# Run
python3 pygenius_desktop.py
```

## Requirements

- Python 3.8 or higher
- Internet connection (for AI features)
- `requests` library (auto-installed)

## AI Features

All AI features are powered by OpenRouter (GPT-3.5):
- No API key setup required - pre-configured!
- Just install and start using AI features immediately

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl+N | New file |
| Ctrl+O | Open file |
| Ctrl+S | Save file |
| F5 | Run code |

## File Locations

- Windows: `%APPDATA%/PyGeniusAI/`
- Linux: `~/.config/PyGeniusAI/`
- macOS: `~/Library/Application Support/PyGeniusAI/`

## Building from Source

### Windows Executable
```bash
pip install pyinstaller requests
pyinstaller --onefile --windowed --name PyGeniusAI pygenius_desktop.py
```

### Linux Executable
```bash
pip install pyinstaller requests
pyinstaller --onefile --windowed --name PyGeniusAI pygenius_desktop.py
```

## License

Same as PyGenius AI Android app.
