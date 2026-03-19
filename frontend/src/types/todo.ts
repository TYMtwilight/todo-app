export type Todo = {
  id: number;
  title: string;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
};

export type TodoRequest = {
  title: string;
  completed?: boolean;
};

export type FilterStatus = "ALL" | "ACTIVE" | "COMPLETED";