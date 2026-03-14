# TODO App

Spring Boot + React で構築する TODO アプリケーション。

## 技術スタック

### バックエンド
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 Database

### フロントエンド
- React 18
- Vite
- TypeScript
- CSS Modules

## セットアップ

### バックエンド
```bash
cd backend
./gradlew bootRun
```
→ http://localhost:8080

### フロントエンド
```bash
cd frontend
npm install
npm run dev
```
→ http://localhost:5173

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/todos | TODO 一覧取得 |
| POST | /api/todos | TODO 作成 |
| PUT | /api/todos/{id} | TODO 更新 |
| PATCH | /api/todos/{id}/toggle | 完了状態切り替え |
| DELETE | /api/todos/{id} | TODO 削除 |