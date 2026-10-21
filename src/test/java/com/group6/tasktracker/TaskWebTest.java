package com.group6.tasktracker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest @AutoConfigureMockMvc
class TaskWebTest {
    @Autowired MockMvc mvc;
    @Autowired TaskRepository repository;
    @Test void fullWorkflowAndStoredValues() throws Exception {
        repository.deleteAll();
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("No tasks yet")));
        mvc.perform(post("/tasks").param("title","  "))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Please enter a task title")));
        mvc.perform(post("/tasks").param("title","First").param("category","College"))
            .andExpect(status().is3xxRedirection());
        var id = repository.findAll().get(0).getId();
        mvc.perform(post("/tasks/"+id+"/edit").param("title","Edited").param("category","Personal").param("dueDate","2026-10-20"))
            .andExpect(status().is3xxRedirection());
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("Edited")));
        mvc.perform(get("/tasks/"+id+"/edit")).andExpect(status().isOk()).andExpect(content().string(containsString("Save changes")));
        mvc.perform(post("/tasks/"+id+"/complete")).andExpect(status().is3xxRedirection());
        var stored = repository.findById(id).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(stored.isCompleted());
        org.junit.jupiter.api.Assertions.assertEquals("Edited", stored.getTitle());
        mvc.perform(get("/").param("category","College")).andExpect(status().isOk()).andExpect(content().string(containsString("No matching tasks")));
        mvc.perform(post("/tasks").param("title","Bad date").param("dueDate","not-a-date"))
            .andExpect(status().isOk()).andExpect(content().string(containsString("valid due date")));
    }
}
