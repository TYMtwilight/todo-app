# TODO アプリ 要件定義書

**プロジェクト名**: todo-app  
**作成日**: 2026-03-13  
**技術スタック**: Spring Boot（バックエンド） + React（フロントエンド）  
**目的**: REST API の知識定着 + フロントエンド・バックエンド統合開発の実践

---

## 1. プロジェクト概要

Spring Boot で REST API サーバを構築し、React で SPA フロントエンドを構築する TODO アプリ。
CRUD 操作を通じて REST API 設計・実装・テストの一連の流れを体験し、知識を定着させる。

### 1.1 学習目標

- Spring Boot による REST API の設計・実装パターンの定着
- React（Vite + TypeScript）による SPA 構築
- フロントエンド・バックエンドそれぞれのテスト手法の習得
- GitHub Issue 管理による開発プロセスの実践

---

## 2. 機能要件

### 2.1 TODO の CRUD 操作

| 操作 | 説明 |
|------|------|
| 作成（Create） | タイトルを入力して新しい TODO を作成する |
| 一覧表示（Read） | 全 TODO を一覧表示する |
| 更新（Update） | TODO のタイトル編集、完了/未完了の切り替え |
| 削除（Delete） | TODO を削除する |

### 2.2 TODO のデータ構造

| フィールド | 型 | 説明 | 制約 |
|-----------|-----|------|------|
| id | Long | 一意な識別子 | 自動採番 |
| title | String | TODO のタイトル | 必須、1〜100 文字 |
| completed | Boolean | 完了状態 | デフォルト: false |
| createdAt | LocalDateTime | 作成日時 | 自動設定 |
| updatedAt | LocalDateTime | 更新日時 | 自動設定 |

### 2.3 フィルタリング機能

- 全件表示
- 未完了のみ表示
- 完了済みのみ表示

### 2.4 画面構成

| 画面 | パス | 説明 |
|------|------|------|
| TODO 一覧 | `/` | メイン画面。一覧表示 + 新規作成フォーム + フィルター |

シングルページ構成 — 1 画面で全操作を完結させる。ルーティング不要なので React 単体で十分。

---

## 3. 非機能要件

### 3.1 パフォーマンス

- API レスポンス: 200ms 以内（ローカル環境）

### 3.2 バリデーション

- バックエンド: Bean Validation（`@NotBlank`, `@Size`）
- フロントエンド: フォームの空欄チェック + エラーメッセージ表示

### 3.3 エラーハンドリング

- バックエンド: `@RestControllerAdvice` による統一エラーレスポンス
- フロントエンド: API エラー時のユーザーフレンドリーな表示

### 3.4 テスト

| レイヤー | テスト種別 | ツール |
|---------|-----------|-------|
| Controller | 単体テスト | `@WebMvcTest` + MockMvc |
| Service | 単体テスト | JUnit 5 + Mockito |
| Repository | 統合テスト | `@DataJpaTest` + H2 |
| フロントエンド | コンポーネントテスト | Vitest + React Testing Library |
| フロントエンド | API 通信テスト | MSW（Mock Service Worker） |

---

## 4. API 設計

### 4.1 エンドポイント一覧

| メソッド | パス | 説明 | リクエスト | レスポンス |
|---------|------|------|-----------|-----------|
| GET | `/api/todos` | TODO 一覧取得 | クエリ: `?status=ALL\|ACTIVE\|COMPLETED` | `200`: Todo[] |
| POST | `/api/todos` | TODO 作成 | Body: `{ "title": "..." }` | `201`: Todo |
| PUT | `/api/todos/{id}` | TODO 更新 | Body: `{ "title": "...", "completed": true }` | `200`: Todo |
| PATCH | `/api/todos/{id}/toggle` | 完了状態の切り替え | なし | `200`: Todo |
| DELETE | `/api/todos/{id}` | TODO 削除 | なし | `204`: No Content |

### 4.2 レスポンス形式

**成功レスポンス（Todo オブジェクト）**

```json
{
  "id": 1,
  "title": "Spring Boot を学ぶ",
  "completed": false,
  "createdAt": "2026-03-13T10:00:00",
  "updatedAt": "2026-03-13T10:00:00"
}
```

