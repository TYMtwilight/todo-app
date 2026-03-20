import styles from "./TodoItem.module.css";
import type { Todo } from "../types/todo";

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