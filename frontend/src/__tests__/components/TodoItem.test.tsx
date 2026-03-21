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

  const defaultProps = {
    todo: baseTodo,
    onToggle: vi.fn(),
    onDelete: vi.fn(),
    onEdit: vi.fn(),
  };

  beforeEach(() => {
    defaultProps.onToggle = vi.fn();
    defaultProps.onDelete = vi.fn();
    defaultProps.onEdit = vi.fn().mockResolvedValue(undefined);
  })

  // --------------------------------------------------
  // 表示
  // --------------------------------------------------
  
  it("TODO のタイトルが表示される", () => {
    render(<TodoItem {...defaultProps} />);
  
    expect(screen.getByText("テスト TODO")).toBeInTheDocument();
  });

  it("未完了の TODO はチェックが外れている", () => {
    render(<TodoItem {...defaultProps} />);

    expect(screen.getByRole("checkbox")).not.toBeChecked();
  });

  it("完了済みの TODO はチェックされている", () => {
    render(<TodoItem {...defaultProps} todo={{ ...baseTodo, completed: true}} />);

    expect(screen.getByRole("checkbox")).toBeChecked();
  });

  // --------------------------------------------------
  // トグル・削除
  // --------------------------------------------------

  it("チェックボックスをクリックすると onToggle が呼ばれる", async () => {
    const mockToggle = vi.fn();
    const user = userEvent.setup();
    render(<TodoItem {...defaultProps} onToggle={mockToggle} />);

    await user.click(screen.getByRole("checkbox"))

    expect(mockToggle).toHaveBeenCalledWith(1);
  });

  it("削除ボタンをクリックすると onDelete が呼ばれる", async () => {
    const mockDelete = vi.fn();
    const user = userEvent.setup();
    render(<TodoItem {...defaultProps} onDelete={mockDelete} />);

    await user.click(screen.getByRole("button", { name: "テスト TODO を削除"}));

    expect(mockDelete).toHaveBeenCalledWith(1);
  });

  it("削除ボタン のaria-label が正しい", () => {
    render(<TodoItem {...defaultProps} />);

    expect(screen.getByRole("button", { name: "テスト TODO を削除"})).toBeInTheDocument();
  })

  // --------------------------------------------------
  // インライン編集
  // --------------------------------------------------

  it("編集ボタンをクリックすると入力欄に切り替わる", async () => {
    const user = userEvent.setup();
    render(<TodoItem {...defaultProps} />);

    await user.click(screen.getByRole("button", { name: "テスト TODO を編集" }));

    expect(screen.getByLabelText("タイトルを編集")).toBeInTheDocument();
    expect(screen.getByLabelText("タイトルを編集")).toHaveValue("テスト TODO");
  });

  it("編集して Enter で確定すると onEdit が呼ばれる", async () => {
    const user = userEvent.setup();
    render(<TodoItem {...defaultProps} />);

    await user.click(screen.getByRole("button", { name: "テスト TODO を編集" }));

    const input = screen.getByLabelText("タイトルを編集");
    await user.clear(input);
    await user.type(input, "更新後タイトル{Enter}");

    expect(defaultProps.onEdit).toHaveBeenCalledWith(1, "更新後タイトル");
  });

  it("Escape を押すと編集がキャンセルさせる", async () => {
    const user = userEvent.setup();
    render(<TodoItem {...defaultProps} />);

    await user.click(screen.getByRole("button", { name: "テスト TODO を編集" }));

    const input = screen.getByLabelText("タイトルを編集");
    await user.clear(input);
    await user.type(input, "変更中{Escape}");

    // 入力欄が消えて元のタイトルが表示される
    expect(screen.queryByLabelText("タイトルを編集")).not.toBeInTheDocument();
    expect(screen.getByText("テスト TODO")).toBeInTheDocument();
    expect(defaultProps.onEdit).not.toHaveBeenCalled();
  });

  it("空のまま確定すると編集がキャンセルされる", async () => {
    const user = userEvent.setup();
    render(<TodoItem {...defaultProps} />);

    await user.click(screen.getByRole("button", { name: "テスト TODO を編集" }));

    const input = screen.getByLabelText("タイトルを編集");
    await user.clear(input);
    await user.type(input, "{Enter}");
    
    expect(screen.getByText("テスト TODO")).toBeInTheDocument();
    expect(defaultProps.onEdit).not.toHaveBeenCalled();
  });

  it("タイトル変更なしで確定すると API を呼ばない", async () => {
    const user = userEvent.setup();
    render(<TodoItem {...defaultProps} />);

    await user.click(screen.getByRole("button", { name: "テスト TODO を編集" }));

    const input = screen.getByLabelText("タイトルを編集");
    await user.type(input, "{Enter}");

    expect(defaultProps.onEdit).not.toHaveBeenCalled();
  })
})