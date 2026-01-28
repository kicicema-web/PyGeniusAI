#!/usr/bin/env python3
"""
Build script for Linux AppImage
Works on Alpine Linux and other distributions
"""

import subprocess
import sys
import os
import shutil
import urllib.request
import struct
import tempfile

def detect_distro():
    """Detect Linux distribution"""
    if os.path.exists("/etc/alpine-release"):
        return "alpine"
    if os.path.exists("/etc/debian_version"):
        return "debian"
    if os.path.exists("/etc/arch-release"):
        return "arch"
    return "unknown"

def check_command(cmd):
    """Check if a command exists"""
    result = subprocess.run(["which", cmd], capture_output=True)
    return result.returncode == 0

def create_appimage_runtime():
    """Download or create AppImage runtime"""
    runtime_path = "tools/runtime-x86_64"
    os.makedirs("tools", exist_ok=True)
    
    if os.path.exists(runtime_path):
        return runtime_path
    
    print("Downloading AppImage runtime...")
    # Try to download pre-built runtime
    urls = [
        "https://github.com/AppImage/AppImageKit/releases/download/continuous/runtime-x86_64",
        "https://github.com/AppImage/type2-runtime/releases/download/continuous/runtime-x86_64",
    ]
    
    for url in urls:
        try:
            urllib.request.urlretrieve(url, runtime_path)
            os.chmod(runtime_path, 0o755)
            print(f"Downloaded runtime from {url}")
            return runtime_path
        except Exception as e:
            print(f"Failed to download from {url}: {e}")
            continue
    
    return None

def create_appimage_manually(appdir, output_path):
    """Create AppImage manually without appimagetool"""
    
    print("Creating AppImage manually...")
    
    # Get runtime
    runtime_path = create_appimage_runtime()
    if not runtime_path:
        print("ERROR: Could not get AppImage runtime")
        return False
    
    # Create squashfs image
    sqfs_path = "build/AppDir.squashfs"
    os.makedirs("build", exist_ok=True)
    
    # Remove old squashfs
    if os.path.exists(sqfs_path):
        os.remove(sqfs_path)
    
    # Build squashfs using mksquashfs
    if check_command("mksquashfs"):
        cmd = [
            "mksquashfs", appdir, sqfs_path,
            "-root-owned", "-noappend", "-comp", "xz",
            "-Xdict-size", "100%"
        ]
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode != 0:
            print(f"mksquashfs failed: {result.stderr}")
            return False
    else:
        print("ERROR: mksquashfs not found. Install squashfs-tools.")
        return False
    
    # Combine runtime + squashfs
    with open(runtime_path, 'rb') as f:
        runtime_data = f.read()
    
    with open(sqfs_path, 'rb') as f:
        sqfs_data = f.read()
    
    # Create AppImage
    with open(output_path, 'wb') as f:
        f.write(runtime_data)
        f.write(sqfs_data)
    
    # Make executable
    os.chmod(output_path, 0o755)
    
    # Cleanup
    os.remove(sqfs_path)
    
    return True

