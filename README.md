# Dịch

An Android app that translates between French, English and Vietnamese, designed for an elderly
person who only reads and speaks Vietnamese. The whole interface is in Vietnamese, buttons are
large, and every translation is read aloud.

Status: milestone M1 (camera translation). Conversation mode comes in M2.

## What it does today

- **Đọc chữ** (read text): point the camera at a label or paper, press **Dịch**, and the
  Vietnamese translation appears in large text and is read aloud. Works fully offline once the
  translation data is downloaded.
- **Nói chuyện** (talk): placeholder until M2.
- Family settings: press and hold the faint gear icon on the home screen for 3 seconds. Choose
  the Vietnamese voice, reading speed, text size, and download the offline translation data.

## Build the APK (Windows)

Requirements: JDK 17 and the Android SDK (installing Android Studio gives you both).

```bat
cd D:\work\dich
gradlew.bat assembleDebug
```

The APK is written to `app\build\outputs\apk\debug\app-debug.apk`. GitHub Actions also builds it
on every push (see the "Build APK" workflow artifacts).

## One-time setup on her phone (Redmi Note 14)

1. Allow installing the APK from your file manager, then install it.
2. Open the app once with internet so it downloads the French, English and Vietnamese
   translation data (about 30 MB each). Check it in family settings.
3. Install Google "Speech Services by Google" if missing, and download the Vietnamese voice in
   Android settings (Text-to-speech output). Make Google the preferred engine.
4. In family settings, pick the Northern-sounding Vietnamese voice with **Nghe thử**.
5. Settings > Apps > Dịch > Battery saver: **No restrictions**. Grant the camera permission.

## Tech

Kotlin, Jetpack Compose, CameraX, ML Kit (text recognition, translation, language ID), Android
TextToSpeech. All processing runs on the phone.
