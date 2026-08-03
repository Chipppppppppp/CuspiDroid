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

- 複数タブ、ブックマーク、履歴、未読管理、閲覧位置の保存
- 5chとカスタムBBSの板・スレッド表示、スワイプ更新、一覧の並べ替え
- URL入力、スレッド検索、ページ内検索、次スレ検索
- 書き込み、スレッド作成、掲示板認証、名前・メール・書き込み履歴
- 返信ツリー、返信・リンク・人気レスの抽出、AA表示、コピペ省略、NG管理
- 画像・GIF・動画のプレビューと拡大、メディアぼかし、ImgBBへのアップロード
- ピュアホワイト／ピュアダークを含むライト／ダーク対応の組み込みテーマ、作成・共有・バックアップできるカスタムテーマ、画面配置・ジェスチャー設定
- ネイティブ表示とWebViewの切り替え、プライベートブラウジング
- スレッド・メディアのキャッシュと各種履歴の個別管理
- Sync2ch同期、バックアップ作成・復元、ChMateバックアップの取り込み

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

下記のBBSは、「設定」→「BBSリンク」→「BBSリンクを管理」→「対応BBSを一括追加」から登録できます。

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
