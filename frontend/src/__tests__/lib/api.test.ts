import { http, HttpResponse } from "msw";
import { setupServer } from "msw/node";
import { fetchTodos, createTodo, updateTodo, toggleTodo, deleteTodo } from "../../lib/api";

//--- モックサーバーの定義 ---

const mockTodo = {
  id: 1,
  title: "テスト TODO",
  completed: false,
  createdAt: "2026-03-19T10:00:00",
  updatedAt: "2026-03-19T10:00:00"
};

const server = setupServer(
  // GET /api/todos
  http.get("http://localhost:8080/api/todos", () => {
    return HttpResponse.json([mockTodo]);
  }),

  // POST /api/todos
  http.post("http://localhost:8080/api/todos", async ({ request } : { request: Request }) => {
    const body = (await request.json()) as { title: string };
    return HttpResponse.json(
      { ...mockTodo, id: 2, title: body.title },
      { status: 201 }
    );
  }),

  // PUT /api/todos/:id
  http.put("http://localhost:8080/api/todos/:id", async({ request } : { request: Request }) => {
    const body = (await request.json()) as { title: string, completed: boolean };
    return HttpResponse.json({ ...mockTodo, ...body });
  }),

  // PATCH /api/todos/:id/toggle
  http.patch("http://localhost:8080/api/todos/:id/toggle", () => {
    return HttpResponse.json({ ...mockTodo, completed: true });
  }),

  // DELETE /api/todos/:id
  http.delete("http://localhost:8080/api/todos/:id", () => {
    return new HttpResponse(null, { status: 204 });
  })
);

// テストのライフサイクル
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// --- テスト ---

describe("API クライアント", () => {
  // --------------------------------------------------
  // fetchTodos
  // --------------------------------------------------

  it("fetchTodos で TODO 一覧を取得できる", async () => {
    const todos = await fetchTodos();

    expect(todos).toHaveLength(1);
    expect(todos[0].title).toBe("テスト TODO");
    expect(todos[0].completed).toBe(false);
  });

  it("fetchTodos にステータスを渡せる", async () => {
    const todos = await fetchTodos("ACTIVE");

    expect(todos).toHaveLength(1);
  });

  it("fetchTodos でサーバーエラー時に例外を投げる", async () => {
    server.use(
      http.get("http://localhost:8080/api/todos", () => {
        return new HttpResponse(null, { status: 500 });
      })
    );

    await expect(fetchTodos()).rejects.toThrow("TODO の取得に失敗しました");
  });

  // --------------------------------------------------
  // createTodo
  // --------------------------------------------------

  it("createTodo で新しい TODO を作成できる", async () => {
    const todo = await createTodo("新規 TODO");

    expect(todo.title).toBe("新規 TODO");
    expect(todo.id).toBe(2);
  });

  it("createTodo でバリデーションエラー時に例外を投げる", async () => {
    server.use(
      http.post("http://localhost:8080/api/todos", () => {
        return HttpResponse.json(
          { status: 400, error: "Bad Request", message: "タイトルは必須です" },
          { status: 400 }
        );
      })
    );

    await expect(createTodo("")).rejects.toThrow("TODO の作成に失敗しました");
  })

  // --------------------------------------------------
  // updateTodo
  // --------------------------------------------------

  it("updateTodo で TODO を更新できる", async () => {
    const todo = await updateTodo(1, { title: "更新された TODO", completed: false});

    expect(todo.title).toBe("更新された TODO");
    expect(todo.completed).toBe(false);
    expect(todo.id).toBe(1);
  });

  it("updateTodo でバリデーションエラー時に例外を投げる", async () => {
    server.use(
      http.put("http://localhost:8080/api/todos/:id", () => {
        return HttpResponse.json(
          { status: 400, error: "Bad Request", message: "タイトルは必須です" },
          { status: 400 }
        );
      })
    );

    await expect(updateTodo(1, { title: "", completed: false })).rejects.toThrow("TODO の更新に失敗しました");
  });

  it("updateTodo で存在しない ID はエラーになる", async () => {
    server.use(
      http.put("http://localhost:8080/api/todos/:id", () => {
        return HttpResponse.json(
          { status: 404, message: "TODO が見つかりません" },
          { status: 404 }
        );
      })
    );

    await expect(updateTodo(9999, { title: "更新", completed: false })).rejects.toThrow("TODO の更新に失敗しました");
  });

  // --------------------------------------------------
  // toggleTodo
  // --------------------------------------------------

  it("toggleTodo で完了状態を切り替えられる", async () => {
    const todo = await toggleTodo(1);

    expect(todo.completed).toBe(true);
  });

  // --------------------------------------------------
  // deleteTodo
  // --------------------------------------------------
  
  it("deleteTodo で TODO を削除できる", async () => {
    await expect(deleteTodo(1)).resolves.toBeUndefined();
  })

  it("deleteTodo で存在しない ID はエラーになる", async () => {
    server.use(
      http.delete("http://localhost:8080/api/todos/:id", () => {
        return HttpResponse.json(
          { status: 404, message: "TODO が見つかりません" },
          { status: 404}
        );
      })
    );

    await expect(deleteTodo(9999)).rejects.toThrow("TODO の削除に失敗しました");
  })
})