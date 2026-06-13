# CuspiDroid

[日本語 README](README.md)

CuspiDroid is an Android app for browsing 5ch-compatible BBS sites with a browser-like interface.

## Features

- Multiple browsing tabs
- Address bar for both URL entry and search
- Thread search through `find.5ch.io`
- Native rendering for 5ch-compatible thread URLs
- WebView handoff for authentication and web posting on the current thread
- Imgur previews, blur protection for graphic images, and full-screen zoom
- Settings for BBS links, history, image blur, and more

## Build

Open this repository in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug
```

## Install

If a device is connected through adb, install with:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## License

CuspiDroid is distributed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for details.
