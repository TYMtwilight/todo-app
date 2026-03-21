import { useState, useRef, useEffect } from "react";
import styles from "./TodoItem.module.css";
import type { Todo } from "../types/todo";

type Props = {
  todo: Todo;
  onToggle: (id: number) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
  onEdit: (id: number, title: string) => Promise<void>;
};

export default function TodoItem({ todo, onToggle, onDelete, onEdit }: Props) {
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState(todo.title);
  const inputRef = useRef<HTMLInputElement>(null);
  
  // 編集モードに入ったら入力欄にフォーカス
  useEffect(() => {
    if (isEditing && inputRef.current) {
      inputRef.current.focus();
      inputRef.current.select();
    }
  }, [isEditing]);

  // 編集を確定する
  const handleSubmit = async() => {
    const trimmed = editTitle.trim();
    if(!trimmed) {
      // 空なら元にもどす
      setEditTitle(todo.title);
      setIsEditing(false);
      return;
    }
    if(trimmed === todo.title) {
      // 変更なしなら閉じるだけ
      setIsEditing(false);
      return;
    }
    try {
      await onEdit(todo.id, trimmed);
      setIsEditing(false)
    } catch {
      // 失敗したら元に戻す
      setEditTitle(todo.title);
      setIsEditing(false);
    }
  };

  // キー操作
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if(e.key === "Enter") {
      handleSubmit();
    }
    if(e.key === "Escape") {
      setEditTitle(todo.title);
      setIsEditing(false);
    }
  }

  return (
    <li className={styles.item}>
      <label className={styles.label}>
        <input 
          type="checkbox"
          checked={todo.completed}
          onChange={() => onToggle(todo.id)}
          className={styles.checkbox}
        />
        {isEditing ? (
          <input
            ref={inputRef}
            type="text"
            value={editTitle}
            onChange={(e) => setEditTitle(e.target.value)}
            onBlur={handleSubmit}
            onKeyDown={handleKeyDown}
            className={styles.editInput}
            aria-label="タイトルを編集"
          />
        ) : (
          <span className={todo.completed ? styles.completed : styles.title}>
            {todo.title}
          </span>
        )}
      </label>
      <button
        onClick={() => setIsEditing(true)}
        className={styles.editButton}
        aria-label={`${todo.title} を編集`}
      >
        編集
      </button>
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