package managers;

import org.junit.jupiter.api.BeforeEach;

class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        createTask();
    }

}
