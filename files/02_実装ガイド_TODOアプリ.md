# TODO アプリ 実装ガイド

**対象**: 要件定義書（01_要件定義書_TODOアプリ.md）に基づく実装手順  
**方針**: バックエンド → フロントエンド → 統合の順で段階的に構築  
**テスト**: 各フェーズで実装と並行してテストを書く（テスト後回しにしない）

---

## 全体の流れ

```
Phase 1: プロジェクト初期設定（v0.1）
    ↓
Phase 2: バックエンド API 実装 + テスト（v0.2）
    ↓
Phase 3: フロントエンド実装 + テスト（v0.3）
    ↓
Phase 4: 統合・仕上げ（v0.4）
```

各 Phase の中で GitHub Issue を作成し、Issue 単位で実装 → テスト → コミット → push のサイクルを回す。

---

## Phase 1: プロジェクト初期設定（1-2日）

### 1.1 GitHub リポジトリ作成

**Issue**: `#1 プロジェクトの初期セットアップ`

やること:

1. GitHub で `todo-app` リポジトリを作成
2. ローカルに clone
3. `README.md` と `PROJECT_CONTEXT.md` を作成
4. `.github/workflows/ci.yml` の空ファイルを用意
5. ラベルとマイルストーンを GitHub 上で設定

```bash
git clone https://github.com/<username>/todo-app.git
cd todo-app
```

### 1.2 バックエンドプロジェクト作成

**Issue**: `#2 Spring Boot プロジェクトの初期設定`

やること:

