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

タイトルバーからスレッド末尾へ移動した後も、上方向へのスクロール操作をすぐに行えます。

書き込み後も既読位置を維持し、成功済みの書き込みを確認処理で再送しません。

CuspiDroid は、5ch と互換掲示板をブラウザライクな UI で閲覧・投稿する Android アプリです。Android 6.0 以降に対応しています。

## 機能

- 複数タブ、検索欄へ自動フォーカスする新規タブ、タブ一覧、ブックマークフォルダー、履歴を使ったブラウザライクな閲覧
- URL 入力と検索を兼ねたアドレスバー、`find.5ch.io` またはカスタムURLによるスレッド検索
- 次スレ検索では、編集可能な検索語、題名の類似度、連番の連続性を使い、同じ板から候補を少数に絞って板一覧と同じ形式で表示
- 書き込み長押し・メディア／リンク／人気レス抽出・次スレ候補では、共通ダイアログによる同じアニメーション、灰色の枠線、背景暗転を採用し、外側のタップで閉じる表示
- 5chおよびカスタムBBSの板・スレッドをネイティブ表示し、引っ張って更新
- スクロール位置と既読位置の保存、未読先頭への移動、未読色分け、レス数・勢いなどの一覧表示と並べ替え
- ツリー表示、返信・リンク・閾値を変更できる人気レスの抽出、AA表示、コピペ省略、板単位にも対応したNGWord・NGName・NGID管理
- アプリ内書き込み、名前・メール履歴、自分の書き込み履歴、認証が必要な掲示板向けのWebView連携
- 長辺を基準に専用の正方形セル内へ全体が収まる画像・GIF・動画の共通プレビューと全画面ズーム、グロ画像判定によるぼかし、ImgBBへのアップロードと同じ比率で縮小表示するアップロード履歴と、他のポップアップと統一した削除確認ダイアログ
- ライト・ダーク・端末テーマ、上下アドレスバー、ボタン配置、スワイプジェスチャーのカスタマイズ
- 日常的に使う表示・閲覧・メディア設定から、接続・データ管理・詳細設定へ進む順に並べた設定トップ。目的別の統一カードから各画面へ移動し、ジェスチャーとBBSリンク管理には直接移動。ストレージでは使用量の確認と灰色のキャッシュ削除ボタンを利用可能
- スレッド・メディアキャッシュと、閲覧・既読・書き込み・アップロード履歴の個別管理
- Sync2chによるブックマーク・タブ・既読位置の同期
- CuspiDroidバックアップの作成・復元と、ChMateバックアップからのデータ取り込み
- 通常タブで真っ黒となる画面やバーの背景をかなり暗い緑色にし、検索窓・投稿・メニューなどの少し灰色がかった背景には従来の暗い緑色を維持。眼鏡アイコンは明るい専用色で表示し、通常時と同じ灰色の枠線をバーやポップアップで共有して、新規タブでも状態を維持するプライベートブラウジング

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

下記のBBSは、アプリの「設定」→「BBSリンク」→「BBSリンクを管理」→「対応BBSを一括追加」からまとめて登録できます。追加前には確認ダイアログが表示され、すでに登録済みの項目は上書きされません。カスタムBBSの一覧は固定の高さで表示され、長い名前やURLは省略されます。削除時には確認ダイアログが表示されます。

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
