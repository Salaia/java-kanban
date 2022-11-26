import managers.InMemoryTaskManager;
import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        runMainTesting(inMemoryTaskManager);
    }

    //  Перенесла из класса по тестированию на случай, если платформе ЯП он нужен для тестов
    public static void runMainTesting(InMemoryTaskManager inMemoryTaskManager) {
        long taskID = inMemoryTaskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        // один эпик с 2 подзадачами
        long epic1ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        // другой эпик с 1 подзадачей
        long epic2ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

        //Assertions.assertEquals(0, inMemoryTaskManager.inMemoryHistoryManager.getHistory().size());
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
        //Assertions.assertEquals(10, inMemoryTaskManager.inMemoryHistoryManager.getHistory().size());

    }

    public static void printTaskList(HashMap<Long, Task> tasks) {
        for (Task task : tasks.values()) {
            System.out.println(task.toString());
        }
    }

    public static void printSubTaskList(HashMap<Long, SubTask> subtasks) {
        for (SubTask subTask : subtasks.values()) {
            System.out.println(subTask.toString());
        }
    }

    public static void printEpicTaskList(HashMap<Long, EpicTask> epicTasks) {
        for (EpicTask epicTask : epicTasks.values()) {
            System.out.println(epicTask.toString());
        }
    }

}