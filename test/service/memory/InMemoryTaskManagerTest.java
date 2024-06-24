package service.memory;

import service.TaskManagerTest;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }
}
