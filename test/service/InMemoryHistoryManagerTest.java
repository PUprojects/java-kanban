package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static InMemoryHistoryManager historyManager;
    private static Task task;

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("Task", "Desc", TaskStatus.NEW);
    }
    @Test
    void shouldAddAnyTasksInHistory() {
        Epic epic = new Epic("Epic", "Desc");
        SubTask subTask = new SubTask(epic, "SubTask", "Desc", TaskStatus.NEW);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subTask);

        List<Task> history = historyManager.getHistory();

        assertNotNull(history);
        assertEquals(history.size(), 3);
    }

    @Test
    void shouldNotChangeTaskData() {
        task.setId(1);
        historyManager.add(task);

        Task taskFromHistory = historyManager.getHistory().getFirst();

        assertEquals(task, taskFromHistory);
        assertEquals(task.getName(), taskFromHistory.getName());
        assertEquals(task.getDescription(), taskFromHistory.getDescription());
        assertEquals(task.getTaskStatus(), taskFromHistory.getTaskStatus());
    }
}