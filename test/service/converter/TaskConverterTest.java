package service.converter;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class TaskConverterTest {
    @DisplayName("Должен конвенртировать задачи в строку")
    @Test
    void shouldConvertTaskToString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        final Task task = new Task("Task1", "Desc task 1", TaskStatus.IN_PROGRESS,
                LocalDateTime.parse("2024-04-01 12:12", formatter), Duration.ofMinutes(15));
        task.setId(1);
        final Epic epic = new Epic("Epic1", "Desc epic 1");
        epic.setStartTime(LocalDateTime.parse("2024-03-15 22:05", formatter));
        epic.setDuration(Duration.ofMinutes(77));
        epic.setId(2);
        final SubTask subTask = new SubTask(epic.getId(), "Sub1", "Desc subtask 1", TaskStatus.DONE,
                LocalDateTime.parse("2024-02-03 02:01", formatter), Duration.ofMinutes(200));
        subTask.setId(3);

        assertEquals("1,TASK,Task1,IN_PROGRESS,Desc task 1,0,15,2024-04-01T12:12", TaskConverter.toString(task), "Строка для сохранения задачи сформирована неверно");
        assertEquals("2,EPIC,Epic1,NEW,Desc epic 1,0,77,2024-03-15T22:05", TaskConverter.toString(epic), "Строка для сохранения эпика сформирована неверно");
        assertEquals("3,SUBTASK,Sub1,DONE,Desc subtask 1,2,200,2024-02-03T02:01", TaskConverter.toString(subTask), "Строка для сохранения подзадачи сформирована неверно");

    }
}
