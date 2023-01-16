package managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

class InMemoryHistoryManagerTest {
    static TaskManager taskManager = Managers.getDefault();

    @Test
    void getHistory() {
        taskManager.deleteAllSimpleTasks();
        taskManager.deleteAllEpicTasks();

        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Long subTask2InEpic2 = taskManager.recordSubTask(new SubTask("SubTask2 in epic2", "some description", epic2ID));
        Long subTask3InEpic2 = taskManager.recordSubTask(new SubTask("SubTask3 in epic2", "some description", epic2ID));


        taskManager.getSimpleTaskByIdOrNull(taskID);
        taskManager.getEpicTaskByIdOrNull(epic1ID);
        taskManager.getEpicTaskByIdOrNull(epic2ID);
        taskManager.getSubTaskByIdOrNull(subTask1InEpic2);
        taskManager.getSubTaskByIdOrNull(subTask2InEpic2);
        taskManager.getSubTaskByIdOrNull(subTask3InEpic2);
        // Повторные запросы
        taskManager.getSimpleTaskByIdOrNull(taskID);
        taskManager.getEpicTaskByIdOrNull(epic1ID);
        taskManager.getSubTaskByIdOrNull(subTask1InEpic2);

        //проверка на отсутствие повторов:
        Set<Task> historySet = new HashSet<>(taskManager.getHistory());
        Assertions.assertEquals(historySet.size(), taskManager.getHistory().size());

        taskManager.deleteEpicTask(epic2ID);
        //Должен уйти эпик с сабами
        Assertions.assertFalse(taskManager.getHistory().contains(taskManager.getEpicTaskByIdOrNull(epic2ID)));
        Assertions.assertFalse(taskManager.getHistory().contains(taskManager.getSubTaskByIdOrNull(subTask1InEpic2)));
        Assertions.assertFalse(taskManager.getHistory().contains(taskManager.getSubTaskByIdOrNull(subTask2InEpic2)));
        Assertions.assertFalse(taskManager.getHistory().contains(taskManager.getSubTaskByIdOrNull(subTask3InEpic2)));
        // Другие выжили
        Assertions.assertTrue(taskManager.getHistory().contains(taskManager.getEpicTaskByIdOrNull(epic1ID)));
        Assertions.assertTrue(taskManager.getHistory().contains(taskManager.getSimpleTaskByIdOrNull(taskID)));

    }

    @Test
    void SimpleFive() {
        Long task1 = taskManager.recordSimpleTask(new Task("N", "d"));
        Long task2 = taskManager.recordSimpleTask(new Task("N", "d"));
        Long task3 = taskManager.recordSimpleTask(new Task("N", "d"));
        Long task4 = taskManager.recordSimpleTask(new Task("N", "d"));
        Long task5 = taskManager.recordSimpleTask(new Task("N", "d"));

        //taskManager.getSimpleTaskByIdOrNull(task1);
        taskManager.getSimpleTaskByIdOrNull(task2);
        taskManager.getSimpleTaskByIdOrNull(task3);
        taskManager.getSimpleTaskByIdOrNull(task4);
        taskManager.getSimpleTaskByIdOrNull(task5);

        // Moves tasks 2, 3 and 4 (body)
        taskManager.getSimpleTaskByIdOrNull(task2);
        Assertions.assertEquals(taskManager.getHistory().get(taskManager.getHistory().size() - 1), taskManager.getSimpleTaskByIdOrNull(task2));
        taskManager.getSimpleTaskByIdOrNull(task3);
        Assertions.assertEquals(taskManager.getHistory().get(taskManager.getHistory().size() - 1), taskManager.getSimpleTaskByIdOrNull(task3));
        taskManager.getSimpleTaskByIdOrNull(task4);
        Assertions.assertEquals(taskManager.getHistory().get(taskManager.getHistory().size() - 1), taskManager.getSimpleTaskByIdOrNull(task4));

        // head and tail
        taskManager.getSimpleTaskByIdOrNull(task1);
        Assertions.assertEquals(taskManager.getHistory().get(taskManager.getHistory().size() - 1), taskManager.getSimpleTaskByIdOrNull(task1));
        taskManager.getSimpleTaskByIdOrNull(task5);
        Assertions.assertEquals(taskManager.getHistory().get(taskManager.getHistory().size() - 1), taskManager.getSimpleTaskByIdOrNull(task5));
        taskManager.getSimpleTaskByIdOrNull(task4);
        Assertions.assertEquals(taskManager.getHistory().get(taskManager.getHistory().size() - 1), taskManager.getSimpleTaskByIdOrNull(task4));

        //проверка на отсутствие повторов:
        Set<Task> historySet = new HashSet<>(taskManager.getHistory());
        Assertions.assertEquals(historySet.size(), taskManager.getHistory().size());


        // remove simple task
        taskManager.deleteSimpleTask(task1);
        Assertions.assertFalse(taskManager.getHistory().contains(taskManager.getSimpleTaskByIdOrNull(task1)));
        taskManager.deleteSimpleTask(task2);
        Assertions.assertFalse(taskManager.getHistory().contains(taskManager.getSimpleTaskByIdOrNull(task2)));
        taskManager.deleteSimpleTask(task4);
        Assertions.assertFalse(taskManager.getHistory().contains(taskManager.getSimpleTaskByIdOrNull(task4)));

        //проверка на отсутствие повторов:
        Set<Task> historySet1 = new HashSet<>(taskManager.getHistory());
        Assertions.assertEquals(historySet1.size(), taskManager.getHistory().size());

    }

}