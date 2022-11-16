import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        runMainTesting(taskManager);
    }

    // Здесь тестирование, описанное в ТЗ, и кусочек моего
    public static void runMainTesting(TaskManager taskManager) {
        // Создайте 2 задачи
        long task1ID = taskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1", taskManager.generateID(), "NEW"));
        long task2ID = taskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2", taskManager.generateID(), "NEW"));

        // один эпик с 2 подзадачами
        long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr", taskManager.generateID(), "NEW"));
        long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", taskManager.generateID(), "NEW", epic1ID));
        long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", taskManager.generateID(), "NEW", epic1ID));

        // другой эпик с 1 подзадачей
        long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr", taskManager.generateID(), "NEW"));
        long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", taskManager.generateID(), "NEW", epic2ID));

        // Распечатать все списки задач
        System.out.println("");
        printTaskList(taskManager.getSimpleTasks());
        printEpicTaskList(taskManager.getEpicTasks());
        printSubTaskList(taskManager.getSubTasks());
        System.out.println("");

        /* Задание: Измените статусы созданных объектов, распечатайте.
            Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        */

        // Update простую задачу
        for (Task task : taskManager.getSimpleTasks().values()) {
            if (task.getName().equals("Task1 name1")) {
                long taskID = task.getID();
                taskManager.updateSimpleTask(new Task("Task1 name1", "Some description Task1",
                        taskID, "IN_PROGRESS"));
                break;
            }
        }

        // Update subtask
        for (SubTask subTask : taskManager.getSubTasks().values()) {
            if (subTask.getName().equals("SubTask2 in epic1")) {
                long taskID = subTask.getID();
                long epicID = subTask.getEpicID();
                taskManager.updateSubTask(new SubTask("SubTask2 in epic1", "some description",
                        taskID, "IN_PROGRESS", epicID)); // Тут должен поменяться и статус подзадачи, и статус эпик1
                break;
            }
        }

        // Распечатать все списки задач
        System.out.println("");
        printTaskList(taskManager.getSimpleTasks());
        printEpicTaskList(taskManager.getEpicTasks());
        printSubTaskList(taskManager.getSubTasks());
        System.out.println("");

        // попробуйте удалить одну из задач и один из эпиков
        // Будем считать, что я магическим образом знаю ID
        taskManager.deleteSimpleTask(taskManager.getSimpleTaskByIDorNull(1));
        taskManager.deleteEpicTask(taskManager.getEpicTaskByIDorNull(6));

        // Распечатать все списки задач
        System.out.println("");
        printTaskList(taskManager.getSimpleTasks());
        printEpicTaskList(taskManager.getEpicTasks());
        printSubTaskList(taskManager.getSubTasks());
        System.out.println("");

        // От меня: проверка на DONE эпика, когда все сабтаски DONE
        long epic3ID = taskManager.recordEpicTask(new EpicTask("EpicName3", "Epic3descr", taskManager.generateID(), "NEW"));
        long subTask1InEpic3 = taskManager.recordSubTask(new SubTask("SubTask1 in epic3", "some description", taskManager.generateID(), "NEW", epic3ID));
        long subTask2InEpic3 = taskManager.recordSubTask(new SubTask("SubTask2 in epic3", "some description", taskManager.generateID(), "NEW", epic3ID));

        for (SubTask subTask : taskManager.getSubTasks().values()) {
            if (subTask.getName().equals("SubTask1 in epic3")) {
                long taskID = subTask.getID();
                long epicID = subTask.getEpicID();
                taskManager.updateSubTask(new SubTask("SubTask1 in epic3", "some description",
                        taskID, "DONE", epicID));
                break;
            }
        }

        for (SubTask subTask : taskManager.getSubTasks().values()) {
            if (subTask.getName().equals("SubTask2 in epic3")) {
                long taskID = subTask.getID();
                long epicID = subTask.getEpicID();
                taskManager.updateSubTask(new SubTask("SubTask2 in epic3", "some description",
                        taskID, "DONE", epicID));
                break;
            }
        }

        // Распечатать все списки задач
        System.out.println("");
        printTaskList(taskManager.getSimpleTasks());
        printEpicTaskList(taskManager.getEpicTasks());
        printSubTaskList(taskManager.getSubTasks());
        System.out.println("");
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