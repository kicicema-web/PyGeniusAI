#!/usr/bin/env python3
"""
Build script for PyGenius AI Desktop executable
"""

import PyInstaller.__main__
import os
import sys

def build():
    """Build the executable using PyInstaller"""
    print("Building PyGenius AI Desktop executable...")
    
    # Create spec file content
    spec_content = '''# -*- mode: python ; coding: utf-8 -*-

block_cipher = None

a = Analysis(
    ['pygenius_desktop.py'],
    pathex=[],
    binaries=[],
    datas=[],
    hiddenimports=['requests'],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[],
    win_no_prefer_redirects=False,
    win_private_assemblies=False,
    cipher=block_cipher,
    noarchive=False,
)

pyz = PYZ(a.pure, a.zipped_data, cipher=block_cipher)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.zipfiles,
    a.datas,
    [],
    name='PyGeniusAI',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon='pygenius.ico' if os.path.exists('pygenius.ico') else None,
)
'''
    
    # Write spec file
    with open('PyGeniusAI.spec', 'w') as f:
        f.write(spec_content)
    
    # Run PyInstaller
    PyInstaller.__main__.run([
        'PyGeniusAI.spec',
        '--clean',
        '--noconfirm'
    ])
    
    print("Build complete!")
    print("Executable location: dist/PyGeniusAI.exe")

if __name__ == "__main__":
    build()
