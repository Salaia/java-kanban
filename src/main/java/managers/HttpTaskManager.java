package managers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import http.KVTaskClient;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;
import tasks.TaskTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
    принимает от HttpTaskServer вызов методов,
    Там, где FileBackedTaskManager(родитель) сохранял в файл, этот будет сохранять на сервер
    по реализации все будет проходить через KVTaskClient
 */

public class HttpTaskManager extends FileBackedTaskManager {
    private final Gson gson;
    private final KVTaskClient client;

    public HttpTaskManager(int port) {
        this(port, false);
    }

    public HttpTaskManager(int port, boolean load) {
        super(null);
        gson = Managers.getGson();
        client = new KVTaskClient("http://localhost:" + port + "/");
        if (load) { // восстановить ли состояние с сервера?
            load();
        }
    }

    // метод получает список тасок / сабтасок / эпиков
    // и раскладывает их по соответствующим мапам родителя
    // раскладывание по приоритетам я вызвала из FileBackedTaskManager
    protected void addTasks(List<? extends Task> tasks) {
        for (Task task : tasks) {
            final Long id = task.getId();
            if (id > countId) {
                countId = id;
            }
            TaskTypes type = task.getTaskType();
            if (type == TaskTypes.TASK) {
                this.simpleTasks.put(id, task);
            } else if (type == TaskTypes.SUBTASK) {
                subTasks.put(id, (SubTask) task);
            } else if (type == TaskTypes.EPIC) {
                epicTasks.put(id, (EpicTask) task);
            }
        }
    } // addTasks

    private void load() { // восстановление состояния с сервера
        ArrayList<Task> tasks = gson.fromJson(client.load("tasks"),
                new TypeToken<ArrayList<Task>>() {
                }.getType());
        addTasks(tasks);

        ArrayList<EpicTask> epics = gson.fromJson(client.load("epics"),
                new TypeToken<ArrayList<EpicTask>>() {
                }.getType());
        addTasks(epics);

        ArrayList<SubTask> subtasks = gson.fromJson(client.load("subtasks"),
                new TypeToken<ArrayList<SubTask>>() {
                }.getType());
        addTasks(subtasks);

        List<Long> history = gson.fromJson(client.load("history"),
                new TypeToken<ArrayList<Long>>() {
                }.getType());
        for (Long taskId : history) {
            getHistoryManager().add(findTask(taskId)); // напишу этот метод в InMemoryTaskManager, вдруг еще где пригодится
        }
        restorePriority();
    } // load()

    @Override
    protected void save() {     // а не старый save() из FileBacked...
        String jsonTasks = gson.toJson(new ArrayList<>(simpleTasks.values()));
        client.put("tasks", jsonTasks);

        String jsonSubtasks = gson.toJson(new ArrayList<>(subTasks.values()));
        client.put("subtasks", jsonSubtasks);

        String jsonEpicTasks = gson.toJson(new ArrayList<>(epicTasks.values()));
        client.put("epics", jsonEpicTasks);

        String jsonHistory = gson.toJson(getHistoryManager().getHistory()
                .stream().map(Task::getId).collect(Collectors.toList()));
        client.put("history", jsonHistory);
    } // save()

    public KVTaskClient getClient() { // нужен для тестов
        return client;
    }
}
