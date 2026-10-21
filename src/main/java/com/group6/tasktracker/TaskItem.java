package com.group6.tasktracker;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class TaskItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120) private String title;
    @Column(length = 1000) private String description;
    @Column(nullable = false) private String category;
    private LocalDate dueDate;
    private boolean completed;
    protected TaskItem() {}
    public TaskItem(String title, String description, String category, LocalDate dueDate) {
        this.title = title; this.description = description;
        this.category = category; this.dueDate = dueDate;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate date) { this.dueDate = date; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean value) { completed = value; }
}
