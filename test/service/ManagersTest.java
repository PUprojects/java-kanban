package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void shouldCreateDefaultManager() {
        assertNotNull(Managers.getDefault());
    }

    @Test
    void shouldCreateDefaultHistoryManager() {
        assertNotNull(Managers.getDefaultHistory());
    }
}