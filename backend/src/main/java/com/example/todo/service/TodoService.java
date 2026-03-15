package com.example.todo.service;

import java.util.List;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;

public interface TodoService {
  List<TodoResponse> findAll(String status);

  TodoResponse create(TodoRequest request);

  TodoResponse update(Long id, TodoRequest request);

  TodoResponse toggleComplete(Long id);

  void delete(Long id);
}