def build_appimage():
    """Build AppImage package"""
    
    distro = detect_distro()
    print(f"Detected distribution: {distro}")
    
    # Create AppDir structure
    appdir = "build/AppDir"
    if os.path.exists(appdir):
        shutil.rmtree(appdir)
    
    dirs = [
        f"{appdir}/usr/bin",
        f"{appdir}/usr/share/pygenius",
        f"{appdir}/usr/share/applications",
        f"{appdir}/usr/share/icons/hicolor/256x256/apps",
        f"{appdir}/usr/share/metainfo",
    ]
    
    for d in dirs:
        os.makedirs(d, exist_ok=True)
    
    # Copy application files
    shutil.copy("pygenius_desktop.py", f"{appdir}/usr/share/pygenius/")
    shutil.copy("pygenius", f"{appdir}/usr/share/pygenius/")
    
    # Create AppRun script
    apprun = """#!/bin/bash
# AppRun script for PyGenius AI AppImage

# Get the directory where this script is located
HERE="$(dirname "$(readlink -f "${0}")")"

# Export Python path
export PYTHONPATH="${HERE}/usr/share/pygenius:${PYTHONPATH}"

# Run the application
exec python3 "${HERE}/usr/share/pygenius/pygenius_desktop.py" "$@"
"""
    with open(f"{appdir}/AppRun", "w") as f:
        f.write(apprun)
    os.chmod(f"{appdir}/AppRun", 0o755)
    
    # Create desktop entry
    desktop_entry = """[Desktop Entry]
Name=PyGenius AI
Exec=AppRun
Icon=pygenius
Type=Application
Categories=Development;IDE;Education;
Comment=Python coding assistant with AI-powered features
Terminal=false
StartupNotify=true
MimeType=text/x-python;
X-AppImage-Name=PyGenius AI
X-AppImage-Version=1.0.0
X-AppImage-Arch=x86_64
"""
    with open(f"{appdir}/pygenius.desktop", "w") as f:
        f.write(desktop_entry)
    shutil.copy(f"{appdir}/pygenius.desktop", f"{appdir}/usr/share/applications/")
    
    # Create icon
    try:
        # Try to create a simple icon with PIL
        try:
            from PIL import Image, ImageDraw
        except ImportError:
            print("Installing Pillow for icon creation...")
            subprocess.run([sys.executable, "-m", "pip", "install", "Pillow", "--break-system-packages"], 
                          capture_output=True)
            from PIL import Image, ImageDraw
        
        img = Image.new('RGBA', (256, 256), (30, 30, 30, 255))
        draw = ImageDraw.Draw(img)
        draw.ellipse([40, 40, 120, 120], fill=(55, 118, 171, 255))
        draw.ellipse([136, 136, 216, 216], fill=(255, 210, 63, 255))
        img.save(f"{appdir}/pygenius.png")
        shutil.copy(f"{appdir}/pygenius.png", f"{appdir}/usr/share/icons/hicolor/256x256/apps/")
    except Exception as e:
        print(f"Could not create icon: {e}")
        # Create empty placeholder
        open(f"{appdir}/pygenius.png", "w").close()
    
    # Create AppStream metadata
    appdata = """<?xml version="1.0" encoding="UTF-8"?>
<component type="desktop-application">
  <id>ai.pygenius.desktop</id>
  <metadata_license>MIT</metadata_license>
  <project_license>MIT</project_license>
  <name>PyGenius AI</name>
  <summary>Python coding assistant with AI-powered features</summary>
  <description>
    <p>
      PyGenius AI is a desktop application for Python coding with AI-powered features.
    </p>
    <p>Features include:</p>
    <ul>
      <li>Code editor with syntax highlighting and line numbers</li>
      <li>Built-in Python console for running code</li>
      <li>AI Tutor powered by OpenRouter API</li>
      <li>Bug detection and code analysis</li>
      <li>Code optimization suggestions</li>
      <li>Dark theme for comfortable coding</li>
    </ul>
  </description>
  <launchable type="desktop-id">pygenius.desktop</launchable>
  <url type="homepage">https://github.com/kicicema-web/pygenius</url>
  <developer_name>Kicicema Web</developer_name>
  <update_contact>kicicema.web@gmail.com</update_contact>
  <content_rating type="oars-1.1" />
  <releases>
    <release version="1.0.0" date="2024-01-28">
      <description>
        <p>Initial release of PyGenius AI Desktop</p>
      </description>
    </release>
  </releases>
</component>
"""
    with open(f"{appdir}/usr/share/metainfo/ai.pygenius.appdata.xml", "w") as f:
        f.write(appdata)
    
    # Create release directory
    os.makedirs("release/linux", exist_ok=True)
    appimage_file = "release/linux/PyGeniusAI-x86_64.AppImage"
    
    # Try different methods to build AppImage
    success = False
    
    # Method 1: Try appimagetool if available and compatible
    appimagetool = "tools/appimagetool-x86_64.AppImage"
    if check_command(appimagetool) or (os.path.exists(appimagetool) and os.access(appimagetool, os.X_OK)):
        print("Trying appimagetool...")
        env = os.environ.copy()
        env["ARCH"] = "x86_64"
        
        result = subprocess.run(
            [appimagetool, appdir, appimage_file],
            capture_output=True,
            text=True,
            env=env
        )
        if result.returncode == 0:
            success = True
        else:
            print(f"appimagetool failed: {result.stderr}")
    
    # Method 2: Manual creation
    if not success:
        print("Trying manual AppImage creation...")
        success = create_appimage_manually(appdir, appimage_file)
    
    if success:
        print("\n" + "="*50)
        print("AppImage built successfully!")
        print("="*50)
        print(f"AppImage: {appimage_file}")
        print(f"\nTo run: chmod +x {appimage_file} && {appimage_file}")
        return True
    else:
        print("Build failed!")
        print("\nTroubleshooting:")
        print("  - Install squashfs-tools: sudo apk add squashfs-tools")
        print("  - Or install on Debian/Ubuntu: sudo apt install squashfs-tools")
        return False

if __name__ == "__main__":
    success = build_appimage()
    sys.exit(0 if success else 1)
