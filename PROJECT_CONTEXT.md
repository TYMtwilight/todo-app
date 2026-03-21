# PROJECT_CONTEXT

## プロジェクト概要
Spring Boot（REST API）+ React（SPA）の TODO アプリ。
学習目的で構築。CRUD 操作・テスト手法・CI/CD を実践した。

## ディレクトリ構成
- backend/ — Spring Boot 3.x（Gradle, Java 17）
- frontend/ — React 18 + Vite（TypeScript, CSS Modules）
- .github/workflows/ — GitHub Actions CI
- docs/ — スクリーンショット等

## バックエンド構成
- entity/Todo.java — JPA Entity（@PrePersist/@PreUpdate で日時自動設定）
- dto/ — TodoRequest（バリデーション付き）, TodoResponse（Entity→DTO 変換）, ErrorResponse
- repository/TodoRepository.java — JpaRepository 継承、カスタムクエリ
- service/TodoService.java + TodoServiceImpl.java — ビジネスロジック
- controller/TodoController.java — REST API 5 エンドポイント
- controller/GlobalExceptionHandler.java — @RestControllerAdvice でエラー統一処理
- controller/HealthController.java — ヘルスチェック
- config/WebConfig.java — CORS 設定（localhost:5173 許可）

## フロントエンド構成
- types/todo.ts — Todo, TodoRequest, FilterStatus
- lib/api.ts — fetch ベースの API クライアント（5 関数）
- hooks/useTodos.ts — 状態管理 + API 呼び出しカスタム Hook
- components/TodoForm.tsx — 入力フォーム（バリデーション付き）
- components/TodoItem.tsx — 1 件の TODO 表示（チェック + 削除）
- components/TodoFilter.tsx — フィルターボタン（ALL/ACTIVE/COMPLETED）
- App.tsx — 全体レイアウト

## テスト構成
- バックエンド: @DataJpaTest / Mockito / @WebMvcTest + MockMvc
- フロントエンド: Vitest + React Testing Library + MSW

## 開発ルール
- GitHub Issue 単位で開発
- コミットメッセージに Issue 番号を含める（例: #5 Todo Entity の作成）
- 実装とテストはセット

## 環境変数
- VITE_API_URL — バックエンドの URL（デフォルト: http://localhost:8080）

## 既知の制約
- H2 インメモリ DB のためアプリ停止時にデータが消える
- 認証機能なし（将来追加を検討）