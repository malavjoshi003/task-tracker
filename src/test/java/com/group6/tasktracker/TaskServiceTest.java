package com.group6.tasktracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {
    private TaskRepository repository;
    private TaskService service;
    @BeforeEach void setup() {
        repository = mock(TaskRepository.class);
        service = new TaskService(repository, Clock.fixed(
            Instant.parse("2026-10-21T06:00:00Z"), ZoneId.of("Asia/Kolkata")));
        when(repository.save(any(TaskItem.class))).thenAnswer(i -> i.getArgument(0));
    }
    @Test void createsTrimmedPendingTask() {
        TaskItem task = service.create("  Coursework  ", "Details", "College", null);
        assertEquals("Coursework", task.getTitle());
        assertFalse(task.isCompleted());
        verify(repository).save(task);
    }
    @Test void rejectsBlankTitlesWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> service.create("  ", "", "", null));
        verify(repository, never()).save(any());
    }
    @Test void updatesOnlySelectedTaskAndKeepsStatus() {
        TaskItem task = new TaskItem("Old", "", "", null);
        task.setCompleted(true);
        when(repository.findById(7L)).thenReturn(java.util.Optional.of(task));
        service.update(7L, "New", "Updated", "Personal", LocalDate.of(2026,10,23));
        assertEquals("New", task.getTitle());
        assertTrue(task.isCompleted());
        verify(repository).save(task);
    }
    @Test void completionIsSafeToRepeat() {
        TaskItem task = new TaskItem("Work", "Keep", "", null);
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(task));
        service.complete(1L); service.complete(1L);
        assertTrue(task.isCompleted());
        assertEquals("Keep", task.getDescription());
    }
    @Test void completionCanBeToggledInBothDirections() {
        TaskItem task = new TaskItem("Work", "", "", null);
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(task));
        service.toggleCompleted(1L);
        assertTrue(task.isCompleted());
        service.toggleCompleted(1L);
        assertFalse(task.isCompleted());
        verify(repository, times(2)).save(task);
    }
    @Test void unknownTaskGivesReadableError() {
        assertThrows(IllegalArgumentException.class, () -> service.complete(99L));
    }
    @Test void deletesExistingTask() {
        TaskItem task = new TaskItem("Work", "", "College", null);
        when(repository.findById(3L)).thenReturn(java.util.Optional.of(task));
        service.delete(3L);
        verify(repository).delete(task);
    }
    @Test void deletingUnknownTaskGivesReadableError() {
        assertThrows(IllegalArgumentException.class, () -> service.delete(99L));
        verify(repository, never()).delete(any(TaskItem.class));
    }
    @Test void emptyProgressIsZero() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of());
        var progress = service.progress();
        assertEquals(0, progress.total());
        assertEquals(0, progress.percentage());
    }
    @Test void progressCountsAllTasks() {
        TaskItem a = new TaskItem("A", "", "College", null);
        TaskItem b = new TaskItem("B", "", "Personal", null); b.setCompleted(true);
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(a,b));
        var progress = service.progress();
        assertEquals(2, progress.total()); assertEquals(1, progress.completed());
        assertEquals(1, progress.pending()); assertEquals(50, progress.percentage());
    }
    @Test void overdueExcludesTodayCompletedAndUndatedTasks() {
        TaskItem task = new TaskItem("Work", "", "", LocalDate.of(2026,10,20));
        assertTrue(service.overdue(task));
        task.setCompleted(true); assertFalse(service.overdue(task));
        task.setCompleted(false); task.setDueDate(LocalDate.of(2026,10,21));
        assertFalse(service.overdue(task));
        task.setDueDate(null); assertFalse(service.overdue(task));
    }
    @Test void categoryFilterDoesNotChangeGlobalCounts() {
        var tasks = List.of(new TaskItem("A", "", "College", null),
                            new TaskItem("B", "", "Personal", null));
        when(repository.findAllByOrderByIdAsc()).thenReturn(tasks);
        assertEquals(1, service.list("College").size());
        assertEquals(2, service.progress().total());
    }
    @Test void filtersByCategoryStatusAndCaseInsensitiveTitle() {
        TaskItem pending = new TaskItem("College Planning", "", "College", null);
        TaskItem completed = new TaskItem("College Review", "", "College", null);
        completed.setCompleted(true);
        TaskItem otherCategory = new TaskItem("College Notes", "", "Personal", null);
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(pending, completed, otherCategory));

        assertEquals(List.of(completed), service.list("College", "completed", "REVIEW"));
        assertEquals(List.of(pending), service.list("College", "pending", "planning"));
        assertEquals(List.of(pending, completed, otherCategory), service.list("", "all", " college "));
    }
    @Test void upcomingTasksAreSortedByDueDateAndCategoryCountsAreGlobal() {
        TaskItem later = new TaskItem("Later", "", "Personal", LocalDate.of(2026, 10, 30));
        TaskItem sooner = new TaskItem("Sooner", "", "College", LocalDate.of(2026, 10, 22));
        TaskItem undated = new TaskItem("No date", "", "College", null);
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(later, sooner, undated));

        assertEquals(List.of(sooner, later), service.upcoming());
        var categoryCounts = service.categoryProgress();
        assertEquals(2, categoryCounts.get(1).count());
        assertEquals(1, categoryCounts.get(2).count());
        assertEquals(67, categoryCounts.get(1).percentage());
    }
}
