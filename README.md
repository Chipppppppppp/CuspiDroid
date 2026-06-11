# CuspiDroid

CuspiDroid は、5ch 系掲示板をブラウザライクに閲覧する Android アプリです。

CuspiDroid is an Android app for browsing 5ch-style BBS sites with a browser-like interface.

## 機能 / Features

- 複数タブを開いてスレッドや検索結果を切り替え / Open and switch between multiple tabs
- 下部検索バーから URL 入力または検索 / Enter URLs or search from the bottom address bar
- `find.5ch.io` を利用したスレ検索 / Search threads through `find.5ch.io`
- 5ch 形式のスレッドをネイティブビューで表示 / Render 5ch-style thread URLs in a native reader
- 表示中スレッドを WebView で開いて認証や Web 書き込みに利用 / Open the current thread in WebView for auth or web posting
- imgur 画像プレビュー、ぼかし表示、全画面ズーム / Imgur previews, blur-to-show, and full-screen zoom
- BBS リンク、履歴、画像ぼかしなどの設定 / Settings for BBS links, history, image blur, and more

## ビルド / Build

Android Studio でこのリポジトリを開くか、次を実行してください。

Open this repository in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug
```

## インストール / Install

端末が adb で接続されている場合は次でインストールできます。

If a device is connected through adb, install with:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```
