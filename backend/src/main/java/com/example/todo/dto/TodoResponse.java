package com.example.todo.dto;

import java.time.LocalDateTime;

public class TodoResponse {

  private Long id;
  private String title;
  private Boolean completed;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // --- コンストラクタ ---

  public TodoResponse() {}

  public TodoResponse(Long id, String title, Boolean completed,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.title = title;
    this.completed = completed;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // --- getter ---

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Boolean getCompleted() {
    return completed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
