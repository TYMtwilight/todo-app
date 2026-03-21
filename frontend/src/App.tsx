import { useTodos } from "./hooks/useTodos";
import TodoForm from "./components/TodoForm";
import TodoItem from "./components/TodoItem";
import TodoFilter from "./components/TodoFilter";
import styles from "./App.module.css";
import type { Todo } from "./types/todo";

export default function App() {
  const {
    todos,
    filter,
    loading,
    error,
    setFilter,
    addTodo,
    toggleTodo,
    removeTodo,
  } = useTodos();

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>TODO アプリ</h1>
      <TodoForm onSaveTodo={addTodo} />
      <TodoFilter current={filter} onChange={setFilter} />
      {loading && <p className={styles.message}>読み込み中...</p>}
      {error && <p className={styles.error}>{error}</p>}
      <ul className={styles.list}>
        {todos.map((todo: Todo) => (
          <TodoItem
            key={todo.id}
            todo={todo}
            onToggle={toggleTodo}
            onDelete={removeTodo}
          />
        ))}
      </ul>
      {!loading && todos.length === 0 && (
        <p className={styles.message}>TODO がありません</p>
      )}
    </div>
  )
}