package com.example.todo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todo.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {
  
  /**
   * 完了状態でフィルタリング
   */
  List<Todo> findByCompleted(Boolean completed);

  /**
   * 全件取得（作成日時の降順）
   */
  List<Todo> findAllByOrderByCreatedAtDesc();
}
