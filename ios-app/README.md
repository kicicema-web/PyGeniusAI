# PyGenius AI - iOS Edition

A native iOS app for PyGenius AI built with SwiftUI.

## Features

- 📝 **Code Editor** - Write Python code with syntax highlighting
- 💻 **Console** - View output from code execution
- 🤖 **AI Tutor** - Ask questions about Python programming
- 💡 **Code Explanation** - Get AI-powered explanations
- 🐛 **Bug Detection** - Find and fix issues
- ⚡ **Code Optimization** - Get improvement suggestions
- 🎨 **Modern UI** - Native iOS design with SwiftUI

## Requirements

- iOS 15.0 or later
- iPhone or iPad
- Internet connection (for AI features)
- Xcode 14+ (for building)

## Project Structure

```
PyGeniusAI/
├── PyGeniusAI/
│   ├── PyGeniusAIApp.swift      # App entry point
│   ├── Info.plist               # App configuration
│   ├── Models/
│   │   └── Models.swift         # Data models
│   ├── Services/
│   │   └── OpenRouterService.swift  # AI API integration
│   ├── ViewModels/
│   │   └── PyGeniusViewModel.swift  # Business logic
│   ├── Views/
│   │   ├── ContentView.swift    # Main tab view
│   │   ├── EditorView.swift     # Code editor
│   │   ├── ConsoleView.swift    # Console output
│   │   ├── AITutorView.swift    # AI chat interface
│   │   └── SettingsView.swift   # Settings screen
│   └── Utils/
│       └── Theme.swift          # UI theming
└── PyGeniusAI.xcodeproj/        # Xcode project
```

## Building on macOS

### Prerequisites

1. macOS 12.0 (Monterey) or later
2. Xcode 14.0 or later
3. Apple Developer account (for device testing)

### Build Steps

1. **Open the project in Xcode:**
   ```bash
   open PyGeniusAI.xcodeproj
   ```

2. **Configure signing:**
   - Select the PyGeniusAI project in Xcode
   - Go to Signing & Capabilities
   - Select your development team
   - Change bundle identifier if needed

3. **Build and run:**
   - Select your target device (iPhone/iPad simulator or physical device)
   - Press Cmd+R to build and run

### Build IPA for Distribution

#### Method 1: Using Xcode

1. Select Product → Archive
2. Wait for archive to complete
3. Click "Distribute App"
4. Select "Ad Hoc" or "App Store Connect"
5. Follow the prompts to export IPA

#### Method 2: Using Command Line

```bash
# Navigate to project directory
cd PyGeniusAI

# Build archive
xcodebuild archive \
  -project PyGeniusAI.xcodeproj \
  -scheme PyGeniusAI \
  -destination 'generic/platform=iOS' \
  -archivePath build/PyGeniusAI.xcarchive

# Export IPA
xcodebuild -exportArchive \
  -archivePath build/PyGeniusAI.xcarchive \
  -exportOptionsPlist exportOptions.plist \
  -exportPath build/IPA
```

Create `exportOptions.plist`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>ad-hoc</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>uploadSymbols</key>
    <true/>
</dict>
</plist>
```

## AI Features

All AI features are powered by OpenRouter (GPT-3.5):
- Pre-configured API key - no setup needed!
- Works immediately after installation

## Installation on iPhone/iPad

### Method 1: App Store (Recommended)
*Coming soon - pending App Store review*

### Method 2: Sideloading (Developer/Enterprise)

1. Build the IPA as described above
2. Install using one of these tools:
   - **AltStore** (Free, no jailbreak needed)
   - **Apple Configurator 2**
   - **Xcode** (for development devices)

### Method 3: TestFlight (Beta Testing)

1. Build and upload to App Store Connect
2. Add testers via TestFlight
3. Testers receive email invitation

## Troubleshooting

### "Unable to find a destination matching the provided destination specifier"
Make sure you have the iOS Simulator installed or a physical device connected.

### "No signing certificate found"
You need to configure code signing in Xcode. Go to Project Settings → Signing & Capabilities.

### Build errors related to Swift version
Make sure you're using Xcode 14 or later with Swift 5.7+.

## Differences from Android/Desktop Versions

Due to iOS platform restrictions:
- **Python Execution**: Limited in-app Python execution (requires server or embedded Python)
- **File System**: Uses iOS document picker for file operations
- **Keyboard**: iOS software keyboard optimized for coding

## License

Same as PyGenius AI Android/Desktop apps.
