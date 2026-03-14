# Phase 1: プロジェクト初期設定 — 詳細手順

**前提**: `todo-app` リモートリポジトリ作成済み、ローカルとの紐づけ完了済み  
**ゴール**: バックエンド・フロントエンドが両方起動し、CORS 設定まで完了した状態

---

## 作業の全体像

```
Step 1: GitHub Issue・ラベル・マイルストーンの設定
Step 2: プロジェクトルートの初期ファイル作成 → push
Step 3: Spring Boot プロジェクト作成 → 起動確認 → push
Step 4: React + Vite プロジェクト作成 → 起動確認 → push
Step 5: CORS 設定 → フロント→バックエンド通信確認 → push
```

---

## Step 1: GitHub Issue・ラベル・マイルストーンの設定

### 1-1. ラベル作成

GitHub のリポジトリページ → `Issues` → `Labels` で以下を作成する。
デフォルトラベルは使わないものを削除してもよい。

| ラベル名 | 色（Hex） | 説明 |
|---------|----------|------|
| `backend` | `#0075ca` | バックエンドタスク |
| `frontend` | `#0e8a16` | フロントエンドタスク |
| `test` | `#fbca04` | テスト関連 |
| `setup` | `#cccccc` | 環境構築 |
| `docs` | `#7057ff` | ドキュメント |
| `bug` | `#d73a4a` | バグ修正 |

### 1-2. マイルストーン作成

GitHub のリポジトリページ → `Issues` → `Milestones` → `New milestone` で以下を作成する。

| マイルストーン名 | 説明 |
|----------------|------|
| v0.1 - 環境構築 | プロジェクト初期設定 |
| v0.2 - バックエンド API | REST API 実装 + テスト |
| v0.3 - フロントエンド | 画面実装 + テスト |
| v0.4 - 統合・仕上げ | 結合テスト + CI + ドキュメント |

### 1-3. Phase 1 の Issue 作成

`Issues` → `New issue` で以下の 4 つを作成する。

**Issue #1: プロジェクトの初期セットアップ**
```
ラベル: setup
マイルストーン: v0.1 - 環境構築

## やること
- [ ] README.md 作成
- [ ] PROJECT_CONTEXT.md 作成
- [ ] .gitignore 設定
- [ ] .github/workflows/ci.yml 空ファイル作成
- [ ] 初回 push
```

**Issue #2: Spring Boot プロジェクトの初期設定**
```
ラベル: setup, backend
マイルストーン: v0.1 - 環境構築

## やること
- [ ] Spring Initializr でプロジェクト生成
- [ ] backend/ ディレクトリに配置
- [ ] application.yml 設定
- [ ] 起動確認（bootRun + H2 Console アクセス）
```

**Issue #3: React + Vite プロジェクトの初期設定**
```
ラベル: setup, frontend
マイルストーン: v0.1 - 環境構築

## やること
- [ ] Vite で React + TypeScript プロジェクト生成
- [ ] ボイラープレート削除
- [ ] 起動確認（npm run dev）
```

**Issue #4: CORS 設定**
```
ラベル: backend
マイルストーン: v0.1 - 環境構築

## やること
- [ ] WebConfig.java で CORS 設定
- [ ] フロント → バックエンド通信確認
```

---

## Step 2: プロジェクトルートの初期ファイル作成

**対応 Issue**: #1

### 2-1. ルートの .gitignore 作成

```bash
cd todo-app
```

以下の内容で `.gitignore` を作成する（プロジェクトルート用）:

```gitignore
# OS
.DS_Store
Thumbs.db

# IDE
.idea/
.vscode/
*.swp
*.swo
*~
```

※ `backend/` と `frontend/` にはそれぞれ専用の `.gitignore` が後で自動生成される。

### 2-2. README.md 作成

```markdown
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
```

### 2-3. PROJECT_CONTEXT.md 作成

このファイルは Claude Code などの AI ツールにプロジェクトの全体像を伝えるためのもの。

```markdown
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
```

### 2-4. GitHub Actions の空ファイル作成

```bash
mkdir -p .github/workflows
touch .github/workflows/ci.yml
```

`ci.yml` には一旦コメントだけ入れておく:

```yaml
# CI 設定は Phase 4 で実装する
```

### 2-5. コミット・push

```bash
git add .
git commit -m "#1 プロジェクトの初期セットアップ"
git push origin main
```

GitHub 上で Issue #1 のチェックボックスを全て完了にし、Issue をクローズする。

---

## Step 3: Spring Boot プロジェクト作成

**対応 Issue**: #2

### 3-1. Spring Initializr でプロジェクト生成

ブラウザで https://start.spring.io/ にアクセスし、以下を設定する:

