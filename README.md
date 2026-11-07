# Dịch

An Android app that translates between French, English and Vietnamese, designed for an elderly
person who only reads and speaks Vietnamese. The whole interface is in Vietnamese, buttons are
large, and every translation is read aloud.

Status: camera translation, conversation mode, market phrases, history and online translation are in.

## What it does today

- **Đọc chữ** (read text): point the camera at a label or paper, press **Dịch**, and the
  Vietnamese translation appears in large text and is read aloud. Works fully offline once the
  translation data is downloaded.
- **Nói chuyện** (talk): split screen. The top half is upside down for the vendor (French or
  English, toggle in the corner); the bottom half is hers in Vietnamese. Each side presses its own
  microphone button and speaks; the translation is shown large on the other half and read aloud.
  Prices like "3,50 €" are shown in big digits. Ready-made market phrases ("Bao nhiêu tiền?", …)
  work without speaking.
- **Đã dịch** (history): the last 50 translations, each with a read-aloud button.
- Family settings: press and hold the faint gear icon on the home screen for 3 seconds. Turn automatic
  read-aloud on or off, choose the Vietnamese voice, reading speed, text size, and download the offline translation data.

## Better translation online (Google Cloud Translation)

Offline translation (ML Kit) is understandable but often wrong. When the phone is online and a
Google Cloud API key is set, the app uses Google Cloud Translation instead and falls back to
offline automatically. The first 500,000 characters each month are free (a $10 monthly credit;
see [pricing](https://cloud.google.com/translate/pricing)), far more than one person uses.

1. In [Google Cloud Console](https://console.cloud.google.com/), create a project and attach a
   billing account (required even for the free allowance).
2. Enable the **Cloud Translation API**.
3. APIs & Services > Credentials > **Create credentials > API key**. Restrict it to the Cloud
   Translation API. Optionally restrict it to Android apps: package `com.bunyu.dich` and the
   SHA-1 of the signing key (`gradlew.bat signingReport`).
4. Either build the key into the APK, so nothing has to be typed on the phone: add this line to
   `local.properties` (next to `sdk.dir`; that file is never committed) and rebuild:

   ```
   google.translate.apiKey=AIza...your key...
   ```

   Or, on her phone: family settings > **Dịch trực tuyến (Google)** > paste the key. A key typed on
   the phone takes priority over the built-in one. Press **Thử dịch trực tuyến** to check it.

   A key built into the APK can be extracted from it, so restrict it to the Cloud Translation API
   (and ideally to the app's package and SHA-1) and do not share the APK publicly.

The key is stored only on the phone, never in this repository. The result screen shows
"trực tuyến" or "ngoại tuyến" so you can tell which engine was used.

## Build the APK (Windows)

Requirements: JDK 17 to 25 and the Android SDK (installing Android Studio gives you both).

### Signing key (once)

Android only installs a new version over the old one when both are signed with the same key.
Create one key on the build PC, keep it outside the repo, and back it up (if it is lost, the app
must be uninstalled once before the next install):

```bat
mkdir D:\work\dich-keys
"D:\AndroidStudio\jbr\bin\keytool" -genkeypair -v -keystore D:\work\dich-keys\dich-release.jks ^
  -alias dich -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Dich"
```

Then add to `D:\work\dich\local.properties` (never committed):

```properties
dich.storeFile=D:/work/dich-keys/dich-release.jks
dich.storePassword=...
dich.keyAlias=dich
dich.keyPassword=...
```

### Build

```bat
cd D:\work\dich
gradlew.bat assembleRelease
```

Install `app\build\outputs\apk\release\app-release.apk` on her phone. It only contains the
64-bit ARM code her Redmi Note 14 needs, so it is much smaller than the debug APK. Always install
APKs built on this PC: the GitHub Actions builds are signed with a different key and will not
install over it.

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
TextToSpeech. Everything runs on the phone, except the optional Google Cloud Translation call.
