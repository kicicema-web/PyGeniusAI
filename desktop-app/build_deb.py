#!/usr/bin/env python3
"""
Build script for Debian (.deb) package
Works on any Linux system, including Alpine
"""

import subprocess
import sys
import os
import shutil
import stat
import tarfile
import gzip
import io

def check_command(cmd):
    """Check if a command exists"""
    result = subprocess.run(["which", cmd], capture_output=True)
    return result.returncode == 0

def create_deb_ar(files, output_path):
    """Create a .deb package using ar format without dpkg-deb"""
    
    # AR format: !<arch> followed by entries
    # Each entry: name/16 + timestamp/12 + owner/6 + group/6 + mode/8 + size/10 + `\n60 + content
    
    ar_header = b"!<arch>\n"
    
    def make_ar_entry(name, content):
        timestamp = b"0           "  # 12 chars
        owner = b"0     "  # 6 chars
        group = b"0     "  # 6 chars
        mode = b"100644  "  # 8 chars
        size = f"{len(content)}".encode().ljust(10)  # 10 chars
        end = b"`\n"
        
        header = name.encode().ljust(16) + timestamp + owner + group + mode + size + end
        # Padding to 2-byte boundary
        if len(content) % 2 == 1:
            content = content + b"\n"
        return header + content
    
    debian_binary = b"2.0\n"
    
    # Build control.tar.gz
    control_tar = io.BytesIO()
    with tarfile.open(fileobj=control_tar, mode="w:gz") as tar:
        for filepath, content in files['control'].items():
            info = tarfile.TarInfo(name=filepath)
            info.size = len(content)
            info.mode = 0o644
            if filepath in ['postinst', 'prerm']:
                info.mode = 0o755
            tar.addfile(info, io.BytesIO(content.encode() if isinstance(content, str) else content))
    control_tar_bytes = control_tar.getvalue()
    
    # Build data.tar.gz
    data_tar = io.BytesIO()
    with tarfile.open(fileobj=data_tar, mode="w:gz") as tar:
        for filepath, content in files['data'].items():
            info = tarfile.TarInfo(name=filepath)
            if isinstance(content, str):
                content = content.encode()
            info.size = len(content)
            info.mode = 0o644
            if filepath.startswith('./usr/bin/'):
                info.mode = 0o755
            tar.addfile(info, io.BytesIO(content))
    data_tar_bytes = data_tar.getvalue()
    
    # Create .deb file
    with open(output_path, 'wb') as f:
        f.write(ar_header)
        f.write(make_ar_entry("debian-binary", debian_binary))
        f.write(make_ar_entry("control.tar.gz", control_tar_bytes))
        f.write(make_ar_entry("data.tar.gz", data_tar_bytes))

