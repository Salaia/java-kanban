import managers.Managers;
import managers.TaskManager;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        runMainTesting(taskManager);
    }

    //  Перенесла из класса по тестированию на случай, если платформе ЯП он нужен для тестов
    public static void runMainTesting(TaskManager taskManager) {
        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        // один эпик с 2 подзадачами
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        Long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        // другой эпик с 1 подзадачей
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

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