| 項目 | 設定値 |
|------|--------|
| Project | Gradle - Groovy |
| Language | Java |
| Spring Boot | 3.4.x（最新の安定版） |
| Group | com.example |
| Artifact | todo |
| Name | todo |
| Package name | com.example.todo |
| Packaging | Jar |
| Java | 17 |

**Dependencies（5つ）**:
1. **Spring Web** — REST API を作るため
2. **Spring Data JPA** — データベースアクセス
3. **H2 Database** — 開発用インメモリ DB
4. **Validation** — リクエストのバリデーション
5. **Spring Boot DevTools** — 開発中のホットリロード

`GENERATE` ボタンを押して ZIP をダウンロードする。

### 3-2. backend/ ディレクトリに配置

```bash
cd todo-app

# ダウンロードした ZIP を解凍
# （~/Downloads に保存された場合の例）
unzip ~/Downloads/todo.zip -d temp-backend

# backend/ ディレクトリとして配置
mv temp-backend/todo backend

# 一時ディレクトリ削除
rm -rf temp-backend
```

配置後のディレクトリ構成を確認:

```bash
ls backend/
```

以下が表示されればOK:
```
build.gradle  gradle/  gradlew  gradlew.bat  settings.gradle  src/
```

### 3-3. application.yml の設定

Spring Initializr が生成するのは `application.properties` なので、YAML 形式に置き換える。

```bash
# properties ファイルを削除
rm backend/src/main/resources/application.properties

# yml ファイルを作成
```

`backend/src/main/resources/application.yml` を以下の内容で作成:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:tododb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: 8080
```

各設定の意味:

| 設定 | 意味 |
|------|------|
| `jdbc:h2:mem:tododb` | インメモリ DB を使用（アプリ停止でデータ消失） |
| `ddl-auto: create-drop` | 起動時にテーブル作成、停止時に削除 |
| `show-sql: true` | 実行される SQL をコンソールに表示（学習用） |
| `format_sql: true` | SQL を見やすく整形 |
| `h2.console.enabled` | H2 の Web コンソールを有効化 |

### 3-4. 起動確認

```bash
cd backend
./gradlew bootRun
```

初回はライブラリのダウンロードに時間がかかる（数分）。以下のようなログが出れば成功:

```
Started TodoApplication in X.XXX seconds
```

#### 確認事項

1. **アプリ起動**: http://localhost:8080 にアクセス → エラーページが出るが正常（まだエンドポイントがないため）
2. **H2 Console**: http://localhost:8080/h2-console にアクセス
   - JDBC URL: `jdbc:h2:mem:tododb`
   - User Name: `sa`
   - Password: （空欄）
   - `Connect` をクリック → コンソールが表示されればOK

確認できたら `Ctrl + C` でアプリを停止する。

### 3-5. コミット・push

```bash
cd todo-app
git add .
git commit -m "#2 Spring Boot プロジェクトの初期設定"
git push origin main
```

GitHub 上で Issue #2 をクローズする。

---

## Step 4: React + Vite プロジェクト作成

**対応 Issue**: #3

### 4-1. Vite でプロジェクト生成

```bash
cd todo-app
npm create vite@latest frontend -- --template react-ts
```

生成後、依存パッケージをインストール:

```bash
cd frontend
npm install
```

### 4-2. ボイラープレートの整理

Vite が生成するデフォルトファイルを整理する。

#### src/App.tsx を書き換え

```tsx
import './App.css'

function App() {
  return (
    <div>
      <h1>TODO アプリ</h1>
      <p>準備完了</p>
    </div>
  )
}

export default App
```

#### src/App.css をクリア

```css
/* Phase 3 で実装する */
```

#### src/index.css を最小限に

```css
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.6;
  color: #333;
}
```

#### 不要ファイルの削除

```bash
rm src/assets/react.svg
rm public/vite.svg
```

#### public/vite.svg 削除後に index.html を修正

`index.html` の `<link rel="icon"` 行を削除するか、favicon を自分で用意する。
タイトルも変更:

```html
<!doctype html>
<html lang="ja">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>TODO App</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

### 4-3. ディレクトリ構造の準備

Phase 3 で使うディレクトリを先に作成しておく。空ディレクトリは Git で追跡されないので `.gitkeep` を置く:

```bash
cd frontend
mkdir -p src/components
mkdir -p src/hooks
mkdir -p src/lib
mkdir -p src/types
mkdir -p src/__tests__/components
mkdir -p src/__tests__/lib

touch src/components/.gitkeep
touch src/hooks/.gitkeep
touch src/lib/.gitkeep
touch src/types/.gitkeep
touch src/__tests__/components/.gitkeep
touch src/__tests__/lib/.gitkeep
```

### 4-4. 起動確認

```bash
npm run dev
```

http://localhost:5173 にアクセスして「TODO アプリ」「準備完了」が表示されればOK。

`Ctrl + C` で停止。

