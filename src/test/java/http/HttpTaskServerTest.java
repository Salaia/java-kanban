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
Не тестирует КВ-клиент и КВ-сервер.
Задача тестов - определить, что после получения эндпойнта был вызван верный метод ТаскМенеджера и в нем корректно поменялось состояние.
Состояние сервера проверяет другой блок тестов.
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

    @BeforeEach
    void start() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
        gson = Managers.getGson();
        taskManager = httpTaskServer.getTaskManager();
        client = HttpClient.newHttpClient();
        createTask();
    }

    @AfterEach
    void stopServers() {
        taskManager.deleteAllSimpleTasks();
        taskManager.deleteAllEpicTasks();
        kvServer.stop();
        httpTaskServer.stop();
    }

    @Test
    public void loadFromHttpServer() {
        taskManager.getSimpleTaskByIdOrNull(simpleTaskId1);
        taskManager.getSubTaskByIdOrNull(subTaskId4);
        taskManager.getEpicTaskByIdOrNull(epicTaskId6);
        final List<Task> initialTasks = taskManager.getSimpleTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<Task> initialHistory = taskManager.getHistory();
        HttpTaskManager restoredTaskManager = new HttpTaskManager(KVServer.PORT, true);

        final List<Task> tasks = restoredTaskManager.getSimpleTasks();
        assertNotNull(tasks, "Возвращает не пустой список задач");
        assertEquals(initialTasks.size(), tasks.size(), "Размер списков SimpleTasks не совпадает");

        final List<SubTask> subtasks = restoredTaskManager.getSubTasks();
        assertNotNull(subtasks, "Возвращает не пустой список подзадач");
        assertEquals(initialSubTasks.size(), subtasks.size(), "Размер списков SubTasks не совпадает");

        final List<EpicTask> epics = restoredTaskManager.getEpicTasks();
        assertNotNull(epics, "Возвращает не пустой список эпиков");
        assertEquals(initialEpicTasks.size(), epics.size(), "Размер списков EpicTasks не совпадает");

        final List<Task> history = restoredTaskManager.getHistory();
        assertNotNull(history, "Возвращает не пустой список истории");
        assertEquals(initialHistory.size(), history.size(), "Размер списков истории не совпадает");
    }

    @Test
    void postTasksTaskSuccessPostOfNewSimpleTask() throws InterruptedException, IOException {
        taskManager.deleteAllSimpleTasks();
        Task newTask = new Task("Test SimpleTask", "SimpleTask(ID=8) with DateTime for postTest", LocalDateTime.of(2023, Month.MARCH, 6, 20, 0, 0), Duration.ofMinutes(15));
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Long id;
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            if (!jsonElementBody.isJsonObject()) {
                assertTrue(jsonElementBody.isJsonObject(), "Ответ от сервера не содержит объект-JSON.");
                return;
            }
            JsonObject jsonObjectBody = jsonElementBody.getAsJsonObject();
            id = jsonObjectBody.get("id").getAsLong();
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
            return;
        }
        Task savedTask = taskManager.getSimpleTaskByIdOrNull(id);
        newTask.setId(id);
        newTask.setStatus(savedTask.getStatus());
        assertEquals(savedTask, newTask, "Задачи не совпадают.");
    }


    @Test
    void postTasksTaskIdSuccessPostOfUpdatedSimpleTaskID2() throws InterruptedException, IOException {
        final Task expectedTask = new Task(simpleTaskId2, "Second SimpleTask",
                Status.IN_PROGRESS, "SimpleTask(Id=2) with DateTime", testStartTime1Plus15m, duration10H);
        String json = gson.toJson(expectedTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        URI url = URI.create("http://localhost:8080/tasks/task?id=" + simpleTaskId2);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            final Task savedTask = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
            assertEquals(expectedTask, savedTask, "Задачи не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void getTasksTaskSuccessGetListOfSimpleTasks() throws InterruptedException, IOException {
        final List<Task> expectedTasks = taskManager.getSimpleTasks();
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            final List<Task> tasksFromResponse = gson.fromJson(jsonElementBody,
                    new TypeToken<List<Task>>() {
                    }.getType());
            assertNotNull(tasksFromResponse, "Задачи нe возвращаются.");
            assertEquals(expectedTasks, tasksFromResponse, "Списки не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void getTasksTaskIdSuccessGetSimpleTasksId2() throws InterruptedException, IOException {
        final Task expectedTask = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
        URI url = URI.create("http://localhost:8080/tasks/task?id=" + simpleTaskId2);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            if (!jsonElementBody.isJsonObject()) {
                assertTrue(jsonElementBody.isJsonObject(), "Ответ от сервера не содержит объект-JSON.");
                return;
            }
            JsonObject jsonObjectBody = jsonElementBody.getAsJsonObject();
            final Task taskFromResponse = new Gson().fromJson(jsonObjectBody, Task.class);
            assertEquals(expectedTask, taskFromResponse, "Задачи не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void deleteTasksTaskSuccessfulDeleteAllSimpleTasks() throws InterruptedException, IOException {
        final List<Task> listContainsTasks = taskManager.getSimpleTasks();
        assertNotNull(listContainsTasks, "Задачи изначально отсутствуют.");
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            final List<Task> emptyList = taskManager.getSimpleTasks();
            assertTrue(emptyList.isEmpty(), "Задачи нe удалены.");
            assertNotEquals(listContainsTasks, emptyList, "Списки не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void deleteTasksTaskIdSuccessfulRemoveSimpleTaskId2() throws InterruptedException, IOException {
        final Task taskForDelete = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
        assertNotNull(taskForDelete, "Задача отсутствует изначально.");
        URI url = URI.create("http://localhost:8080/tasks/task?id=" + simpleTaskId2);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            final Task deletedTask = taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
            assertNull(deletedTask, "Задача не удалена.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void postTasksSubtaskSuccessPostOfNewSubTask() throws InterruptedException, IOException {
        SubTask newTask = new SubTask("Test SubTask", "SubTask(Id=8) of first EpicTask(Id=3) with DateTime for postTest",
                LocalDateTime.of(2023, Month.MARCH, 6, 20, 0, 0), Duration.ofMinutes(15), epicTaskId3);
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Long id;
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            if (!jsonElementBody.isJsonObject()) {
                assertTrue(jsonElementBody.isJsonObject(), "Ответ от сервера не содержит объект-JSON.");
                return;
            }
            JsonObject jsonObjectBody = jsonElementBody.getAsJsonObject();
            id = jsonObjectBody.get("id").getAsLong();
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
            return;
        }
        final Task savedTask = taskManager.getSubTaskByIdOrNull(id);
        newTask.setId(id);
        newTask.setStatus(savedTask.getStatus());
        assertEquals(savedTask, newTask, "Задачи не совпадают.");
    }

    @Test
    void postTasksSubTaskIdSuccessPostOfUpdatedSubTaskID4() throws InterruptedException, IOException {
        final SubTask expectedTask = new SubTask(subTaskId4, "First SubTask", Status.IN_PROGRESS,
                "SubTask(Id=4) of first EpicTask(Id=3) with DateTime", testStartTime2Plus15m, duration2H, epicTaskId3);
        String json = gson.toJson(expectedTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        URI url = URI.create("http://localhost:8080/tasks/subtask?id=" + subTaskId4);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            final Task savedTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
            assertEquals(expectedTask, savedTask, "Задачи не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void getTasksSubTaskSuccessGetListOfSubTasks() throws InterruptedException, IOException {
        final List<SubTask> expectedTasks = taskManager.getSubTasks();
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            final List<Task> tasksFromResponse = gson.fromJson(jsonElementBody,
                    new TypeToken<List<SubTask>>() {
                    }.getType());
            assertNotNull(tasksFromResponse, "Задачи нe возвращаются.");
            assertEquals(expectedTasks, tasksFromResponse, "Списки не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void getTasksSubTaskIdSuccessGetSubTasksId4() throws InterruptedException, IOException {
        final SubTask expectedTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
        URI url = URI.create("http://localhost:8080/tasks/subtask?id=" + subTaskId4);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            if (!jsonElementBody.isJsonObject()) {
                assertTrue(jsonElementBody.isJsonObject(), "Ответ от сервера не содержит объект-JSON.");
                return;
            }
            JsonObject jsonObjectBody = jsonElementBody.getAsJsonObject();
            final SubTask taskFromResponse = new Gson().fromJson(jsonObjectBody, SubTask.class);
            assertEquals(expectedTask, taskFromResponse, "Задачи не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void deleteTasksSubTaskSuccessfulRemoveAllSSubTasks() throws InterruptedException, IOException {
        final List<SubTask> listContainsTasks = taskManager.getSubTasks();
        assertNotNull(listContainsTasks, "Задачи изначально отсутствуют.");
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            final List<SubTask> emptyList = taskManager.getSubTasks();
            assertTrue(emptyList.isEmpty(), "Задачи нe удалены.");
            assertNotEquals(listContainsTasks, emptyList, "Списки не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void deleteTasksSubTaskIdSuccessfulRemoveSubTaskId4() throws InterruptedException, IOException {
        final SubTask taskForDelete = taskManager.getSubTaskByIdOrNull(subTaskId4);
        assertNotNull(taskForDelete, "Задача отсутствует изначально.");
        URI url = URI.create("http://localhost:8080/tasks/subtask?id=" + subTaskId4);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            final SubTask deletedTask = taskManager.getSubTaskByIdOrNull(subTaskId4);
            assertNull(deletedTask, "Задача не удалена.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void postTasksEpicSuccessPostOfNewEpicTask() throws InterruptedException, IOException {
        EpicTask newTask = new EpicTask("Test EpicTask", "EpicTask(Id=8)");
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);

        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Long id;
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            if (!jsonElementBody.isJsonObject()) {
                assertTrue(jsonElementBody.isJsonObject(), "Ответ от сервера не содержит объект-JSON.");
                return;
            }
            JsonObject jsonObjectBody = jsonElementBody.getAsJsonObject();
            id = jsonObjectBody.get("id").getAsLong();
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
            return;
        }
        final Task savedTask = taskManager.getEpicTaskByIdOrNull(id);
        newTask.setId(id);
        newTask.setStatus(savedTask.getStatus());
        assertEquals(savedTask, newTask, "Задачи не совпадают.");
    }

    @Test
    void postTasksEpicIdSuccessPostOfUpdatedEpicTaskID6() throws InterruptedException, IOException {
        EpicTask expectedTask = new EpicTask(epicTaskId6, "Updated Second EpicTask", "EpicTask(Id=6 without DateTime)");
        String json = gson.toJson(expectedTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        URI url = URI.create("http://localhost:8080/tasks/epic?id=" + epicTaskId6);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            final EpicTask savedTask = taskManager.getEpicTaskByIdOrNull(epicTaskId6);
            expectedTask.setStatus(savedTask.getStatus());
            assertEquals(expectedTask, savedTask, "Задачи не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void getTasksEpicSuccessGetListOfEpicTasks() throws InterruptedException, IOException {
        final List<EpicTask> expectedTasks = taskManager.getEpicTasks();
        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            final List<Task> tasksFromResponse = gson.fromJson(jsonElementBody,
                    new TypeToken<List<EpicTask>>() {
                    }.getType());
            assertNotNull(tasksFromResponse, "Задачи нe возвращаются.");
            assertEquals(expectedTasks, tasksFromResponse, "Списки не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void getTasksEpicIdSuccessGetEpicTasksId3() throws InterruptedException, IOException {
        final EpicTask expectedTask = taskManager.getEpicTaskByIdOrNull(epicTaskId3);
        URI url = URI.create("http://localhost:8080/tasks/epic?id=" + epicTaskId3);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            if (!jsonElementBody.isJsonObject()) {
                assertTrue(jsonElementBody.isJsonObject(), "Ответ от сервера не содержит объект-JSON.");
                return;
            }
            JsonObject jsonObjectBody = jsonElementBody.getAsJsonObject();
            final EpicTask taskFromResponse = new Gson().fromJson(jsonObjectBody, EpicTask.class);
            assertEquals(expectedTask, taskFromResponse, "Задачи не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void deleteTasksEpicIdSuccessfulRemoveEpicTaskId3() throws InterruptedException, IOException {
        final EpicTask taskForDelete = taskManager.getEpicTaskByIdOrNull(epicTaskId3);
        assertNotNull(taskForDelete, "Задача отсутствует изначально.");
        URI url = URI.create("http://localhost:8080/tasks/epic?id=" + epicTaskId3);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            final EpicTask deletedTask = taskManager.getEpicTaskByIdOrNull(epicTaskId3);
            assertNull(deletedTask, "Задача не удалена.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void getTasksSubTaskEpicIdSuccessGetAllSubTasksOfEpicOrNullId3() throws InterruptedException, IOException {
        final List<SubTask> expectedTasks = taskManager.getAllSubTasksOfEpicOrNull(epicTaskId3);
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic?id=" + epicTaskId3);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            final List<Task> tasksFromResponse = gson.fromJson(jsonElementBody,
                    new TypeToken<List<SubTask>>() {
                    }.getType());
            assertNotNull(tasksFromResponse, "Задачи нe возвращаются.");
            assertEquals(expectedTasks, tasksFromResponse, "Списки не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

    @Test
    void getTasksHistorySuccessGetListOfHistory() throws InterruptedException, IOException {
        taskManager.getSimpleTaskByIdOrNull(simpleTaskId1);
        taskManager.getSimpleTaskByIdOrNull(simpleTaskId2);
        final List<Task> expectedHistory = taskManager.getHistory();
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonElement jsonElementBody = JsonParser.parseString(response.body());
            final List<Task> tasksFromResponse = gson.fromJson(jsonElementBody,
                    new TypeToken<List<Task>>() {
                    }.getType());
            assertNotNull(tasksFromResponse, "Задачи нe возвращаются.");
            assertEquals(expectedHistory, tasksFromResponse, "Списки не совпадают.");
        } else {
            assertEquals(200, response.statusCode(), "Код ответа от сервера не соответствует ожидаемому.");
        }
    }

}