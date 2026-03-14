# PROJECT_CONTEXT

## プロジェクト概要
Spring Boot（REST API）+ React（SPA）の TODO アプリ。
学習目的で構築しており、CRUD 操作・テスト手法・CI/CD を実践する。

## ディレクトリ構成
- backend/ — Spring Boot（Gradle, Java 17）
- frontend/ — React + Vite（TypeScript）

## 開発ルール
- GitHub Issue 単位で開発を進める
- 実装とテストはセットで書く
- コミットメッセージに Issue 番号を含める（例: #1 初期セットアップ）

## API 設計
- ベースパス: /api/todos
- データベース: H2（インメモリ）
- バリデーション: Bean Validation
- エラーハンドリング: @RestControllerAdvice

## テスト方針
- バックエンド: JUnit 5 + Mockito + MockMvc
- フロントエンド: Vitest + React Testing Library + MSW