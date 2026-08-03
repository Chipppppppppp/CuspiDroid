<p align="center">
  <img src="docs/icon.svg" alt="CuspiDroid icon" width="128" height="128">
</p>

<h1 align="center">CuspiDroid</h1>

<p align="center">
  <a href="LICENSE">
    <img alt="License: GPL v3" src="https://img.shields.io/badge/License-GPLv3-0F766E?style=for-the-badge">
  </a>
</p>

[Japanese README](README.md)

CuspiDroid is an Android app for browsing and posting to 5ch and compatible BBS sites with a browser-like interface. It supports Android 6.0 and later.

## Features

- Multiple tabs, bookmarks, histories, unread tracking, and saved reading positions
- Native board and thread views for 5ch and custom BBS sites, pull-to-refresh, and list sorting
- URL entry, thread search, find-in-page, and next-thread search
- Posting, thread creation, BBS authentication, and name, email, and post histories
- Reply trees, reply/link/popular-post filters, categorized favorite posts, AA rendering, repeated-copy omission, and NG rules
- Image, GIF, and video previews and zoom, media blurring, and ImgBB uploads
- Built-in light and dark themes including Pure White and Pure Dark, custom themes that can be created, shared, and backed up, layout options, and gesture settings
- Switching between native and WebView displays, plus private browsing
- Separate management of thread and media caches and histories
- Sync2ch synchronization, backup creation and restoration, and ChMate backup import

## Install

Download the APK from [Releases](https://github.com/Chipppppppppp/CuspiDroid/releases).

To use Obtainium, add this GitHub repository as the app source.

<p align="center">
  <a href="https://apps.obtainium.imranr.dev/redirect.html?r=obtainium://add/https://github.com/Chipppppppppp/CuspiDroid">
    <img src="docs/images/badge_obtainium.png"
         alt="Get it on Obtainium"
         height="80">
  </a>
</p>

## Build

Open this repository in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug
```

To build an optimized release APK:

```powershell
.\gradlew.bat assembleRelease
```

## Custom BBS List

Add all BBS links below from **Settings → BBS Links → Manage BBS links → Add all supported BBS links**.

| Name | URL |
| --- | --- |
| Machi BBS | `https://machi.to/bbsmenu.html` |
| Shitaraba | `https://bbs-menu.pages.dev/shitaraba_bbsmenu/bbsmenu.json` |
| Futaba Channel | `https://www.2chan.net/bbsmenu.html` |
| BBSPINK | `https://bbspink.org/ex0ch/bbsmenu.html` |
| Open2ch | `https://menu.open2ch.net/bbsmenu.html` |
| Edge | `https://bbs.eddibb.cc/liveedge/` |
| AfternoonTea | `https://afternoontea.st/boards/bbsmenu.html` |

## License

CuspiDroid is distributed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for details.
