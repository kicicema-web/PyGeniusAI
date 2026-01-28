#!/usr/bin/env python3
"""
Build script for Windows executable
Can run on Windows natively or via Wine on Linux
"""

import subprocess
import sys
import os
import shutil
import platform

def is_windows():
    """Check if running on Windows"""
    return sys.platform == "win32"

def is_wine_available():
    """Check if Wine is available"""
    result = subprocess.run(["which", "wine"], capture_output=True)
    return result.returncode == 0

def install_package(package):
    """Install a Python package with fallbacks"""
    
    # Try with --break-system-packages first (for modern distros)
    for extra_arg in [["--break-system-packages"], ["--user"], []]:
        cmd = [sys.executable, "-m", "pip", "install"] + extra_arg + [package]
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode == 0:
            return True
    
    return False

def build_windows_exe():
    """Build Windows executable using PyInstaller"""
    
    # Check if we're on Windows or have Wine
    if not is_windows():
        print("WARNING: Not running on Windows.")
        print("Windows .exe can only be built on Windows or using Wine.")
        
        if is_wine_available():
            print("Wine is available. You could try:")
            print("  wine python build_windows.py")
        else:
            print("\nOptions:")
            print("  1. Run this on a Windows machine")
            print("  2. Install Wine: sudo apk add wine")
            print("  3. Use GitHub Actions (automated on push)")
        
        print("\nSkipping Windows build on non-Windows system.")
        return False
    
    # Check if PyInstaller is installed
    try:
        import PyInstaller
        print("PyInstaller already installed")
    except ImportError:
        print("Installing PyInstaller...")
        if not install_package("pyinstaller"):
            print("ERROR: Could not install PyInstaller")
            return False
    
    # Install requests
    print("Installing requests...")
    if not install_package("requests"):
        print("WARNING: Could not install requests")
    
    # Clean previous builds
    for folder in ['build', 'dist']:
        if os.path.exists(folder):
            shutil.rmtree(folder)
    
    # Build command
    cmd = [
        sys.executable, "-m", "PyInstaller",
        "--onefile",
        "--windowed",
        "--name", "PyGeniusAI",
        "--add-data", "pygenius_desktop.py;.",
        "--clean",
        "--noconfirm",
        "pygenius_desktop.py"
    ]
    
    # Add icon if available
    if os.path.exists("pygenius.ico"):
        cmd.extend(["--icon", "pygenius.ico"])
    
    print("Building Windows executable...")
    print(f"Command: {' '.join(cmd)}")
    
    result = subprocess.run(cmd)
    
    if result.returncode == 0:
        print("\n" + "="*50)
        print("Build successful!")
        print("="*50)
        print(f"Executable: dist/PyGeniusAI.exe")
        
        # Create release package
        os.makedirs("release/windows", exist_ok=True)
        shutil.copy("dist/PyGeniusAI.exe", "release/windows/")
        
        # Create README for Windows
        with open("release/windows/README.txt", "w") as f:
            f.write("""PyGenius AI - Windows Edition
==============================

To run PyGenius AI:
1. Double-click on PyGeniusAI.exe

Requirements:
- Windows 10 or later
- Internet connection (for AI features)

Features:
- Code Editor with syntax highlighting
- Built-in Python console
- AI Tutor powered by OpenRouter
- Bug detection and code optimization

Keyboard Shortcuts:
- Ctrl+N: New file
- Ctrl+O: Open file
- Ctrl+S: Save file
- F5: Run code

Support: https://github.com/kicicema-web/pygenius
""")
        
        print(f"\nRelease package created in: release/windows/")
        return True
    else:
        print("Build failed!")
        return False

if __name__ == "__main__":
    success = build_windows_exe()
    sys.exit(0 if success else 1)
