package http;

import managers.Managers;
import org.junit.jupiter.api.*;
import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTestBlockFour extends HttpTaskManagerTest {

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
    void deleteAllSubTasksFromEmptySubTaskMap() {
        taskManager.deleteAllSubTasks();
        final List<SubTask> emptyTestSubTasks = taskManager.getSubTasks();
        assertNotNull(emptyTestSubTasks, "Задачи нe возвращаются.");
        assertTrue(emptyTestSubTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyTestSubTasks.size(), "Неверное количество задач.");
        assertTrue(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds().isEmpty(),
                "Список задач SubTasksOfEpicList не пуст.");
        assertTrue(taskManager.getEpicTaskByIdOrNull(epicTaskId6).getSubTasksIds().isEmpty(),
                "Список задач SubTasksOfEpicList не пуст.");
        taskManager.deleteAllSubTasks();
        final List<SubTask> emptyEmptyTestSubTasks = taskManager.getSubTasks();
        assertNotNull(emptyEmptyTestSubTasks, "Задачи нe возвращаются.");
        assertTrue(emptyEmptyTestSubTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyEmptyTestSubTasks.size(), "Неверное количество задач.");
        assertTrue(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds().isEmpty(),
                "Список задач SubTasksOfEpicList не пуст.");
        assertTrue(taskManager.getEpicTaskByIdOrNull(epicTaskId6).getSubTasksIds().isEmpty(),
                "Список задач SubTasksOfEpicList не пуст.");
        assertEquals(emptyTestSubTasks, emptyEmptyTestSubTasks, "Списки не совпадают.");
    }

    @Test
    void getSubTaskByIdOrNullSuccessfullyReturnSubTask() {
        SubTask expectedSubTask = new SubTask(subTaskId4, "First SubTask", Status.NEW,
                "SubTask(ID=4) of first EpicTask(ID=3) with DateTime", testStartTime2, duration2H, epicTaskId3);
        final SubTask savedSubTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        assertNotNull(savedSubTask, "Задача не найдена.");
        assertEquals(expectedSubTask, savedSubTask, "Задачи не совпадают.");
    }

    @Test
    void getSubTaskByIdOrNullReturnFailWrongId() {
        final SubTask savedSubTask = taskManager.getSubTaskByIdOrNull(0L);
        assertNull(savedSubTask, "Обращение по несуществующему ID возвращает задачу.");
    }

    @Test
    void getSubTaskByIdOrNullReturnFailIdOfWrongTaskTypes() {
        final Task savedSubTask = taskManager.getSubTaskByIdOrNull(simpleTaskId2);
        assertNull(savedSubTask, "Обращение по  ID задачи другого типа возвращает задачу данного типа.");
    }

    @Test
    void getSubTaskByIdOrNullReturnFailEmptyId() {
        final Task savedSubTask = taskManager.getSubTaskByIdOrNull(null);
        assertNull(savedSubTask, "Обращение по  ID null возвращает задачу.");
    }

    @Test
    void deleteSubTaskSuccessfullyRemovedSubTask() {
        taskManager.updateSubTask(new SubTask(subTaskId4, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=4) of first EpicTask(ID=3) with DateTime", testStartTime2Plus15m, duration2H, epicTaskId3));
        taskManager.updateSubTask(new SubTask(subTaskId5, "Second SubTask", Status.DONE,
                "SubTask(ID=5) of first EpicTask(ID=3) with DateTime", testStartTime3Minus15m, duration2H, epicTaskId3));
        final Long epicId = taskManager.getSubTaskByIdOrNull(subTaskId4).getEpicId();
        assertEquals(2, taskManager.getEpicTaskByIdOrNull(epicId).getSubTasksIds().size(),
                "Неверное количество задач.");
        final Status initialEpicStatus = taskManager.getEpicTaskByIdOrNull(epicId).getStatus();
        taskManager.deleteSubTask(subTaskId4);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(2, testSubTasks.size(), "Неверное количество задач.");
        assertNull(taskManager.getSubTaskByIdOrNull(subTaskId4), "Задача не удалена.");
        final Status newEpicStatus = taskManager.getEpicTaskByIdOrNull(epicId).getStatus();
        assertNotEquals(initialEpicStatus, newEpicStatus, "Статус Epic задачи не пересчитан.");
        final LocalDateTime newEpicStartTime = taskManager.getEpicTaskByIdOrNull(epicId).getStartTime();
        final Duration newEpicDuration = taskManager.getEpicTaskByIdOrNull(epicId).getDuration();
        assertEquals(taskManager.getSubTaskByIdOrNull(subTaskId5).getStartTime(), newEpicStartTime,
                "Неверно пересчитано время начала Epic задачи.");
        assertEquals(taskManager.getSubTaskByIdOrNull(subTaskId5).getDuration(), newEpicDuration,
                "Неверно пересчитана длительность Epic задачи.");
        taskManager.deleteSubTask(subTaskId5);
        assertTrue(taskManager.getEpicTaskByIdOrNull(epicId).getSubTasksIds().isEmpty(),
                "Список задач SubTasksOfEpicList не пуст.");
        final Status noSubTasksEpicStatus = taskManager.getEpicTaskByIdOrNull(epicId).getStatus();
        assertNotEquals(newEpicStatus, noSubTasksEpicStatus, "Статус Epic задачи не пересчитан.");
        assertEquals(Status.NEW, noSubTasksEpicStatus, "Статус Epic задачи без подзадач задан неверно.");
        final LocalDateTime noSubTasksEpicStartTime = taskManager.getEpicTaskByIdOrNull(epicId).getStartTime();
        final Duration noSubTasksEpicDuration = taskManager.getEpicTaskByIdOrNull(epicId).getDuration();
        assertNotEquals(newEpicStartTime, noSubTasksEpicStartTime, "Не пересчитано время начала Epic задачи.");
        assertNotEquals(newEpicDuration, noSubTasksEpicDuration, "Не пересчитано длительность Epic задачи.");
        assertNull(noSubTasksEpicStartTime, "У Epic без подзадач есть время начала задачи.");
        assertNull(noSubTasksEpicDuration, "У Epic без подзадач есть продолжительность.");
    }

    @Test
    void deleteSubTaskFailWrongId() {
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteSubTask(0L);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks, testSubTasks, "Списки не совпадают.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Размер списков не совпадает.");
    }

    @Test
    void deleteSubTaskFailWrongTaskTypesId() {
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteSubTask(simpleTaskId2);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks, testSubTasks, "Списки не совпадают.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Размер списков не совпадает.");
    }

    @Test
    void deleteSubTaskFailEmptyId() {
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteSubTask(null);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks, testSubTasks, "Списки не совпадают.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Размер списков не совпадает.");
    }

    @Test
    void recordEpicTaskSuccessfulCreationNewEpicTask() {
        taskManager.deleteAllEpicTasks();
        EpicTask epicTask = new EpicTask("First EpicTask", "EpicTask(ID=7)");
        final Long epicTaskId8 = taskManager.recordEpicTask(epicTask);
        EpicTask expectedEpicTask = new EpicTask(
                epicTaskId8, "First EpicTask", Status.NEW, "EpicTask(ID=7)");
        final EpicTask savedEpicTask = taskManager.getEpicTaskByIdOrNull(epicTaskId8);
        assertNotNull(savedEpicTask, "Задача не найдена.");
        assertEquals(epicTask, savedEpicTask, "Задачи не совпадают.");
        assertEquals(expectedEpicTask, savedEpicTask, "Задача не совпадает с введенной вручную.");
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertNotNull(testEpicTasks, "Задачи нe возвращаются.");
        assertEquals(1, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(expectedEpicTask, testEpicTasks.get(0), "Задачи не совпадают.");
        final Duration epicDuration = taskManager.getEpicTaskByIdOrNull(epicTaskId8).getDuration();
        final LocalDateTime epicStart = taskManager.getEpicTaskByIdOrNull(epicTaskId8).getStartTime();
        assertNull(epicStart, "У Epic без подзадач есть время начала задачи.");
        assertNull(epicDuration, "У Epic без подзадач есть продолжительность.");
    }
}
