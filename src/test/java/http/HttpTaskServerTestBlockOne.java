package http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import managers.HttpTaskManager;
import managers.Managers;
import org.junit.jupiter.api.*;
import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

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

public class HttpTaskServerTestBlockOne extends HttpTaskServerTest{
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
    void start() {
        gson = Managers.getGson();
        taskManager = httpTaskServer.getTaskManager();
        client = HttpClient.newHttpClient();
        taskManager.deleteAllSimpleTasks();
        taskManager.deleteAllEpicTasks();
        createTask();
    }

    @AfterEach
    void clearManager() {
        taskManager.deleteAllSimpleTasks();
        taskManager.deleteAllEpicTasks();
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
}
