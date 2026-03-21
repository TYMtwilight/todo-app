import { useState, useEffect, useCallback } from "react";
import type { Todo, FilterStatus } from "../types/todo";
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
    } catch {
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
  }

  // TODO 削除
  const removeTodo = async (id: number) => {
    await api.deleteTodo(id);
    setTodos((prev) => prev.filter((todo) => todo.id !== id));
  }

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
  }
}

