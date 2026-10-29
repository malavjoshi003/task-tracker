package com.group6.tasktracker;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    public void toggleCompleted(long id) {
        TaskItem task = find(id);
        task.setCompleted(!task.isCompleted());
        repository.save(task);
    }
    public void delete(long id) { repository.delete(find(id)); }
    @Transactional(readOnly = true)
    public TaskItem find(long id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found.")); }
    @Transactional(readOnly = true)
    public List<TaskItem> list(String category) {
        return list(category, "all", "");
    }
    @Transactional(readOnly = true)
    public List<TaskItem> list(String category, String status, String query) {
        String normalizedQuery = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        return repository.findAllByOrderByIdAsc().stream()
            .filter(t -> category == null || category.isBlank() || t.getCategory().equals(category))
            .filter(t -> status == null || status.isBlank() || status.equals("all")
                || status.equals("completed") == t.isCompleted())
            .filter(t -> normalizedQuery.isEmpty()
                || t.getTitle().toLowerCase(Locale.ROOT).contains(normalizedQuery)).toList();
    }
    @Transactional(readOnly = true)
    public List<TaskItem> upcoming() {
        return repository.findAllByOrderByIdAsc().stream()
            .filter(task -> task.getDueDate() != null)
            .sorted(Comparator.comparing(TaskItem::getDueDate).thenComparing(TaskItem::getId))
            .limit(4).toList();
    }
    @Transactional(readOnly = true)
    public List<CategoryProgress> categoryProgress() {
        List<TaskItem> tasks = repository.findAllByOrderByIdAsc();
        return CATEGORIES.stream().map(category -> {
            long count = tasks.stream().filter(task -> task.getCategory().equals(category)).count();
            long percentage = tasks.isEmpty() ? 0 : Math.round(count * 100.0 / tasks.size());
            return new CategoryProgress(category, count, percentage);
        }).toList();
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
    public record CategoryProgress(String category, long count, long percentage) {}
}
