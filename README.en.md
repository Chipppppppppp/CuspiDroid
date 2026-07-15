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

After jumping to a thread's bottom from the title bar, you can immediately scroll upward.

Posting preserves the read position and does not resubmit an already successful post during confirmation.

## Features

- Browser-like navigation with multiple tabs, search-field autofocus on new tabs, a tab overview, bookmark folders, and history
- Lightweight tab metadata is stored separately from page caches so the tab overview appears quickly after a cold start; page caches load lazily for the selected tab, while bookmark folders and unread state are prepared in the background
- Combined URL and search address bar with thread search through `find.5ch.io` or a custom URL
- Next-thread search with an editable query that narrows candidates from the current board using title similarity and thread-number continuity, displayed like the board list
- A shared dialog design for long-pressed posts, media/link/popular-post filters, and next-thread candidates, with matching animations, gray borders, background dimming, and tap-outside dismissal
- Native board and thread views for 5ch and custom BBS sites, with pull-to-refresh
- Saved scroll/read positions, first-unread jumps, unread coloring, list metadata, and sorting
- Tree view, reply/link/popular-post filters with an editable popularity threshold, AA rendering, repeated-copy omission, distinct colored left bars for your posts and replies to them, and board-scoped NGWord/NGName/NGID rules
- In-app posting with thread creation from the board title bar on 5ch, Edge, and other supported boards, using the regular posting UI for the initial message and automatically opening the created thread in a new tab; plus name/mail history, personal post history, and WebView integration for BBS authentication
- Shared image, GIF, and video previews scaled by their longest edge to fit fully inside dedicated square cells, with full-screen zoom, graphic-image blur detection, ImgBB uploads, upload-history media scaled with the same aspect-preserving behavior, and delete-confirmation dialogs consistent with the app's other popups
- Light, dark, and system themes; top or bottom address bar; configurable buttons and swipe gestures
- A concise settings overview ordered from everyday appearance, reading, and media options through connectivity, data management, and advanced tools. Unified cards open category screens, Gestures and BBS link management open directly, and Storage provides usage details plus a gray cache-delete button
- Thread/media caching and separate controls for browsing, read, post, identity, and upload histories
- Sync2ch synchronization for bookmarks, normal tabs, and read positions
- CuspiDroid backup creation/restoration and ChMate backup import
- Private browsing always follows dark-mode styling even when the app is set to light mode. It maps normal tabs' pure-black screen and bar backgrounds to a much darker green, uses dark green for slightly gray search, post, and menu surfaces, keeps the brighter dedicated glasses-icon color, replaces every normally gray border with one shared slightly brighter green, and preserves private state in new tabs

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

You can add all BBS links below at once from **Settings → BBS Links → Manage BBS links → Add all supported BBS links**. A confirmation dialog is shown first, and existing entries are not overwritten. Custom BBS entries have a fixed height, with long names and URLs truncated. Deleting an entry also requires confirmation.

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