def build_deb_package():
    """Build Debian package"""
    
    package_name = "pygenius-ai"
    version = "1.0.0"
    arch = "all"
    
    print("Building Debian package...")
    
    # Prepare files
    control_files = {}
    data_files = {}
    
    # Control file
    control_files['control'] = f"""Package: pygenius-ai
Version: {version}
Section: devel
Priority: optional
Architecture: {arch}
Depends: python3 (>= 3.8), python3-requests, python3-tk
Maintainer: Kicicema Web <kicicema.web@gmail.com>
Description: PyGenius AI - Python coding assistant
 PyGenius AI is a desktop application for Python coding with AI-powered features.
 Features include:
  - Code editor with syntax highlighting
  - Built-in Python console
  - AI Tutor powered by OpenRouter
  - Bug detection and code optimization
Homepage: https://github.com/kicicema-web/pygenius
"""
    
    # Postinst script
    control_files['postinst'] = """#!/bin/bash
set -e

# Create icon symlink if needed
if [ ! -f /usr/share/icons/hicolor/256x256/apps/pygenius.png ]; then
    ln -sf /usr/share/icons/hicolor/scalable/apps/applications-python.svg /usr/share/icons/hicolor/256x256/apps/pygenius.png 2>/dev/null || true
fi

# Update desktop database
if command -v update-desktop-database >/dev/null 2>&1; then
    update-desktop-database /usr/share/applications
fi

# Update icon cache
if command -v gtk-update-icon-cache >/dev/null 2>&1; then
    gtk-update-icon-cache /usr/share/icons/hicolor/ 2>/dev/null || true
fi

echo "PyGenius AI has been installed!"
echo "Run 'pygenius' from terminal or find it in your applications menu."
exit 0
"""
    
    # Prerm script
    control_files['prerm'] = """#!/bin/bash
set -e
exit 0
"""
    
    # Data files
    with open("pygenius_desktop.py", "r") as f:
        data_files['./usr/share/pygenius/pygenius_desktop.py'] = f.read()
    
    with open("pygenius", "r") as f:
        data_files['./usr/share/pygenius/pygenius'] = f.read()
    
    data_files['./usr/bin/pygenius'] = """#!/bin/bash
# PyGenius AI wrapper script
exec python3 /usr/share/pygenius/pygenius_desktop.py "$@"
"""
    
    data_files['./usr/share/applications/pygenius.desktop'] = """[Desktop Entry]
Name=PyGenius AI
Comment=Python coding assistant with AI-powered features
Exec=pygenius
Type=Application
Icon=pygenius
Categories=Development;IDE;Education;
Terminal=false
StartupNotify=true
MimeType=text/x-python;
"""
    
    data_files['./usr/share/doc/pygenius/copyright'] = """Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
Upstream-Name: PyGenius AI
Source: https://github.com/kicicema-web/pygenius

Files: *
Copyright: 2024 Kicicema Web
License: MIT
"""
    
    # Create release directory
    os.makedirs("release/linux", exist_ok=True)
    deb_file = f"release/linux/{package_name}_{version}_{arch}.deb"
    
    # Build the .deb package
    try:
        if check_command("dpkg-deb"):
            # Use dpkg-deb if available
            print("Using dpkg-deb for building...")
            build_dir = "build/deb"
            if os.path.exists(build_dir):
                shutil.rmtree(build_dir)
            
            deb_root = f"{build_dir}/{package_name}_{version}_{arch}"
            os.makedirs(f"{deb_root}/DEBIAN")
            os.makedirs(f"{deb_root}/usr/share/pygenius")
            os.makedirs(f"{deb_root}/usr/share/applications")
            os.makedirs(f"{deb_root}/usr/share/doc/pygenius")
            os.makedirs(f"{deb_root}/usr/bin")
            
            # Write control files
            with open(f"{deb_root}/DEBIAN/control", "w") as f:
                f.write(control_files['control'])
            with open(f"{deb_root}/DEBIAN/postinst", "w") as f:
                f.write(control_files['postinst'])
            os.chmod(f"{deb_root}/DEBIAN/postinst", 0o755)
            with open(f"{deb_root}/DEBIAN/prerm", "w") as f:
                f.write(control_files['prerm'])
            os.chmod(f"{deb_root}/DEBIAN/prerm", 0o755)
            
            # Write data files
            shutil.copy("pygenius_desktop.py", f"{deb_root}/usr/share/pygenius/")
            shutil.copy("pygenius", f"{deb_root}/usr/share/pygenius/")
            
            with open(f"{deb_root}/usr/bin/pygenius", "w") as f:
                f.write(data_files['./usr/bin/pygenius'])
            os.chmod(f"{deb_root}/usr/bin/pygenius", 0o755)
            
            with open(f"{deb_root}/usr/share/applications/pygenius.desktop", "w") as f:
                f.write(data_files['./usr/share/applications/pygenius.desktop'])
            
            with open(f"{deb_root}/usr/share/doc/pygenius/copyright", "w") as f:
                f.write(data_files['./usr/share/doc/pygenius/copyright'])
            
            # Build
            result = subprocess.run(["dpkg-deb", "--build", deb_root, deb_file], capture_output=True, text=True)
            if result.returncode != 0:
                print(f"dpkg-deb failed: {result.stderr}")
                raise Exception("dpkg-deb failed")
        else:
            # Use pure Python implementation
            print("Using pure Python .deb builder (no dpkg-deb found)...")
            files = {'control': control_files, 'data': data_files}
            create_deb_ar(files, deb_file)
        
        print("\n" + "="*50)
        print("Debian package built successfully!")
        print("="*50)
        print(f"Package: {deb_file}")
        print(f"\nTo install: sudo dpkg -i {deb_file}")
        print("If dependencies are missing: sudo apt-get install -f")
        return True
        
    except Exception as e:
        print(f"Build failed: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = build_deb_package()
    sys.exit(0 if success else 1)
