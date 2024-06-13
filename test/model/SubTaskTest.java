package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    @DisplayName("Подзадачи с одниковым id должны быть одинаковы")
    @Test
    public void shouldBeEqualsWithSameId() {
        SubTask subTask1 = new SubTask(0, "Sub1", "", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));
        SubTask subTask2 = new SubTask(0, "Sub2", "", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(15));

        subTask1.setId(3);
        subTask2.setId(3);

        assertEquals(subTask1, subTask2);
    }

}