import styles from "./TodoFilter.module.css";
import type { FilterStatus } from "../types/todo";

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
      {filters.map(({label, value}) => (
        <button
          key={value}
          onClick={() => onChange(value)}
          className={`${styles.button} ${current == value ? styles.active : ""}`}
        >
          {label}
        </button>
      ))}
    </div>
  );
}