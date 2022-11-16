import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
    Не стала убирать из Main тестирование по ТЗ, сюда перенесла только часть.
    Попыталась что-то сделать по мануалам на ютубе и JetBrains, но не уверена, что движусь в правильном направлении.
    А что лучше почитать по JUnit? Просто открыть официальную документацию и учиться изучать по ней?
    Или лучше что-то менее объёмное для начала?
    И мне пытаться изучать JUnit вне Maven или, если уж подключаю одно, то лучше уж строиться и на другом?
    Maven я почти не знаю, но надо же будет изучить...
 */

class TaskManagerTest {

    TaskManager taskManager = new TaskManager();
    long task1ID = taskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1", taskManager.generateID(), "NEW"));


    @Test
    void createTwoSimpleTasks() {
        //long task1ID = taskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1", taskManager.generateID(), "NEW"));
        long task2ID = taskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2", taskManager.generateID(), "NEW"));
    }

    @Test
    void checkSimpleTaskName() {
        assertEquals("Task1 name1", taskManager.getSimpleTaskByIDorNull(1).getName());
    }

    @Test
    void checkSimpleTaskDescription() {
        assertEquals("Some description Task1", taskManager.getSimpleTaskByIDorNull(1).getDescription());
    }

    @Test
    void createOneEpicWithTwoSubtasks() {
        long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr", taskManager.generateID(), "NEW"));
        long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", taskManager.generateID(), "NEW", epic1ID));
        long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", taskManager.generateID(), "NEW", epic1ID));
    }

    @Test
    void updateSimpleTask() {
        for (Task task : taskManager.getSimpleTasks().values()) {
            if (task.getName().equals("Task1 name1")) {
                long taskID = task.getID();
                taskManager.updateSimpleTask(new Task("Task1 name1", "Some description Task1",
                        taskID, "IN_PROGRESS"));
                break;
            }
        }
    }
}