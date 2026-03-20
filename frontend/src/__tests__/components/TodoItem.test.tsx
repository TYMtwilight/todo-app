import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TodoItem from "../../components/TodoItem";
import type { Todo } from "../../types/todo";

describe("TodoItem", () => {
  const baseTodo: Todo = {
    id: 1,
    title:"テスト TODO",
    completed: false,
    createdAt: "2026-03-19T10:00:00",
    updatedAt: "2026-03-19T10:00:00",
  };

  it("TODO のタイトルが表示される", () => {
    render(<TodoItem todo={baseTodo} onToggle={vi.fn()} onDelete={vi.fn()} />);
  
    expect(screen.getByText("テスト TODO")).toBeInTheDocument();
  });

  it("未完了の TODO はチェックが外れている", () => {
    render(<TodoItem todo={baseTodo} onToggle={vi.fn()} onDelete={vi.fn()} />);

    expect(screen.getByRole("checkbox")).not.toBeChecked();
  });

  it("完了済みの TODO はチェックされている", () => {
    const completedTodo = { ...baseTodo, completed: true };
    render(<TodoItem todo={completedTodo} onToggle={vi.fn()} onDelete={vi.fn()} />);

    expect(screen.getByRole("checkbox")).toBeChecked();
  });

  it("チェックボックスをクリックすると onToggle が呼ばれる", async () => {
    const mockToggle = vi.fn();
    const user = userEvent.setup();
    render(<TodoItem todo={baseTodo} onToggle={mockToggle} onDelete={vi.fn()} />);

    await user.click(screen.getByRole("checkbox"))

    expect(mockToggle).toHaveBeenCalledWith(1);
  });

  it("削除ボタンをクリックすると onDelete が呼ばれる", async () => {
    const mockDelete = vi.fn();
    const user = userEvent.setup();
    render(<TodoItem todo={baseTodo} onToggle={vi.fn()} onDelete={mockDelete} />);

    await user.click(screen.getByRole("button", { name: "テスト TODO を削除"}));

    expect(mockDelete).toHaveBeenCalledWith(1);
  });

  it("削除ボタン のaria-label が正しい", () => {
    render(<TodoItem todo={baseTodo} onToggle={vi.fn()} onDelete={vi.fn()} />);

    expect(screen.getByRole("button", { name: "テスト TODO を削除"})).toBeInTheDocument();
  })

})