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
2. Extract `PyGeniusAI-Desktop-Windows.zip`
3. Double-click `run_windows.bat`

#### Option 2: Build Executable (Advanced)
```bash
pip install pyinstaller requests
pyinstaller --onefile --windowed pygenius_desktop.py
```
The executable will be in `dist/PyGeniusAI.exe`

### Linux

#### Option 1: Pre-built Executable
1. Download `PyGeniusAI-Desktop-Linux.zip`
2. Extract: `unzip PyGeniusAI-Desktop-Linux.zip`
3. Run: `./PyGeniusAI`

#### Option 2: Run with Python
```bash
pip3 install requests
python3 pygenius_desktop.py
```

### macOS

#### Option 1: Run with Python (Easiest)
1. Make sure you have Python 3.8+ installed
2. Download `PyGeniusAI-Desktop-macOS.zip`
3. Extract and run: `./run_macos.sh`

#### Option 2: Build macOS App (Recommended)
```bash
python3 setup_macos.py
```
Then drag `dist/PyGenius AI.app` to your Applications folder.

See [README_MACOS.md](README_MACOS.md) for detailed macOS instructions.

## Requirements

- Python 3.8 or higher (for running with Python)
- Internet connection (for AI features)
- `requests` library (auto-installed)

## AI Features

All AI features are powered by OpenRouter (GPT-3.5):
- No API key setup required - pre-configured!
- Just install and start using AI features immediately

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl/Cmd+N | New file |
| Ctrl/Cmd+O | Open file |
| Ctrl/Cmd+S | Save file |
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

### macOS App Bundle
```bash
python3 setup_macos.py
```

See [README_MACOS.md](README_MACOS.md) for macOS-specific build instructions.

## Files

- `pygenius_desktop.py` - Main application
- `run_windows.bat` - Windows launcher
- `run_macos.sh` - macOS launcher
- `setup_macos.py` - macOS app builder
- `create_dmg.sh` - macOS DMG creator
- `README_MACOS.md` - macOS-specific instructions

## License

Same as PyGenius AI Android app.
