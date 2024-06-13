package service.memory;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {

    private static InMemoryHistoryManager historyManager;
    private static Task task;


    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("Task", "Desc", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task.setId(1);
    }

    @Test
    void shouldAddAnyTasksInHistory() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(2);

        SubTask subTask = new SubTask(2, "SubTask", "Desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));

        subTask.setId(3);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subTask);

        List<Task> history = historyManager.getHistory();

        assertNotNull(history);
        assertEquals(3, history.size(), "Количество задач в истории не равно 3");
    }

    @Test
    void shouldNotChangeTaskData() {
        historyManager.add(task);

        Task taskFromHistory = historyManager.getHistory().get(0);

        assertEquals(task, taskFromHistory);
        assertEquals(task.getName(), taskFromHistory.getName());
        assertEquals(task.getDescription(), taskFromHistory.getDescription());
        assertEquals(task.getTaskStatus(), taskFromHistory.getTaskStatus());
    }

    @Test
    void shouldKeepTaskOrder() {
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task3.setId(3);

        historyManager.add(task3);
        historyManager.add(task);
        historyManager.add(task2);

        List<Task> historyFromManager = historyManager.getHistory();

        assertEquals(task3, historyFromManager.get(0), "Первая задача не совпадает в списке истории");
        assertEquals(task, historyFromManager.get(1), "Вторая задача не совпадает в списке истории");
        assertEquals(task2, historyFromManager.get(2), "Третья задача не совпадает в списке истории");
    }

    @Test
    void shouldRemoveTaskFromBegin() {
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task3.setId(3);

        historyManager.add(task3);
        historyManager.add(task);
        historyManager.add(task2);

        historyManager.remove(task3.getId());

        List<Task> historyFromManager = historyManager.getHistory();

        assertEquals(2, historyFromManager.size(), "Неверное количество задач в истории после удаления");
        assertEquals(task, historyFromManager.get(0), "Не верная история в начале списка");
        assertEquals(task2, historyFromManager.get(1), "Не верная история в конце списка");
    }

    @Test
    void shouldRemoveTaskFromEnd() {
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task3.setId(3);

        historyManager.add(task3);
        historyManager.add(task);
        historyManager.add(task2);

        historyManager.remove(task2.getId());

        List<Task> historyFromManager = historyManager.getHistory();

        assertEquals(2, historyFromManager.size(), "Неверное количество задач в истории после удаления");
        assertEquals(task3, historyFromManager.get(0), "Не верная история в начале списка");
        assertEquals(task, historyFromManager.get(1), "Не верная история в конце списка");
    }

    @Test
    void shouldRemoveTaskFromMiddle() {
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        task3.setId(3);

        historyManager.add(task3);
        historyManager.add(task);
        historyManager.add(task2);

        historyManager.remove(task.getId());

        List<Task> historyFromManager = historyManager.getHistory();

        assertEquals(2, historyFromManager.size(), "Неверное количество задач в истории после удаления");
        assertEquals(task3, historyFromManager.get(0), "Не верная история в начале списка");
        assertEquals(task2, historyFromManager.get(1), "Не верная история в конце списка");
    }
}
