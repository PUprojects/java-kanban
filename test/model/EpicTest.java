package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    @DisplayName("Эпики с одниковым id должны быть одинаковы")
    @Test
    public void shouldBeEqualsWithSameId() {
        Epic epic1 = new Epic("Epic1", "Desc1");
        Epic epic2 = new Epic("Epic2", "Desc2");

        epic1.setId(1);
        epic2.setId(1);

        assertEquals(epic1, epic2);
    }

}