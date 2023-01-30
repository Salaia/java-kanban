package managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;
import tasks.TaskTypes;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/*
    Эти тесты выдумывала с трудом, так как было трудно понять, зачем и как тестировать методы, а не ситуации
    (кейсы, так их лучше назвать?)
    В общем, высасывала из пальца.
    Также здесь должен быть тест на расчёт статуса эпика, но, во-первых, его немного есть в новом тесте - по эпику.
    Во-вторых, это было в моих старых тестах, не уверена, что нужно сюда дублировать.

    На попытке придумать негативные сценарии использования отдельных методов моя голова отказалась работать.
    Ввиду того, что я не понимаю, что меня просят проверить - я не понимаю как это делать :(
 */

class TaskManagerTest {

    TaskManager manager = new FileBackedTaskManager(
            new File("src/main/java/storage/TaskManagerSaved.csv"));

    static Long taskID;
    static Long epic1ID;
    static Long epic2ID;
    static Long subTask1InEpic2;
    static Long subTask2InEpic2;
    static Long subTask3InEpic2;


    @BeforeEach
    void beforeEach() {
        // создаем разные таски
        taskID = manager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        epic1ID = manager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        epic2ID = manager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        subTask1InEpic2 = manager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        subTask2InEpic2 = manager.recordSubTask(new SubTask("SubTask2 in epic2", "some description", epic2ID));
        subTask3InEpic2 = manager.recordSubTask(new SubTask("SubTask3 in epic2", "some description", epic2ID));

        // заполняем историю
        manager.getSimpleTaskByIdOrNull(taskID);
        manager.getSimpleTaskByIdOrNull(taskID);
        manager.getEpicTaskByIdOrNull(epic2ID);
        manager.getSubTaskByIdOrNull(subTask3InEpic2);
        manager.getSimpleTaskByIdOrNull(taskID);
    }

    @AfterEach
    void afterEach() {
        //проверка, что у все сабтаски привязаны к своим эпикам
        for (SubTask sub : manager.getSubTasks()) {
            assertNotNull(sub.getEpicId());
        }
    }

    @Test
    void getHistory() {
        assertNotNull(manager.getHistory());
    }

    @Test
    void getSimpleTasks() {
        assertNotNull(manager.getSimpleTasks());
    }

    @Test
    void getEpicTasks() {
        assertNotNull(manager.getEpicTasks());
    }

    @Test
    void getSubTasks() {
        assertNotNull(manager.getSubTasks());
    }

    @Test
    void deleteAllSimpleTasks() {
        assertFalse(manager.getSimpleTasks().isEmpty());
        manager.deleteAllSimpleTasks();
        assertTrue(manager.getSimpleTasks().isEmpty());
    }

    @Test
    void deleteAllEpicTasks() {
        assertFalse(manager.getEpicTasks().isEmpty());
        manager.deleteAllEpicTasks();
        assertTrue(manager.getEpicTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
    }

    @Test
    void deleteAllSubTasks() {
        assertFalse(manager.getSubTasks().isEmpty());
        manager.deleteAllSubTasks();
        assertFalse(manager.getEpicTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
    }

    @Test
    void deleteSimpleTask() {
        // нормальное поведение
        assertTrue(manager.getSimpleTasks().contains(manager.getSimpleTaskByIdOrNull(taskID)));
        manager.deleteSimpleTask(taskID);
        assertFalse(manager.getSimpleTasks().contains(manager.getSimpleTaskByIdOrNull(taskID)));

        manager.deleteSimpleTask(epic1ID);
        assertTrue(manager.getEpicTasks().contains(manager.getEpicTaskByIdOrNull(epic1ID)));
        manager.deleteSimpleTask(null);
    }

    @Test
    void deleteEpicTask() {
        assertTrue(manager.getEpicTasks().contains(manager.getEpicTaskByIdOrNull(epic1ID)));
        manager.deleteEpicTask(epic1ID);
        assertFalse(manager.getEpicTasks().contains(manager.getEpicTaskByIdOrNull(epic1ID)));
    }

    @Test
    void deleteSubTask() {
        assertTrue(manager.getSubTasks().contains(manager.getSubTaskByIdOrNull(subTask1InEpic2)));
        manager.deleteSubTask(subTask1InEpic2);
        assertFalse(manager.getSubTasks().contains(manager.getSubTaskByIdOrNull(subTask1InEpic2)));
    }

    @Test
    void getSimpleTaskByIdOrNull() {
        assertEquals(TaskTypes.TASK, manager.getSimpleTaskByIdOrNull(taskID).getTaskType());
    }

    @Test
    void getEpicTaskByIdOrNull() {
        assertEquals(TaskTypes.EPIC, manager.getEpicTaskByIdOrNull(epic1ID).getTaskType());
    }

    @Test
    void getSubTaskByIdOrNull() {
        assertEquals(TaskTypes.SUBTASK, manager.getSubTaskByIdOrNull(subTask1InEpic2).getTaskType());
    }

    @Test
    void getAllSubTasksOfEpicOrNull() {
        assertNotNull(manager.getAllSubTasksOfEpicOrNull(epic1ID));
        manager.deleteEpicTask(epic1ID);
        assertNull(manager.getAllSubTasksOfEpicOrNull(epic1ID));
    }

    @Test
    void recordSimpleTask() {
        assertEquals(1, manager.getSimpleTasks().size());
        Long newTaskId = manager.recordSimpleTask(new Task("n", "d"));
        assertEquals(2, manager.getSimpleTasks().size());
        assertTrue(manager.getSimpleTasks().contains(manager.getSimpleTaskByIdOrNull(newTaskId)));
    }

    @Test
    void recordEpicTask() {
        assertEquals(2, manager.getEpicTasks().size());
        Long newEpic = manager.recordEpicTask(new EpicTask("n", "d"));
        assertEquals(3, manager.getEpicTasks().size());
        assertTrue(manager.getEpicTasks().contains(manager.getEpicTaskByIdOrNull(newEpic)));
    }

    @Test
    void recordSubTask() {
        assertEquals(3, manager.getSubTasks().size());
        Long newSubId = manager.recordSubTask(new SubTask("n", "d", epic1ID));
        assertEquals(4, manager.getSubTasks().size());
        assertTrue(manager.getSubTasks().contains(manager.getSubTaskByIdOrNull(newSubId)));
    }

    @Test
    void updateSimpleTask() {
        Task newTask = manager.getSimpleTaskByIdOrNull(taskID);
        assertEquals("SimpleTaskName", manager.getSimpleTaskByIdOrNull(taskID).getName());
        newTask.setName("n");
        manager.updateSimpleTask(newTask);
        assertEquals("n", manager.getSimpleTaskByIdOrNull(taskID).getName());
    }

    @Test
    void updateEpicTask() {
        EpicTask newEpic = manager.getEpicTaskByIdOrNull(epic1ID);
        assertEquals("EpicName1", manager.getEpicTaskByIdOrNull(epic1ID).getName());
        newEpic.setName("n");
        manager.updateEpicTask(newEpic);
        assertEquals("n", manager.getEpicTaskByIdOrNull(epic1ID).getName());
    }

    @Test
    void updateSubTask() {
        SubTask newSub = manager.getSubTaskByIdOrNull(subTask1InEpic2);
        assertEquals("SubTask1 in epic2", manager.getSubTaskByIdOrNull(subTask1InEpic2).getName());
        newSub.setName("n");
        manager.updateSubTask(newSub);
        assertEquals("n", manager.getSubTaskByIdOrNull(subTask1InEpic2).getName());
    }
}