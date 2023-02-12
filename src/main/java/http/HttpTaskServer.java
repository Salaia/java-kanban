package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import managers.Managers;
import managers.TaskManager;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.net.*;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

// будет слушать порт 8080 и принимать запросы
// !!! только он взаимодействует с пользователем !!!!

public class HttpTaskServer {
    public static final int PORT = 8080; // KVServer 8078 - должны отличаться
    private final HttpServer server;
    private Gson gson;

    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = Managers.getGson();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/tasks", this::handler);
    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefaultHttp());
    }

    public static void main(String[] args) throws IOException {
        final HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start();
    }

    private void handler(HttpExchange exchange) {
        try {
            final String path = exchange.getRequestURI().getPath().substring(7); // beginIndex 7
            switch (path) {
                case "": {
                    if (!exchange.getRequestMethod().equals("GET")) {
                        exchange.sendResponseHeaders(405, 0);
                    }
                    final String response = gson.toJson(taskManager.getPrioritizedTasks());
                    sendText(exchange, response);
                    break;
                }
                case "history": {
                    if (!exchange.getRequestMethod().equals("GET")) {
                        exchange.sendResponseHeaders(405, 0);
                    }
                    final String response = gson.toJson(taskManager.getHistory());
                    sendText(exchange, response);
                    break;
                }
                case "task": {
                    handleTask(exchange);
                    break;
                }
                case "subtask": {
                    handleSubtask(exchange);
                    break;
                }
                case "subtask/epic": {
                    if (!exchange.getRequestMethod().equals("GET")) {
                        exchange.sendResponseHeaders(405, 0);
                    }
                    final String query = exchange.getRequestURI().getQuery();
                    String idParam = query.substring(3); //?id=
                    final Long id = Long.parseLong(idParam);
                    final List<SubTask> subtasks = taskManager.getAllSubTasksOfEpicOrNull(id);
                    final String response = gson.toJson(subtasks);
                    sendText(exchange, response);
                    break;
                }
                case "epic":
                    handleEpic(exchange);
                    break;
                default: {
                    exchange.sendResponseHeaders(404, 0);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.close();
        }
        exchange.close();
    }

    private void handleTask(HttpExchange exchange) throws IOException {
        final String query = exchange.getRequestURI().getQuery();
        switch (exchange.getRequestMethod()) {
            case "GET": {
                if (query == null) {
                    final List<Task> tasks = taskManager.getSimpleTasks();
                    final String response = gson.toJson(tasks);
                    sendText(exchange, response);
                    return;
                }
                String idParam = query.substring(3);
                final Long id = Long.parseLong(idParam);
                final Task task = taskManager.getSimpleTaskByIdOrNull(id);
                final String response = gson.toJson(task);
                sendText(exchange, response);
                break;
            }
            case "DELETE": {
                if (query == null) {
                    taskManager.deleteAllSimpleTasks();
                    exchange.sendResponseHeaders(200, 0);
                    return;
                }
                String idParam = query.substring(3);
                final Long id = Long.parseLong(idParam);
                taskManager.deleteSimpleTask(id);
                exchange.sendResponseHeaders(200, 0);
                break;
            }
            case "POST": {
                String json = readText(exchange);
                if (json.isEmpty()) {
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                final Task task = gson.fromJson(json, Task.class);
                final Long id = task.getId();
                if (id != null) {
                    taskManager.updateSimpleTask(task);
                    final String response = gson.toJson(task);
                    sendText(exchange, response);
                } else {
                    Long taskId = taskManager.recordSimpleTask(task);
                    task.setId(taskId);
                    final String response = gson.toJson(task);
                    sendText(exchange, response);
                }
                break;
            }
        }
        exchange.close();
    } // handleTask(HttpExchange exchange)

    private void handleSubtask(HttpExchange exchange) throws IOException {
        final String query = exchange.getRequestURI().getQuery();
        switch (exchange.getRequestMethod()) {
            case "GET": {
                if (query == null) {
                    final List<SubTask> subTasks = taskManager.getSubTasks();
                    final String response = gson.toJson(subTasks);
                    sendText(exchange, response);
                    return;
                }
                String idParam = query.substring(3);
                final Long id = Long.parseLong(idParam);
                final SubTask subTask = taskManager.getSubTaskByIdOrNull(id);
                final String response = gson.toJson(subTask);
                sendText(exchange, response);
                break;
            }
            case "DELETE": {
                if (query == null) {
                    taskManager.deleteAllSubTasks();
                    exchange.sendResponseHeaders(200, 0);
                    return;
                }
                String idParam = query.substring(3);
                final Long id = Long.parseLong(idParam);
                taskManager.deleteSubTask(id);
                exchange.sendResponseHeaders(200, 0);
                break;
            }
            case "POST": {
                String json = readText(exchange);
                if (json.isEmpty()) {
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                final SubTask subTask = gson.fromJson(json, SubTask.class);
                final Long id = subTask.getId();
                if (id != null) {
                    taskManager.updateSubTask(subTask);
                    final String response = gson.toJson(subTask);
                    sendText(exchange, response);
                } else {
                    Long subId = taskManager.recordSubTask(subTask);
                    subTask.setId(subId);
                    final String response = gson.toJson(subTask);
                    sendText(exchange, response);
                }
                break;
            } // case "POST"
        } // switch
        exchange.close();
    } // handleSubtask(HttpExchange exchange)

    private void handleEpic(HttpExchange exchange) throws IOException {
        final String query = exchange.getRequestURI().getQuery();
        switch (exchange.getRequestMethod()) {
            case "GET": {
                if (query == null) {
                    final List<EpicTask> epicTasks = taskManager.getEpicTasks();
                    final String response = gson.toJson(epicTasks);
                    sendText(exchange, response);
                    return;
                }
                String idParam = query.substring(3);
                final Long id = Long.parseLong(idParam);
                final EpicTask epic = taskManager.getEpicTaskByIdOrNull(id);
                final String response = gson.toJson(epic);
                sendText(exchange, response);
                break;
            }
            case "DELETE": {
                if (query == null) {
                    taskManager.deleteAllEpicTasks();
                    exchange.sendResponseHeaders(200, 0);
                    return;
                }
                String idParam = query.substring(3);
                final Long id = Long.parseLong(idParam);
                taskManager.deleteEpicTask(id);
                exchange.sendResponseHeaders(200, 0);
                break;
            }
            case "POST": {
                String json = readText(exchange);
                if (json.isEmpty()) {
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
                final EpicTask epicTask = gson.fromJson(json, EpicTask.class);
                final Long id = epicTask.getId();
                if (id != null) {
                    taskManager.updateEpicTask(epicTask);
                    final String response = gson.toJson(epicTask);
                    sendText(exchange, response);
                } else {
                    Long epicId = taskManager.recordEpicTask(epicTask);
                    epicTask.setId(epicId);
                    final String response = gson.toJson(epicTask);
                    sendText(exchange, response);
                }
                break;
            } // case "POST"
        } // switch
        exchange.close();
    } // handleEpic(HttpExchange exchange)

    // KVServer method
    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        server.start();
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
    }

    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), UTF_8);
    }

    public void stop() {
        System.out.println("HttpServer stopped");
        server.stop(1);
    }

    public TaskManager getTaskManager() { // нужен для тестирования
        return taskManager;
    }
}