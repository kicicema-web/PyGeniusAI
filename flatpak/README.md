# Flathub Submission for PyGenius AI

This directory contains the Flatpak manifest and metadata for PyGenius AI.

## Files

- `com.pygeniusai.desktop.yaml` - Flatpak manifest
- `com.pygeniusai.desktop.desktop` - Desktop entry
- `com.pygeniusai.desktop.metainfo.xml` - AppStream metadata
- `com.pygeniusai.desktop.svg` - Application icon
- `pygenius` - Launcher script

## Building Locally

```bash
# Install flatpak and flatpak-builder
sudo apt install flatpak flatpak-builder

# Add Flathub repository
flatpak remote-add --user --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo

# Build the app
flatpak-builder --force-clean build-dir com.pygeniusai.desktop.yaml

# Test the app
flatpak-builder --run build-dir com.pygeniusai.desktop.yaml pygenius

# Create a bundle
flatpak-builder --repo=repo --force-clean build-dir com.pygeniusai.desktop.yaml
flatpak build-bundle repo pygenius.flatpak com.pygeniusai.desktop

# Install the bundle
flatpak install pygenius.flatpak
```

## Submitting to Flathub

### Manual Submission

1. Fork [flathub/flathub](https://github.com/flathub/flathub)
2. Create a new branch: `com.pygeniusai.desktop`
3. Copy these files to the branch:
   - `com.pygeniusai.desktop.yaml`
   - `com.pygeniusai.desktop.desktop`
   - `com.pygeniusai.desktop.metainfo.xml`
   - `com.pygeniusai.desktop.svg`
   - `pygenius`
4. Submit a pull request

### References

- [Flathub App Submission](https://github.com/flathub/flathub/wiki/App-Submission)
- [Flatpak Manifest Documentation](https://docs.flatpak.org/en/latest/manifests.html)
- [AppStream Metadata](https://www.freedesktop.org/software/appstream/docs/)
