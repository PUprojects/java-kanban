package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.exeptions.TaskTimeValidateException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;


    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        taskManager = createManager();
    }

    @Test
    void shouldCreateTasks() {
        final Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        final Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));

        final Task savedTask1 = taskManager.create(task1);
        final Task savedTask2 = taskManager.create(task2);

        assertNotNull(savedTask1);
        assertNotNull(savedTask2);
        assertEquals(task1, savedTask1);
        assertEquals(task2, savedTask2);
        assertNotEquals(savedTask1, savedTask2);
        assertNotEquals(savedTask1.getId(), 0);
        assertNotEquals(savedTask2.getId(), 0);

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks);
        assertEquals(tasks.size(), 2);
        assertEquals(tasks.get(0), savedTask1);
        assertEquals(tasks.get(1), savedTask2);
    }

    @Test
    void shouldCreateEpics() {
        final Epic epic1 = new Epic("Epic1", "Desc");
        final Epic epic2 = new Epic("Epic2", "Desc");

        final Task savedEpic1 = taskManager.createEpic(epic1);
        final Task savedEpic2 = taskManager.createEpic(epic2);

        assertNotNull(savedEpic1);
        assertNotNull(savedEpic2);
        assertEquals(epic1, savedEpic1);
        assertEquals(epic2, savedEpic2);
        assertEquals(epic1.getTaskStatus(), TaskStatus.NEW);
        assertEquals(epic2.getTaskStatus(), TaskStatus.NEW);
        assertNotEquals(savedEpic1, savedEpic2);
        assertNotEquals(savedEpic1.getId(), 0);
        assertNotEquals(savedEpic2.getId(), 0);

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics);
        assertEquals(epics.size(), 2);
        assertEquals(epics.get(0), savedEpic1);
        assertEquals(epics.get(1), savedEpic2);
    }

    @Test
    void shouldCreateSubtasksAndAddItToEpics() {
        final Epic epic1 = new Epic("Epic1", "Desc");
        final Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(1, "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(1, "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(2, "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        final SubTask savedSubtask1 = taskManager.createSubTask(subTask1);
        final SubTask savedSubtask2 = taskManager.createSubTask(subTask2);
        final SubTask savedSubtask3 = taskManager.createSubTask(subTask3);

        taskManager.addSubTaskToEpic(epic1, subTask1);
        taskManager.addSubTaskToEpic(epic1, subTask2);
        taskManager.addSubTaskToEpic(epic2, subTask3);

        assertNotNull(savedSubtask1);
        assertNotNull(savedSubtask2);
        assertNotNull(savedSubtask3);
        assertEquals(savedSubtask1, subTask1);
        assertEquals(savedSubtask2, subTask2);
        assertEquals(savedSubtask3, subTask3);
        assertNotEquals(savedSubtask1, savedSubtask2);
        assertNotEquals(savedSubtask1, savedSubtask3);
        assertNotEquals(savedSubtask3, savedSubtask2);
        assertNotEquals(savedSubtask1.getId(), 0);
        assertNotEquals(savedSubtask2.getId(), 0);
        assertNotEquals(savedSubtask3.getId(), 0);

        List<SubTask> subTasks = taskManager.getSubtasks();

        assertNotNull(subTasks);
        assertEquals(subTask1, subTasks.get(0));
        assertEquals(subTask2, subTasks.get(1));
        assertEquals(subTask3, subTasks.get(2));

        List<Integer> epic1SubTakIds = epic1.getSubTasksIds();
        List<Integer> epic2SubTakIds = epic2.getSubTasksIds();

        assertEquals(epic1SubTakIds.size(), 2);
        assertEquals(epic2SubTakIds.size(), 1);

        assertEquals(epic1SubTakIds.get(0), subTask1.getId());
        assertEquals(epic1SubTakIds.get(1), subTask2.getId());
        assertEquals(epic2SubTakIds.get(0), subTask3.getId());
    }

    @Test
    void shouldAddInHistoryWhenCallGetById() {
        Task task = new Task("Task", "Desc", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        Epic epic = new Epic("Epic", "Desc");

        taskManager.create(task);
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask(epic.getId(), "SubTask", "Desc", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        taskManager.createSubTask(subTask);

        List<Task> history = taskManager.getHistory();

        assertNotNull(history);
        assertEquals(0, history.size());

        taskManager.get(1);
        taskManager.getEpic(2);
        taskManager.getSubTask(3);

        history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(history.size(), 3);
        assertEquals(history.get(0), task);
        assertEquals(history.get(1), epic);
        assertEquals(history.get(2), subTask);
    }

    @Test
    void shouldNotChangeTaskWhenAdd() {
        Task task = new Task("Task", "Desc", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));

        Task savedTask = taskManager.create(task);

        assertEquals(task, savedTask);
        assertEquals(task.getName(), savedTask.getName());
        assertEquals(task.getDescription(), savedTask.getDescription());
        assertEquals(task.getTaskStatus(), savedTask.getTaskStatus());
    }

    @Test
    void shouldUpdateTask() {
        final Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(15));
        final Task task2 = new Task("Task2", "Desc2", TaskStatus.IN_PROGRESS,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(25));

        taskManager.create(task1);

        task2.setId(task1.getId());

        final Task updated = taskManager.update(task2);

        assertNotNull(updated);
        assertEquals(task2, updated);
        assertEquals(task2.getName(), updated.getName());
        assertEquals(task2.getDescription(), updated.getDescription());
        assertEquals(task2.getTaskStatus(), updated.getTaskStatus());
        assertEquals(task2.getDuration(), updated.getDuration());
        assertEquals(task2.getStartTime(), updated.getStartTime());
        assertEquals(task2.getEndTime(), updated.getEndTime());
    }

    @Test
    void shouldUpdateEpic() {
        final Epic epic1 = new Epic("Epic1", "Desc");
        final Epic epic2 = new Epic("Epic2", "Desc2");

        taskManager.createEpic(epic1);

        epic2.setId(epic1.getId());

        final Epic updated = taskManager.updateEpic(epic2);
        assertNotNull(updated);
        assertEquals(epic2, updated);
        assertEquals(epic2.getName(), updated.getName());
        assertEquals(epic2.getDescription(), updated.getDescription());
        assertEquals(epic2.getTaskStatus(), updated.getTaskStatus());
    }

    @Test
    void shouldUpdateSubTask() {
        final Epic epic1 = new Epic("Epic1", "Desc");

        taskManager.createEpic(epic1);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(0, "Sub3", "Desc3", TaskStatus.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(55));

        taskManager.createSubTask(subTask1);
        subTask2.setId(subTask1.getId());
        SubTask updated = taskManager.updateSubTask(subTask2);

        assertNotNull(updated);
        assertEquals(subTask2, updated);
        assertEquals(subTask2.getName(), updated.getName());
        assertEquals(subTask2.getDescription(), updated.getDescription());
        assertEquals(subTask2.getTaskStatus(), updated.getTaskStatus());
        assertEquals(subTask2.getDuration(), updated.getDuration());
        assertEquals(subTask2.getStartTime(), updated.getStartTime());
        assertEquals(subTask2.getEndTime(), updated.getEndTime());
    }

    @Test
    void shouldUpdateEpicStatus() {
        final Epic epic1 = new Epic("Epic1", "Desc");

        taskManager.createEpic(epic1);

        assertEquals(epic1.getTaskStatus(), TaskStatus.NEW);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        assertEquals(epic1.getTaskStatus(), TaskStatus.NEW);

        SubTask newSubTask1 = new SubTask(0, "Sub1", "Desc1", TaskStatus.IN_PROGRESS,
                LocalDateTime.now(), Duration.ofMinutes(15));
        SubTask newSubTask2 = new SubTask(0, "Sub1", "Desc1", TaskStatus.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        newSubTask1.setId(subTask1.getId());
        newSubTask2.setId(subTask2.getId());

        taskManager.updateSubTask(newSubTask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getTaskStatus());

        taskManager.updateSubTask(newSubTask2);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getTaskStatus());

        newSubTask1.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubTask(newSubTask1);
        assertEquals(TaskStatus.DONE, epic1.getTaskStatus());
    }

    @Test
    void shouldRemoveTask() {
        final Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(15));
        final Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(15));

        taskManager.create(task1);
        taskManager.create(task2);

        assertEquals(taskManager.getTasks().size(), 2);

        taskManager.delete(1);

        final List<Task> tasks = taskManager.getTasks();

        assertEquals(tasks.size(), 1);
        assertEquals(tasks.get(0), task2);
    }

    @Test
    void shouldRemoveEpic() {
        final Epic epic1 = new Epic("Epic1", "Desc");
        final Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(epic2.getId(), "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        assertEquals(taskManager.getEpics().size(), 2);
        assertEquals(taskManager.getSubtasks().size(), 3);

        taskManager.deleteEpic(1);

        List<Epic> epics = taskManager.getEpics();
        assertEquals(epics.size(), 1);
        assertEquals(epics.get(0), epic2);
        assertEquals(taskManager.getSubtasks().size(), 1);
    }

    @Test
    void shouldRemoveSubTask() {
        final Epic epic1 = new Epic("Epic1", "Desc");
        final Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(epic2.getId(), "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        assertEquals(taskManager.getEpicSubtasks(1).size(), 2);
        assertEquals(taskManager.getEpicSubtasks(2).size(), 1);

        taskManager.deleteSubTask(subTask2.getId());

        List<SubTask> subTasks = taskManager.getSubtasks();
        assertEquals(subTasks.size(), 2);
        assertEquals(subTasks.get(1), subTask3);

        assertEquals(taskManager.getEpicSubtasks(1).size(), 1);
        assertEquals(taskManager.getEpicSubtasks(2).size(), 1);
    }

    @Test
    void shouldClearTasks() {
        final Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(15));
        final Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(15));

        taskManager.create(task1);
        taskManager.create(task2);

        assertEquals(taskManager.getTasks().size(), 2);

        taskManager.clearTasks();

        final List<Task> tasks = taskManager.getTasks();

        assertEquals(tasks.size(), 0);
    }

    @Test
    void shouldClearEpics() {
        final Epic epic1 = new Epic("Epic1", "Desc");
        final Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(epic2.getId(), "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        assertEquals(taskManager.getEpics().size(), 2);
        assertEquals(taskManager.getSubtasks().size(), 3);

        taskManager.clearEpics();

        assertEquals(taskManager.getEpics().size(), 0);
        assertEquals(taskManager.getSubtasks().size(), 0);
    }

    @Test
    void shouldClearSubtasks() {
        final Epic epic1 = new Epic("Epic1", "Desc");
        final Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(epic2.getId(), "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        assertEquals(taskManager.getEpics().size(), 2);
        assertEquals(taskManager.getSubtasks().size(), 3);
        assertEquals(taskManager.getEpicSubtasks(1).size(), 2);
        assertEquals(taskManager.getEpicSubtasks(2).size(), 1);

        taskManager.clearSubTasks();

        assertEquals(taskManager.getEpics().size(), 2);
        assertEquals(taskManager.getSubtasks().size(), 0);
        assertEquals(taskManager.getEpicSubtasks(1).size(), 0);
        assertEquals(taskManager.getEpicSubtasks(2).size(), 0);
    }

    @Test
    void shouldRemoveSubtasksFromEpic() {
        final Epic epic1 = new Epic("Epic1", "Desc");
        final Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(epic2.getId(), "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        assertEquals(taskManager.getSubtasks().size(), 3);
        assertEquals(taskManager.getEpicSubtasks(1).size(), 2);
        assertEquals(taskManager.getEpicSubtasks(2).size(), 1);

        taskManager.removeSubTaskFromEpic(epic1, subTask1.getId());

        assertEquals(taskManager.getEpicSubtasks(1).size(), 1);
        assertEquals(taskManager.getEpicSubtasks(2).size(), 1);

        assertEquals(taskManager.getEpicSubtasks(epic1.getId()).get(0), subTask2);

        taskManager.removeAllSubtasksFromEpic(epic2);
        assertEquals(taskManager.getEpicSubtasks(1).size(), 1);
        assertEquals(taskManager.getEpicSubtasks(2).size(), 0);
    }

    @Test
    void shouldNotDuplicateHistory() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(5),
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(6),
                Duration.ofMinutes(15));
        Epic epic1 = new Epic("Epic1", "Desc");
        Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(epic2.getId(), "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        taskManager.create(task1);
        taskManager.create(task2);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        taskManager.get(task1.getId());
        taskManager.get(task2.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubTask(subTask1.getId());
        taskManager.getSubTask(subTask2.getId());
        taskManager.getSubTask(subTask3.getId());

        taskManager.getEpic(epic2.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.get(task2.getId());
        taskManager.get(task1.getId());
        taskManager.getSubTask(subTask3.getId());
        taskManager.getSubTask(subTask1.getId());
        taskManager.getSubTask(subTask2.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(7, history.size(), "Размер истории должен быть равен количеству уникальных запросов");
        assertEquals(history.get(0), epic2);
        assertEquals(history.get(1), epic1);
        assertEquals(history.get(2), task2);
        assertEquals(history.get(3), task1);
        assertEquals(history.get(4), subTask3);
        assertEquals(history.get(5), subTask1);
        assertEquals(history.get(6), subTask2);
    }

    @Test
    void shouldRemoveTasksFromHistory() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(5),
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(6),
                Duration.ofMinutes(15));
        Epic epic1 = new Epic("Epic1", "Desc");
        Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(epic2.getId(), "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        taskManager.create(task1);
        taskManager.create(task2);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        taskManager.get(task1.getId());
        taskManager.get(task2.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubTask(subTask1.getId());
        taskManager.getSubTask(subTask2.getId());
        taskManager.getSubTask(subTask3.getId());

        taskManager.delete(task2.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(6, history.size(), "Размер истории должен быть равен 6 после удаления");
        assertEquals(history, List.of(task1, epic1, epic2, subTask1, subTask2, subTask3), "Задачи task1 не должно быть в списке");
    }

    @Test
    void shouldRemoveEpicsAndSubtasksFromHistory() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(5),
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(6),
                Duration.ofMinutes(15));
        Epic epic1 = new Epic("Epic1", "Desc");
        Epic epic2 = new Epic("Epic2", "Desc");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        final SubTask subTask1 = new SubTask(epic1.getId(), "Sub1", "Desc1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        final SubTask subTask2 = new SubTask(epic1.getId(), "Sub2", "Desc2", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        final SubTask subTask3 = new SubTask(epic2.getId(), "Sub3", "Desc3", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(15));

        taskManager.create(task1);
        taskManager.create(task2);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);

        taskManager.get(task1.getId());
        taskManager.get(task2.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubTask(subTask1.getId());
        taskManager.getSubTask(subTask2.getId());
        taskManager.getSubTask(subTask3.getId());

        taskManager.deleteEpic(epic1.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(4, history.size(), "Размер истории должен быть равен 4 после удаления");
        assertEquals(List.of(task1, task2, epic2, subTask3), history, "epic1, subTask1, subTask2, subTask3 не должно быть в списке");
    }

    @DisplayName("Задача не пересекается сама с собой")
    @Test
    void shouldNotCrossWithMyself() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 10),
                Duration.ofMinutes(15));

        taskManager.create(task1);

        Task task2 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 10),
                Duration.ofMinutes(15));
        task2.setId(task1.getId());

        assertDoesNotThrow(() -> taskManager.update(task2));
    }

    @DisplayName("Должно выбрасываться исключение при пересечении")
    @Test
    void shouldThrowExceptionOnCross() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 10),
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 15),
                Duration.ofMinutes(15));
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 5),
                Duration.ofMinutes(15));

        taskManager.create(task1);

        assertThrows(TaskTimeValidateException.class, () -> taskManager.create(task2), "Должно быть выброшено исключение: задача начинается до завршения предыдущей");
        assertThrows(TaskTimeValidateException.class, () -> taskManager.create(task3), "Должно быть выброшено исключение: задача заканчивается после начала предыдущей");
    }

    @DisplayName("Не должно быть выброшено исключений для задач на стыке и в свободной временной зоне")
    @Test
    void shouldNotThrowsExceptionsWhenNoCrosses() {
        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 15),
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 30),
                Duration.ofMinutes(15));
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 0),
                Duration.ofMinutes(15));
        Task task4 = new Task("Task4", "Desc3", TaskStatus.NEW, LocalDateTime.of(2024, 6, 12, 10, 5),
                Duration.ofMinutes(15));

        taskManager.create(task1);

        assertDoesNotThrow(() -> taskManager.create(task2), "Задача должна быть добавлена встык после завершения текущей");
        assertDoesNotThrow(() -> taskManager.create(task3), "Задача должна быть добавлена встык до начала текущей");
        assertDoesNotThrow(() -> taskManager.create(task4), "Задача должна быть добавлена в свободный временной интервал");
    }

    @DisplayName("Должно быть расчитано время начала, продолжительность и время окончания для Эпика")
    @Test
    void shouldCalculateTimeFieldsForEpic() {
        Epic epic1 = new Epic("Epic1", "Desc");

        taskManager.createEpic(epic1);

        SubTask task1 = new SubTask(epic1.getId(), "Task1", "Desc1", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 15),
                Duration.ofMinutes(10));
        SubTask task2 = new SubTask(epic1.getId(), "Task2", "Desc2", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 30),
                Duration.ofMinutes(35));
        SubTask task3 = new SubTask(epic1.getId(), "Task3", "Desc3", TaskStatus.NEW, LocalDateTime.of(2024, 6, 11, 10, 0),
                Duration.ofMinutes(15));
        SubTask task4 = new SubTask(epic1.getId(), "Task4", "Desc3", TaskStatus.NEW, LocalDateTime.of(2024, 6, 12, 10, 5),
                Duration.ofMinutes(115));

        taskManager.createSubTask(task1);
        taskManager.createSubTask(task2);
        taskManager.createSubTask(task3);
        taskManager.createSubTask(task4);

        assertEquals(LocalDateTime.of(2024, 6, 11, 10, 0), epic1.getStartTime(),
                "Время начала эпика должно быть 2024-06-11Т10:00");
        assertEquals(Duration.ofMinutes(175), epic1.getDuration(), "Продолжительность эпика должна быть 175 минут");
        assertEquals(LocalDateTime.of(2024, 6, 12, 12, 0), epic1.getEndTime(),
                "Время завершения эпика должно быть 2024-06-12Т12:00");

        taskManager.deleteSubTask(task1.getId());
        assertEquals(LocalDateTime.of(2024, 6, 11, 10, 0), epic1.getStartTime(),
                "Время начала эпика должно быть 2024-06-11Т10:00");
        assertEquals(Duration.ofMinutes(165), epic1.getDuration(), "Продолжительность эпика должна быть 165 минут");
        assertEquals(LocalDateTime.of(2024, 6, 12, 12, 0), epic1.getEndTime(),
                "Время завершения эпика должно быть 2024-06-12Т12:00");

        taskManager.deleteSubTask(task4.getId());
        assertEquals(LocalDateTime.of(2024, 6, 11, 10, 0), epic1.getStartTime(),
                "Время начала эпика должно быть 2024-06-11Т10:00");
        assertEquals(Duration.ofMinutes(50), epic1.getDuration(), "Продолжительность эпика должна быть 50 минут");
        assertEquals(LocalDateTime.of(2024, 6, 11, 11, 5), epic1.getEndTime(),
                "Время завершения эпика должно быть 2024-06-11Т11:05");

        taskManager.deleteSubTask(task3.getId());
        assertEquals(LocalDateTime.of(2024, 6, 11, 10, 30), epic1.getStartTime(),
                "Время начала эпика должно быть 2024-06-11Т10:30");
        assertEquals(Duration.ofMinutes(35), epic1.getDuration(), "Продолжительность эпика должна быть 35 минут");
        assertEquals(LocalDateTime.of(2024, 6, 11, 11, 5), epic1.getEndTime(),
                "Время завершения эпика должно быть 2024-06-11Т11:05");

    }
}
