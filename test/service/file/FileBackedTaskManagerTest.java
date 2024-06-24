package service.file;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import service.TaskManagerTest;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    Path temFile;

    @Override
    protected FileBackedTaskManager createManager() {
        createTempFile();
        return FileBackedTaskManager.loadFromFile(temFile);
    }


    void createTempFile() {
        try {
            temFile = Files.createTempFile("tasks", ".tmp");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void deleteTempFile() {
        try {
            Files.deleteIfExists(temFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateFileBackedTaskManagerFromEmptyFile() {
        assertNotNull(taskManager, "Должен быть создан менеджер задач с сохранением в файл");
        assertEquals(0, taskManager.getTasks().size(), "Список задач должен быть пуст");
        assertEquals(0, taskManager.getEpics().size(), "Список эпиков должен быть пуст");
        assertEquals(0, taskManager.getSubtasks().size(), "Список подзадач должен быть пуст");
    }

    @DisplayName("менеджер с файлом должен сохранять и восстанавливать состояние из пустого файла")
    @Test
    void shouldSaveAndRestoreWithoutTasks() {
        assertNotNull(taskManager, "Должен быть создан менеджер задач с сохранением в файл");
        taskManager.save();

        try {
            List<String> lines = Files.readAllLines(temFile);
            assertEquals(1, lines.size(), "В файле должна быть одна строка");
            assertEquals("id,type,name,status,description,epic,duration,startTime", lines.get(0), "Первая строка должна содержать заголовок");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(temFile);
        assertNotNull(manager2, "Должен быть создан менеджер задач с сохранением в файл из файла без задач");
        assertEquals(0, manager2.getTasks().size(), "Список задач должен быть пуст");
        assertEquals(0, manager2.getEpics().size(), "Список эпиков должен быть пуст");
        assertEquals(0, manager2.getSubtasks().size(), "Список подзадач должен быть пуст");

    }

    @Test
    void shouldSaveAndRestoreTasks() {
        final Task task = new Task("Task1", "Desc task 1", TaskStatus.IN_PROGRESS, LocalDateTime.now(),
                Duration.ofMinutes(37));
        taskManager.create(task);
        final Epic epic = new Epic("Epic1", "Desc epic 1");
        taskManager.createEpic(epic);
        final SubTask subTask = new SubTask(epic.getId(), "Sub1", "Desc subtask 1", TaskStatus.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(15));
        taskManager.createSubTask(subTask);

        final FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(temFile);
        assertNotNull(manager2, "Должен быть создан менеджер задач с сохранением в файл из файла с данными");
        assertEquals(1, manager2.getTasks().size(), "Список задач должен содржать 1 элемент");
        assertEquals(1, manager2.getEpics().size(), "Список эпиков должен содржать 1 элемент");
        assertEquals(1, manager2.getSubtasks().size(), "Список подзадач должен содржать 1 элемент");

        final Task task2 = manager2.get(task.getId());
        assertTrue(task2.compareAllFields(task), "Поля загруженной и сохранённой задачи должны быть идентичны");
        final Epic epic2 = manager2.getEpic(epic.getId());
        assertTrue(epic2.compareAllFields(epic), "Поля загруженного и сохранённого эпика должны быть идентичны");
        final SubTask subTask2 = manager2.getSubTask(subTask.getId());
        assertTrue(subTask2.compareAllFields(subTask), "Поля загруженной и сохранённой подзадачи должны быть идентичны");
    }
}
