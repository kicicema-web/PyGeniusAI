#!/usr/bin/env python3
"""
Master build script - builds all packages
Works on any Linux distribution including Alpine
"""

import subprocess
import sys
import os
import shutil
import platform

def run_build(script_name):
    """Run a build script"""
    print(f"\n{'='*60}")
    print(f"Running {script_name}...")
    print('='*60)
    result = subprocess.run([sys.executable, script_name])
    return result.returncode == 0

def detect_platform():
    """Detect the current platform"""
    system = platform.system()
    machine = platform.machine()
    
    # Detect Linux distro
    distro = "unknown"
    if system == "Linux":
        if os.path.exists("/etc/alpine-release"):
            distro = "alpine"
        elif os.path.exists("/etc/debian_version"):
            distro = "debian"
        elif os.path.exists("/etc/arch-release"):
            distro = "arch"
        elif os.path.exists("/etc/fedora-release"):
            distro = "fedora"
    
    return system, machine, distro

def main():
    """Build all packages"""
    
    system, machine, distro = detect_platform()
    
    print("""
╔══════════════════════════════════════════════════════════════╗
║                PyGenius AI - Build All                       ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
""")
    
    print(f"Platform: {system} {machine}")
    if distro != "unknown":
        print(f"Distribution: {distro}")
    print("")
    
    # Clean release directory
    if os.path.exists("release"):
        shutil.rmtree("release")
    os.makedirs("release", exist_ok=True)
    
    results = {}
    
    # Build Windows executable (only on Windows or with BUILD_WINDOWS env var)
    if system == "Windows" or os.environ.get("BUILD_WINDOWS"):
        results["Windows .exe"] = run_build("build_windows.py")
    else:
        print("\n[SKIP] Windows executable - requires Windows")
        print("       Set BUILD_WINDOWS=1 to force (if using Wine)")
        results["Windows .exe"] = None
    
    # Build Debian package (on Linux)
    if system == "Linux":
        results["Linux .deb"] = run_build("build_deb.py")
    else:
        print("\n[SKIP] Debian package - requires Linux")
        results["Linux .deb"] = None
    
    # Build AppImage (on Linux)
    if system == "Linux":
        results["Linux AppImage"] = run_build("build_appimage.py")
    else:
        print("\n[SKIP] AppImage - requires Linux")
        results["Linux AppImage"] = None
    
    # Print summary
    print("\n" + "="*60)
    print("BUILD SUMMARY")
    print("="*60)
    
    for name, result in results.items():
        if result is True:
            status = "✓ SUCCESS"
        elif result is False:
            status = "✗ FAILED"
        else:
            status = "⊘ SKIPPED"
        print(f"  {name:25} {status}")
    
    print("\n" + "="*60)
    print("RELEASE FILES")
    print("="*60)
    
    release_dir = "release"
    if os.path.exists(release_dir):
        found_files = False
        for root, dirs, files in os.walk(release_dir):
            level = root.replace(release_dir, '').count(os.sep)
            indent = '  ' * level
            print(f'{indent}{os.path.basename(root)}/')
            subindent = '  ' * (level + 1)
            for file in files:
                filepath = os.path.join(root, file)
                size = os.path.getsize(filepath)
                size_str = f"{size/1024/1024:.1f} MB" if size > 1024*1024 else f"{size/1024:.1f} KB"
                print(f'{subindent}{file} ({size_str})')
                found_files = True
        
        if not found_files:
            print("  (no files built)")
    
    print("\n" + "="*60)
    
    # Check if any builds succeeded
    successful = sum(1 for r in results.values() if r is True)
    if successful > 0:
        print(f"Build complete! {successful} package(s) built successfully.")
    else:
        print("No packages were built.")
        print("\nTroubleshooting:")
        if distro == "alpine":
            print("  For Alpine Linux, install: sudo apk add squashfs-tools")
        print("  Run individual build scripts for more details:")
        print("    python3 build_deb.py")
        print("    python3 build_appimage.py")
    
    print("="*60)
    
    return successful > 0

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
