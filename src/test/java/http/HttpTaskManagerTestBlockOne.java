package http;

import managers.Managers;
import org.junit.jupiter.api.*;
import tasks.Status;
import tasks.Task;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTestBlockOne extends HttpTaskManagerTest{

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
    void recordSimpleTaskSuccessfulCreationNewSimpleTaskWithDateTime() {
        taskManager.deleteAllSimpleTasks();
        Task simpleTask = new Task(
                "Second SimpleTask", "SimpleTask(ID=2) with DateTime", testStartTime1, duration10H);
        final Long simpleTaskId8 = taskManager.recordSimpleTask(simpleTask);
        Task expectedSimpleTask = new Task(
                simpleTaskId8, "Second SimpleTask", Status.NEW,
                "SimpleTask(ID=2) with DateTime", testStartTime1, duration10H);
        final Task savedSimpleTask = taskManager.getSimpleTaskByIdOrNull(simpleTaskId8);
        assertNotNull(savedSimpleTask, "Задача не найдена.");
        assertEquals(simpleTask, savedSimpleTask, "Задачи не совпадают.");
        assertEquals(expectedSimpleTask, savedSimpleTask, "Задача не совпадает с введенной вручную.");
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertNotNull(testSimpleTasks, "Задачи нe возвращаются.");
        assertEquals(1, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(simpleTask, testSimpleTasks.get(0), "Задачи не совпадают.");
        assertNotNull(taskManager.getSimpleTaskByIdOrNull(simpleTaskId8).getStartTime(),
                "Отсутствует время начала задачи.");
        assertNotNull(taskManager.getSimpleTaskByIdOrNull(simpleTaskId8).getDuration(),
                "Отсутствует продолжительность задачи.");
        assertEquals(taskManager.getSimpleTaskByIdOrNull(simpleTaskId8).getStartTime(), testStartTime1,
                "Время начала задачи  не соответствует заданной.");
        assertEquals(taskManager.getSimpleTaskByIdOrNull(simpleTaskId8).getDuration(), duration10H,
                "Длительность задачи не соответствует заданной.");
    }

    @Test
    void saveSimpleCreationFailTaskDateTimeCollisionTroubleReturnIdMinus1() {
        taskManager.deleteAllSimpleTasks();
        Task simpleTask = new Task("Second SimpleTask", "SimpleTask(ID=8) with DateTime", testStartTime1,
                duration10H);
        Task simpleTaskFail = new Task("Second SimpleTask", "SimpleTask(ID=8) with DateTime", testStartTime1,
                duration10H);
        final Long simpleTaskId8 = taskManager.recordSimpleTask(simpleTask);
        assertEquals(8, simpleTaskId8);
        final Long collisionFailIdMinus1 = taskManager.recordSimpleTask(simpleTaskFail);
        final Task failedSimpleTaskIsNull = taskManager.getSimpleTaskByIdOrNull(collisionFailIdMinus1);
        assertNull(failedSimpleTaskIsNull, "Задача создана вопреки ошибке коллизии времени.");
        assertEquals(-1L, collisionFailIdMinus1,
                "ID задачи, не созданной из-за ошибки коллизии времени задан неверно.");
    }

    @Test
    void updateSimpleTaskSuccessfulUpdateSimpleTaskWithDateTime() {
        taskManager.deleteSimpleTask(simpleTaskId1);
        Task expectedSimpleTask = new Task(simpleTaskId2, "Second SimpleTask updated",
                Status.IN_PROGRESS, "SimpleTask(ID=2) with DateTime", testStartTime1Plus15m, duration10H);
        taskManager.updateSimpleTask(expectedSimpleTask);
        final Task updatedSimpleTask = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
        assertNotNull(updatedSimpleTask, "Задача не найдена.");
        assertEquals(expectedSimpleTask, updatedSimpleTask, "Задачи не совпадают.");
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertNotNull(testSimpleTasks, "Задачи нe возвращаются.");
        assertEquals(1, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(expectedSimpleTask, testSimpleTasks.get(0), "Задачи не совпадают.");
        assertNotNull(taskManager.getSimpleTaskByIdOrNull(simpleTaskId2).getStartTime(),
                "Отсутствует время начала задачи.");
        assertNotNull(taskManager.getSimpleTaskByIdOrNull(simpleTaskId2).getDuration(),
                "Отсутствует продолжительность задачи.");
        assertEquals(taskManager.getSimpleTaskByIdOrNull(simpleTaskId2).getStartTime(), expectedSimpleTask.getStartTime(),
                "Время начала задач не совпадает.");
        assertEquals(taskManager.getSimpleTaskByIdOrNull(simpleTaskId2).getDuration(), expectedSimpleTask.getDuration(),
                "Длительность задач не совпадает.");
    }

    @Test
    void updateSimpleTaskFailTaskDateTimeCollisionTroubleInitialTaskSaved() {
        taskManager.deleteSimpleTask(simpleTaskId1);
        Task initialSimpleTask = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
        LocalDateTime occupiedStartTime = taskManager.getSubTaskByIdOrNull(subTaskId4).getStartTime();
        Task expectedSimpleTask = new Task(simpleTaskId2, "Second SimpleTask", Status.IN_PROGRESS,
                "SimpleTask(ID=2) with DateTime Collision Trouble", occupiedStartTime, duration10H);
        taskManager.updateSimpleTask(expectedSimpleTask);
        final Task updatedSimpleTask = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
        assertNotNull(updatedSimpleTask, "Задача не найдена.");
        assertNotEquals(expectedSimpleTask, updatedSimpleTask, "Задача обновлена вопреки ошибке коллизии времени.");
        assertEquals(initialSimpleTask, updatedSimpleTask, "Исходная задача не сохранилась.");
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertNotNull(testSimpleTasks, "Задачи нe возвращаются.");
        assertEquals(1, testSimpleTasks.size(), "Неверное количество задач.");
    }

    @Test
    void updateSimpleTaskFailInCauseOfWrongId() {
        final List<Task> initialSimpleTasks = taskManager.getSimpleTasks();
        Task expectedSimpleTask = new Task(0L, "Second SimpleTask", Status.IN_PROGRESS,
                "SimpleTask(ID=0) with free ID", testStartTime1Plus15m, duration10H);
        taskManager.updateSimpleTask(expectedSimpleTask);
        final Task updatedTaskExpectedIdZero = taskManager.getSimpleTaskByIdOrNull(0L);
        assertNull(updatedTaskExpectedIdZero, "Обновленная задача с не существовавшим ранее ID сохранена.");
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertEquals(2, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(initialSimpleTasks, testSimpleTasks, "Списки задач отличаются.");
    }

    @Test
    void updateSimpleTaskFailInCauseOfIdOfWrongTaskTypes() {
        final List<Task> initialSimpleTasks = taskManager.getSimpleTasks();
        Task expectedSimpleTask = new Task(subTaskId4, "Second SimpleTask", Status.IN_PROGRESS,
                "SimpleTask(ID=0) with free ID", testStartTime1Plus15m, duration10H);
        taskManager.updateSimpleTask(expectedSimpleTask);
        final Task updatedTaskExpectedId4 = taskManager.getSimpleTaskByIdOrNull(subTaskId4);
        assertNull(updatedTaskExpectedId4, "Обновленная задача с ID задачи другого типа сохранена.");
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertEquals(2, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(initialSimpleTasks, testSimpleTasks, "Списки задач отличаются.");
    }

    @Test
    void updateSimpleTaskFailInCauseOfEmptyId() {
        final List<Task> initialSimpleTasks = taskManager.getSimpleTasks();
        Task expectedSimpleTask = new Task(null, "Second SimpleTask", Status.IN_PROGRESS,
                "SimpleTask(ID=0) with free ID", testStartTime1Plus15m, duration10H);
        taskManager.updateSimpleTask(expectedSimpleTask);
        final Task updatedTaskExpectedIdNull = taskManager.getSimpleTaskByIdOrNull(null);
        assertNull(updatedTaskExpectedIdNull, "Обновленная задача без ID сохранена.");
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertEquals(2, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(initialSimpleTasks, testSimpleTasks, "Списки задач отличаются.");
    }

    @Test
    void getSimpleTasksSuccessfulReturnListOfTasks() {
        Task expectedSimpleTask = new Task(simpleTaskId2, "Second SimpleTask", Status.NEW,
                "SimpleTask(ID=2) with DateTime", testStartTime1, duration10H);
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertNotNull(testSimpleTasks, "Задачи нe возвращаются.");
        assertEquals(2, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(expectedSimpleTask, testSimpleTasks.get(1), "Задачи не совпадают.");
    }

    @Test
    void getSimpleTasksFailReturnEmptyList() {
        taskManager.deleteAllSimpleTasks();
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertNotNull(testSimpleTasks, "Пустой список задач не возвращается.");
        assertTrue(testSimpleTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, testSimpleTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteAllSimpleTasksSuccessfullyRemovedAllSimpleTasks() {
        taskManager.deleteAllSimpleTasks();
        final List<Task> emptyTestSimpleTasks = taskManager.getSimpleTasks();
        assertNotNull(emptyTestSimpleTasks, "Пустой список задач не возвращается.");
        assertTrue(emptyTestSimpleTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyTestSimpleTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteAllSimpleTasksFromEmptySimpleTaskMap() {
        taskManager.deleteAllSimpleTasks();
        final List<Task> emptyTestSimpleTasks = taskManager.getSimpleTasks();
        assertNotNull(emptyTestSimpleTasks, "Пустой список задач не возвращается.");
        assertTrue(emptyTestSimpleTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyTestSimpleTasks.size(), "Неверное количество задач.");
        taskManager.deleteAllSimpleTasks();
        final List<Task> emptyEmptySimpleTasks = taskManager.getSimpleTasks();
        assertNotNull(emptyEmptySimpleTasks, "Пустой список задач не возвращается.");
        assertTrue(emptyEmptySimpleTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyEmptySimpleTasks.size(), "Неверное количество задач.");
        assertEquals(emptyTestSimpleTasks, emptyEmptySimpleTasks, "Списки не совпадают.");
    }
}
