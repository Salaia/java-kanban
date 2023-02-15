package http;

import managers.Managers;
import org.junit.jupiter.api.*;
import tasks.Status;
import tasks.SubTask;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTestBlockThree extends HttpTaskManagerTest{

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
    void updateSubTaskSuccessfulUpdateSubTaskWithDateTime() {
        final Status initialEpicStatus = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStatus();
        SubTask expectedSubTask = new SubTask(subTaskId4, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=4) of first EpicTask(ID=3) with DateTime", testStartTime2Plus15m, duration2H, epicTaskId3);
        taskManager.updateSubTask(expectedSubTask);
        final SubTask updatedSubTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        assertNotNull(updatedSubTask, "Задача не найдена.");
        assertEquals(expectedSubTask, updatedSubTask, "Задачи не совпадают.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(expectedSubTask, testSubTasks.get(0), "Задачи не совпадают.");
        final List<Long> testSubTasksOfEpic = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds();
        assertEquals(2, testSubTasksOfEpic.size(), "Неверное количество задач.");
        assertEquals(expectedSubTask, taskManager.getSubTaskByIdOrNull(testSubTasksOfEpic.get(0)), "Задачи не совпадают.");
        final Status newEpicStatus = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStatus();
        assertNotEquals(initialEpicStatus, newEpicStatus, "Статус Epic задачи не пересчитан.");
        assertNotNull(taskManager.getSubTaskByIdOrNull(subTaskId4).getStartTime(), "Отсутствует время начала задачи.");
        assertNotNull(taskManager.getSubTaskByIdOrNull(subTaskId4).getDuration(), "Отсутствует продолжительность задачи.");
        assertEquals(taskManager.getSubTaskByIdOrNull(subTaskId4).getStartTime(), expectedSubTask.getStartTime(),
                "Время начала задач не совпадает.");
        assertEquals(taskManager.getSubTaskByIdOrNull(subTaskId4).getDuration(), expectedSubTask.getDuration(),
                "Длительность задач не совпадает.");
        LocalDateTime epicStartTime;
        if (taskManager.getSubTaskByIdOrNull(subTaskId4).getStartTime().isBefore(taskManager.getSubTaskByIdOrNull(subTaskId5).getStartTime())) {
            epicStartTime = taskManager.getSubTaskByIdOrNull(subTaskId4).getStartTime();
        } else {
            epicStartTime = taskManager.getSubTaskByIdOrNull(subTaskId5).getStartTime();
        }
        final Duration epicDuration = taskManager.getSubTaskByIdOrNull(subTaskId4).getDuration().
                plus(taskManager.getSubTaskByIdOrNull(subTaskId5).getDuration());
        assertNotNull(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStartTime(), "Отсутствует время начала Epic задачи.");
        assertNotNull(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getDuration(), "Отсутствует продолжительность Epic задачи.");
        assertEquals(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStartTime(), epicStartTime,
                "Неверно пересчитано время начала Epic задачи.");
        assertEquals(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getDuration(), epicDuration,
                "Неверно пересчитана длительность Epic задачи.");
    }

    @Test
    void updateSubTaskFailSubTaskDateTimeCollisionTroubleInitialSubTaskSaved() {
        SubTask initialSubTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        LocalDateTime occupiedStartTime = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2).getStartTime();
        SubTask expectedSubTask = new SubTask(subTaskId4, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=4) of first EpicTask(ID=3) with DateTime", occupiedStartTime, duration2H, epicTaskId3);
        taskManager.updateSubTask(expectedSubTask);
        final SubTask updatedSubTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        assertNotNull(updatedSubTask, "Задача не найдена.");
        assertNotEquals(expectedSubTask, updatedSubTask, "Задача обновлена вопреки ошибке коллизии времени.");
        assertEquals(initialSubTask, updatedSubTask, "Исходная задача не сохранилась.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void updateSubTaskFailNoSuchEpic() {
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        SubTask initialSubTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        final List<Long> subTasksOfEpicId3 = taskManager.getEpicTaskByIdOrNull(initialSubTask.getEpicId()).getSubTasksIds();
        SubTask expectedSubTask = new SubTask(subTaskId4, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=4) of first EpicTask(ID=0) with DateTime", testStartTime2Plus15m, duration2H, 0L);
        taskManager.updateSubTask(expectedSubTask);
        final SubTask updatedSubTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        assertNotNull(updatedSubTask, "Задача не найдена.");
        assertNotEquals(expectedSubTask, updatedSubTask, "Задача обновлена вопреки отсутствию EpicTask.");
        assertEquals(initialSubTask, updatedSubTask, "Исходная задача не сохранилась.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        final List<Long> newSubTasksOfEpicId3 = taskManager.getEpicTaskByIdOrNull(initialSubTask.getEpicId()).getSubTasksIds();
        assertEquals(subTasksOfEpicId3, newSubTasksOfEpicId3, "Изменен список подзадач EpicTask.");
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks, testSubTasks, "Списки задач отличаются.");
    }

    @Test
    void updateSubTaskFailTryToChangeEpicTaskIdToExisting() {
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        final List<Long> subTasksOfEpicId3 = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds();
        final List<Long> subTasksOfEpicId6 = taskManager.getEpicTaskByIdOrNull(epicTaskId6).getSubTasksIds();
        SubTask initialSubTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        SubTask expectedSubTask = new SubTask(subTaskId4, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=4) of first EpicTask(ID=6) with DateTime", testStartTime2Plus15m, duration2H, epicTaskId6);
        taskManager.updateSubTask(expectedSubTask);
        final SubTask updatedSubTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        assertNotNull(updatedSubTask, "Задача не найдена.");
        assertNotEquals(expectedSubTask, updatedSubTask, "Задача обновлена вопреки запрета смены EpicTask.");
        assertEquals(initialSubTask, updatedSubTask, "Исходная задача не сохранилась.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        final List<Long> newSubTasksOfEpicId3 = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds();
        final List<Long> newSubTasksOfEpicId6 = taskManager.getEpicTaskByIdOrNull(epicTaskId6).getSubTasksIds();
        assertEquals(subTasksOfEpicId3, newSubTasksOfEpicId3, "Изменен список подзадач EpicTask.");
        assertEquals(subTasksOfEpicId6, newSubTasksOfEpicId6, "Изменен список подзадач EpicTask.");
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks, testSubTasks, "Списки задач отличаются.");
    }

    @Test
    void updateSubTaskFailInCauseOfWrongId() {
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        SubTask expectedSubTask = new SubTask(0L, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=0) with free ID", testStartTime2Plus15m, duration2H, epicTaskId3);
        taskManager.updateSubTask(expectedSubTask);
        final SubTask updatedSubTaskExpectedIdZero = taskManager.getSubTaskByIdOrNull(0L);
        assertNull(updatedSubTaskExpectedIdZero, "Обновленная задача с не существовавшим ранее ID сохранена.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks, testSubTasks, "Списки задач отличаются.");
    }

    @Test
    void updateSubTaskFailInCauseOfIdOfWrongTaskTypes() {
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        SubTask expectedSubTask = new SubTask(simpleTaskId2, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=0) with wrong ID", testStartTime2Plus15m, duration2H, epicTaskId3);
        taskManager.updateSubTask(expectedSubTask);
        final SubTask updatedSubTaskExpectedId2 = taskManager.getSubTaskByIdOrNull(simpleTaskId2);
        assertNull(updatedSubTaskExpectedId2, "Обновленная задача с ID задачи другого типа сохранена.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks, testSubTasks, "Списки задач отличаются.");
    }

    @Test
    void updateSubTaskFailInCauseOfEmptyId() {
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        SubTask expectedSubTask = new SubTask(null, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=0) with empty ID", testStartTime2Plus15m, duration2H, epicTaskId3);
        taskManager.updateSubTask(expectedSubTask);
        final SubTask updatedSubTaskExpectedId2 = taskManager.getSubTaskByIdOrNull(null);
        assertNull(updatedSubTaskExpectedId2, "Обновленная задача без ID сохранена.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks, testSubTasks, "Списки задач отличаются.");
    }

    @Test
    void getSubTasksSuccessfulReturnListOfSubTasks() {
        SubTask expectedSubTask = new SubTask(subTaskId4, "First SubTask", Status.NEW,
                "SubTask(ID=4) of first EpicTask(ID=3) with DateTime", testStartTime2, duration2H, epicTaskId3);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(expectedSubTask, testSubTasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void getSubTasksFailReturnEmptyList() {
        taskManager.deleteAllSubTasks();
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertTrue(testSubTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteAllSubTasksSuccessfullyRemovedAllSimpleTasks() {
        taskManager.deleteAllSubTasks();
        final List<SubTask> emptyTestSubTasks = taskManager.getSubTasks();
        assertNotNull(emptyTestSubTasks, "Задачи нe возвращаются.");
        assertTrue(emptyTestSubTasks.isEmpty(), "Список задач не пуст.");
        assertEquals(0, emptyTestSubTasks.size(), "Неверное количество задач.");
        assertTrue(taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds().isEmpty(),
                "Список задач SubTasksOfEpicList не пуст.");
        assertTrue(taskManager.getEpicTaskByIdOrNull(epicTaskId6).getSubTasksIds().isEmpty(),
                "Список задач SubTasksOfEpicList не пуст.");
        final Status noSubTasksEpicStatusOfEpicId3 = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStatus();
        final Status noSubTasksEpicStatusOfEpicId6 = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStatus();
        assertEquals(Status.NEW, noSubTasksEpicStatusOfEpicId3, "Статус Epic задачи без подзадач задан неверно.");
        assertEquals(Status.NEW, noSubTasksEpicStatusOfEpicId6, "Статус Epic задачи без подзадач задан неверно.");
    }
}
