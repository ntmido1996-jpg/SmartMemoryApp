# Smart Memory Assistant

**Smart Memory Assistant** is an Android application designed for engineers and busy professionals who suffer from quick forgetfulness. The app allows for quick voice recording of tasks and setting precise alarms with minimal taps to ensure tasks are not forgotten amidst work pressure.

## Features

### 1. Voice Recording
- Large, prominent button for voice recording
- Converts speech to text instantly using Android's built-in SpeechRecognizer
- Works offline without requiring internet connection

### 2. Smart Alarm System
- **Quick Buttons**: Set alarms with one tap (15 min, 30 min, 1 hour, 2 hours)
- **Custom Time**: Open date/time picker for precise scheduling
- **Repetition**: Option to enable daily repetition at the same time

### 3. Custom Audio & Alarm Behavior
- Upload custom audio files (MP3/WAV) from device
- Volume control slider within the app
- Play/Pause button to manually control audio playback
- Alarm sound loops until user dismisses it

### 4. Full-Screen Alarm Overlay
- When alarm time arrives, a full-screen overlay appears covering the app
- Displays task name in large text
- **Dismiss button**: Stop sound and close alarm
- **Snooze buttons**: Multiple options (5 min, 10 min, 15 min) to temporarily postpone alarm

### 5. Task Management
- Edit existing tasks
- Modify note text
- Extend time with quick buttons (+5 min, +15 min)
- Reset alarm time completely

## Technical Specifications

### Permissions Required
- `RECORD_AUDIO` - For voice recording
- `WAKE_LOCK` - To keep device awake during alarms
- `FOREGROUND_SERVICE` - For alarm service
- `POST_NOTIFICATIONS` - For alarm notifications
- `READ_EXTERNAL_STORAGE` & `WRITE_EXTERNAL_STORAGE` - For custom audio files
- `SYSTEM_ALERT_WINDOW` - For full-screen overlay (requires manual permission)
- `SCHEDULE_EXACT_ALARM` - For precise alarm scheduling

### Key Technologies
- **Native Kotlin** - Modern Android development language
- **AlarmManager** - Ensures alarms work in Sleep Mode with 100% accuracy
- **SpeechRecognizer** - Built-in Android speech-to-text (offline capable)
- **BroadcastReceiver** - Handles alarm triggers
- **Service** - Background alarm processing
- **Overlay** - Full-screen alarm display

### Offline Capability
- Voice recognition works offline using Android's built-in SpeechRecognizer
- No external APIs or internet required for core functionality

## Installation

### Build Requirements
- Java 17
- Android SDK 34
- Gradle 8.2.0
- Kotlin 1.9.0

### Build Process
The project includes a GitHub Actions workflow (`android_build.yml`) that automatically builds a Debug APK every time code is pushed and makes the APK available as an artifact.

## Usage

1. **Request Overlay Permission**: On first launch, the app will prompt you to grant SYSTEM_ALERT_WINDOW permission for the full-screen alarm overlay.
2. **Record Task**: Tap the microphone button to record your task voice note.
3. **Set Alarm**: Choose from quick buttons or open custom time picker.
4. **Custom Audio (Optional)**: Select an audio file from your device for custom alarm sound.
5. **Save**: Tap "Save Task" to schedule the alarm.
6. **Alarm Trigger**: When alarm time arrives, a full-screen overlay will appear with the task and alarm controls.

## Architecture

```
SmartMemoryApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/smartmemoryapp/
│   │       │       ├── activities/
│   │       │       │   ├── MainActivity.kt
│   │       │       │   ├── DateTimePickerActivity.kt
│   │       │       │   └── AlarmOverlayActivity.kt
│   │       │       ├── services/
│   │       │       │   ├── AlarmService.kt
│   │       │       │   ├── AlarmReceiver.kt
│   │       │       │   └── BootReceiver.kt
│   │       │       └── utils/
│   │       │           └── AlarmUtils.kt
│   │       └── res/
│   │           ├── layout/
│   │           ├── values/
│   │           └── drawable/
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── README.md
```

## GitHub Actions Workflow

The project includes `.github/workflows/android_build.yml` which:
1. Sets up JDK 17
2. Grants execute permissions to gradlew
3. Builds the Debug APK
4. Uploads the APK as a workflow artifact

## License

This project is for personal use and educational purposes.

## Support

For issues or questions, please open an issue in the GitHub repository.
