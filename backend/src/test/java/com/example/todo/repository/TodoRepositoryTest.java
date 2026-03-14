package com.example.todo.repository;

import com.example.todo.entity.Todo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TodoRepositoryTest {

  @Autowired
  private TodoRepository todoRepository;

  // --------------------------------------------------
  // findAllByOrderByCreatedAtDesc
  // --------------------------------------------------

  @Test
  void 全件取得_データがない場合は空リストを返す() {
    // When
    List<Todo> todos = todoRepository.findAllByOrderByCreatedAtDesc();

    // Then
    assertThat(todos).isEmpty();
  }

  @Test
  void 全件取得_複数件を作成日時の降順で返す() {
    // Given
    Todo todo1 = new Todo();
    todo1.setTitle("最初の TODO");
    todoRepository.save(todo1);

    Todo todo2 = new Todo();
    todo2.setTitle("2番目の TODO");
    todoRepository.save(todo2);

    // When
    List<Todo> todos = todoRepository.findAllByOrderByCreatedAtDesc();

    // Then
    assertThat(todos).hasSize(2);
    // 降順なので 2番目が先に来る
    assertThat(todos.get(0).getTitle()).isEqualTo("2番目の TODO");
    assertThat(todos.get(1).getTitle()).isEqualTo("最初の TODO");
  }

  // --------------------------------------------------
  // findByCompleted
  // --------------------------------------------------

  @Test
  void 未完了のTODOだけ取得できる() {
    // Given
    Todo active = new Todo();
    active.setTitle("未完了タスク");
    active.setCompleted(false);
    todoRepository.save(active);

    Todo completed = new Todo();
    completed.setTitle("完了済みタスク");
    completed.setCompleted(true);
    todoRepository.save(completed);

    // When
    List<Todo> activeTodos = todoRepository.findByCompleted(false);

    // Then
    assertThat(activeTodos).hasSize(1);
    assertThat(activeTodos.get(0).getTitle()).isEqualTo("未完了タスク");
    assertThat(activeTodos.get(0).getCompleted()).isFalse();
  }

  @Test
  void 完了済みのTODOだけ取得できる() {
    // Given
    Todo active = new Todo();
    active.setTitle("未完了タスク");
    active.setCompleted(false);
    todoRepository.save(active);

    Todo completed = new Todo();
    completed.setTitle("完了済みタスク");
    completed.setCompleted(true);
    todoRepository.save(completed);

    // When
    List<Todo> completedTodos = todoRepository.findByCompleted(true);

    // Then
    assertThat(completedTodos).hasSize(1);
    assertThat(completedTodos.get(0).getTitle()).isEqualTo("完了済みタスク");
    assertThat(completedTodos.get(0).getCompleted()).isTrue();
  }

  // --------------------------------------------------
  // Entity のライフサイクル確認
  // --------------------------------------------------

  @Test
  void 保存時にcreatedAtとupdatedAtが自動設定される() {
    // Given
    Todo todo = new Todo();
    todo.setTitle("日時テスト");

    LocalDateTime beforeSave = LocalDateTime.now();

    // When
    Todo saved = todoRepository.save(todo);

    LocalDateTime afterSave = LocalDateTime.now();

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getCreatedAt())
        .isAfterOrEqualTo(beforeSave)
        .isBeforeOrEqualTo(afterSave); // 保存直前〜直後の間の時刻であること（正確にその時点で設定されている）
    assertThat(saved.getUpdatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt())
        .isAfterOrEqualTo(beforeSave)
        .isBeforeOrEqualTo(afterSave); // 保存直前〜直後の間の時刻であること（正確にその時点で設定されている）
    assertThat(saved.getCompleted()).isFalse(); // デフォルト値
  }
}
