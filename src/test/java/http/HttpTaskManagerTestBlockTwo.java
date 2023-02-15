package http;

import managers.Managers;
import org.junit.jupiter.api.*;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTestBlockTwo extends HttpTaskManagerTest{

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
    void getSimpleTaskByIdOrNullSuccessfulReturnSimpleTask() {
        Task expectedSimpleTask = new Task(simpleTaskId2, "Second SimpleTask", Status.NEW,
                "SimpleTask(ID=2) with DateTime", testStartTime1, duration10H);
        final Task savedSimpleTask = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
        assertNotNull(savedSimpleTask, "Задача не найдена.");
        assertEquals(expectedSimpleTask, savedSimpleTask, "Задачи не совпадают.");
    }

    @Test
    void getSimpleTaskByIdOrNullReturnFailWrongId() {
        final Task savedSimpleTask = taskManager.getSimpleTaskByIdOrNull(0L);
        assertNull(savedSimpleTask, "Обращение по несуществующему ID возвращает задачу.");
    }

    @Test
    void getSimpleTaskByIdOrNullReturnFailIdOfWrongTaskTypes() {
        final Task savedSimpleTask = taskManager.getSimpleTaskByIdOrNull(subTaskId4);
        assertNull(savedSimpleTask, "Обращение по  ID задачи другого типа возвращает задачу данного типа.");
    }

    @Test
    void getSimpleTaskByIdOrNullReturnFailEmptyId() {
        final Task savedSimpleTask = taskManager.getSimpleTaskByIdOrNull(null);
        assertNull(savedSimpleTask, "Обращение по  ID null возвращает задачу.");
    }

    @Test
    void deleteSimpleTaskSuccessfullyRemovedTask() {
        final List<Task> initialSimpleTasks = taskManager.getSimpleTasks();
        taskManager.deleteSimpleTask(simpleTaskId2);
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertEquals(1, testSimpleTasks.size(), "Неверное количество задач.");
        assertNotEquals(initialSimpleTasks, testSimpleTasks, "Списки совпадают.");
        assertNotEquals(initialSimpleTasks.size(), testSimpleTasks.size(), "Размер списков совпадает.");
        assertNull(taskManager.getSimpleTaskByIdOrNull(simpleTaskId2), "Задача не удалена.");
    }

    @Test
    void deleteSimpleTaskFailWrongId() {
        final List<Task> initialSimpleTasks = taskManager.getSimpleTasks();
        taskManager.deleteSimpleTask(0L);
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertEquals(2, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(initialSimpleTasks, testSimpleTasks, "Списки не совпадают.");
        assertEquals(initialSimpleTasks.size(), testSimpleTasks.size(), "Размер списков не совпадает.");
    }

    @Test
    void deleteSimpleTaskFailWrongTaskTypesId() {
        final List<Task> initialSimpleTasks = taskManager.getSimpleTasks();
        taskManager.deleteSimpleTask(subTaskId4);
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertEquals(2, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(initialSimpleTasks, testSimpleTasks, "Списки не совпадают.");
        assertEquals(initialSimpleTasks.size(), testSimpleTasks.size(), "Размер списков не совпадает.");
    }

    @Test
    void deleteSimpleTaskFailEmptyId() {
        final List<Task> initialSimpleTasks = taskManager.getSimpleTasks();
        taskManager.deleteSimpleTask(null);
        final List<Task> testSimpleTasks = taskManager.getSimpleTasks();
        assertEquals(2, testSimpleTasks.size(), "Неверное количество задач.");
        assertEquals(initialSimpleTasks, testSimpleTasks, "Списки не совпадают.");
        assertEquals(initialSimpleTasks.size(), testSimpleTasks.size(), "Размер списков не совпадает.");
    }

    @Test
    void recordSubTaskSuccessfulCreationNewSubTaskWithDateTime() {
        taskManager.deleteAllSubTasks();
        SubTask subTask = new SubTask("First SubTask", "SubTask(ID=8) of first EpicTask(ID=3) with DateTime",
                testStartTime2, duration2H, epicTaskId3);
        final Long subTaskId8 = taskManager.recordSubTask(subTask);
        final SubTask savedSubTask = taskManager.getSubTaskByIdOrNull(subTaskId8);
        SubTask expectedSubTask = new SubTask(subTaskId8, "First SubTask", Status.NEW,
                "SubTask(ID=8) of first EpicTask(ID=3) with DateTime", testStartTime2, duration2H, epicTaskId3);
        assertNotNull(taskManager.getEpicTaskByIdOrNull(savedSubTask.getEpicId()), "EpicTask  отсутствует.");
        assertNotNull(savedSubTask, "Задача не найдена.");
        assertEquals(subTask, savedSubTask, "Задачи не совпадают.");
        assertEquals(expectedSubTask, savedSubTask, "Задачи не совпадают.");
        assertTrue(taskManager.getEpicTaskByIdOrNull(savedSubTask.getEpicId()).getSubTasksIds().contains(savedSubTask.getId()),
                "Задача не добавлена в subTasksOfEpicList.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(1, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(subTask, testSubTasks.get(0), "Задачи не совпадают.");
        final List<Long> testSubTasksOfEpic = taskManager.getEpicTaskByIdOrNull(savedSubTask.getEpicId()).getSubTasksIds();
        assertEquals(1, testSubTasksOfEpic.size(), "Неверное количество задач.");
        assertEquals(subTask, taskManager.getSubTaskByIdOrNull(testSubTasksOfEpic.get(0)), "Задачи не совпадают.");
        assertNotNull(taskManager.getSubTaskByIdOrNull(subTaskId8).getStartTime(), "Отсутствует время начала задачи.");
        assertNotNull(taskManager.getSubTaskByIdOrNull(subTaskId8).getDuration(), "Отсутствует продолжительность задачи.");
        assertEquals(taskManager.getSubTaskByIdOrNull(subTaskId8).getStartTime(), testStartTime2,
                "Время начала задач не совпадает.");
        assertEquals(taskManager.getSubTaskByIdOrNull(subTaskId8).getDuration(), duration2H,
                "Длительность задач не совпадает.");
        SubTask subTask2 = new SubTask("Second SubTask", "SubTask(ID=9) of first EpicTask(ID=3) with DateTime",
                testStartTime3, duration5H, epicTaskId3);
        final Long subTaskId9 = taskManager.recordSubTask(subTask2);
        LocalDateTime epicStartTime;
        if (taskManager.getSubTaskByIdOrNull(subTaskId8).getStartTime().isBefore(taskManager.getSubTaskByIdOrNull(subTaskId9).getStartTime())) {
            epicStartTime = taskManager.getSubTaskByIdOrNull(subTaskId8).getStartTime();
        } else {
            epicStartTime = taskManager.getSubTaskByIdOrNull(subTaskId9).getStartTime();
        }
        final Duration epicDuration = taskManager.getSubTaskByIdOrNull(subTaskId8).getDuration().
                plus(taskManager.getSubTaskByIdOrNull(subTaskId9).getDuration());
        assertNotNull(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStartTime(), "Отсутствует время начала Epic задачи.");
        assertNotNull(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getDuration(), "Отсутствует продолжительность Epic задачи.");
        assertEquals(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStartTime(), epicStartTime,
                "Неверно пересчитано время начала Epic задачи.");
        assertEquals(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getDuration(), epicDuration,
                "Неверно пересчитана длительность Epic задачи.");
    }

    @Test
    void recordSubTaskCreationFailTaskDateTimeCollisionTroubleReturnIdMinus1L() {
        taskManager.deleteAllSubTasks();
        SubTask subTask = new SubTask("First SubTask", "SubTask(ID=8) of first EpicTask(ID=3) with DateTime",
                testStartTime2, duration2H, epicTaskId3);
        final Long subTaskId8 = taskManager.recordSubTask(subTask);
        assertEquals(8, subTaskId8);
        final Long collisionFailIdMinus1 = taskManager.recordSubTask(subTask);
        final SubTask failedSubTaskIsNull = taskManager.getSubTaskByIdOrNull(collisionFailIdMinus1);
        assertNull(failedSubTaskIsNull, "Задача создана вопреки ошибке коллизии времени.");
        assertEquals(-1L, collisionFailIdMinus1,
                "ID задачи, не созданной из-за ошибки коллизии времени задан неверно.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(1, testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void recordSubTaskFailNoSuchEpicTaskReturnIdMinus2L() {
        taskManager.deleteAllEpicTasks();
        SubTask subTask = new SubTask("First SubTask", "SubTask(ID=8) of first EpicTask(ID=3) with DateTime",
                testStartTime2, duration2H, epicTaskId3);
        final Long noSuchEpicFailIdMinus2 = taskManager.recordSubTask(subTask);
        final SubTask savedSubTask = taskManager.getSubTaskByIdOrNull(noSuchEpicFailIdMinus2);
        assertNull(taskManager.getEpicTaskByIdOrNull(subTask.getEpicId()), "По запрашиваемому ID возвращается Epic.");
        assertNotEquals(subTask, savedSubTask, "Задачи не совпадают.");
        assertNull(taskManager.getSubTaskByIdOrNull(noSuchEpicFailIdMinus2), "SubTask создана вопреки отсутствию EpicTask");
        assertEquals(-2L, noSuchEpicFailIdMinus2,
                "ID задачи, не созданной из-за отсутствия EpicTask задан неверно.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(0, testSubTasks.size(), "Неверное количество задач.");
        assertTrue(testSubTasks.isEmpty(), "Список не пуст.");
    }
}
