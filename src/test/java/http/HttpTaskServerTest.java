package http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import managers.HttpTaskManager;
import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.*;
import tasks.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
Здесь только инициализация, сами тесты перенесла в два блока-наследника, иначе у моего компьютера памяти не хватает
 */

class HttpTaskServerTest {
    static KVServer kvServer;
    static HttpTaskServer httpTaskServer;
    static TaskManager taskManager;
    static Gson gson;
    static HttpClient client;
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

}