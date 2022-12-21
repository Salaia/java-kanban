package managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSOutput;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

class InMemoryHistoryManagerTest {
    static TaskManager taskManager = Managers.getDefault();

    @Test
    void getHistory() {
        System.out.println(taskManager.getHistory());
        System.out.println("\nИстория в тестах смешивается, то есть, в тест getHistory попадает история из SimpleFive\n");

        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Long subTask2InEpic2 = taskManager.recordSubTask(new SubTask("SubTask2 in epic2", "some description", epic2ID));
        Long subTask3InEpic2 = taskManager.recordSubTask(new SubTask("SubTask3 in epic2", "some description", epic2ID));


        taskManager.getSimpleTaskByIDorNull(taskID);
        taskManager.getEpicTaskByIDorNull(epic1ID);
        taskManager.getEpicTaskByIDorNull(epic2ID);
        taskManager.getSubTaskByIDorNull(subTask1InEpic2);
        taskManager.getSubTaskByIDorNull(subTask2InEpic2);
        taskManager.getSubTaskByIDorNull(subTask3InEpic2);
        // Повторные запросы
        taskManager.getSimpleTaskByIDorNull(taskID);
        taskManager.getEpicTaskByIDorNull(epic1ID);
        taskManager.getSubTaskByIDorNull(subTask1InEpic2);

        System.out.println("Проверка по ТЗ, проверка на отсутствие повторов: \n" + taskManager.getHistory());

        System.out.println("Remove epic with subs:");
        taskManager.deleteEpicTask(epic1ID);
        taskManager.deleteSubTask(subTask1InEpic2); // Почему-то удаляет simple task
        taskManager.deleteEpicTask(epic2ID);
        System.out.println("Должен уйти эпик с сабами: \n" + taskManager.getHistory());

    }

    @Test
    void SimpleFive() {
        Long task1 = taskManager.recordSimpleTask(new Task("N", "d"));
        Long task2 = taskManager.recordSimpleTask(new Task("N", "d"));
        Long task3 = taskManager.recordSimpleTask(new Task("N", "d"));
        Long task4 = taskManager.recordSimpleTask(new Task("N", "d"));
        Long task5 = taskManager.recordSimpleTask(new Task("N", "d"));

        taskManager.getSimpleTaskByIDorNull(task1);
        taskManager.getSimpleTaskByIDorNull(task2);
        taskManager.getSimpleTaskByIDorNull(task3);
        taskManager.getSimpleTaskByIDorNull(task4);
        taskManager.getSimpleTaskByIDorNull(task5);
        System.out.println("Simple history direct order: " + taskManager.getHistory() + "\n");

        // move your body
        System.out.println("Moves tasks 2, 3 and 4 (body)");
        taskManager.getSimpleTaskByIDorNull(task2);
        System.out.println(taskManager.getHistory());
        taskManager.getSimpleTaskByIDorNull(task3);
        System.out.println(taskManager.getHistory());
        taskManager.getSimpleTaskByIDorNull(task4);
        System.out.println(taskManager.getHistory() + "\n");

        // head and tail
        taskManager.getSimpleTaskByIDorNull(task1);
        System.out.println("head -> tail:\n" + taskManager.getHistory()); // head -> tail
        taskManager.getSimpleTaskByIDorNull(task5);
        System.out.println("\nold tail-head is tail again:\n" + taskManager.getHistory()); // old tail is tail again
        taskManager.getSimpleTaskByIDorNull(task4);
        System.out.println("After moving tail-head, move body node to tail:\n" + taskManager.getHistory());

        // remove simple task
        taskManager.deleteSimpleTask(task1);
        System.out.println("Removed body simple task: \n" + taskManager.getHistory()); // Все норм
        taskManager.deleteSimpleTask(task2);
        System.out.println("Removed head simple task: \n" + taskManager.getHistory()); // Все норм
        taskManager.deleteSimpleTask(task4);
        System.out.println("Removed tail simple task: \n" + taskManager.getHistory()); // Все норм

        // Отдельно оно работает, но если вслед за этим запустить второй тест - нет.

    }

}