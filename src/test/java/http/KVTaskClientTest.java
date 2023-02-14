package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import managers.HttpTaskManager;
import managers.Managers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KVTaskClientTest {
    static KVServer kvServer;
    static KVTaskClient kvTaskClient;
    static HttpTaskManager taskManager;
    static Gson gson;
    static LocalDateTime testStartTime1;
    static LocalDateTime testStartTime1Plus15m;
    static Duration duration10H;
    static LocalDateTime testStartTime2;
    static LocalDateTime testStartTime2Plus15m;
    static Duration duration2H;
    static LocalDateTime testStartTime3;
    static LocalDateTime testStartTime3Minus15m;
    static Duration duration5H;
    static Long simpleTaskId1;
    static Long simpleTaskId2;
    static Long epicTaskId3;
    static Long subTaskId4;
    static Long subTaskId5;
    static Long epicTaskId6;
    static Long subTaskId7;

    static void createTask() {
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


    @BeforeAll
    static void init() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        gson = Managers.getGson();
        taskManager = Managers.getDefaultHttp();
        kvTaskClient = taskManager.getClient();
        createTask();
    }

    @AfterAll
    static void stopServers() {
        kvServer.stop();
    }

    @Test
    void dataOnServerShouldEqualDataInMemory() {
        // заполняем историю
        taskManager.getSimpleTaskByIdOrNull(simpleTaskId1);
        taskManager.getEpicTaskByIdOrNull(epicTaskId3);
        taskManager.getSubTaskByIdOrNull(subTaskId4);

        // GET /load/<ключ>?API_TOKEN=
        ArrayList<Task> tasks = gson.fromJson(kvTaskClient.load("tasks"),
                new TypeToken<ArrayList<Task>>() {
                }.getType());
        ArrayList<SubTask> subtasks = gson.fromJson(kvTaskClient.load("subtasks"),
                new TypeToken<ArrayList<SubTask>>() {
                }.getType());
        ArrayList<EpicTask> epics = gson.fromJson(kvTaskClient.load("epics"),
                new TypeToken<ArrayList<EpicTask>>() {
                }.getType());
        // История хранится по id, а сравнивать надо с листом тасок, так что тут на операцию больше
        ArrayList<Long> historyIds = gson.fromJson(kvTaskClient.load("history"),
                new TypeToken<ArrayList<Long>>() {
                }.getType());
        List<Task> historyTasks = new ArrayList<>();
        for (Long id : historyIds) {
            if (taskManager.findTask(id) != null) {
                historyTasks.add(taskManager.findTask(id)); // метод findTask пришлось сделать публичным
            }
        }

        assertEquals(tasks, taskManager.getSimpleTasks(), "Коллекции тасок не равны!");
        assertEquals(subtasks, taskManager.getSubTasks(), "Коллекции сабтасок не равны!");
        assertEquals(epics, taskManager.getEpicTasks(), "Коллекции эпиков не равны!");
        assertEquals(historyTasks, taskManager.getHistory(), "Истории не равны!");
    }

    @Test
    void dataOnServerShouldEqualDataInMemoryAfterUpdatingTasks() {
        // Сохраняем старое состояние сервера
        ArrayList<Task> tasksOld = gson.fromJson(kvTaskClient.load("tasks"),
                new TypeToken<ArrayList<Task>>() {
                }.getType());
        ArrayList<SubTask> subtasksOld = gson.fromJson(kvTaskClient.load("subtasks"),
                new TypeToken<ArrayList<SubTask>>() {
                }.getType());
        ArrayList<EpicTask> epicsOld = gson.fromJson(kvTaskClient.load("epics"),
                new TypeToken<ArrayList<EpicTask>>() {
                }.getType());
        ArrayList<Long> historyIdsOld = gson.fromJson(kvTaskClient.load("history"),
                new TypeToken<ArrayList<Long>>() {
                }.getType());
        List<Task> historyTasksOld = new ArrayList<>();
        for (Long id : historyIdsOld) {
            if (taskManager.findTask(id) != null) {
                historyTasksOld.add(taskManager.findTask(id));
            }
        }

        // Обновляем по таске каждого типа
        Task taskOld = taskManager.getSimpleTaskByIdOrNull(simpleTaskId1);
        Task taskForUpdate = new Task(simpleTaskId1, "New name(simpleTaskId1)",
                taskOld.getStatus(), "New description");
        taskManager.updateSimpleTask(taskForUpdate);
        SubTask subTaskOld = taskManager.getSubTaskByIdOrNull(subTaskId7);
        SubTask subTaskForUpdate = new SubTask(subTaskId7, "New name(subTaskId5)",
                subTaskOld.getStatus(), "New description", epicTaskId6);
        taskManager.updateSubTask(subTaskForUpdate);
        EpicTask epicTaskOld = taskManager.getEpicTaskByIdOrNull(epicTaskId6);
        EpicTask epicTaskForUpdate = new EpicTask(epicTaskId6, "New name (epicTaskId3)",
                epicTaskOld.getStatus(), "New description");
        taskManager.updateEpicTask(epicTaskForUpdate);

        // Достаем новое состояние с сервера
        ArrayList<Task> tasks = gson.fromJson(kvTaskClient.load("tasks"),
                new TypeToken<ArrayList<Task>>() {
                }.getType());
        ArrayList<SubTask> subtasks = gson.fromJson(kvTaskClient.load("subtasks"),
                new TypeToken<ArrayList<SubTask>>() {
                }.getType());
        ArrayList<EpicTask> epics = gson.fromJson(kvTaskClient.load("epics"),
                new TypeToken<ArrayList<EpicTask>>() {
                }.getType());
        // История хранится по id, а сравнивать надо с листом тасок, так что тут на операцию больше
        ArrayList<Long> historyIds = gson.fromJson(kvTaskClient.load("history"),
                new TypeToken<ArrayList<Long>>() {
                }.getType());
        List<Task> historyTasks = new ArrayList<>();
        for (Long id : historyIds) {
            if ((taskManager.findTask(id)) != null) {
                historyTasks.add(taskManager.findTask(id)); // метод findTask пришлось сделать публичным
            }
        }

        // Должно быть равно новому состоянию
        assertEquals(tasks, taskManager.getSimpleTasks(), "Коллекции тасок не равны!");
        assertEquals(subtasks, taskManager.getSubTasks(), "Коллекции сабтасок не равны!");
        assertEquals(epics, taskManager.getEpicTasks(), "Коллекции эпиков не равны!");
        assertEquals(historyTasks, taskManager.getHistory(), "Истории не равны!");

        // Не должно быть равно старому состоянию
        assertNotEquals(tasksOld, taskManager.getSimpleTasks(), "Хранятся старые таски!");
        assertNotEquals(subtasksOld, taskManager.getSubTasks(), "Хранятся старые сабтаски!");
        assertNotEquals(epicsOld, taskManager.getEpicTasks(), "Хранятся старые эпики!");
        assertNotEquals(historyTasksOld, taskManager.getHistory(), "Хранится старая история!");
    }

}