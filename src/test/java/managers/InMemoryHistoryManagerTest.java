package managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

class InMemoryHistoryManagerTest {
    static TaskManager taskManager = Managers.getDefault();

    @Test
    void getHistory() {
        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

        Assertions.assertEquals(0, taskManager.getHistory().size());
        taskManager.getSimpleTaskByIDorNull(taskID);
        System.out.println(taskManager.getHistory());
        taskManager.getEpicTaskByIDorNull(epic1ID);
        System.out.println(taskManager.getHistory());
        taskManager.getSubTaskByIDorNull(subTask1InEpic2);
        System.out.println(taskManager.getHistory());

        for (int i = 0; i < 10; i++) {
            taskManager.getSimpleTaskByIDorNull(taskID);
        }

        System.out.println(taskManager.getHistory()); // должны остаться только task
        Assertions.assertEquals(10, taskManager.getHistory().size());
    }

}