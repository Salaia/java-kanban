import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;

import java.util.HashMap;

/*
    Интересно, у нас будет GUI? Нам его дадут или сами сделаем? и где он будет жить?
 */
public class Main {

    public static void main(String[] args) {
        Main main = new Main(); // Что-то мне надоело плодить методы static
        TaskManager taskManager = new TaskManager();
        taskManager.generateTaskManager();
        main.runMainTesting(taskManager);
    }

    // Здесь тестирование, описанное в ТЗ, и кусочек моего
    public void runMainTesting(TaskManager taskManager) {
        // Создайте 2 задачи
        taskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1", taskManager.generateID(), "NEW"));
        taskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2", taskManager.generateID(), "NEW"));

        // один эпик с 2 подзадачами
        int epic1ID = taskManager.generateID(); // костыль, подумать, как это адекватнее нарисовать... или бог с ним, всё равно из GUI по-другому
        taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr", epic1ID, "NEW"));
        taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", taskManager.generateID(), "NEW", epic1ID));
        taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", taskManager.generateID(), "NEW", epic1ID));

        // другой эпик с 1 подзадачей
        int epic2ID = taskManager.generateID();
        taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr", epic2ID, "NEW"));
        taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", taskManager.generateID(), "NEW", epic2ID));

        // Распечатать все списки задач
        System.out.println("");
        printTaskList(taskManager.getSimpleTasks());
        printEpicTaskList(taskManager.getEpicTasks());
        printSubTaskList(taskManager.getSubTasks());
        System.out.println("");

        /* Задание: Измените статусы созданных объектов, распечатайте.
            Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
            От меня: тогда я поменяю статусы некоторых задач, а не всех - чтобы нагляднее было
            Хм, не знаю, как это будет в GUI, но тут придётся подумать, как вытаскивать ID.
            Ведь ID генерируется само, я не знаю какое ID у какой задачи.
            Что логичнее всего мне может быть известно? Имя задачи или подзадачи? */

        // Update простую задачу
        for (Task task : taskManager.getSimpleTasks().values()) {
            if (task.getName().equals("Task1 name1")) {
                int taskID = task.getID();
                taskManager.updateSimpleTask(new Task("Task1 name1", "Some description Task1",
                        taskID, "IN_PROGRESS"));
                break;
            }
        }

        // Update subtask
        for (SubTask subTask : taskManager.getSubTasks().values()) {
            if (subTask.getName().equals("SubTask2 in epic1")) {
                int taskID = subTask.getID();
                int epicID = subTask.getEpicID();
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
        int epic3ID = taskManager.generateID();
        taskManager.recordEpicTask(new EpicTask("EpicName3", "Epic3descr", epic3ID, "NEW"));
        taskManager.recordSubTask(new SubTask("SubTask1 in epic3", "some description", taskManager.generateID(), "NEW", epic3ID));
        taskManager.recordSubTask(new SubTask("SubTask2 in epic3", "some description", taskManager.generateID(), "NEW", epic3ID));

        for (SubTask subTask : taskManager.getSubTasks().values()) {
            if (subTask.getName().equals("SubTask1 in epic3")) {
                int taskID = subTask.getID();
                int epicID = subTask.getEpicID();
                taskManager.updateSubTask(new SubTask("SubTask1 in epic3", "some description",
                        taskID, "DONE", epicID));
                break;
            }
        }

        for (SubTask subTask : taskManager.getSubTasks().values()) {
            if (subTask.getName().equals("SubTask2 in epic3")) {
                int taskID = subTask.getID();
                int epicID = subTask.getEpicID();
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

    public void printTaskList(HashMap<Integer, Task> tasks) {
        for (Task task : tasks.values()) {
            System.out.println(task.toString());
        }
    }

    public void printSubTaskList(HashMap<Integer, SubTask> subtasks) {
        for (SubTask subTask : subtasks.values()) {
            System.out.println(subTask.toString());
        }
    }

    public void printEpicTaskList(HashMap<Integer, EpicTask> epicTasks) {
        for (EpicTask epicTask : epicTasks.values()) {
            System.out.println(epicTask.toString());
        }
    }

}