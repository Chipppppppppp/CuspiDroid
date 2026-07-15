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

CuspiDroid は、5ch と互換掲示板をブラウザライクな UI で閲覧・投稿する Android アプリです。Android 6.0 以降に対応しています。

## 機能

- 複数タブ、タブ一覧、ブックマークフォルダー、履歴を使ったブラウザライクな閲覧
- URL 入力と検索を兼ねたアドレスバー、`find.5ch.io` またはカスタムURLによるスレッド検索
- 5chおよびカスタムBBSの板・スレッドをネイティブ表示し、引っ張って更新
- スクロール位置と既読位置の保存、未読先頭への移動、未読色分け、レス数・勢いなどの一覧表示と並べ替え
- ツリー表示、返信・リンク・人気レスの抽出、AA表示、コピペ省略、板単位にも対応したNGWord・NGName・NGID管理
- アプリ内書き込み、名前・メール履歴、自分の書き込み履歴、認証が必要な掲示板向けのWebView連携
- 画像・GIF・動画のプレビューと全画面ズーム、グロ画像判定によるぼかし、ImgBBへのアップロード
- ライト・ダーク・端末テーマ、上下アドレスバー、ボタン配置、スワイプジェスチャーのカスタマイズ
- スレッド・メディアキャッシュと、閲覧・既読・書き込み・アップロード履歴の個別管理
- Sync2chによるブックマーク・タブ・既読位置の同期
- CuspiDroidバックアップの作成・復元と、ChMateバックアップからのデータ取り込み
- プライベートブラウジング

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

下記のBBSは、アプリの「設定」→「BBSリンク」→「BBSリンクを管理」→「README掲載BBSを一括追加」からまとめて登録できます。すでに登録済みの項目は上書きされません。

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
