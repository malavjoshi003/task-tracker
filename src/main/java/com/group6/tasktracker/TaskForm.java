package com.group6.tasktracker;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
public class TaskForm {
    private String title = "";
    private String description = "";
    private String category = "Uncategorized";
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dueDate;
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getCategory() { return category; }
    public void setCategory(String value) { category = value; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate value) { dueDate = value; }
    public static TaskForm from(TaskItem task) {
        TaskForm form = new TaskForm(); form.title = task.getTitle();
        form.description = task.getDescription(); form.category = task.getCategory();
        form.dueDate = task.getDueDate(); return form;
    }
}