### 4-5. コミット・push

```bash
cd todo-app
git add .
git commit -m "#3 React + Vite プロジェクトの初期設定"
git push origin main
```

GitHub 上で Issue #3 をクローズする。

---

## Step 5: CORS 設定

**対応 Issue**: #4

### 5-1. WebConfig.java 作成

`config` パッケージのディレクトリを作成し、ファイルを配置する:

```bash
mkdir -p backend/src/main/java/com/example/todo/config
```

`backend/src/main/java/com/example/todo/config/WebConfig.java` を以下の内容で作成:

```java
package com.example.todo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
            .allowedHeaders("*");
    }
}
```

各設定の意味:

| 設定 | 意味 |
|------|------|
| `/api/**` | `/api/` 以下の全パスに適用 |
| `allowedOrigins` | React の開発サーバー（Vite）のオリジンを許可 |
| `allowedMethods` | 許可する HTTP メソッド（CRUD に必要な全メソッド） |
| `allowedHeaders("*")` | 全ヘッダーを許可（`Content-Type` など） |

### 5-2. 動作確認用の仮エンドポイント作成

CORS が正しく機能しているか確認するため、一時的にシンプルなエンドポイントを作る。

```bash
mkdir -p backend/src/main/java/com/example/todo/controller
```

`backend/src/main/java/com/example/todo/controller/HealthController.java`:

```java
package com.example.todo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
```

### 5-3. フロントから通信テスト

バックエンドを起動:

```bash
cd backend
./gradlew bootRun
```

別のターミナルで、フロントの `src/App.tsx` を一時的に書き換えて通信テスト:

```tsx
import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [status, setStatus] = useState<string>('確認中...')

  useEffect(() => {
    fetch('http://localhost:8080/api/health')
      .then(res => res.json())
      .then(data => setStatus(`バックエンド接続成功: ${data.status}`))
      .catch(() => setStatus('バックエンド接続失敗'))
  }, [])

  return (
    <div>
      <h1>TODO アプリ</h1>
      <p>{status}</p>
    </div>
  )
}

export default App
```

フロントを起動:

```bash
cd frontend
npm run dev
```

http://localhost:5173 にアクセスして **「バックエンド接続成功: ok」** が表示されれば CORS 設定完了。

もし「バックエンド接続失敗」が表示された場合:
- ブラウザの DevTools → Console で CORS エラーが出ていないか確認
- バックエンドが起動しているか確認（別ターミナルのログ）
- `allowedOrigins` のポート番号が `5173` になっているか確認

### 5-4. 通信確認後の App.tsx を戻す

テストが終わったら `App.tsx` を元のシンプルな状態に戻す:

```tsx
import './App.css'

function App() {
  return (
    <div>
      <h1>TODO アプリ</h1>
      <p>準備完了</p>
    </div>
  )
}

export default App
```

HealthController はそのまま残しておいてよい（本番でもヘルスチェックに使える）。

### 5-5. コミット・push

```bash
cd todo-app
git add .
git commit -m "#4 CORS 設定 + ヘルスチェックエンドポイント"
git push origin main
```

GitHub 上で Issue #4 をクローズする。

---

## Phase 1 完了チェック

全て完了したら確認:

- [ ] GitHub に Issue #1〜#4 が作成され、全てクローズされている
- [ ] ラベル 6 種類、マイルストーン 4 つが設定されている
- [ ] `./gradlew bootRun` でバックエンドが起動する
- [ ] http://localhost:8080/h2-console にアクセスできる
- [ ] http://localhost:8080/api/health で `{"status":"ok"}` が返る
- [ ] `npm run dev` でフロントエンドが起動する
- [ ] http://localhost:5173 に画面が表示される
- [ ] GitHub に 4 つのコミットが push されている

```
todo-app/
├── .github/workflows/ci.yml      ← 空ファイル（Phase 4 で実装）
├── .gitignore
├── README.md
├── PROJECT_CONTEXT.md
├── backend/
│   ├── src/main/java/com/example/todo/
│   │   ├── TodoApplication.java
│   │   ├── config/
│   │   │   └── WebConfig.java     ← CORS 設定
│   │   └── controller/
│   │       └── HealthController.java ← ヘルスチェック
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── build.gradle
│   └── ...
└── frontend/
    ├── src/
    │   ├── main.tsx
    │   ├── App.tsx
    │   ├── components/             ← 空（Phase 3 で実装）
    │   ├── hooks/                  ← 空（Phase 3 で実装）
    │   ├── lib/                    ← 空（Phase 3 で実装）
    │   └── types/                  ← 空（Phase 3 で実装）
    ├── index.html
    ├── vite.config.ts
    ├── package.json
    └── ...
```

**ここまで完了したら Phase 2（バックエンド API 実装 + テスト）に進む準備ができています。**
