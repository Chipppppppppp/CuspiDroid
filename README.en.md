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

- Browser-like navigation with multiple tabs, search-field autofocus on new tabs, a tab overview, bookmark folders, and unread counts in history on both the new-tab page and in Settings
- Lightweight tab metadata is stored separately from page caches so the tab overview appears quickly after a cold start; page caches load lazily for the selected tab, while bookmark folders and unread state are prepared in the background
- Both Reload All and pull-to-refresh in the tab overview update only lightweight metadata such as titles, response counts, and unread counts, without downloading new post bodies; threads with detected updates load those posts lazily when their tab is opened, and collapsed bookmark folders immediately reflect the latest aggregate unread count
- Combined URL and search address bar with thread search through `find.5ch.io` or a custom URL. Searches on `find.5ch.io` use a normal WebView browser session starting from its home page, then present the retrieved results natively. HTTP 502 is shown as a temporary search-site outage instead of "No search results"
- Next-thread search with an editable query that narrows candidates from the current board using title similarity and thread-number continuity, displayed like the board list
- A shared dialog design for long-pressed posts, media/link/popular-post filters, and next-thread candidates, with matching animations, gray borders, background dimming, and tap-outside dismissal
- Native board and thread views for 5ch and custom BBS sites, with pull-to-refresh. Pulling from either end of a thread uses the same stretch effect as boards and the tab overview, and a bottom-edge refresh immediately marks all loaded posts as read. Board loading is isolated from other background I/O; board lists prioritize the initially visible range, then load and render later threads in small batches as scrolling approaches them. Custom BBS sites such as Edge index history and read-state data before building the list, avoiding a full history scan and repeated BBS-settings parsing for every thread. Edge board links switch to the loading screen before title/URL analysis or loading starts, then begin loading independently of the visible surface so the tab overview remains available before the title or URL is finalized. Superseded board loads are canceled. Hidden tabs keep only fetched data and defer View construction until shown, while board-cache serialization, writes, and stale-file cleanup run on a dedicated worker, keeping the tab overview responsive when several boards are open. Returning from a thread to an uncached board switches immediately to its loading screen. When "Open 5ch links in a new tab" is enabled, the thread's "Go to board" action also opens the board in a new tab
- Saved scroll/read positions, first-unread jumps, unread coloring, theme-controlled scroll sidebar and unread-range colors, list metadata and sorting, with ranks, post counts, and speed transitioning from the theme's low color to its high color as their values increase
- Tree view connecting source posts and replies with theme-defined lines, reply/link/popular-post filters with an editable popularity threshold, automatic AA detection for bodies of at least four nonblank lines by scoring repeated leading whitespace, punctuation, quotation marks, box-drawing characters, logical operators, and set operators plus repeated in-line whitespace and requiring a per-line score above 0.75, AA rendering, repeated-copy omission, distinct colored left bars for your posts and replies to them, and board-scoped NGWord/NGName/NGID rules
- In-app posting with thread creation from the board title bar on 5ch, Edge, and other supported boards, using the regular posting UI for the initial message and automatically opening the created thread in a new tab. Edge posts are marked complete only after a valid success response; when authentication is required, the app displays and copies the code, opens WebView, and automatically retries the original post. Also includes name/mail history, personal post history, and WebView integration for BBS authentication
- Shared image, GIF, and video previews scaled by their longest edge to fit fully inside dedicated square cells, with finger-tracking horizontal swipes between thread media, full-screen zoom, strong graphic-image blurring in post previews and the media list, an even stronger blur in the expanded viewer, centered “Sensitive” buttons using the same background and border colors as menus and popups across all three views, a default-on option that blurs every media item both in posts containing warning terms and in posts targeted by those replies, ImgBB uploads, upload-history media scaled with the same aspect-preserving behavior, and delete-confirmation dialogs consistent with the app's other popups
- Sixteen built-in themes with Light and Dark variants for Indigo, Teal, Green, Blue, Light Blue, Purple, Crimson, and Pink, plus an automatic setting that switches themes with the device's light/dark mode. Automatic mode uses Teal by default, and its light-mode and dark-mode themes can be configured independently. Every built-in theme follows the “Color (Light/Dark)” naming format, and the selected theme is shared by normal and private browsing. The selector shows the current palette and every candidate with an eight-color preview, light/dark and theme type details, and a selection mark. Create custom themes by editing 17 independent semantic colors—including screens, bars, cards, posts, fields, text, borders, selection/in-page-search highlighting, accents, the unread range, reply-to-me marker, and metadata gradients—plus system-bar icon brightness. Priority words reuse the in-page-search color but remain visually distinct through a thick underline that preserves the text color. Menus share the card color, the sidebar rail shares the border color, the thumb and my-post marker share the accent, and tree connectors share the strong-border color for a more cohesive palette. Loading indicators consistently use the selected theme's accent color. Each color supports direct `#RRGGBB` entry and a visual picker with hue presets, hue/saturation/brightness sliders, and a live preview; complete themes are importable and exportable as version 2 JSON files containing those 17 colors (older formats are not compatible). Also includes a top or bottom address bar and configurable buttons and swipe gestures. The launcher icon keeps the original pot design while aligning the center shading's upper edge with the projecting rim underside, its lower edge with the frustum base at the same curve ratio, and its side edges with the pot walls at the same slope. The README SVG is generated automatically from the Android vector during builds. Legacy Android and Android 13+ themed icons are also supported
- Switching from the native view to WebView keeps the main app's actual search bar in the same screen, including its suggestions, design, configured button placement, and menu structure. New-tab, tab-overview, and bookmark controls are omitted, while "Open in WebView" becomes "Return to app" for restoring the native view. Find-in-page also uses the same safe in-app search bar, with matches in threads, boards, and board lists highlighted using the theme's in-page-search color while preserving the original text color
- A concise settings overview ordered from everyday appearance, reading, and media options through connectivity, data management, and advanced tools. Unified cards open category screens, Gestures and BBS link management open directly, and Storage provides usage details plus a gray cache-delete button
- Thread/media caching and separate controls for browsing, read, post, identity, and upload histories
- Sync2ch synchronization for bookmarks, normal tabs, and read positions
- CuspiDroid backup creation/restoration and ChMate backup import
- Private browsing keeps the same theme as normal browsing while an accent-colored bar smoothly appears at the top of the screen with the glasses icon and “Private” label, and preserves private state in new tabs

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