**エラーレスポンス**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "タイトルは必須です",
  "timestamp": "2026-03-13T10:00:00"
}
```

---

## 5. 技術スタック詳細

### 5.1 バックエンド

| 項目 | 技術 |
|------|------|
| フレームワーク | Spring Boot 3.x |
| 言語 | Java 17+ |
| ORM | Spring Data JPA |
| データベース | H2（開発用インメモリ） |
| バリデーション | Jakarta Bean Validation |
| API ドキュメント | SpringDoc OpenAPI（Swagger UI） |
| テスト | JUnit 5, Mockito, MockMvc |
| ビルドツール | Gradle |

### 5.2 フロントエンド

| 項目 | 技術 |
|------|------|
| ライブラリ | React 18 |
| ビルドツール | Vite |
| 言語 | TypeScript |
| スタイリング | CSS Modules |
| HTTP クライアント | fetch API |
| テスト | Vitest, React Testing Library, MSW |

### 5.3 開発ツール

| 項目 | 技術 |
|------|------|
| バージョン管理 | Git + GitHub |
| タスク管理 | GitHub Issues + GitHub Projects |
| IDE | Cursor（Claude Code 統合） |
| CI | GitHub Actions |

---

## 6. プロジェクト構成

```
todo-app/
├── backend/                     # Spring Boot プロジェクト
│   ├── src/main/java/com/example/todo/
│   │   ├── TodoApplication.java
│   │   ├── config/
│   │   │   └── WebConfig.java
│   │   ├── controller/
│   │   │   ├── TodoController.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── service/
│   │   │   ├── TodoService.java
│   │   │   └── TodoServiceImpl.java
│   │   ├── repository/
│   │   │   └── TodoRepository.java
│   │   ├── entity/
│   │   │   └── Todo.java
│   │   ├── dto/
│   │   │   ├── TodoRequest.java
│   │   │   ├── TodoResponse.java
│   │   │   └── ErrorResponse.java
│   │   └── enums/
│   │       └── TodoStatus.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── src/test/java/com/example/todo/
│   │   ├── controller/
│   │   │   └── TodoControllerTest.java
│   │   ├── service/
│   │   │   └── TodoServiceTest.java
│   │   └── repository/
│   │       └── TodoRepositoryTest.java
│   └── build.gradle
│
├── frontend/                    # React + Vite プロジェクト
│   ├── src/
│   │   ├── main.tsx
│   │   ├── App.tsx
│   │   ├── App.module.css
│   │   ├── components/
│   │   │   ├── TodoList.tsx
│   │   │   ├── TodoList.module.css
│   │   │   ├── TodoItem.tsx
│   │   │   ├── TodoItem.module.css
│   │   │   ├── TodoForm.tsx
│   │   │   ├── TodoForm.module.css
│   │   │   ├── TodoFilter.tsx
│   │   │   └── TodoFilter.module.css
│   │   ├── hooks/
│   │   │   └── useTodos.ts
│   │   ├── lib/
│   │   │   └── api.ts
│   │   └── types/
│   │       └── todo.ts
│   ├── src/__tests__/
│   │   ├── components/
│   │   │   ├── TodoList.test.tsx
│   │   │   ├── TodoItem.test.tsx
│   │   │   └── TodoForm.test.tsx
│   │   └── lib/
│   │       └── api.test.ts
│   ├── index.html
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── package.json
│
├── .github/
│   └── workflows/
│       └── ci.yml
├── PROJECT_CONTEXT.md
└── README.md
```

---

## 7. GitHub Issue 管理

### 7.1 ラベル

| ラベル | 色 | 用途 |
|--------|-----|------|
| `backend` | 青 | バックエンドタスク |
| `frontend` | 緑 | フロントエンドタスク |
| `test` | 黄 | テスト関連 |
| `setup` | グレー | 環境構築 |
| `docs` | 紫 | ドキュメント |
| `bug` | 赤 | バグ修正 |

### 7.2 マイルストーン

| マイルストーン | 期間目安 | 内容 |
|--------------|---------|------|
| v0.1 - 環境構築 | 1-2日 | プロジェクト初期設定 |
| v0.2 - バックエンド API | 3-4日 | REST API 実装 + テスト |
| v0.3 - フロントエンド | 3-4日 | 画面実装 + テスト |
| v0.4 - 統合・仕上げ | 2-3日 | 結合テスト + CI + ドキュメント |

---

## 8. 完成基準

- [ ] 全 CRUD 操作が正常に動作する
- [ ] フィルタリング機能が動作する
- [ ] バリデーションエラーが適切に表示される
- [ ] バックエンドテストカバレッジ 80% 以上
- [ ] フロントエンドの主要コンポーネントにテストがある
- [ ] Swagger UI で API を確認できる
- [ ] GitHub Actions で CI が動作する
- [ ] README にセットアップ手順が記載されている
