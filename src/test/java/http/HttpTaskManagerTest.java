package http;

import managers.HttpTaskManager;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

public class HttpTaskManagerTest {
    protected HttpTaskManager taskManager;
    protected static KVServer kvServer;
    protected static HttpTaskServer httpTaskServer;
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
}
