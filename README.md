<p align="center">
  <img src="docs/icon.svg" alt="CuspiDroid icon" width="128" height="128">
</p>

<h1 align="center">CuspiDroid</h1>

<p align="center">
  <a href="LICENSE">
    <img alt="License: GPL v3" src="https://img.shields.io/badge/License-GPLv3-0F766E?style=for-the-badge">
  </a>
</p>

[English README](README.en.md)

CuspiDroid は、5ch 互換の掲示板をブラウザライクな UI で閲覧する Android アプリです。

## 機能

- 複数タブでの閲覧
- URL 入力と検索を兼ねたアドレスバー
- `find.5ch.io` を利用したスレッド検索
- 5ch 互換スレッドのネイティブ表示
- 現在のスレッドを WebView で開く認証・書き込み補助
- imgur プレビュー、グロ画像のぼかし表示、全画面ズーム
- BBS リンク、履歴、既読、NG 設定、優先ワード、ブックマークなどの管理
- ダークテーマとプライベートブラウジング

## インストール

APK は [Releases](https://github.com/Chipppppppppp/CuspiDroid/releases) からダウンロードできます。

Obtainium を使う場合は、この GitHub リポジトリをアプリソースとして追加してください。

<p align="center">
  <a href="https://apps.obtainium.imranr.dev/redirect.html?r=obtainium://add/https://github.com/Chipppppppppp/CuspiDroid">
    <img src="docs/images/badge_obtainium.png"
         alt="Get it on Obtainium"
         height="80">
  </a>
</p>

## ビルド

Android Studio でこのリポジトリを開くか、次のコマンドを実行してください。

```powershell
.\gradlew.bat assembleDebug
```

最適化済みの release APK を作る場合:

```powershell
.\gradlew.bat assembleRelease
```

## カスタムBBS一覧

| 名前 | URL |
| --- | --- |
| まちBBS | `https://machi.to/bbsmenu.html` |
| したらば掲示板 | `https://bbs-menu.pages.dev/shitaraba_bbsmenu/bbsmenu.json` |
| ふたばちゃんねる | `https://www.2chan.net/bbsmenu.html` |
| BBSPINK | `https://bbspink.org/ex0ch/bbsmenu.html` |
| おーぷん2ちゃんねる | `https://menu.open2ch.net/bbsmenu.html` |
| エッヂ | `https://bbs.eddibb.cc/liveedge/` |
| AfternoonTea | `https://afternoontea.st/boards/bbsmenu.html` |

## ライセンス

CuspiDroid は GNU General Public License v3.0 の下で配布されています。詳細は [LICENSE](LICENSE) を参照してください。