1. [Spring Initializr](https://start.spring.io/) で以下の設定でプロジェクト生成
   - Gradle - Groovy
   - Java 17
   - Spring Boot 3.x
   - Dependencies: Spring Web, Spring Data JPA, H2 Database, Validation, Spring Boot DevTools
2. 生成した ZIP を `backend/` ディレクトリに展開
3. `application.yml` を設定

```yaml
# backend/src/main/resources/application.yml
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
  h2:
    console:
      enabled: true
      path: /h2-console

server:
  port: 8080
```

4. 起動確認: `./gradlew bootRun` → `http://localhost:8080` にアクセス

**確認ポイント**: アプリが起動し、H2 Console にアクセスできること

### 1.3 フロントエンドプロジェクト作成

**Issue**: `#3 React + Vite プロジェクトの初期設定`

やること:

1. Vite で React + TypeScript プロジェクト作成

```bash
cd todo-app
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
```

2. 不要なボイラープレートを削除
   - `src/App.css` の中身をクリア
   - `src/App.tsx` をシンプルな内容に書き換え
   - `src/assets/` の不要ファイル削除
3. 起動確認: `npm run dev` → `http://localhost:5173`

**確認ポイント**: フロントが起動し、画面が表示されること

### 1.4 CORS 設定

**Issue**: `#4 CORS 設定`

フロント（:5173）からバックエンド（:8080）への通信を許可する。

```java
// backend/src/main/java/com/example/todo/config/WebConfig.java
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

**Phase 1 完了時の状態**: 両プロジェクトが起動し、CORS が設定済み。

---

## Phase 2: バックエンド API 実装 + テスト（3-4日）

### 実装順序の考え方

```
Entity → Repository → Service → Controller
（データに近い層から外側に向かって実装する）
```

各層を実装したら、その層のテストも書く。次の層に進む前にテストが通ることを確認する。

---

### 2.1 Entity 作成

**Issue**: `#5 Todo Entity の作成`

```java
// backend/src/main/java/com/example/todo/entity/Todo.java
@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // コンストラクタ、getter、setter
}
```

**学習ポイント**:
- `@PrePersist` / `@PreUpdate` で日時を自動設定する JPA のライフサイクルコールバック
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` による自動採番

---

### 2.2 DTO 作成

**Issue**: `#6 リクエスト/レスポンス DTO の作成`

```java
// TodoRequest.java — クライアントからのリクエスト
public class TodoRequest {
    @NotBlank(message = "タイトルは必須です")
    @Size(max = 100, message = "タイトルは100文字以内です")
    private String title;

    private Boolean completed;

    // getter, setter
}

// TodoResponse.java — クライアントへのレスポンス
public class TodoResponse {
    private Long id;
    private String title;
    private Boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity から DTO に変換する static メソッド
    public static TodoResponse from(Todo todo) {
        TodoResponse response = new TodoResponse();
        response.id = todo.getId();
        response.title = todo.getTitle();
        response.completed = todo.getCompleted();
        response.createdAt = todo.getCreatedAt();
        response.updatedAt = todo.getUpdatedAt();
        return response;
    }
}

// ErrorResponse.java — エラーレスポンス
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
}
```

**学習ポイント**:
- Entity をそのまま返さず DTO に変換する理由（レイヤー間の疎結合、不要なフィールドの非公開）
- Bean Validation アノテーション（`@NotBlank`, `@Size`）の使い方

---

### 2.3 Repository 作成 + テスト

**Issue**: `#7 TodoRepository の作成とテスト`

#### 実装

```java
// TodoRepository.java
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByCompleted(Boolean completed);
    List<Todo> findAllByOrderByCreatedAtDesc();
}
```

#### テスト

```java
// TodoRepositoryTest.java
@DataJpaTest
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Test
    void 全件取得_作成日時の降順で返される() {
        // Given: 2件のTODOを作成
        Todo todo1 = new Todo();
        todo1.setTitle("最初のTODO");
        todoRepository.save(todo1);

        Todo todo2 = new Todo();
        todo2.setTitle("2番目のTODO");
        todoRepository.save(todo2);

        // When
        List<Todo> todos = todoRepository.findAllByOrderByCreatedAtDesc();

        // Then
        assertThat(todos).hasSize(2);
    }

    @Test
    void 完了状態でフィルタリングできる() {
        // Given
        Todo activeTodo = new Todo();
        activeTodo.setTitle("未完了");
        activeTodo.setCompleted(false);
        todoRepository.save(activeTodo);

        Todo completedTodo = new Todo();
        completedTodo.setTitle("完了済み");
        completedTodo.setCompleted(true);
        todoRepository.save(completedTodo);

        // When
        List<Todo> activeTodos = todoRepository.findByCompleted(false);

        // Then
        assertThat(activeTodos).hasSize(1);
        assertThat(activeTodos.get(0).getTitle()).isEqualTo("未完了");
    }
}
```

**学習ポイント**:
- `@DataJpaTest` は JPA 関連の Bean のみロードし、H2 インメモリ DB を使う
- テストメソッド名を日本語にすると何をテストしているか一目瞭然
- Given-When-Then パターンでテストを構造化

---

### 2.4 Service 作成 + テスト

**Issue**: `#8 TodoService の作成とテスト`

#### 実装

```java
// TodoService.java（インターフェース）
public interface TodoService {
    List<TodoResponse> findAll(String status);
    TodoResponse create(TodoRequest request);
    TodoResponse update(Long id, TodoRequest request);
    TodoResponse toggleComplete(Long id);
    void delete(Long id);
}

// TodoServiceImpl.java
@Service
@Transactional(readOnly = true)
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;

    public TodoServiceImpl(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    public List<TodoResponse> findAll(String status) {
        List<Todo> todos;
        switch (status.toUpperCase()) {
            case "ACTIVE":
                todos = todoRepository.findByCompleted(false);
                break;
            case "COMPLETED":
                todos = todoRepository.findByCompleted(true);
                break;
            default:
                todos = todoRepository.findAllByOrderByCreatedAtDesc();
                break;
        }
        return todos.stream()
            .map(TodoResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TodoResponse create(TodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        Todo saved = todoRepository.save(todo);
        return TodoResponse.from(saved);
    }

    @Override
    @Transactional
    public TodoResponse update(Long id, TodoRequest request) {
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("TODO が見つかりません: id=" + id));
        todo.setTitle(request.getTitle());
        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
        }
        return TodoResponse.from(todoRepository.save(todo));
    }

    @Override
    @Transactional
    public TodoResponse toggleComplete(Long id) {
        Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("TODO が見つかりません: id=" + id));
        todo.setCompleted(!todo.getCompleted());
        return TodoResponse.from(todoRepository.save(todo));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new RuntimeException("TODO が見つかりません: id=" + id);
        }
        todoRepository.deleteById(id);
    }
}
```

#### テスト

```java
// TodoServiceTest.java
@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoServiceImpl todoService;

    @Test
    void 新しいTODOを作成できる() {
        // Given
        TodoRequest request = new TodoRequest();
        request.setTitle("テスト TODO");

        Todo savedTodo = new Todo();
        savedTodo.setId(1L);
        savedTodo.setTitle("テスト TODO");
        savedTodo.setCompleted(false);

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        // When
        TodoResponse response = todoService.create(request);

        // Then
        assertThat(response.getTitle()).isEqualTo("テスト TODO");
        assertThat(response.getCompleted()).isFalse();
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void 存在しないIDで更新すると例外が発生する() {
        // Given
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        TodoRequest request = new TodoRequest();
        request.setTitle("更新");

        // When & Then
        assertThatThrownBy(() -> todoService.update(999L, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("TODO が見つかりません");
    }

    @Test
    void 完了状態をトグルできる() {
        // Given
        Todo todo = new Todo();
        todo.setId(1L);
        todo.setTitle("テスト");
        todo.setCompleted(false);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        // When
        TodoResponse response = todoService.toggleComplete(1L);

        // Then
        assertThat(todo.getCompleted()).isTrue();
    }
}
```

**学習ポイント**:
- `@Mock` と `@InjectMocks` で依存関係を差し替えた単体テスト
- `when(...).thenReturn(...)` でモックの振る舞いを定義
- `verify(...)` で特定メソッドが呼ばれたことを検証
- Service テストは DB に依存しない（Repository をモック化）

---

### 2.5 Controller 作成 + テスト

**Issue**: `#9 TodoController の作成とテスト`

#### 実装

```java
// TodoController.java
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAll(
            @RequestParam(defaultValue = "ALL") String status) {
        return ResponseEntity.ok(todoService.findAll(status));
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(
            @Valid @RequestBody TodoRequest request) {
        TodoResponse created = todoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.ok(todoService.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TodoResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.toggleComplete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### エラーハンドリング

```java
// GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

#### テスト

```java
// TodoControllerTest.java
@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void GET_全TODO取得_200を返す() throws Exception {
        // Given
        List<TodoResponse> todos = List.of(
            new TodoResponse(1L, "テスト1", false, LocalDateTime.now(), LocalDateTime.now())
        );
        when(todoService.findAll("ALL")).thenReturn(todos);

        // When & Then
        mockMvc.perform(get("/api/todos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("テスト1"));
    }

    @Test
    void POST_正常なリクエストで201を返す() throws Exception {
        // Given
        TodoRequest request = new TodoRequest();
        request.setTitle("新しい TODO");

        TodoResponse response = new TodoResponse(
            1L, "新しい TODO", false, LocalDateTime.now(), LocalDateTime.now()
        );
        when(todoService.create(any(TodoRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("新しい TODO"));
    }

    @Test
    void POST_タイトル空でバリデーションエラー_400を返す() throws Exception {
        // Given
        TodoRequest request = new TodoRequest();
        request.setTitle("");

        // When & Then
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void DELETE_存在するTODOを削除_204を返す() throws Exception {
        // Given
        doNothing().when(todoService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/todos/1"))
            .andExpect(status().isNoContent());
    }
}
```

**学習ポイント**:
- `@WebMvcTest` は Controller 層のみロード（Service はモック化）
- `MockMvc` で HTTP リクエストをシミュレーション
- `jsonPath` でレスポンス JSON の内容を検証
- ステータスコードの検証（`isOk()`, `isCreated()`, `isBadRequest()`, `isNoContent()`）

---

### 2.6 Swagger UI の追加

**Issue**: `#10 Swagger UI の設定`

1. `build.gradle` に依存追加:

```groovy
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

2. 起動後 `http://localhost:8080/swagger-ui.html` で API を確認

**確認ポイント**: Swagger UI から全エンドポイントを手動テストし、期待通りのレスポンスが返ること

---

### Phase 2 完了チェック

- [ ] 全エンドポイントが Swagger UI で動作確認済み
- [ ] Repository テスト: 全パス
- [ ] Service テスト: 全パス
- [ ] Controller テスト: 全パス
- [ ] `./gradlew test` で全テスト GREEN

---

## Phase 3: フロントエンド実装 + テスト（3-4日）

### 3.1 型定義と API クライアント

**Issue**: `#11 型定義と API クライアントの作成`

#### 型定義

```typescript
// src/types/todo.ts
export type Todo = {
  id: number;
  title: string;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
};

export type TodoRequest = {
  title: string;
  completed?: boolean;
};

export type FilterStatus = "ALL" | "ACTIVE" | "COMPLETED";
```

#### API クライアント

```typescript
// src/lib/api.ts
const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function fetchTodos(status: string = "ALL"): Promise<Todo[]> {
  const res = await fetch(`${API_BASE}/api/todos?status=${status}`);
  if (!res.ok) throw new Error("TODO の取得に失敗しました");
  return res.json();
}

export async function createTodo(title: string): Promise<Todo> {
  const res = await fetch(`${API_BASE}/api/todos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title }),
  });
  if (!res.ok) {
    const error = await res.json();
    throw new Error(error.message || "TODO の作成に失敗しました");
  }
  return res.json();
}

