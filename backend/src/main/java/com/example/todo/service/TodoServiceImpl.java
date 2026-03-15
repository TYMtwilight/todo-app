package com.example.todo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;

@Service
public class TodoServiceImpl implements TodoService {

  private final TodoRepository todoRepository;

  // コンストラクタインジェクション
  public TodoServiceImpl(TodoRepository todoRepository) {
    this.todoRepository = todoRepository;
  }

  @Override
  public List<TodoResponse> findAll(String status) {
    List<Todo> todos;
    switch (status.toUpperCase()) {
      case "ACTIVE":
        todos = todoRepository.findByCompleted(false);
        break;
      case "COMPLETED":
        todos = todoRepository.findByCompleted(true);
        break;
      default:
        todos = todoRepository.findAllByOrderByCreatedAtDesc();
        break;
    }
    return todos.stream()
        .map(TodoResponse::from)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public TodoResponse create(TodoRequest request) {
    Todo todo = new Todo();
    todo.setTitle(request.getTitle());
    Todo saved = todoRepository.save(todo);
    return TodoResponse.from(saved);
  }

  @Override
  @Transactional
  public TodoResponse update(Long id, TodoRequest request) {
    if (id == null) {
      throw new IllegalArgumentException("ID は必須です");
    }
    Todo todo = todoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("TODO が見つかりません: id =" + id));
    todo.setTitle(request.getTitle());
    if (request.getCompleted() != null) {
      todo.setCompleted(request.getCompleted());
    }
    Todo updated = todoRepository.save(todo);
    return TodoResponse.from(updated);
  }

  @Override
  @Transactional
  public TodoResponse toggleComplete(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("ID は必須です");
    }
    Todo todo = todoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("TODO が見つかりません: id=" + id));
    todo.setCompleted(!todo.getCompleted());
    Todo toggled = todoRepository.save(todo);
    return TodoResponse.from(toggled);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("ID は必須です");
    }
    if (!todoRepository.existsById(id)) {
      throw new RuntimeException("TODO が見つかりません: id=" + id);
    }
    todoRepository.deleteById(id);
  }
}
