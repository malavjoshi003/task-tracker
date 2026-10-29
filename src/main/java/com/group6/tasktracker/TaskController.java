package com.group6.tasktracker;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TaskController {
    private final TaskService service;
    public TaskController(TaskService service) { this.service = service; }
    @GetMapping("/")
    public String index(@RequestParam(defaultValue="") String category,
                        @RequestParam(defaultValue="all") String status,
                        @RequestParam(defaultValue="") String q,
                        @RequestParam(defaultValue="false") boolean newTask, Model model) {
        model.addAttribute("form", new TaskForm());
        String view = page(model, category, validStatus(status), q, null);
        model.addAttribute("newTask", newTask);
        return view;
    }
    @PostMapping("/tasks")
    public String create(@ModelAttribute("form") TaskForm form, BindingResult binding,
                         @RequestParam(defaultValue="") String returnCategory,
                         @RequestParam(defaultValue="all") String returnStatus,
                         @RequestParam(defaultValue="") String returnQuery,
                         Model model, RedirectAttributes redirect) {
        String status = validStatus(returnStatus);
        if (binding.hasErrors()) {
            model.addAttribute("error", "Please enter a valid due date.");
            return page(model, returnCategory, status, returnQuery, null);
        }
        TaskItem created;
        try { created = service.create(form.getTitle(), form.getDescription(), form.getCategory(), form.getDueDate()); }
        catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return page(model, returnCategory, status, returnQuery, null);
        }
        redirect.addFlashAttribute("message", "Task added.");
        addFilters(redirect, created.getCategory(), "pending", "");
        return "redirect:/";
    }
    @GetMapping("/tasks/{id}/edit")
    public String edit(@PathVariable long id, @RequestParam(defaultValue="") String category,
                       @RequestParam(defaultValue="all") String status,
                       @RequestParam(defaultValue="") String q,
                       Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("form", TaskForm.from(service.find(id)));
            return page(model, category, validStatus(status), q, id);
        }
        catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("message", ex.getMessage());
            addFilters(redirect, category, validStatus(status), q);
            return "redirect:/";
        }
    }
    @PostMapping("/tasks/{id}/edit")
    public String update(@PathVariable long id, @ModelAttribute("form") TaskForm form, BindingResult binding,
                         @RequestParam(defaultValue="") String returnCategory,
                         @RequestParam(defaultValue="all") String returnStatus,
                         @RequestParam(defaultValue="") String returnQuery,
                         Model model, RedirectAttributes redirect) {
        String status = validStatus(returnStatus);
        if (binding.hasErrors()) {
            model.addAttribute("error", "Please enter a valid due date.");
            return page(model, returnCategory, status, returnQuery, id);
        }
        try { service.update(id, form.getTitle(), form.getDescription(), form.getCategory(), form.getDueDate()); }
        catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return page(model, returnCategory, status, returnQuery, id);
        }
        redirect.addFlashAttribute("message", "Task updated.");
        TaskItem updated = service.find(id);
        String visibleStatus = status.equals("all") ? "all" : updated.isCompleted() ? "completed" : "pending";
        addFilters(redirect, updated.getCategory(), visibleStatus, "");
        return "redirect:/";
    }
    @PostMapping("/tasks/{id}/complete")
    public String complete(@PathVariable long id, @RequestParam(defaultValue="") String category,
                           @RequestParam(defaultValue="all") String status,
                           @RequestParam(defaultValue="") String q, RedirectAttributes redirect) {
        try { service.complete(id); redirect.addFlashAttribute("message", "Task completed."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("message", ex.getMessage()); }
        addFilters(redirect, category, validStatus(status), q);
        return "redirect:/";
    }
    @PostMapping("/tasks/{id}/toggle")
    public String toggle(@PathVariable long id, @RequestParam(defaultValue="") String category,
                         @RequestParam(defaultValue="all") String status,
                         @RequestParam(defaultValue="") String q, RedirectAttributes redirect) {
        try {
            service.toggleCompleted(id);
            redirect.addFlashAttribute("message", "Task status updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("message", ex.getMessage());
        }
        addFilters(redirect, category, validStatus(status), q);
        return "redirect:/";
    }
    @PostMapping("/tasks/{id}/delete")
    public String delete(@PathVariable long id, @RequestParam(defaultValue="") String category,
                         @RequestParam(defaultValue="all") String status,
                         @RequestParam(defaultValue="") String q, RedirectAttributes redirect) {
        try { service.delete(id); redirect.addFlashAttribute("message", "Task deleted."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("message", ex.getMessage()); }
        addFilters(redirect, category, validStatus(status), q);
        return "redirect:/";
    }
    private String page(Model model, String category, String status, String query, Long editingId) {
        model.addAttribute("tasks", service.list(category, status, query));
        model.addAttribute("progress", service.progress());
        model.addAttribute("upcomingTasks", service.upcoming());
        model.addAttribute("categoryProgress", service.categoryProgress());
        model.addAttribute("categories", TaskService.CATEGORIES); model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status); model.addAttribute("selectedQuery", query);
        model.addAttribute("editingId", editingId); model.addAttribute("newTask", editingId != null);
        model.addAttribute("today", service.today()); return "index";
    }
    private String validStatus(String status) {
        return "pending".equals(status) || "completed".equals(status) ? status : "all";
    }
    private void addFilters(RedirectAttributes redirect, String category, String status, String query) {
        redirect.addAttribute("category", category);
        redirect.addAttribute("status", status);
        redirect.addAttribute("q", query);
    }
}
