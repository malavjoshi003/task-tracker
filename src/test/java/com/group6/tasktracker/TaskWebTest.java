package com.group6.tasktracker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@SpringBootTest @AutoConfigureMockMvc
class TaskWebTest {
    @Autowired MockMvc mvc;
    @Autowired TaskRepository repository;
    @Test void fullWorkflowAndStoredValues() throws Exception {
        repository.deleteAll();
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().string(containsString("No tasks yet")))
            .andExpect(content().string(containsString("Task Tracker")))
            .andExpect(content().string(containsString("Add tasks, stay organized, and celebrate what you finish.")))
            .andExpect(content().string(containsString("Latest tasks")))
            .andExpect(content().string(containsString("href=\"/?newTask=true#quick-add\"")))
            .andExpect(content().string(containsString("quick-add")));
        mvc.perform(get("/").param("newTask", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("<details class=\"dashboard-card quick-add-card\" id=\"quick-add\" open")));
        mvc.perform(post("/tasks").param("title","  "))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Please enter a task title")));
        mvc.perform(post("/tasks").param("title","First").param("category","College"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/?category=College&status=pending&q="));
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

    @Test void categoryFilterShowsActionsAndDeletePreservesFilter() throws Exception {
        repository.deleteAll();
        mvc.perform(post("/tasks").param("title","College task").param("category","College"))
            .andExpect(status().is3xxRedirection());
        mvc.perform(post("/tasks").param("title","Personal task").param("category","Personal"))
            .andExpect(status().is3xxRedirection());

        var collegeId = repository.findAllByOrderByIdAsc().get(0).getId();
        mvc.perform(get("/").param("category","College"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("College task")))
            .andExpect(content().string(not(containsString("Personal task"))))
            .andExpect(content().string(containsString("aria-label=\"Complete College task\"")))
            .andExpect(content().string(containsString(">Delete</button>")));

        mvc.perform(post("/tasks/" + collegeId + "/delete").param("category","College"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/?category=College&status=all&q="));
        mvc.perform(get("/").param("category","College"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("No matching tasks")));
        org.junit.jupiter.api.Assertions.assertEquals(1, repository.count());
    }

    @Test void combinedFiltersAndTogglePreserveState() throws Exception {
        repository.deleteAll();
        mvc.perform(post("/tasks").param("title", "College Planning").param("category", "College"))
            .andExpect(status().is3xxRedirection());
        mvc.perform(post("/tasks").param("title", "College Review").param("category", "College"))
            .andExpect(status().is3xxRedirection());
        mvc.perform(post("/tasks").param("title", "Personal Review").param("category", "Personal"))
            .andExpect(status().is3xxRedirection());
        var tasks = repository.findAllByOrderByIdAsc();
        long planningId = tasks.get(0).getId();
        long reviewId = tasks.get(1).getId();
        mvc.perform(post("/tasks/" + reviewId + "/complete"))
            .andExpect(status().is3xxRedirection());

        mvc.perform(get("/").param("category", "College").param("status", "completed").param("q", "REVIEW"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("College Review")))
            .andExpect(content().string(not(containsString("College Planning"))))
            .andExpect(content().string(not(containsString("Personal Review"))));

        mvc.perform(get("/tasks/" + planningId + "/edit")
                .param("category", "College").param("status", "pending").param("q", "planning"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"returnQuery\" value=\"planning\"")))
            .andExpect(content().string(containsString("id=\"quick-add\" open")));
        mvc.perform(post("/tasks/" + planningId + "/edit")
                .param("title", "Renamed Planning").param("category", "Personal")
                .param("returnCategory", "College").param("returnStatus", "pending").param("returnQuery", "planning"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/?category=Personal&status=pending&q="));
        mvc.perform(get("/").param("category", "Personal").param("status", "pending"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Renamed Planning")));

        mvc.perform(post("/tasks/" + reviewId + "/toggle")
                .param("category", "College").param("status", "completed").param("q", "review"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/?category=College&status=completed&q=review"));
        org.junit.jupiter.api.Assertions.assertFalse(repository.findById(reviewId).orElseThrow().isCompleted());
        mvc.perform(get("/").param("category", "College").param("status", "pending").param("q", "REVIEW"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("College Review")));

        mvc.perform(post("/tasks/" + reviewId + "/toggle")
                .param("category", "College").param("status", "pending").param("q", "review"))
            .andExpect(status().is3xxRedirection());
        org.junit.jupiter.api.Assertions.assertTrue(repository.findById(reviewId).orElseThrow().isCompleted());
    }
}
