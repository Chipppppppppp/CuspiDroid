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
- Both Reload All and pull-to-refresh in the tab overview update only lightweight metadata such as titles, response counts, and unread counts, without downloading new post bodies; threads with detected updates load those posts lazily when their tab is opened, and collapsed bookmark folders immediately reflect the latest aggregate unread count
- Combined URL and search address bar with thread search through `find.5ch.io` or a custom URL. Searches on `find.5ch.io` use a normal WebView browser session starting from its home page, then present the retrieved results natively. HTTP 502 is shown as a temporary search-site outage instead of "No search results"
- Next-thread search with an editable query that narrows candidates from the current board using title similarity and thread-number continuity, displayed like the board list
- A shared dialog design for long-pressed posts, media/link/popular-post filters, and next-thread candidates, with matching animations, gray borders, background dimming, and tap-outside dismissal
- Native board and thread views for 5ch and custom BBS sites, with pull-to-refresh. Pulling from either end of a thread uses the same stretch effect as boards and the tab overview, and a bottom-edge refresh immediately marks all loaded posts as read. Board loading is isolated from other background I/O, custom BBS lists such as Edge render incrementally in small batches, and returning from a thread to an uncached board switches immediately to its loading screen. When "Open 5ch links in a new tab" is enabled, the thread's "Go to board" action also opens the board in a new tab
- Saved scroll/read positions, first-unread jumps, unread coloring, list metadata, and sorting
- Tree view, reply/link/popular-post filters with an editable popularity threshold, AA rendering, repeated-copy omission, distinct colored left bars for your posts and replies to them, and board-scoped NGWord/NGName/NGID rules
- In-app posting with thread creation from the board title bar on 5ch, Edge, and other supported boards, using the regular posting UI for the initial message and automatically opening the created thread in a new tab. Edge posts are marked complete only after a valid success response; when authentication is required, the app displays and copies the code, opens WebView, and automatically retries the original post. Also includes name/mail history, personal post history, and WebView integration for BBS authentication
- Shared image, GIF, and video previews scaled by their longest edge to fit fully inside dedicated square cells, with finger-tracking horizontal swipes between thread media, full-screen zoom, graphic-image blurring and reveal buttons shared across post previews, the media list, and the expanded viewer, a default-on option that blurs every media item both in posts containing warning terms and in posts targeted by those replies, ImgBB uploads, upload-history media scaled with the same aspect-preserving behavior, and delete-confirmation dialogs consistent with the app's other popups
- Light, dark, and system themes; top or bottom address bar; configurable buttons and swipe gestures
- Switching from the native view to WebView keeps the main app's actual search bar in the same screen, including its suggestions, design, configured button placement, and menu structure. New-tab, tab-overview, and bookmark controls are omitted, while "Open in WebView" becomes "Return to app" for restoring the native view. Find-in-page also uses the same safe in-app search bar
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
