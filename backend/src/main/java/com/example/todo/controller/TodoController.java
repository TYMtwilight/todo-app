package com.example.todo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.service.TodoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

  private final TodoService todoService;

  public TodoController(TodoService todoService) {
    this.todoService = todoService;
  }

  /**
   * TODO 一覧取得
   * GET /api/todos?status=ALL|ACTIVE|COMPLETED
   */
  @GetMapping
  public ResponseEntity<List<TodoResponse>> getAll(
      @RequestParam(defaultValue = "ALL") String status) {
    return ResponseEntity.ok(todoService.findAll(status));
  }

  /**
   * TODO 更新
   * POST /api/todos
   */
  @PostMapping
  public ResponseEntity<TodoResponse> create(
      @Valid @RequestBody TodoRequest request) {
    TodoResponse created = todoService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * TODO 更新
   * PUT /api/todos/{id}
   */
  @PutMapping("/{id}")
  public ResponseEntity<TodoResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody TodoRequest request) {
    TodoResponse updated = todoService.update(id, request);
    return ResponseEntity.ok(updated);
  }

  /**
   * 完了状態トグル
   * PATCH /api/todos/{id}/toggle
   */
  @PatchMapping("/{id}/toggle")
  public ResponseEntity<TodoResponse> toggle(@PathVariagle Long id) {
    TodoResponse toggled = todoService.toggleComplete(id);
    return ResponseEntity.ok(toggled);
  }

  /**
   * TODO 削除
   * DELETE /api/todos/{id}
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    todoService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
