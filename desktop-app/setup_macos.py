#!/usr/bin/env python3
"""
Setup script to create macOS .app bundle for PyGenius AI
Run this on a Mac to build the application
"""

import os
import sys
import subprocess

def check_py2app():
    """Check if py2app is installed"""
    try:
        import py2app
        return True
    except ImportError:
        return False

def install_py2app():
    """Install py2app"""
    print("Installing py2app...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "py2app", "-q"])
    print("py2app installed!")

def create_setup_py():
    """Create setup.py for py2app"""
    setup_content = '''from setuptools import setup

APP = ['pygenius_desktop.py']
DATA_FILES = []
OPTIONS = {
    'argv_emulation': True,
    'packages': ['requests'],
    'includes': ['tkinter', 'threading', 'json', 'os', 'sys'],
    'excludes': ['matplotlib', 'numpy', 'pandas', 'PyQt5', 'PyQt6', 'PySide2', 'PySide6'],
    'plist': {
        'CFBundleName': 'PyGenius AI',
        'CFBundleDisplayName': 'PyGenius AI',
        'CFBundleGetInfoString': "PyGenius AI Desktop",
        'CFBundleIdentifier': "com.pygeniusai.desktop",
        'CFBundleVersion': "1.0.1",
        'CFBundleShortVersionString': "1.0.1",
        'NSHumanReadableCopyright': "Copyright 2026, PyGenius AI",
        'LSMinimumSystemVersion': "10.13",
        'NSHighResolutionCapable': True,
    }
}

setup(
    name='PyGenius AI',
    app=APP,
    data_files=DATA_FILES,
    options={'py2app': OPTIONS},
    setup_requires=['py2app'],
)
'''
    with open('setup_mac.py', 'w') as f:
        f.write(setup_content)
    print("Created setup_mac.py")

def build_app():
    """Build the macOS app"""
    print("\nBuilding PyGenius AI for macOS...")
    print("This may take a few minutes...\n")
    
    # Clean previous builds
    if os.path.exists('build'):
        import shutil
        shutil.rmtree('build')
    if os.path.exists('dist'):
        import shutil
        shutil.rmtree('dist')
    
    # Build the app
    subprocess.check_call([
        sys.executable,
        'setup_mac.py',
        'py2app',
        '--packages', 'requests'
    ])
    
    print("\n" + "="*50)
    print("Build complete!")
    print("="*50)
    print("\nYour app is located at:")
    print("  dist/PyGenius AI.app")
    print("\nTo install:")
    print("  1. Open the 'dist' folder")
    print("  2. Drag 'PyGenius AI.app' to your Applications folder")
    print("  3. Double-click to launch!")
    print("\nNote: On first launch, you may need to right-click and select 'Open'")
    print("      to bypass Gatekeeper security.")

def main():
    print("="*50)
    print("PyGenius AI - macOS App Builder")
    print("="*50)
    print()
    
    # Check if running on macOS
    if sys.platform != 'darwin':
        print("Warning: This script is designed to run on macOS.")
        print(f"Current platform: {sys.platform}")
        response = input("Continue anyway? (y/N): ")
        if response.lower() != 'y':
            sys.exit(1)
    
    # Check/install py2app
    if not check_py2app():
        install_py2app()
    else:
        print("py2app is already installed")
    
    # Create setup file
    create_setup_py()
    
    # Build
    try:
        build_app()
    except Exception as e:
        print(f"\nError during build: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()
