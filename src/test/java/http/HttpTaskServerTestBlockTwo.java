package http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTestBlockTwo extends HttpTaskServerTest {
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
