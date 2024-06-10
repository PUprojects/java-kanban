package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    @Test
    public void shouldBeEqualsWithSameId() {
        SubTask subTask1 = new SubTask(0, "Sub1", "", TaskStatus.NEW);
        SubTask subTask2 = new SubTask(0, "Sub2", "", TaskStatus.NEW);

        subTask1.setId(3);
        subTask2.setId(3);

        assertEquals(subTask1, subTask2);
    }

}