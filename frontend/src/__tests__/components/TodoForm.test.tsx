import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TodoForm from "../../components/TodoForm";

describe("TodoForm", () => {
  const mockOnSaveTodo = vi.fn();

  beforeEach(() => {
    mockOnSaveTodo.mockReset();
    mockOnSaveTodo.mockResolvedValue(undefined);
  })

  it("入力欄と追加ボタンが表示される", () => {
    render(<TodoForm onSaveTodo={mockOnSaveTodo} />);

    expect(screen.getByPlaceholderText("新しい TODO を入力...")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "追加"})).toBeInTheDocument();
  });

  it("入力して submit すると onSubmit が呼ばれる", async () => {
    const user = userEvent.setup();
    render(<TodoForm onSaveTodo={mockOnSaveTodo} />);

    await user.type(screen.getByPlaceholderText("新しい TODO を入力..."), "テスト TODO");
    await user.click(screen.getByRole("button", { name: "追加" }));

    expect(mockOnSaveTodo).toHaveBeenCalledWith("テスト TODO");
  });

  it("前後のスペースはトリムされる", async () => {
    const user = userEvent.setup();
    render(<TodoForm onSaveTodo={mockOnSaveTodo} />);

    await user.type(screen.getByPlaceholderText("新しい TODO を入力..."), "　　  テスト TODO  　　");
    await user.click(screen.getByRole("button", { name: "追加" }));

    expect(mockOnSaveTodo).toHaveBeenCalledWith("テスト TODO");
  });

  it("送信成功後に入力欄がクリアされる", async () => {
    const user = userEvent.setup();
    render (<TodoForm onSaveTodo={mockOnSaveTodo} />);

    const input = screen.getByPlaceholderText("新しい TODO を入力...");
    await user.type(input, "テスト TODO");
    await user.click(screen.getByRole("button", { name: "追加"}));

    expect(input).toHaveValue("");
  });

  it("空のまま submit するとエラーが表示される", async () => {
    const user = userEvent.setup();
    render(<TodoForm onSaveTodo={mockOnSaveTodo} />);

    await user.click(screen.getByRole("button", { name: "追加" }));

    expect(screen.getByText("タイトルを入力してください")).toBeInTheDocument();
    expect(mockOnSaveTodo).not.toHaveBeenCalled();
  });

  it("スペースのみで submit するとエラーが表示される", async () => {
    const user = userEvent.setup();
    render(<TodoForm onSaveTodo={mockOnSaveTodo} />);

    await user.type(screen.getByPlaceholderText("新しい TODO を入力..."), "    ");
    await user.click(screen.getByRole("button", { name: "追加" }));

    expect(screen.getByText("タイトルを入力してください")).toBeInTheDocument();
    expect(mockOnSaveTodo).not.toHaveBeenCalled();
  });
})