# CuspiDroid

[English README](README.en.md)

CuspiDroid は、5ch 互換の掲示板をブラウザライクな UI で閲覧する Android アプリです。

## 機能

- 複数タブの閲覧
- URL 入力と検索を兼ねたアドレスバー
- `find.5ch.io` を利用したスレッド検索
- 5ch 互換スレッドのネイティブ表示
- 現在のスレッドを WebView で開く認証・書き込み補助
- imgur 画像プレビュー、グロ画像のぼかし表示、全画面ズーム
- BBS リンク、履歴、画像ぼかしなどの設定

## ビルド

Android Studio でこのリポジトリを開くか、次のコマンドを実行してください。

```powershell
.\gradlew.bat assembleDebug
```

## インストール

端末が adb で接続されている場合は、次のコマンドでインストールできます。

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## ライセンス

CuspiDroid は GNU General Public License v3.0 の下で配布されます。詳細は [LICENSE](LICENSE) を参照してください。
