package service.server;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.TaskManager;
import service.memory.InMemoryTaskManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();


    @BeforeEach
    void httpTaskServerTestInit() {
        manager.clearTasks();
        manager.clearSubTasks();
        manager.clearEpics();
        server.start();
    }

    @AfterEach
    void httpTaskServerTestStop() {
        server.stop();
    }

    @Test
    @DisplayName("Сервер должен выдавать список задач")
    public void shouldGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(25));
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, LocalDateTime.now().plusHours(2),
                Duration.ofMinutes(75));

        manager.create(task1);
        manager.create(task2);
        manager.create(task3);

        List<Task> tasksFromManager = manager.getTasks();

        String taskJson = gson.toJson(tasksFromManager);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");
            assertEquals(taskJson, response.body(), "Теуст json со списком всех задач некорректен");
        }
    }

    @Test
    @DisplayName("Сервер должен возвращать конекретную задачу по id")
    public void shouldGetTaskById() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW, startTime,
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(25));
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, startTime.plusHours(2),
                Duration.ofMinutes(75));

        manager.create(task1);
        task2 = manager.create(task2);
        manager.create(task3);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks/" + task2.getId());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");

            Task taskReturned = gson.fromJson(response.body(), Task.class);

            assertEquals(task2, taskReturned, "Возвращена некорректная задача");
            assertTrue(task2.compareAllFields(taskReturned), "Данные в возвращённой задаче некорректны");
        }

    }

    @Test
    @DisplayName("Сервер должен возвращать код 404 для неконекретного id задачи")
    public void shouldReturnError404OnBadTaskId() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks/11");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode(), "Должен бытьо возвращён код ошибки 404");
        }
    }

    @Test
    @DisplayName("Сервер должен создавать задачи")
    public void shouldAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test task 1", "Test task description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        String taskJson = gson.toJson(task);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");

            List<Task> tasksFromManager = manager.getTasks();
            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Количество задач должно быть равно 1");
            assertNotNull(tasksFromManager.get(0).getId(), "Id задачи не должен быть null");
            assertEquals("Test task 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
        }
    }

    @Test
    @DisplayName("Сервер должен возвращать код 406 для пересекающейся задачи")
    public void shouldReturn406OnTimeValidation() throws IOException, InterruptedException {
        Task task1 = new Task("Test task 1", "Test task 1 description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Test task 2", "Test task 2 description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));

        manager.create(task1);

        String taskJson = gson.toJson(task2);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(406, response.statusCode(), "Должен бытьо возвращён код ошибки 406");
        }
    }

    @Test
    @DisplayName("Сервер должен обновлять задачи")
    public void shouldUpdateTask() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW, startTime,
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(25));

        manager.create(task1);
        task2.setId(task1.getId());

        String taskJson = gson.toJson(task2);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");

            List<Task> tasksFromManager = manager.getTasks();
            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Количество задач должно быть равно 1");
            assertTrue(tasksFromManager.get(0).compareAllFields(task2), "Все полля задачи 1 должны быть обновлены");
        }
    }

    @Test
    @DisplayName("Сервер должен удалять задачи")
    public void shouldDeleteTasks() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW, startTime,
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(25));
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, startTime.plusHours(2),
                Duration.ofMinutes(75));

        task1 = manager.create(task1);
        task2 = manager.create(task2);
        task3 = manager.create(task3);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks/" + task2.getId());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(204, response.statusCode(), "Должен бытьо возвращён код успеха 204");

            List<Task> tasksFromManager = manager.getTasks();
            assertNotNull(tasksFromManager, "Задачи не возвращаются");
            assertEquals(2, tasksFromManager.size(), "Количество задач должно быть равно 2");
            assertTrue(tasksFromManager.contains(task1), "Задача 1 должна быть в списке");
            assertFalse(tasksFromManager.contains(task2), "Задачи 2 не должно быть в списке");
            assertTrue(tasksFromManager.contains(task3), "Задача 3 должна быть в списке");
        }
    }

    @Test
    @DisplayName("Сервер не должен возвращать ошибку при удалении не существующей задачи")
    public void shouldNotErrorOnDeleteInvalidTask() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks/543");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(204, response.statusCode(), "Должен бытьо возвращён код успеха 204");
        }
    }

    @Test
    @DisplayName("Сервер должен выдавать список подзадач")
    public void shouldGetAllSubasks() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 decription");
        SubTask subTask1 = new SubTask(1, "Task1", "Desc1", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(15));
        SubTask subTask2 = new SubTask(1, "Task2", "Desc2", TaskStatus.NEW, LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(25));
        SubTask subTask3 = new SubTask(1, "Task3", "Desc3", TaskStatus.NEW, LocalDateTime.now().plusHours(2),
                Duration.ofMinutes(75));

        manager.createEpic(epic1);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        manager.createSubTask(subTask3);

        List<SubTask> subtasksFromManager = manager.getSubtasks();

        String subtaskJson = gson.toJson(subtasksFromManager);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");
            assertEquals(subtaskJson, response.body(), "Текст json со списком всех задач некорректен");
        }
    }

    @Test
    @DisplayName("Сервер должен возвращать конекретную подзадачу по id")
    public void shouldGetSubTaskById() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 decription");
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        SubTask subTask1 = new SubTask(1, "Task1", "Desc1", TaskStatus.NEW, startTime,
                Duration.ofMinutes(15));
        SubTask subTask2 = new SubTask(1, "Task2", "Desc2", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(25));
        SubTask subTask3 = new SubTask(1, "Task3", "Desc3", TaskStatus.NEW, startTime.plusHours(2),
                Duration.ofMinutes(75));

        manager.createEpic(epic1);
        manager.createSubTask(subTask1);
        subTask2 = manager.createSubTask(subTask2);
        manager.createSubTask(subTask3);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks/" + subTask2.getId());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");

            SubTask subtaskReturned = gson.fromJson(response.body(), SubTask.class);

            assertEquals(subTask2, subtaskReturned, "Возвращена некорректная задача");
            assertTrue(subTask2.compareAllFields(subtaskReturned), "Данные в возвращённой задаче некорректны");
        }

    }

    @Test
    @DisplayName("Сервер должен возвращать код 404 для неконекретного id подзадачи")
    public void shouldReturnError404OnBadSubTaskId() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks/11");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode(), "Должен бытьо возвращён код ошибки 404");
        }
    }

    @Test
    @DisplayName("Сервер должен создавать подзадачи")
    public void shouldAddSubTask() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");
        SubTask subTask = new SubTask(1, "Test subtask 1", "Test task description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));

        manager.createEpic(epic1);

        String taskJson = gson.toJson(subTask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");

            List<SubTask> tasksFromManager = manager.getSubtasks();
            assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Количество подзадач должно быть равно 1");
            assertNotNull(tasksFromManager.get(0).getId(), "Id подзадачи не должен быть null");
            assertEquals("Test subtask 1", tasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
        }
    }

    @Test
    @DisplayName("Сервер должен возвращать код 406 для пересекающейся подзадачи")
    public void shouldReturn406OnTimeValidationSubtask() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");
        SubTask task1 = new SubTask(1, "Test task 1", "Test task 1 description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        SubTask task2 = new SubTask(1, "Test task 2", "Test task 2 description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));

        manager.createEpic(epic1);
        manager.createSubTask(task1);

        String taskJson = gson.toJson(task2);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(406, response.statusCode(), "Должен быть возвращён код ошибки 406");
        }
    }

    @Test
    @DisplayName("Сервер должен обновлять подзадачи")
    public void shouldUpdateSubTask() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        SubTask task1 = new SubTask(1, "Task1", "Desc1", TaskStatus.NEW, startTime,
                Duration.ofMinutes(15));
        SubTask task2 = new SubTask(1, "Task2", "Desc2", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(25));

        manager.createEpic(epic1);
        manager.createSubTask(task1);
        task2.setId(task1.getId());

        String taskJson = gson.toJson(task2);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");

            List<SubTask> tasksFromManager = manager.getSubtasks();
            assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Количество подзадач должно быть равно 1");
            assertTrue(tasksFromManager.get(0).compareAllFields(task2), "Все полля подзадачи 1 должны быть обновлены");
        }
    }

    @Test
    @DisplayName("Сервер должен удалять подзадачи")
    public void shouldDeleteSubTasks() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        SubTask task1 = new SubTask(1, "Task1", "Desc1", TaskStatus.NEW, startTime,
                Duration.ofMinutes(15));
        SubTask task2 = new SubTask(1, "Task2", "Desc2", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(25));
        SubTask task3 = new SubTask(1, "Task3", "Desc3", TaskStatus.NEW, startTime.plusHours(2),
                Duration.ofMinutes(75));

        manager.createEpic(epic1);
        task1 = manager.createSubTask(task1);
        task2 = manager.createSubTask(task2);
        task3 = manager.createSubTask(task3);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks/" + task2.getId());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(204, response.statusCode(), "Должен бытьо возвращён код успеха 204");

            List<SubTask> tasksFromManager = manager.getSubtasks();
            assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
            assertEquals(2, tasksFromManager.size(), "Количество подзадач должно быть равно 2");
            assertTrue(tasksFromManager.contains(task1), "Подзадача 1 должна быть в списке");
            assertFalse(tasksFromManager.contains(task2), "Подзадачи 2 не должно быть в списке");
            assertTrue(tasksFromManager.contains(task3), "Подзадача 3 должна быть в списке");
        }
    }

    @Test
    @DisplayName("Сервер не должен возвращать ошибку при удалении не существующей подзадачи")
    public void shouldNotErrorOnDeleteInvalidSubTask() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks/543");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(204, response.statusCode(), "Должен бытьо возвращён код успеха 204");
        }
    }

    @Test
    @DisplayName("Сервер должен выдавать список эпиков")
    public void shouldGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");
        Epic epic2 = new Epic("Epic 1", "Epic 1 description");
        Epic epic3 = new Epic("Epic 1", "Epic 1 description");

        manager.createEpic(epic1);
        manager.createEpic(epic2);
        manager.createEpic(epic3);

        List<Epic> tasksFromManager = manager.getEpics();

        String taskJson = gson.toJson(tasksFromManager);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");
            assertEquals(taskJson, response.body(), "Текст json со списком всех эпиков некорректен");
        }
    }

    @Test
    @DisplayName("Сервер должен возвращать конекретный эпик по id")
    public void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");
        Epic epic2 = new Epic("Epic 1", "Epic 1 description");
        Epic epic3 = new Epic("Epic 1", "Epic 1 description");

        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        SubTask task1 = new SubTask(2, "Task1", "Desc1", TaskStatus.NEW, startTime,
                Duration.ofMinutes(15));
        SubTask task2 = new SubTask(2, "Task2", "Desc2", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(25));
        SubTask task3 = new SubTask(2, "Task3", "Desc3", TaskStatus.NEW, startTime.plusHours(2),
                Duration.ofMinutes(75));

        manager.createEpic(epic1);
        epic2 = manager.createEpic(epic2);
        manager.createEpic(epic3);

        manager.createSubTask(task1);
        manager.createSubTask(task2);
        manager.createSubTask(task3);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics/" + epic2.getId());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");

            Epic epicReturned = gson.fromJson(response.body(), Epic.class);

            assertEquals(epic2, epicReturned, "Возвращён некорректный эпик");
            assertTrue(epic2.compareAllFields(epicReturned), "Данные в возвращённом эпике некорректны");
        }

    }

    @Test
    @DisplayName("Сервер должен возвращать код 404 для неконекретного id эпика")
    public void shouldReturnError404OnBadEpicId() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics/11");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode(), "Должен бытьо возвращён код ошибки 404");
        }
    }

    @Test
    @DisplayName("Сервер должен возвращать код 404 для неконекретного id эпика при запросе подзадач")
    public void shouldReturnError404OnBadEpicIdSubtasksQuery() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics/121/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode(), "Должен бытьо возвращён код ошибки 404");
        }
    }

    @Test
    @DisplayName("Сервер должен возвращать подзадачи конекретного эпика по id")
    public void shouldGetEpicSubtasksById() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        SubTask task1 = new SubTask(1, "Task1", "Desc1", TaskStatus.NEW, startTime,
                Duration.ofMinutes(15));
        SubTask task2 = new SubTask(1, "Task2", "Desc2", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(25));
        SubTask task3 = new SubTask(1, "Task3", "Desc3", TaskStatus.NEW, startTime.plusHours(2),
                Duration.ofMinutes(75));

        epic1 = manager.createEpic(epic1);
        manager.createSubTask(task1);
        manager.createSubTask(task2);
        manager.createSubTask(task3);

        List<SubTask> subtasksFromManager = manager.getEpicSubtasks(epic1.getId());
        String subtasksJson = gson.toJson(subtasksFromManager);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics/" + epic1.getId() + "/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");
            assertEquals(subtasksJson, response.body(), "Теуст json со списком всех задач некорректен");
        }
    }

    @Test
    @DisplayName("Сервер должен создавать эпики")
    public void shouldAddEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");

        String taskJson = gson.toJson(epic1);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");

            List<Epic> tasksFromManager = manager.getEpics();
            assertNotNull(tasksFromManager, "Эпики не возвращаются");
            assertEquals(1, tasksFromManager.size(), "Количество эпиков должно быть равно 1");
            assertNotNull(tasksFromManager.get(0).getId(), "Id эпика не должен быть null");
            assertEquals("Epic 1", tasksFromManager.get(0).getName(), "Некорректное имя эпика");
        }
    }

    @Test
    @DisplayName("Сервер должен удалять эпики")
    public void shouldDeleteEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 description");
        Epic epic2 = new Epic("Epic 1", "Epic 1 description");
        Epic epic3 = new Epic("Epic 1", "Epic 1 description");

        epic1 = manager.createEpic(epic1);
        epic2 = manager.createEpic(epic2);
        epic3 = manager.createEpic(epic3);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics/" + epic2.getId());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(204, response.statusCode(), "Должен бытьо возвращён код успеха 204");

            List<Epic> epicsFromManager = manager.getEpics();
            assertNotNull(epicsFromManager, "Задачи не возвращаются");
            assertEquals(2, epicsFromManager.size(), "Количество эпиков должно быть равно 2");
            assertTrue(epicsFromManager.contains(epic1), "Эпик 1 должна быть в списке");
            assertFalse(epicsFromManager.contains(epic2), "Эпик 2 не должно быть в списке");
            assertTrue(epicsFromManager.contains(epic3), "Эпик 3 должна быть в списке");
        }
    }

    @Test
    @DisplayName("Сервер не должен возвращать ошибку при удалении не существующеего эпика")
    public void shouldNotErrorOnDeleteInvalidEpic() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics/543");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(204, response.statusCode(), "Должен бытьо возвращён код успеха 204");
        }
    }

    @Test
    @DisplayName("Сервер должен возвращать историю")
    public void shouldReturnHistory() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        Task task = new Task("Task", "Desc", TaskStatus.NEW, startTime, Duration.ofMinutes(15));
        Epic epic = new Epic("Epic", "Desc");

        manager.create(task);
        epic = manager.createEpic(epic);
        SubTask subTask = new SubTask(epic.getId(), "SubTask", "Desc", TaskStatus.NEW,
                startTime.plusHours(1), Duration.ofMinutes(15));
        manager.createSubTask(subTask);

        manager.get(1);
        manager.getEpic(2);
        manager.getSubTask(3);

        List<Task> history = manager.getHistory();

        String historyJson = gson.toJson(history);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/history");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");
            assertEquals(historyJson, response.body(), "Текст json со списком истории задач некорректен");
        }
    }

    @Test
    @DisplayName("Должен возвращать список задач в порядке приоритета")
    public void shouldGetPrioritizedTasks() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 25, 10, 15);
        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW, startTime.plusHours(1),
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, startTime,
                Duration.ofMinutes(25));
        Task task3 = new Task("Task3", "Desc3", TaskStatus.NEW, startTime.plusHours(2),
                Duration.ofMinutes(75));

        manager.create(task1);
        manager.create(task2);
        manager.create(task3);

        Set<Task> listFromManager = manager.getPrioritizedTasks();
        String jsonText = gson.toJson(listFromManager);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/prioritized");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), "Должен бытьо возвращён код успеха 200");
            assertEquals(jsonText, response.body(), "Текст json со списком задач в порядке приоритета некорректен");
        }
    }
}
