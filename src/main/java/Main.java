import com.google.gson.Gson;
import com.google.gson.internal.bind.util.ISO8601Utils;
import http.HttpTaskServer;
import http.KVServer;
import managers.HttpTaskManager;
import managers.Managers;
import managers.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

public class Main {

    static TaskManager taskManager;
    static HttpClient client;
    static Gson gson;

/*
    Тут лежит предварительное тестирование из ТЗ, то есть код с платформы, пусть лежит как изначальный образец
 */

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello");
        new KVServer().start();
        new HttpTaskServer().start();

        taskManager = new HttpTaskManager(8080);
        client = HttpClient.newHttpClient();
        gson = Managers.getGson();

        // долго думала, куда запихнуть подсказки из ТЗ, место нашла только в предварительном тестировании
        createSimpleTaskTest();
        getSimpleTaskByIdTest();
        getAllTasksTest();

    }

    public static void getAllTasksTest() throws IOException, InterruptedException {
        // Подсказка: как получить все задачи
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void createSimpleTaskTest() throws IOException, InterruptedException {
        // Подсказка: как создать задачу
        URI url = URI.create("http://localhost:8080/tasks/task");
        Task newTask = new Task("Second SimpleTask", "SimpleTask(ID=2) with DateTime", LocalDateTime.of(2023, Month.MARCH, 5, 14, 0, 0), Duration.ofMinutes(15));
        String json = gson.toJson(newTask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void getSimpleTaskByIdTest() throws IOException, InterruptedException {
        // Подсказка: как получить задачу с id = 1
        URI url = URI.create("http://localhost:8080/tasks/task?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}