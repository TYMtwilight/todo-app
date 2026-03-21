import type { Todo, TodoRequest } from "../types/todo";

const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

/**
 * タスク一覧取得
 * GET /api/todos?status=ALL|ACTIVE|COMPLETED
 */
export async function fetchTodos(status: string = "ALL"): Promise<Todo[]> {
  const res = await fetch(`${API_BASE}/api/todos?status=${status}`);
  if (!res.ok) throw new Error("TODO の取得に失敗しました");
  return res.json();
}

/**
 * TODO 作成
 * POST /api/todos
 */
export async function createTodo(title: string): Promise<Todo> {
  const res = await fetch(`${API_BASE}/api/todos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title }),
  });
  if (!res.ok) {
    const error = await res.json();
    throw new Error(error.mesage || "TODO の作成に失敗しました");
  }
  return res.json();
}

/**
 * TODO 更新
 * PUT /api/todos/{id}
 */
export async function updateTodo(id: number, data: TodoRequest): Promise<Todo> {
  const res = await fetch(`${API_BASE}/api/todos/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("TODO の更新に失敗しました");
  return res.json();
}

/**
 * 完了状態トグル
 * PATCH /api/todos/{id}/toggle
 */
export async function toggleTodo(id: number): Promise<Todo> {
  const res = await fetch(`${API_BASE}/api/todos/${id}/toggle`, {
    method: "PATCH",
  });
  if (!res.ok) throw new Error("TODO の切り替えに失敗しました");
  return res.json();
}

/**
 * TODO 削除
 * DELETE /api/todos/{id}
 */
export async function deleteTodo(id: number): Promise<void> {
  const res = await fetch(`${API_BASE}/api/todos/${id}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("TODO の削除に失敗しました");
}
