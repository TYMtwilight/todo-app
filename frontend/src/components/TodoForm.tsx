import { useState } from "react";
import styles from "./TodoForm.module.css";

type Props = {
  onSaveTodo: (title: string) => Promise<void>;
};

export default function TodoForm({ onSaveTodo }: Props) {
  const [title, setTitle] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    const trimmed = title.trim();
    if (!trimmed) {
      setError("タイトルを入力してください");
      return;  
    }
    setIsSubmitting(true);
    setError("");
    try {
      await onSaveTodo(trimmed);
      setTitle("");
    } catch {
      setError("追加に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.form}>
      <div className={styles.inputRow}>
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
      </div>
      { error && <p className={styles.error}>{error}</p> }
    </form>
  );
}