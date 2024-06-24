package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @DisplayName("Задачи с одниковым id должны быть одинаковы")
    @Test
    public void shouldBeEqualsWithSameId() {
        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(15));
        Task task2 = new Task("Task2", "Desc2", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(15));

        task1.setId(2);
        task2.setId(2);

        assertEquals(task1,task2);
    }


}