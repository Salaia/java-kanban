package managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
        static InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();

        @Test
        void getHistory(){
                long taskID = inMemoryTaskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
                // один эпик с 2 подзадачами
                long epic1ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
                long subTask1InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
                long subTask2InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
                // другой эпик с 1 подзадачей
                long epic2ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
                long subTask1InEpic2 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

                Assertions.assertEquals(0, inMemoryTaskManager.getInMemoryHistoryManager().getHistory().size());
                inMemoryTaskManager.getSimpleTaskByIDorNull(taskID);
                System.out.println(inMemoryTaskManager.getInMemoryHistoryManager().getHistory());
                inMemoryTaskManager.getEpicTaskByIDorNull(epic1ID);
                System.out.println(inMemoryTaskManager.getInMemoryHistoryManager().getHistory());
                inMemoryTaskManager.getSubTaskByIDorNull(subTask1InEpic2);
                System.out.println(inMemoryTaskManager.getInMemoryHistoryManager().getHistory());
                for (int i = 0; i < 10; i++) {
                        inMemoryTaskManager.getSimpleTaskByIDorNull(taskID);
                }
                System.out.println(inMemoryTaskManager.getInMemoryHistoryManager().getHistory()); // должны остаться только task
                Assertions.assertEquals(10, inMemoryTaskManager.getInMemoryHistoryManager().getHistory().size());

        }

}