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
    public String index(@RequestParam(defaultValue="") String category, Model model) {
        model.addAttribute("form", new TaskForm()); return page(model, category, null);
    }
    @PostMapping("/tasks")
    public String create(@ModelAttribute("form") TaskForm form, BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) { model.addAttribute("error", "Please enter a valid due date."); return page(model, "", null); }
        try { service.create(form.getTitle(), form.getDescription(), form.getCategory(), form.getDueDate()); }
        catch (IllegalArgumentException ex) { model.addAttribute("error", ex.getMessage()); return page(model, "", null); }
        redirect.addFlashAttribute("message", "Task added."); return "redirect:/";
    }
    @GetMapping("/tasks/{id}/edit")
    public String edit(@PathVariable long id, Model model, RedirectAttributes redirect) {
        try { model.addAttribute("form", TaskForm.from(service.find(id))); return page(model, "", id); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("message", ex.getMessage()); return "redirect:/"; }
    }
    @PostMapping("/tasks/{id}/edit")
    public String update(@PathVariable long id, @ModelAttribute("form") TaskForm form, BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) { model.addAttribute("error", "Please enter a valid due date."); return page(model, "", id); }
        try { service.update(id, form.getTitle(), form.getDescription(), form.getCategory(), form.getDueDate()); }
        catch (IllegalArgumentException ex) { model.addAttribute("error", ex.getMessage()); return page(model, "", id); }
        redirect.addFlashAttribute("message", "Task updated."); return "redirect:/";
    }
    @PostMapping("/tasks/{id}/complete")
    public String complete(@PathVariable long id, RedirectAttributes redirect) {
        try { service.complete(id); redirect.addFlashAttribute("message", "Task completed."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("message", ex.getMessage()); }
        return "redirect:/";
    }
    private String page(Model model, String category, Long editingId) {
        model.addAttribute("tasks", service.list(category)); model.addAttribute("progress", service.progress());
        model.addAttribute("categories", TaskService.CATEGORIES); model.addAttribute("selectedCategory", category);
        model.addAttribute("editingId", editingId); model.addAttribute("today", service.today()); return "index";
    }
}
