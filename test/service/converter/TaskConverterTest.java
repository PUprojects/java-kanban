package service.converter;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskConverterTest {
    @Test
    void shouldConvertTaskToString() {
        final Task task = new Task("Task1", "Desc task 1", TaskStatus.IN_PROGRESS);
        task.setId(1);
        final Epic epic = new Epic("Epic1", "Desc epic 1");
        epic.setId(2);
        final SubTask subTask = new SubTask(epic.getId(), "Sub1", "Desc subtask 1", TaskStatus.DONE);
        subTask.setId(3);

        assertEquals("1,TASK,Task1,IN_PROGRESS,Desc task 1,0", TaskConverter.toString(task), "Строка для сохранения задачи сформирована неверно");
        assertEquals("2,EPIC,Epic1,NEW,Desc epic 1,0", TaskConverter.toString(epic), "Строка для сохранения эпика сформирована неверно");
        assertEquals("3,SUBTASK,Sub1,DONE,Desc subtask 1,2", TaskConverter.toString(subTask), "Строка для сохранения подзадачи сформирована неверно");

    }
}
