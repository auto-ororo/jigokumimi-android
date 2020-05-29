# Jigokumimi Android

## 説明

音楽収集アプリ「Jigokumimi」のAndroid版です。

ユーザーの位置情報、及びSpotify APIから取得した利用データをサーバー(Jigokumimi API)に送信し、

近くにいる「Jigokumimi」利用ユーザーが聴いているSpotify上のトラック/アーティストを取得します。

### 動作環境

- Android 5.0 Lollipop 以上

### 機能一覧

- ログイン機能(JWTトークン認証)
- ユーザー登録･変更機能
- 位置情報取得･送信機能
- トラック/アーティスト検索機能
- 検索履歴参照･削除機能
- 音楽再生機能(トラックの30秒プレビュー再生)
- 連携Spotifyアカウントへのお気に入り登録/フォロー機能
- デモ機能

## 技術スタック

### 開発言語

Kotlin Ver1.3.72

### デザインパターン

AAC(Android Architecture Components)､Android Jetpackを用いたMVVMパターンで実装

### 採用ライブラリ

| ライブラリ             | バージョン | 概要                                              |
| ---------------------- | ---------- | ------------------------------------------------- |
| Retrofit2              | 2.7.1      | HTTPクライアント                                  |
| Moshi                  | 1.6.0      | JSON整形                                          |
| Coroutine              | 1.3.5      | 非同期処理･軽量スレッド                           |
| Lifecycle              | 2.2.0      | Activity,Fragment,ViewModelのライフサイクルを取得 |
| Navigation             | 2.2.1      | Fragment間の画面遷移                              |
| Material Design        | 1.1.0      | マテリアルUIコンポーネント                        |
| Glide                  | 4.11.0     | 画像描画ライブラリ                                |
| Timer                  | 4.7.1      | ログ出力                                          |
| Spotify Auth SDK       | 1.2.3      | Spotifyアカウント連携                             |
| Play Services Location | 17.0.0     | 位置情報の取得                                    |
| Shared Preferences     | 1.1.0      | 端末内への各種設定値保存                          |
| Junit                  | 4.12       | 単体テストフレームワーク                          |
| Mockk                  | 1.9.3      | テスト時のモック作成                              |
| Faker                  | 1.0.2      | テスト･デモモード時のダミーデータ作成             |
| Espresso               | 3.2.0      | UIテストフレームワーク                            |
| Hamcrest               | 1.3        | Junitテストアサーションライブラリ                 |

## 開発環境

### 必要要件(ローカルPC)

Android Studio 3.5以上

### 構築手順

1. リポジトリクローン

```bash
$ git clone https://github.com/auto-ororo/jigokumimi-android.git
```

2. クローンしたリポジトリをAndroid Studioで開く

### テスト

#### 単体テスト

Android Studioの｢Android｣プロジェクトウィンドウ内｢com.ororo.auto.jigokumimi(test)｣を右クリック→｢Run 'Tests in 'Jigokumimi''｣をクリック

#### UIテスト

Android Studioの｢Android｣プロジェクトウィンドウ内｢com.ororo.auto.jigokumimi(android test)｣を右クリック→｢Run 'Tests in 'Jigokumimi''｣をクリック