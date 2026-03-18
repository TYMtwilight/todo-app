package com.example.todo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
public class TodoControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TodoService todoService;

  @Autowired
  private ObjectMapper objectMapper;

  // --- テストデータ作成ヘルパー ---

  private TodoResponse createResponse(Long id, String title, Boolean completed) {
    return new TodoResponse(id, title, completed, LocalDateTime.now(), LocalDateTime.now());
  }

  // --------------------------------------------------
  // GET /api/todos
  // --------------------------------------------------

  @Test
  void GET_全TODO取得_200を返す() throws Exception {
    // Given
    List<TodoResponse> todos = List.of(
        createResponse(1L, "タスク1", false),
        createResponse(2L, "タスク2", true));
    when(todoService.findAll("ALL")).thenReturn(todos);

    // When & Then
    mockMvc.perform(get("/api/todos"))
        .andExpect(status().isOk());
        // .andExpect(jsonPath("$", hasSize(2)))
        // .andExpect(jsonPath("$[0].title").value("タスク1"))
        // .andExpect(jsonPath("$[1].title").value("タスク2"));
  }

  @Test
  void GET_ステータス指定で200を返す() throws Exception {
    // Given
    List<TodoResponse> activeTodos = List.of(
        createResponse(1L, "未完了タスク", false));
    when(todoService.findAll("ACTIVE")).thenReturn(activeTodos);

    // When & Then
    mockMvc.perform(get("/api/todos").param("status", "ACTIVE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].completed").value(false));
  }

  // --------------------------------------------------
  // POST /api/todos
  // --------------------------------------------------

  @Test
  void POST_正常なリクエストで201を返す() throws Exception {
    // Given
    TodoRequest request = new TodoRequest();
    request.setTitle("新しいタスク");

    TodoResponse response = createResponse(1L, "新しいタスク", false);
    when(todoService.create(any(TodoRequest.class))).thenReturn(response);

    // When & Then
    mockMvc.perform(post("/api/todos")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("新しいタスク"))
        .andExpect(jsonPath("$.completed").value(false));
  }

  @Test
  void POST_タイトル空で400を返す() throws Exception {
    // Given
    TodoRequest request = new TodoRequest();
    request.setTitle("");

    // When & Then
    mockMvc.perform(post("/api/todos")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void POST_タイトルnullで400を返す() throws Exception {
    // Given - title を設定しない
    TodoRequest request = new TodoRequest();

    // When & Then
    mockMvc.perform(post("/api/todos")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  // --------------------------------------------------
  // PUT /api/todos/{id}
  // --------------------------------------------------

  @Test
  void PUT_正常な更新で200を返す() throws Exception {
    // Given
    TodoRequest request = new TodoRequest();
    request.setTitle("更新後タイトル");

    TodoResponse response = createResponse(1L, "更新後タイトル", false);
    when(todoService.update(eq(1L), any(TodoRequest.class))).thenReturn(response);

    // When & Then
    mockMvc.perform(put("/api/todos/1").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("更新後タイトル"));
  }

  @Test
  void PUT_存在しないIDで404を返す() throws Exception {
    // Given
    TodoRequest request = new TodoRequest();
    request.setTitle("更新");
    when(todoService.update(eq(999L), any(TodoRequest.class)))
        .thenThrow(new RuntimeException("TODO がみつかりません：id = 999"));

    // When & Then
    mockMvc.perform(put("/api/todos/999")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  // --------------------------------------------------
  // PATCH /api/todos/{id}/toggle
  // --------------------------------------------------

  @Test
  void PATCH_トグルで200を返す() throws Exception {
    // Given
    TodoResponse response = createResponse(1L, "タスク", true);
    when(todoService.toggleComplete(1L)).thenReturn(response);

    // When & Then
    mockMvc.perform(patch("/api/todos/1/toggle"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(true));
  }

  // --------------------------------------------------
  // DELETE /api/todos/{id}
  // --------------------------------------------------

  @Test
  void DELETE_正常な削除で204を返す() throws Exception {
    // Given
    doNothing().when(todoService).delete(1L);

    // When & Then
    mockMvc.perform(delete("/api/todos/1"))
        .andExpect(status().isNoContent());
  }

  @Test
  void DELETE_存在しないIDで404を返す() throws Exception {
    // Given
    doThrow(new RuntimeException("TODO が見つかりません：id=999"))
        .when(todoService).delete(999L);

    // When & Then
    mockMvc.perform(delete("/api/todos/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }
}
