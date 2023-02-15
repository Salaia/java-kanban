package http;

import managers.Managers;
import org.junit.jupiter.api.*;
import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTestBlockFive extends HttpTaskManagerTest {

    @BeforeAll
    static void startServers() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
    }

    @AfterAll
    static void stopServers() {
        kvServer.stop();
        httpTaskServer.stop();
    }

    @BeforeEach
    void init() {
        taskManager = Managers.getDefaultHttp();
        clearManager();
        createTask();
    }

    @AfterEach
    void clear() {
        clearManager();
    }

    @Test
    void updateEpicTaskSuccessfulUpdateEpicTask() {
        SubTask expectedSubTask = new SubTask(subTaskId7, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=6) of first EpicTask(ID=6)", epicTaskId6);
        taskManager.updateSubTask(expectedSubTask);
        EpicTask expectedEpicTask = new EpicTask(epicTaskId6, "Second EpicTask", "EpicTask(ID=6 without DateTime)");
        expectedEpicTask.setStatus(Status.IN_PROGRESS);
        taskManager.updateEpicTask(expectedEpicTask);
        final EpicTask updatedEpicTask = taskManager.getEpicTaskByIdOrNull(epicTaskId6);
        assertNotNull(updatedEpicTask, "Задача не найдена.");
        assertEquals(expectedEpicTask, updatedEpicTask, "Задачи не совпадают.");
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertNotNull(testEpicTasks, "Задачи нe возвращаются.");
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(expectedEpicTask, testEpicTasks.get(1), "Задачи не совпадают.");
        final Status epicStatus = taskManager.getEpicTaskByIdOrNull(epicTaskId6).getStatus();
        assertEquals(Status.IN_PROGRESS, epicStatus, "Статус Epic задачи не пересчитан.");
    }

    @Test
    void updateEpicTaskFailInCauseOfWrongId() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        EpicTask expectedEpicTask = new EpicTask(0L, "Second EpicTask", "EpicTask(ID=6 without DateTime)");
        taskManager.updateEpicTask(expectedEpicTask);
        final Task updatedTaskExpectedIdZero = taskManager.getEpicTaskByIdOrNull(0L);
        assertNull(updatedTaskExpectedIdZero, "Обновленная задача с не существовавшим ранее ID сохранена.");
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks, testEpicTasks, "Списки задач отличаются.");
    }

    @Test
    void updateEpicTaskFailInCauseOfWrongTaskTypes() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        EpicTask expectedEpicTask = new EpicTask(simpleTaskId2, "Second EpicTask", "EpicTask(ID=6 without DateTime)");
        taskManager.updateEpicTask(expectedEpicTask);
        final Task updatedTaskExpectedId4 = taskManager.getEpicTaskByIdOrNull(simpleTaskId2);
        assertNull(updatedTaskExpectedId4, "Обновленная задача с ID задачи другого типа сохранена.");
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks, testEpicTasks, "Списки задач отличаются.");
    }

    @Test
    void updateEpicTaskFailInCauseOfEmptyId() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        EpicTask expectedEpicTask = new EpicTask(null, "Second EpicTask", "EpicTask(ID=6 without DateTime)");
        taskManager.updateEpicTask(expectedEpicTask);
        final Task updatedTaskExpectedId4 = taskManager.getEpicTaskByIdOrNull(null);
        assertNull(updatedTaskExpectedId4, "Обновленная задача без ID сохранена.");
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks, testEpicTasks, "Списки задач отличаются.");
    }

    @Test
    void getEpicTasksSuccessfulReturnListOfEpicTasks() {
        taskManager.deleteSubTask(subTaskId7); //наличие сабтаски мешает equals
        EpicTask expectedEpicTask = new EpicTask(epicTaskId6, "Second EpicTask", Status.NEW,
                "EpicTask(ID=6 without DateTime)");
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertNotNull(testEpicTasks, "Задачи нe возвращаются.");
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(expectedEpicTask, testEpicTasks.get(1), "Задачи не совпадают.");
    }

    @Test
    void getEpicTasksFailReturnEmptyList() {
        taskManager.deleteAllEpicTasks();
        final List<EpicTask> testSubTasks = taskManager.getEpicTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        final List<SubTask> emptySubTasks = taskManager.getSubTasks();
        assertTrue(emptySubTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptySubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteAllEpicTasksSuccessfullyRemovedAllEpicTasksAndSubTasks() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        assertFalse(initialEpicTasks.isEmpty(), "Список задач пуст.");
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        assertFalse(initialSubTasks.isEmpty(), "Список задач пуст.");
        taskManager.deleteAllEpicTasks();
        final List<EpicTask> emptyEpicTasks = taskManager.getEpicTasks();
        assertTrue(emptyEpicTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> emptySubTasks = taskManager.getSubTasks();
        assertTrue(emptySubTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyEpicTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteAllEpicTasksRemoveFromEmptyMap() {
        taskManager.deleteAllEpicTasks();
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        assertTrue(initialEpicTasks.isEmpty(), "Список не задач пуст.");
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        assertTrue(initialSubTasks.isEmpty(), "Список не задач пуст.");
        taskManager.deleteAllEpicTasks();
        final List<EpicTask> emptyEpicTasks = taskManager.getEpicTasks();
        assertTrue(emptyEpicTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> emptySubTasks = taskManager.getSubTasks();
        assertTrue(emptySubTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyEpicTasks.size(), "Неверное количество задач.");
    }

    @Test
    void getEpicTaskByIdOrNullIdSuccessfulReturnEpicTask() {
        taskManager.deleteSubTask(subTaskId7); //наличие сабтаски мешает equals
        EpicTask expectedEpicTask = new EpicTask(epicTaskId6, "Second EpicTask", Status.NEW,
                "EpicTask(ID=6 without DateTime)");
        final EpicTask savedEpicTask = taskManager.getEpicTaskByIdOrNull(epicTaskId6);
        assertNotNull(savedEpicTask, "Задача не найдена.");
        assertEquals(expectedEpicTask, savedEpicTask, "Задачи не совпадают.");
    }

    @Test
    void getEpicTaskByIdOrNullFailReturnNullInCauseOfWrongId() {
        final EpicTask savedEpicTask = taskManager.getEpicTaskByIdOrNull(0L);
        assertNull(savedEpicTask, "Обращение по несуществующему ID возвращает задачу.");
    }
}
