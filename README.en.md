<p align="center">
  <img src="docs/icon.svg" alt="CuspiDroid icon" width="128" height="128">
</p>

<h1 align="center">CuspiDroid</h1>

<p align="center">
  <a href="https://github.com/ImranR98/Obtainium">
    <img alt="Get it on Obtainium" src="https://img.shields.io/badge/Get%20it%20on-Obtainium-2563EB?style=for-the-badge">
  </a>
  <a href="LICENSE">
    <img alt="License: GPL v3" src="https://img.shields.io/badge/License-GPLv3-0F766E?style=for-the-badge">
  </a>
</p>

[Japanese README](README.md)

CuspiDroid is an Android app for browsing 5ch-compatible BBS sites with a browser-like interface.

## Features

- Multiple browsing tabs
- Address bar for both URL entry and search
- Thread search through `find.5ch.io`
- Native rendering for 5ch-compatible thread URLs
- WebView handoff for authentication and web posting on the current thread
- Imgur previews, blur protection for graphic images, and full-screen zoom
- Settings for BBS links, history, read positions, NG rules, priority words, and bookmarks
- Dark theme and private browsing

## Install

Download the APK from [Releases](https://github.com/Chipppppppppp/CuspiDroid/releases).

To use Obtainium, add this GitHub repository as the app source.

## Build

Open this repository in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug
```

To build an optimized release APK:

```powershell
.\gradlew.bat assembleRelease
```

## License

CuspiDroid is distributed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for details.
