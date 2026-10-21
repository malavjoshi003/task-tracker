package com.group6.tasktracker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TaskRepository extends JpaRepository<TaskItem, Long> {
    List<TaskItem> findAllByOrderByIdAsc();
}
