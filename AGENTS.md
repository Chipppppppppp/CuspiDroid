# AGENTS.md

このリポジトリで作業するエージェント向けの指示です。

- 毎回の指示で、作業完了後に git にコミットすること。
- GitHub リリース時には、配布物の sha-256 を添付すること。
- GitHub リリース時には、ひとつ前のリリース時点の `README.md` と現在の `README.md` を比較し、追加された機能を GitHub リリースの説明に記載すること。
- 機能を追加・変更した場合は、その内容に合わせて日本語版 `README.md` と英語版 `README.en.md` の両方を更新し、記載内容を同期すること。
- `BbsLinksActivity.RECOMMENDED_CUSTOM_BBS_LINKS` の「対応BBSを一括追加」対象と、`README.md` および `README.en.md` のカスタムBBS一覧は、名前・URL・並び順を常に同期すること。

## UIテーマの参照方法

- UIの色は `Theme` を唯一の参照元とし、画面クラスへ `Color.rgb(...)` や色リテラルを追加しないこと。役割に応じて `background`（画面）、`topBar`（上下バー）、`surface`（カード・パネル・メニュー）、`post`（投稿）、`unread`（未読投稿）、`field`（入力欄）、`text`（本文）、`muted` / `subtle`（補助表示）、`border` / `strongBorder`（境界線）、`active` / `searchHighlight`（選択・検索・優先ワード強調）、`accent`（操作・アイコン）を使うこと。スクロールサイドバー本体は `border`、つまみと自分の投稿バーは `accent`、未読範囲は `sidebarUnread`、自分への返信バーは `replyPostMarker`、ツリー線は `strongBorder` を参照すること。順位・勢い・レス数など値の強さを補間する表示には `metricLow` / `metricHigh` を使うこと。
- `Theme.background(context)` のような通常のアクセサーを使うこと。`MainActivity` では既存の `bgColor()`、`surfaceColor()` などのラッパーを優先すること。通常・プライベートは常に同じパレットを使い、プライベート状態を理由に画面全体の色を分岐させないこと。プライベート状態は画面最上部の専用バーで表し、その塗り色には `accent`、文字・アイコンには `Theme.contrastingText(...)` を使うこと。
- システムバーには `Theme.applySystemBars(...)`、ダイアログには `Theme.styleDialog(...)` / `Theme.stylePopupDialog(...)`、チェックボックスとラジオボタンには `Theme.tintCompoundButton(...)`、ローディング用の `ProgressBar` には `Theme.tintProgressBar(...)` を使い、選択中のパレットと明暗設定を反映すること。
- `accent` を塗り色として使うボタンやバッジの前景色は、任意のカスタム色でもコントラストを保つため `Theme.contrastingText(...)` で決めること。
- 新しいセマンティック色を追加する場合は、まず既存色へ統合できないか確認すること。独立色が必要な場合は、インディゴ・ティール・グリーン・ブルー・ライトブルー・パープル・クリムゾン・ピンクそれぞれのライト／ダーク全組み込みパレット、`Theme.Palette` のJSON入出力、`Theme.COLOR_KEYS` と `Theme.EDITABLE_COLOR_KEYS`、テーマ編集画面の表示名・GUIカラーピッカー・色プレビューを同時に更新し、既存のテーマファイルを読み込める互換性を維持すること。
- ランチャーアイコンの壺形状を変更する場合は、`ic_launcher_foreground.xml` と `ic_launcher_monochrome.xml` の輪郭を同期し、通常・円形それぞれの旧Android用アイコン、アダプティブアイコン、Android 13以降のモノクロテーマアイコンをすべて維持すること。
