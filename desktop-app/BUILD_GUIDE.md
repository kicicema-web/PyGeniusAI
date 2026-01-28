# PyGenius AI - Build Guide

This guide explains how to build distributable packages for PyGenius AI on all platforms.

## 🚀 Quick Start

### Available Build Scripts

| Script | Purpose | Platform |
|--------|---------|----------|
| `build_windows.py` | Windows .exe executable | Windows |
| `build_deb.py` | Debian/Ubuntu .deb package | Linux |
| `build_appimage.py` | Linux AppImage (universal) | Linux |
| `build_all.py` | Build all packages | All |

---

## 🪟 Windows Build

### Requirements
- Windows 10/11
- Python 3.8 or higher

### Build Steps

```bash
# Install dependencies
pip install pyinstaller requests pillow

# Build executable
python build_windows.py
```

Output: `release/windows/PyGeniusAI.exe`

---

## 🐧 Linux Build

### Requirements
- Ubuntu/Debian or compatible
- Python 3.8 or higher
- `dpkg-dev` and `fakeroot` for .deb package

### Build Steps

```bash
# Install dependencies
sudo apt update
sudo apt install -y python3 python3-pip python3-tk python3-requests dpkg-dev fakeroot libfuse2

# Build .deb package
python3 build_deb.py

# Build AppImage
python3 build_appimage.py

# Or build all
python3 build_all.py
```

Output:
- `release/linux/pygenius-ai_1.0.0_all.deb`
- `release/linux/PyGeniusAI-x86_64.AppImage`

### Install .deb
```bash
sudo dpkg -i pygenius-ai_1.0.0_all.deb
sudo apt-get install -f  # Fix dependencies if needed
```

### Run AppImage
```bash
chmod +x PyGeniusAI-x86_64.AppImage
./PyGeniusAI-x86_64.AppImage
```

---

## 📦 Automated GitHub Releases

The repository includes GitHub Actions workflows for automated building and releasing.

### Setup

1. Push a version tag:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

2. GitHub Actions will automatically:
   - Build Windows executable
   - Build Linux .deb and AppImage
   - Create a GitHub release with all artifacts

---

## 🐳 Flathub Submission

### Prerequisites

```bash
# Install Flatpak and builder
sudo apt install flatpak flatpak-builder

# Add Flathub repository
flatpak remote-add --user --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo
```

### Manual Submission

1. Fork [flathub/flathub](https://github.com/flathub/flathub)
2. Create a new branch: `ai.pygenius.desktop`
3. Copy files from `flathub/` directory:
   - `ai.pygenius.yml`
   - `ai.pygenius.desktop`
   - `ai.pygenius.appdata.xml`
4. Submit a pull request

### Automated Submission (requires token)

```bash
# Set your GitHub token
export GITHUB_TOKEN="your-github-token"

# Run submission script
cd flathub
python3 submit.py
```

---

## 🔧 Development

### Project Structure

```
.
├── pygenius_desktop.py      # Main application
├── pygenius                  # Linux launcher
├── run.sh                    # Quick launcher
├── install.sh                # Linux installer
├── build_*.py                # Build scripts
├── .github/workflows/        # GitHub Actions
│   └── release.yml
└── flathub/                  # Flathub manifest files
    ├── ai.pygenius.yml
    ├── ai.pygenius.desktop
    ├── ai.pygenius.appdata.xml
    └── submit.py
```

### Testing

```bash
# Run directly with Python
python3 pygenius_desktop.py
```

---

## 📋 Release Checklist

- [ ] Update version number in all build scripts
- [ ] Update CHANGELOG.md
- [ ] Test all builds locally
- [ ] Create and push git tag
- [ ] Verify GitHub Actions completes successfully
- [ ] Download and test release artifacts
- [ ] Submit to Flathub (if applicable)
- [ ] Update website/download links

---

## 🆘 Troubleshooting

### Windows Build Issues

**Issue**: `pyinstaller` not found
**Fix**: `pip install pyinstaller`

**Issue**: Missing `tcl/tk`
**Fix**: Install Python with tkinter support from python.org

### Linux Build Issues

**Issue**: `dpkg-deb` not found
**Fix**: `sudo apt install dpkg-dev`

**Issue**: AppImage doesn't run
**Fix**: `sudo apt install libfuse2`

### GitHub Actions Issues

**Issue**: Release workflow not triggered
**Fix**: Ensure you push a tag with `v` prefix (e.g., `v1.0.0`)

---

## 📚 References

- [PyInstaller Documentation](https://pyinstaller.org/)
- [Flatpak Builder Documentation](https://docs.flatpak.org/)
- [Flathub App Submission](https://github.com/flathub/flathub/wiki/App-Submission)
- [GitHub Actions Documentation](https://docs.github.com/actions)