export async function updateTodo(id: number, data: TodoRequest): Promise<Todo> {
  const res = await fetch(`${API_BASE}/api/todos/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("TODO の更新に失敗しました");
  return res.json();
}

export async function toggleTodo(id: number): Promise<Todo> {
  const res = await fetch(`${API_BASE}/api/todos/${id}/toggle`, {
    method: "PATCH",
  });
  if (!res.ok) throw new Error("TODO の切り替えに失敗しました");
  return res.json();
}

export async function deleteTodo(id: number): Promise<void> {
  const res = await fetch(`${API_BASE}/api/todos/${id}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("TODO の削除に失敗しました");
}
```

**ポイント**: Vite では環境変数に `VITE_` プレフィックスが必要。`import.meta.env.VITE_API_URL` で取得する。

---

### 3.2 カスタム Hook 作成

**Issue**: `#12 useTodos カスタム Hook の作成`

TODO の状態管理と API 呼び出しを 1 つの Hook にまとめる。コンポーネントから直接 API を呼ばず、Hook 経由で操作する。

```typescript
// src/hooks/useTodos.ts
import { useState, useEffect, useCallback } from "react";
import { Todo, FilterStatus } from "../types/todo";
import * as api from "../lib/api";

export function useTodos() {
  const [todos, setTodos] = useState<Todo[]>([]);
  const [filter, setFilter] = useState<FilterStatus>("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // TODO 一覧取得
  const loadTodos = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await api.fetchTodos(filter);
      setTodos(data);
    } catch (err) {
      setError("TODO の読み込みに失敗しました");
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    loadTodos();
  }, [loadTodos]);

  // TODO 作成
  const addTodo = async (title: string) => {
    const created = await api.createTodo(title);
    setTodos((prev) => [created, ...prev]);
  };

  // 完了状態トグル
  const toggleTodo = async (id: number) => {
    const updated = await api.toggleTodo(id);
    setTodos((prev) =>
      prev.map((todo) => (todo.id === id ? updated : todo))
    );
  };

  // TODO 削除
  const removeTodo = async (id: number) => {
    await api.deleteTodo(id);
    setTodos((prev) => prev.filter((todo) => todo.id !== id));
  };

  return {
    todos,
    filter,
    loading,
    error,
    setFilter,
    addTodo,
    toggleTodo,
    removeTodo,
    reload: loadTodos,
  };
}
```

**学習ポイント**:
- カスタム Hook でロジックを切り出すことで、コンポーネントは表示に集中できる
- `useCallback` で不要な再レンダリングを防止
- 楽観的更新: API 成功後にローカルの state を即座に更新（再フェッチ不要）

---

### 3.3 コンポーネント実装

**Issue**: `#13 TodoForm コンポーネントの実装`
**Issue**: `#14 TodoItem コンポーネントの実装`
**Issue**: `#15 TodoFilter コンポーネントの実装`
**Issue**: `#16 TodoList コンポーネントの実装（App に組み込み）`

#### 実装順序

```
TodoForm（入力フォーム）
    ↓
TodoItem（1件の表示）
    ↓
TodoFilter（フィルターボタン）
    ↓
TodoList（一覧表示）
    ↓
App.tsx（全体を組み合わせ）
```

小さいコンポーネントから作り、最後に組み合わせる。

#### TodoForm

```tsx
// src/components/TodoForm.tsx
import { useState } from "react";
import styles from "./TodoForm.module.css";

type Props = {
  onSubmit: (title: string) => Promise<void>;
};

export default function TodoForm({ onSubmit }: Props) {
  const [title, setTitle] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = title.trim();
    if (!trimmed) {
      setError("タイトルを入力してください");
      return;
    }
    setIsSubmitting(true);
    setError("");
    try {
      await onSubmit(trimmed);
      setTitle("");
    } catch (err) {
      setError("追加に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.form}>
      <input
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder="新しい TODO を入力..."
        className={styles.input}
        disabled={isSubmitting}
      />
      <button
        type="submit"
        disabled={isSubmitting}
        className={styles.button}
      >
        追加
      </button>
      {error && <p className={styles.error}>{error}</p>}
    </form>
  );
}
```

#### TodoItem

```tsx
// src/components/TodoItem.tsx
import styles from "./TodoItem.module.css";
import { Todo } from "../types/todo";

type Props = {
  todo: Todo;
  onToggle: (id: number) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
};

export default function TodoItem({ todo, onToggle, onDelete }: Props) {
  return (
    <li className={styles.item}>
      <label className={styles.label}>
        <input
          type="checkbox"
          checked={todo.completed}
          onChange={() => onToggle(todo.id)}
          className={styles.checkbox}
        />
        <span className={todo.completed ? styles.completed : styles.title}>
          {todo.title}
        </span>
      </label>
      <button
        onClick={() => onDelete(todo.id)}
        className={styles.deleteButton}
        aria-label={`${todo.title} を削除`}
      >
        削除
      </button>
    </li>
  );
}
```

#### TodoFilter

```tsx
// src/components/TodoFilter.tsx
import styles from "./TodoFilter.module.css";
import { FilterStatus } from "../types/todo";

type Props = {
  current: FilterStatus;
  onChange: (status: FilterStatus) => void;
};

const filters: { label: string; value: FilterStatus }[] = [
  { label: "すべて", value: "ALL" },
  { label: "未完了", value: "ACTIVE" },
  { label: "完了済み", value: "COMPLETED" },
];

export default function TodoFilter({ current, onChange }: Props) {
  return (
    <div className={styles.filter}>
      {filters.map(({ label, value }) => (
        <button
          key={value}
          onClick={() => onChange(value)}
          className={`${styles.button} ${current === value ? styles.active : ""}`}
        >
          {label}
        </button>
      ))}
    </div>
  );
}
```

#### App.tsx（全体の組み合わせ）

```tsx
// src/App.tsx
import { useTodos } from "./hooks/useTodos";
import TodoForm from "./components/TodoForm";
import TodoItem from "./components/TodoItem";
import TodoFilter from "./components/TodoFilter";
import styles from "./App.module.css";

export default function App() {
  const { todos, filter, loading, error, setFilter, addTodo, toggleTodo, removeTodo } =
    useTodos();

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>TODO アプリ</h1>
      <TodoForm onSubmit={addTodo} />
      <TodoFilter current={filter} onChange={setFilter} />
      {loading && <p>読み込み中...</p>}
      {error && <p className={styles.error}>{error}</p>}
      <ul className={styles.list}>
        {todos.map((todo) => (
          <TodoItem
            key={todo.id}
            todo={todo}
            onToggle={toggleTodo}
            onDelete={removeTodo}
          />
        ))}
      </ul>
      {!loading && todos.length === 0 && (
        <p className={styles.empty}>TODO がありません</p>
      )}
    </div>
  );
}
```

---

### 3.4 フロントエンドのテスト

**Issue**: `#17 フロントエンドテスト環境の設定（Vitest + RTL + MSW）`
**Issue**: `#18 コンポーネントテスト・API テストの作成`

#### テスト環境セットアップ

```bash
cd frontend
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom \
  @testing-library/user-event jsdom msw
```

Vite の設定にテスト設定を追加:

```typescript
// vite.config.ts
/// <reference types="vitest" />
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./src/test-setup.ts",
  },
});
```

```typescript
// src/test-setup.ts
import "@testing-library/jest-dom/vitest";
```

#### コンポーネントテストの例

```tsx
// src/__tests__/components/TodoForm.test.tsx
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TodoForm from "../../components/TodoForm";

describe("TodoForm", () => {
  const mockOnSubmit = vi.fn();

  beforeEach(() => {
    mockOnSubmit.mockClear();
  });

  it("入力して submit すると onSubmit が呼ばれる", async () => {
    const user = userEvent.setup();
    render(<TodoForm onSubmit={mockOnSubmit} />);

    const input = screen.getByPlaceholderText("新しい TODO を入力...");
    const button = screen.getByRole("button", { name: "追加" });

    await user.type(input, "テスト TODO");
    await user.click(button);

    expect(mockOnSubmit).toHaveBeenCalledWith("テスト TODO");
  });

  it("空のまま submit するとエラーが表示される", async () => {
    const user = userEvent.setup();
    render(<TodoForm onSubmit={mockOnSubmit} />);

    const button = screen.getByRole("button", { name: "追加" });
    await user.click(button);

    expect(screen.getByText("タイトルを入力してください")).toBeInTheDocument();
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });
});
```

```tsx
// src/__tests__/components/TodoItem.test.tsx
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TodoItem from "../../components/TodoItem";
import { Todo } from "../../types/todo";

describe("TodoItem", () => {
  const todo: Todo = {
    id: 1,
    title: "テスト TODO",
    completed: false,
    createdAt: "2026-03-13T10:00:00",
    updatedAt: "2026-03-13T10:00:00",
  };

  it("TODO のタイトルが表示される", () => {
    render(<TodoItem todo={todo} onToggle={vi.fn()} onDelete={vi.fn()} />);
    expect(screen.getByText("テスト TODO")).toBeInTheDocument();
  });

  it("チェックボックスをクリックすると onToggle が呼ばれる", async () => {
    const mockToggle = vi.fn();
    const user = userEvent.setup();
    render(<TodoItem todo={todo} onToggle={mockToggle} onDelete={vi.fn()} />);

    await user.click(screen.getByRole("checkbox"));
    expect(mockToggle).toHaveBeenCalledWith(1);
  });

  it("削除ボタンをクリックすると onDelete が呼ばれる", async () => {
    const mockDelete = vi.fn();
    const user = userEvent.setup();
    render(<TodoItem todo={todo} onToggle={vi.fn()} onDelete={mockDelete} />);

    await user.click(screen.getByRole("button", { name: "テスト TODO を削除" }));
    expect(mockDelete).toHaveBeenCalledWith(1);
  });
});
```

#### MSW を使った API テストの例

```typescript
// src/__tests__/lib/api.test.ts
import { http, HttpResponse } from "msw";
import { setupServer } from "msw/node";
import { fetchTodos, createTodo } from "../../lib/api";

const server = setupServer(
  http.get("http://localhost:8080/api/todos", () => {
    return HttpResponse.json([
      { id: 1, title: "テスト", completed: false, createdAt: "...", updatedAt: "..." },
    ]);
  }),
  http.post("http://localhost:8080/api/todos", async ({ request }) => {
    const body = (await request.json()) as { title: string };
    return HttpResponse.json(
      { id: 2, title: body.title, completed: false, createdAt: "...", updatedAt: "..." },
      { status: 201 }
    );
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe("API クライアント", () => {
  it("fetchTodos で TODO 一覧を取得できる", async () => {
    const todos = await fetchTodos();
    expect(todos).toHaveLength(1);
    expect(todos[0].title).toBe("テスト");
  });

  it("createTodo で新しい TODO を作成できる", async () => {
    const todo = await createTodo("新規 TODO");
    expect(todo.title).toBe("新規 TODO");
    expect(todo.id).toBe(2);
  });

  it("サーバーエラー時に例外を投げる", async () => {
    server.use(
      http.get("http://localhost:8080/api/todos", () => {
        return new HttpResponse(null, { status: 500 });
      })
    );

    await expect(fetchTodos()).rejects.toThrow("TODO の取得に失敗しました");
  });
});
```

**学習ポイント**:
- Vitest は Vite ネイティブで Jest 互換のテストランナー。`vi.fn()` で Jest の `jest.fn()` と同じことができる
- React Testing Library はユーザーの操作をシミュレーション（`userEvent`）
- `screen.getByRole` でアクセシビリティベースの要素取得（`aria-label` が活きる）
- MSW はネットワークレベルで API をモックするため、実際の fetch が動く

---

### Phase 3 完了チェック

- [ ] TODO の作成・一覧表示・完了切り替え・削除が画面上で動作する
- [ ] フィルタリングが動作する
- [ ] バリデーションエラーが表示される
- [ ] コンポーネントテスト: 全パス
- [ ] API クライアントテスト: 全パス
- [ ] `npx vitest run` で全テスト GREEN

---

## Phase 4: 統合・仕上げ（2-3日）

### 4.1 統合テスト（手動確認）

**Issue**: `#19 統合テスト`

バックエンド + フロントエンドを同時に起動し、全操作を手動で確認する。

確認チェックリスト:

- [ ] TODO を新規作成できる
- [ ] TODO 一覧が表示される
- [ ] 完了/未完了を切り替えられる
- [ ] TODO を削除できる
- [ ] フィルタリングが動作する
- [ ] 空タイトルでエラーが表示される
- [ ] 存在しない ID への操作でエラーが適切に処理される

### 4.2 GitHub Actions CI

**Issue**: `#20 GitHub Actions CI の設定`

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: backend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: "temurin"
          java-version: "17"
      - name: Run tests
        run: ./gradlew test

  frontend-test:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: frontend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: "20"
      - run: npm ci
      - run: npx vitest run
```

### 4.3 ドキュメント整備

**Issue**: `#21 README とドキュメントの整備`

README.md に以下を記載する:

- プロジェクト概要
- 技術スタック
- セットアップ手順（バックエンド + フロントエンド）
- テストの実行方法
- API エンドポイント一覧
- スクリーンショット（完成後に追加）

### 4.4 振り返り・学習記録

**Issue**: `#22 プロジェクト振り返り`

daily-learning に振り返りを記録:
- 学んだ技術とパターン
- 苦労した点と解決方法
- code-snippets に追加するべきコード
- 次のプロジェクトへの改善点

---

## テスト戦略のまとめ

### バックエンドのテストピラミッド

```
        ┌─────────────┐
        │  Controller  │  @WebMvcTest + MockMvc
        │  テスト       │  → HTTP リクエスト/レスポンスの検証
        ├─────────────┤
        │  Service     │  JUnit 5 + Mockito
        │  テスト       │  → ビジネスロジックの検証
        ├─────────────┤
        │  Repository  │  @DataJpaTest + H2
        │  テスト       │  → DB アクセスの検証
        └─────────────┘
```

### フロントエンドのテスト構成

```
        ┌─────────────┐
        │ コンポーネント │  Vitest + React Testing Library
        │ テスト        │  → UI の表示・操作の検証
        ├─────────────┤
        │  API         │  MSW + Vitest
        │ テスト        │  → API 通信の検証
        └─────────────┘
```

### テストを書く際の心構え

1. **実装と同時にテストを書く** — 後回しにすると書かなくなる
2. **テストメソッド名は日本語で** — 何をテストしているか明確にする
3. **Given-When-Then** — テストの構造を統一する
4. **各層のテストは独立** — Service テストで DB は使わない、Controller テストで Service は使わない
5. **正常系 + 異常系** — エラーケースのテストも必ず書く

---

## GitHub Issue 一覧（テンプレート）

実装開始時に以下の Issue を作成する:

| # | タイトル | ラベル | マイルストーン |
|---|---------|--------|--------------|
| 1 | プロジェクトの初期セットアップ | setup | v0.1 |
| 2 | Spring Boot プロジェクトの初期設定 | setup, backend | v0.1 |
| 3 | React + Vite プロジェクトの初期設定 | setup, frontend | v0.1 |
| 4 | CORS 設定 | backend | v0.1 |
| 5 | Todo Entity の作成 | backend | v0.2 |
| 6 | リクエスト/レスポンス DTO の作成 | backend | v0.2 |
| 7 | TodoRepository の作成とテスト | backend, test | v0.2 |
| 8 | TodoService の作成とテスト | backend, test | v0.2 |
| 9 | TodoController の作成とテスト | backend, test | v0.2 |
| 10 | Swagger UI の設定 | backend, docs | v0.2 |
| 11 | 型定義と API クライアントの作成 | frontend | v0.3 |
| 12 | useTodos カスタム Hook の作成 | frontend | v0.3 |
| 13 | TodoForm コンポーネントの実装 | frontend | v0.3 |
| 14 | TodoItem コンポーネントの実装 | frontend | v0.3 |
| 15 | TodoFilter コンポーネントの実装 | frontend | v0.3 |
| 16 | TodoList + App への組み込み | frontend | v0.3 |
| 17 | フロントエンドテスト環境の設定 | frontend, test | v0.3 |
| 18 | コンポーネントテスト・API テストの作成 | frontend, test | v0.3 |
| 19 | 統合テスト | test | v0.4 |
| 20 | GitHub Actions CI の設定 | setup | v0.4 |
| 21 | README とドキュメントの整備 | docs | v0.4 |
| 22 | プロジェクト振り返り | docs | v0.4 |

---

## 困ったときのチェックリスト

### バックエンドが動かない

1. `./gradlew bootRun` のエラーメッセージを読む
2. H2 Console（`/h2-console`）でテーブルが作成されているか確認
3. Swagger UI でリクエストを送って確認
4. テストを実行してどの層で失敗しているか特定

### フロントエンドが動かない

1. ブラウザの DevTools > Console でエラーを確認
2. DevTools > Network で API リクエストが飛んでいるか確認
3. CORS エラーの場合はバックエンドの WebConfig を確認（ポートは `5173`）
4. `console.log` でレスポンスの中身を確認

### テストが通らない

1. エラーメッセージを読んで失敗箇所を特定
2. テストデータの準備（Given）が正しいか確認
3. モックの設定（`when` / `vi.fn()`）が正しいか確認
4. 実装側のバグか、テスト側の期待値が間違っているかを切り分ける

---

**このガイドの使い方**: Phase ごとに Issue を作成し、1 Issue ずつ実装 → テスト → コミット → push を繰り返す。焦らず、各ステップで動作確認をしてから次に進むこと。
