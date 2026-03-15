package com.example.todo.service;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

  @Mock
  private TodoRepository todoRepository;

  @InjectMocks
  private TodoServiceImpl todoService;

  // --- テストデータ作成ヘルパー ---

  private Todo createTodo(Long id, String title, Boolean completed) {
    Todo todo = new Todo();
    todo.setId(id);
    todo.setTitle(title);
    todo.setCompleted(completed);
    return todo;
  }

  // --------------------------------------------------
  // findAll
  // --------------------------------------------------

  @Test
  void findAll_ALLで全件取得できる() {
    // Given
    List<Todo> todos = List.of(
        createTodo(1L, "タスク1", false),
        createTodo(2L, "タスク2", true));
    when(todoRepository.findAllByOrderByCreatedAtDesc()).thenReturn(todos);

    // when
    List<TodoResponse> result = todoService.findAll("ALL");

    // then
    assertThat(result).hasSize(2);
    verify(todoRepository).findAllByOrderByCreatedAtDesc();
  }

  @Test
  void findAll_ACTIVEで未完了のみ取得できる() {
    // Given
    List<Todo> todos = List.of(
        createTodo(1L, "未完了", false));
    when(todoRepository.findByCompleted(false)).thenReturn(todos);

    // When
    List<TodoResponse> result = todoService.findAll("ACTIVE");

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("未完了");
    verify(todoRepository).findByCompleted(false);
  }

  @Test
  void findAll_COMPLETEDで完了済みのみ取得できる() {
    // Given
    List<Todo> todos = List.of(
        createTodo(2L, "完了済み", true));
    when(todoRepository.findByCompleted(true)).thenReturn(todos);

    // When
    List<TodoResponse> result = todoService.findAll("COMPLETED");

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("完了済み");
    verify(todoRepository).findByCompleted(true);
  }

  // --------------------------------------------------
  // create
  // --------------------------------------------------

  @Test
  void create_新しいTODOを作成できる() {
    // Given
    TodoRequest request = new TodoRequest();
    request.setTitle("新しいタスク");

    Todo saved = createTodo(1L, "新しいタスク", false);
    when(todoRepository.save(any(Todo.class))).thenReturn(saved);

    // When
    TodoResponse result = todoService.create(request);

    // Then
    assertThat(result.getTitle()).isEqualTo("新しいタスク");
    assertThat(result.getCompleted()).isFalse();
    verify(todoRepository).save(any(Todo.class));
  }

  // --------------------------------------------------
  // update
  // --------------------------------------------------

  @Test
  void update_タイトルを更新できる() {
    // Given
    Todo existing = createTodo(1L, "古いタイトル", false);
    when(todoRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(todoRepository.save(any(Todo.class))).thenReturn(existing);

    TodoRequest request = new TodoRequest();
    request.setTitle("新しいタイトル");

    // When
    TodoResponse result = todoService.update(1L, request);

    // Then
    assertThat(result.getTitle()).isEqualTo("新しいタイトル");
    verify(todoRepository).findById(1L);
    verify(todoRepository).save(existing);
  }

  @Test
  void update_存在しないIDで例外が発生する() {
    // Given
    when(todoRepository.findById(999L)).thenReturn(Optional.empty());
    TodoRequest request = new TodoRequest();
    request.setTitle("更新");

    // When & Then
    assertThatThrownBy(() -> todoService.update(999L, request))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("TODO が見つかりません");
  }

  @Test
  void update_idがnullのときIllegalArgumentExceptionをスローする() {
    TodoRequest request = new TodoRequest();
    request.setTitle("タイトル");
    assertThrows(IllegalArgumentException.class, () -> todoService.update(null, request));
    verify(todoRepository, never()).findById(any());
    verify(todoRepository, never()).save(any());
  }

  // --------------------------------------------------
  // toggleComplete
  // --------------------------------------------------

  @Test
  void toggleComplete_falseからtrueに切り替わる() {
    // Given
    Todo todo = createTodo(1L, "タスク", false);
    when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
    when(todoRepository.save(any(Todo.class))).thenReturn(todo);

    // When
    todoService.toggleComplete(1L);

    // Then
    assertThat(todo.getCompleted()).isTrue();
  }

  @Test
  void toggleComplete_trueからfalseに切り替わる() {
    // Given
    Todo todo = createTodo(1L, "タスク", true);
    when(todoRepository.findById(1L)).thenReturn(Optional.of(todo));
    when(todoRepository.save(any(Todo.class))).thenReturn(todo);

    // When
    todoService.toggleComplete(1L);

    // Then
    assertThat(todo.getCompleted()).isFalse();
  }

  @Test
  void toggleComplete_存在しないIDで例外が発生する() {
    // Given
    when(todoRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> todoService.toggleComplete(999L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("TODO が見つかりません");
  }

  // --------------------------------------------------
  // delete
  // --------------------------------------------------

  @Test
  void delete_存在するTODOを削除できる() {
    // Given
    when(todoRepository.existsById(1L)).thenReturn(true);
    doNothing().when(todoRepository).deleteById(1L);

    // When
    todoService.delete(1L);

    // Then
    verify(todoRepository).existsById(1L);
    verify(todoRepository).deleteById(1L);
  }

  @Test
  void delete_存在しないIDで例外が発生する() {
    // Given
    when(todoRepository.existsById(999L)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> todoService.delete(999L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("TODO が見つかりません");
  }

  @Test
  void delete_idがnullのときIllegalArgumentExceptionをスローする() {
    TodoRequest request = new TodoRequest();
    request.setTitle("タイトル");
    assertThrows(IllegalArgumentException.class, () -> todoService.delete(null));
    verify(todoRepository, never()).existsById(any());
    verify(todoRepository, never()).deleteById(any());
  }
}
