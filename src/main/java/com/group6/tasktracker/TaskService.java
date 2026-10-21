package com.group6.tasktracker;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
@Transactional
public class TaskService {
    public static final List<String> CATEGORIES = List.of("Uncategorized", "College", "Personal", "Other");
    private final TaskRepository repository;
    private final Clock clock;
    public TaskService(TaskRepository repository, Clock clock) {
        this.repository = repository; this.clock = clock;
    }
    public TaskItem create(String title, String description, String category, LocalDate dueDate) {
        String validTitle = validTitle(title);
        return repository.save(new TaskItem(validTitle, validDescription(description), validCategory(category), dueDate));
    }
    public void update(long id, String title, String description, String category, LocalDate dueDate) {
        // Validate everything before mutating a managed entity.
        String newTitle = validTitle(title), newDescription = validDescription(description), newCategory = validCategory(category);
        TaskItem task = find(id);
        task.setTitle(newTitle); task.setDescription(newDescription);
        task.setCategory(newCategory); task.setDueDate(dueDate);
        repository.save(task);
    }
    public void complete(long id) { TaskItem task = find(id); task.setCompleted(true); repository.save(task); }
    @Transactional(readOnly = true)
    public TaskItem find(long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found.")); }
    @Transactional(readOnly = true)
    public List<TaskItem> list(String category) {
        return repository.findAllByOrderByIdAsc().stream()
            .filter(t -> category == null || category.isBlank() || t.getCategory().equals(category)).toList();
    }
    @Transactional(readOnly = true)
    public Progress progress() {
        List<TaskItem> tasks = repository.findAllByOrderByIdAsc();
        long completed = tasks.stream().filter(TaskItem::isCompleted).count();
        return new Progress(tasks.size(), completed, tasks.size()-completed,
            tasks.isEmpty() ? 0 : Math.round(completed * 100.0 / tasks.size()));
    }
    public LocalDate today() { return LocalDate.now(clock); }
    public boolean overdue(TaskItem task) {
        return !task.isCompleted() && task.getDueDate() != null && task.getDueDate().isBefore(today());
    }
    private String validTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Please enter a task title.");
        if (title.strip().length() > 120) throw new IllegalArgumentException("Title must be 120 characters or fewer.");
        return title.strip();
    }
    private String validDescription(String description) {
        String value = description == null ? "" : description.strip();
        if (value.length() > 1000) throw new IllegalArgumentException("Description must be 1000 characters or fewer.");
        return value;
    }
    private String validCategory(String category) {
        if (category == null || category.isBlank()) return "Uncategorized";
        if (!CATEGORIES.contains(category)) throw new IllegalArgumentException("Please choose a listed category.");
        return category;
    }
    public record Progress(long total, long completed, long pending, long percentage) {}
}
