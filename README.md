# PyGenius AI - Android Python IDE
any qusetions? lokofibn@gmail.com
An AI-powered Python development environment for Android, combining a full-featured editor with an intelligent tutoring system.

## Features

### 🐍 Python Runtime
- Full Python 3.11 runtime via Chaquopy
- NumPy, Pandas, Matplotlib support
- Package management with pip
- Offline execution

### 🤖 AI Integration
- On-device AI with TensorFlow Lite
- Code completion and suggestions
- Bug prediction and error explanation
- Voice-to-code functionality
- Interactive Python tutor

### 📝 Code Editor
- Syntax highlighting for Python
- Line numbers
- Real-time bug detection
- File management

### 🎓 Learning Mode
- Interactive Python lessons
- Beginner to Advanced levels
- Hints and solutions
- Progress tracking

### 📊 Console
- Real-time output
- Error highlighting
- AI annotations
- Execution control

## Architecture

```
PyGeniusAI/
├── app/src/main/
│   ├── java/com/pygeniusai/
│   │   ├── MainActivity.kt           # Main UI
│   │   ├── PyGeniusApplication.kt    # App initialization
│   │   ├── ai/                       # AI engine
│   │   ├── python/                   # Python runtime
│   │   ├── data/                     # Data persistence
│   │   ├── ui/                       # UI components
│   │   ├── widget/                   # Home screen widget
│   │   └── service/                  # Background services
│   ├── python/                       # Python runtime scripts
│   └── res/                          # Android resources
└── build.gradle.kts                  # Build configuration
```

## Building

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Build Steps

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run

```bash
./gradlew assembleDebug
```

## Permissions

- `INTERNET` - For AI model download (optional cloud features)
- `RECORD_AUDIO` - For voice coding
- `WRITE_EXTERNAL_STORAGE` - For saving scripts (Android 9 and below)

## License

MIT License - See LICENSE file
