package com.example.todo.controller;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.todo.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * バリデーションエラー（@Valid で検出）
   * 例：タイトルが空、100文字超過
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
        .collect(Collectors.joining(", "));

    ErrorResponse error = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        message,
        LocalDateTime.now());
    return ResponseEntity.badRequest().body(error);
  }

  /**
   * リソース未検出エラー
   * 例：存在しない ID で更新・削除しようとした
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse(
      HttpStatus.NOT_FOUND.value(),
      "Not Found",
      ex.getMessage(),
      LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }
}
