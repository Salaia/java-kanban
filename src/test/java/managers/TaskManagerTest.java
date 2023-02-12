package managers;

import org.junit.jupiter.api.Test;
import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected LocalDateTime testStartTime1;
    protected LocalDateTime testStartTime1Plus15m;
    protected Duration duration10H;
    protected LocalDateTime testStartTime2;
    protected LocalDateTime testStartTime2Plus15m;
    protected Duration duration2H;
    protected LocalDateTime testStartTime3;
    protected LocalDateTime testStartTime3Minus15m;
    protected Duration duration5H;
    protected Long simpleTaskId1;
    protected Long simpleTaskId2;
    protected Long epicTaskId3;
    protected Long subTaskId4;
    protected Long subTaskId5;
    protected Long epicTaskId6;
    protected Long subTaskId7;

    protected void createTask() {
        testStartTime1 = LocalDateTime.of(2023, Month.MARCH, 5, 14, 0, 0);
        testStartTime1Plus15m = testStartTime1.plusMinutes(15);
        duration10H = Duration.ofHours(10);
        testStartTime2 = LocalDateTime.of(2023, Month.MARCH, 1, 18, 30, 0);
        testStartTime2Plus15m = testStartTime2.plusMinutes(15);
        duration2H = Duration.ofHours(2);
        testStartTime3 = LocalDateTime.of(2023, Month.MARCH, 3, 10, 0, 0);
        testStartTime3Minus15m = testStartTime3.minusMinutes(15);
        duration5H = Duration.ofHours(5);

        simpleTaskId1 = taskManager.recordSimpleTask(
                new Task("First SimpleTask", "SimpleTask(ID=1) without DateTime"));
        simpleTaskId2 = taskManager.recordSimpleTask(
                new Task("Second SimpleTask", "SimpleTask(ID=2) with DateTime", testStartTime1, duration10H));
        epicTaskId3 = taskManager.recordEpicTask(
                new EpicTask("First EpicTask", "EpicTask(ID=3) with DateTime"));
        subTaskId4 = taskManager.recordSubTask(
                new SubTask("First SubTask", "SubTask(ID=4) of first EpicTask(ID=3) with DateTime",
                        testStartTime2, duration2H, epicTaskId3));
        subTaskId5 = taskManager.recordSubTask(
                new SubTask("Second SubTask", "SubTask(ID=5) of first EpicTask(ID=3) with DateTime",
                        testStartTime3, duration5H, epicTaskId3));
        epicTaskId6 = taskManager.recordEpicTask(
                new EpicTask("Second EpicTask", "EpicTask(ID=6 without DateTime)"));
        subTaskId7 = taskManager.recordSubTask(
                new SubTask("Third SubTask", "SubTask(ID=7) of first EpicTask(ID=6) without DateTime", epicTaskId6));
    }

    protected void clearManager() {
        taskManager.deleteAllSimpleTasks();
        taskManager.deleteAllEpicTasks();
    }

    @Test
    void recordSimpleTaskSuccessfulCreationNewSimpleTaskWithDateTime() {
        taskManager.deleteAllSimpleTasks();
        Task simpleTask = new Task(
                "Second SimpleTask", "SimpleTask(ID=2) with DateTime", testStartTime1, duration10H);
        final Long simpleTaskId8 = taskManager.recordSimpleTask(simpleTask);
        Task expectedSimpleTask = new Task(
                simpleTaskId8, TaskTypes.TASK, "Second SimpleTask", Status.NEW,
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
        final Long collisionFailIdMinus1 = taskManager.recordSimpleTask(simpleTaskFail);
        final Task failedSimpleTaskIsNull = taskManager.getSimpleTaskByIdOrNull(collisionFailIdMinus1);
        assertNull(failedSimpleTaskIsNull, "Задача создана вопреки ошибке коллизии времени.");
        assertEquals(-1L, collisionFailIdMinus1,
                "ID задачи, не созданной из-за ошибки коллизии времени задан неверно.");
    }

    @Test
    void updateSimpleTaskSuccessfulUpdateSimpleTaskWithDateTime() {
        taskManager.deleteSimpleTask(simpleTaskId1);
        Task expectedSimpleTask = new Task(simpleTaskId2, TaskTypes.TASK, "Second SimpleTask updated",
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
        Task expectedSimpleTask = new Task(simpleTaskId2, TaskTypes.TASK, "Second SimpleTask", Status.IN_PROGRESS,
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
        Task expectedSimpleTask = new Task(0L, TaskTypes.TASK, "Second SimpleTask", Status.IN_PROGRESS,
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
        Task expectedSimpleTask = new Task(subTaskId4, TaskTypes.TASK, "Second SimpleTask", Status.IN_PROGRESS,
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
        Task expectedSimpleTask = new Task(null, TaskTypes.TASK, "Second SimpleTask", Status.IN_PROGRESS,
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
        Task expectedSimpleTask = new Task(simpleTaskId2, TaskTypes.TASK, "Second SimpleTask", Status.NEW,
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

    @Test
    void getSimpleTaskByIdOrNullSuccessfulReturnSimpleTask() {
        Task expectedSimpleTask = new Task(simpleTaskId2, TaskTypes.TASK, "Second SimpleTask", Status.NEW,
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
        SubTask expectedSubTask = new SubTask(subTaskId8, TaskTypes.SUBTASK, "First SubTask", Status.NEW,
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

    @Test
    void updateSubTaskSuccessfulUpdateSubTaskWithDateTime() {
        final Status initialEpicStatus = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getStatus();
        SubTask expectedSubTask = new SubTask(subTaskId4, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
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
        SubTask expectedSubTask = new SubTask(subTaskId4, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
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
        SubTask expectedSubTask = new SubTask(subTaskId4, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
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
        SubTask expectedSubTask = new SubTask(subTaskId4, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
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
        SubTask expectedSubTask = new SubTask(0L, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
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
        SubTask expectedSubTask = new SubTask(simpleTaskId2, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
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
        SubTask expectedSubTask = new SubTask(null, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
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
        SubTask expectedSubTask = new SubTask(subTaskId4, TaskTypes.SUBTASK, "First SubTask", Status.NEW,
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
        SubTask expectedSubTask = new SubTask(subTaskId4, TaskTypes.SUBTASK, "First SubTask", Status.NEW,
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
        taskManager.updateSubTask(new SubTask(subTaskId4, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
                "SubTask(ID=4) of first EpicTask(ID=3) with DateTime", testStartTime2Plus15m, duration2H, epicTaskId3));
        taskManager.updateSubTask(new SubTask(subTaskId5, TaskTypes.SUBTASK, "Second SubTask", Status.DONE,
                "SubTask(ID=5) of first EpicTask(ID=3) with DateTime", testStartTime3Minus15m, duration2H, epicTaskId3));
        final Long epicId = taskManager.getSubTaskByIdOrNull(subTaskId4).getEpicId();
        assertEquals(2, taskManager.getEpicTaskByIdOrNull(epicId).getSubTasksIds().size(),
                "Неверное количество задач.");
        final Status initialEpicStatus = taskManager.getEpicTaskByIdOrNull(epicId).getStatus();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        final LocalDateTime epicStartTime = taskManager.getEpicTaskByIdOrNull(epicId).getStartTime();
        final Duration epicDuration = taskManager.getEpicTaskByIdOrNull(epicId).getDuration();
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
                epicTaskId8, TaskTypes.EPIC, "First EpicTask", Status.NEW, "EpicTask(ID=7)");
        final EpicTask savedEpicTask = taskManager.getEpicTaskByIdOrNull(epicTaskId8);
        assertNotNull(savedEpicTask, "Задача не найдена.");
        assertEquals(epicTask, savedEpicTask, "Задачи не совпадают.");
        assertEquals(expectedEpicTask, savedEpicTask, "Задача не совпадает с введенной вручную.");
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertNotNull(testEpicTasks, "Задачи нe возвращаются.");
        assertEquals(1, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(expectedEpicTask, testEpicTasks.get(0), "Задачи не совпадают.");
        final LocalDateTime epicStartTime = taskManager.getEpicTaskByIdOrNull(epicTaskId8).getStartTime();
        final Duration epicDuration = taskManager.getEpicTaskByIdOrNull(epicTaskId8).getDuration();
        final LocalDateTime epicStart = taskManager.getEpicTaskByIdOrNull(epicTaskId8).getStartTime();
        assertNull(epicStart, "У Epic без подзадач есть время начала задачи.");
        assertNull(epicDuration, "У Epic без подзадач есть продолжительность.");
    }

    @Test
    void updateEpicTaskSuccessfulUpdateEpicTask() {
        SubTask expectedSubTask = new SubTask(subTaskId7, TaskTypes.SUBTASK, "First SubTask", Status.IN_PROGRESS,
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
        EpicTask expectedEpicTask = new EpicTask(epicTaskId6, TaskTypes.EPIC, "Second EpicTask", Status.NEW,
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
        EpicTask expectedEpicTask = new EpicTask(epicTaskId6, TaskTypes.EPIC, "Second EpicTask", Status.NEW,
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

    @Test
    void getEpicTaskByIdOrNullFailReturnNullInCauseOfWrongTaskTypes() {
        final EpicTask savedEpicTask = taskManager.getEpicTaskByIdOrNull(simpleTaskId2);
        assertNull(savedEpicTask, "Обращение по несуществующему ID возвращает задачу.");
    }

    @Test
    void getEpicTaskByIdOrNullFailReturnNullInCauseOfEmptyId() {
        final EpicTask savedEpicTask = taskManager.getEpicTaskByIdOrNull(null);
        assertNull(savedEpicTask, "Обращение по несуществующему ID возвращает задачу.");
    }

    @Test
    void deleteEpicTaskSuccessfullyRemovedEpicTaskAndSubTasksOfThisEpic() {
        taskManager.getEpicTaskByIdOrNull(epicTaskId3);
        assertNotNull(taskManager.getEpicTaskByIdOrNull(epicTaskId3), "Задача отсутствует.");
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteEpicTask(epicTaskId3);
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(1, testEpicTasks.size(), "Неверное количество задач.");
        assertNull(taskManager.getEpicTaskByIdOrNull(epicTaskId3), "Задача не удалена.");
        assertNotEquals(initialEpicTasks.size(), testEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(1, testSubTasks.size(), "Неверное количество задач.");
        assertNotEquals(initialSubTasks.size(), testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteEpicTaskFailInCauseOfWrongId() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteEpicTask(0L);
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks.size(), testEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteEpicTaskFailInCauseOfWrongTaskTypes() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteEpicTask(simpleTaskId2);
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks.size(), testEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteEpicTaskFailInCauseOfEmptyId() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteEpicTask(null);
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks.size(), testEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void getSubTasksOfEpicSuccessfulReturnSubTasksOfEpicList() {
        taskManager.deleteEpicTask(epicTaskId6);
        SubTask expectedSubTask = new SubTask(subTaskId4, TaskTypes.SUBTASK, "First SubTask", Status.NEW,
                "SubTask(ID=4) of first EpicTask(ID=3) with DateTime", testStartTime2, duration2H, epicTaskId3);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        final List<Long> epicSubTasks = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds();
        assertEquals(testSubTasks.size(), epicSubTasks.size(), "Неверное количество задач.");
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(2, testSubTasks.size(), "Неверное количество задач.");
        SubTask subTaskFromEpicList = taskManager.getSubTaskByIdOrNull(
                taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds().get(0));
        assertEquals(expectedSubTask, subTaskFromEpicList, "Задачи не совпадают.");
        assertEquals(expectedSubTask, testSubTasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void getSubTasksOfEpicFailReturnEmptyListInCauseOfEmptySubTasksMap() {
        taskManager.deleteAllSubTasks();
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertTrue(testSubTasks.isEmpty(), "Список не пуст.");
        final List<Long> epicSubTasks = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(0, testSubTasks.size(), "Неверное количество задач.");
        assertTrue(epicSubTasks.isEmpty(), "Список не пуст.");
    }

    @Test
    void getSubTasksOfEpicFailReturnEmptyListInCauseOfEmptySubTasksOfEpicList() {
        taskManager.deleteSubTask(subTaskId7);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertFalse(testSubTasks.isEmpty(), "Список пуст.");
        final List<Long> epicSubTasks = taskManager.getEpicTaskByIdOrNull(epicTaskId6).getSubTasksIds();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(0, epicSubTasks.size(), "Неверное количество задач.");
        assertTrue(epicSubTasks.isEmpty(), "Список не пуст.");
    }
}