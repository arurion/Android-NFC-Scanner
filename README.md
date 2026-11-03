# NFC Reader

NFC対応カード/タグの内容を解析するAndroidアプリ（Jetpack Compose）。

## 機能
- 交通系IC（Suica / PASMO / ICOCA など）の残高・利用履歴・駅名の解析
- NDEF、MIFARE Classic、IsoDep、NFC-V などのタグ情報とバイナリダンプ表示
- **任意のMIFARE Classic鍵**の登録（Key A/B、16進12桁）。スキャン時に既定鍵より先に試行します。鍵の推測・総当たりは行いません
- スキャン履歴の保存

## ビルド
Gradle 8.4 / AGP 8.3.2 / Kotlin 2.0.21 / compileSdk 34 / JDK 17以上

```
gradle :app:assembleDebug
```

## GitHub Actions
`.github/workflows/build.yml` が main への push / PR / タグ `v*.*.*` / 手動実行で debug・release APK をビルドします。
本番署名を使う場合は Secrets に `RELEASE_STORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` を設定してください（未設定ならデバッグ署名）。
