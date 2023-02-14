package managers;

import http.HttpTaskServer;
import http.KVServer;
import org.junit.jupiter.api.*;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    static KVServer kvServer;
    static HttpTaskServer httpTaskServer;

    @BeforeEach
        // Это жестоко по отношению к машине, но тесты требуют перезапуска.
        // Мне пришлось запускать блоками - мой ноут виснет где-то на 20-ом тесте
    void startServers() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();

        taskManager = Managers.getDefaultHttp();
        createTask();
    }

    @AfterEach
    void stopServers() {
        clearManager(); // Я починиль. Виноват был новый метод в InMemoryTaskManager - findTask(id)
        kvServer.stop();
        httpTaskServer.stop();
    }

    @Test
    void restoreManagerFromFilledServer() {
        // заполняем историю
        taskManager.getSimpleTaskByIdOrNull(simpleTaskId1);
        taskManager.getEpicTaskByIdOrNull(epicTaskId3);
        taskManager.getSubTaskByIdOrNull(subTaskId4);

        // какие-то таски удаляем
        taskManager.deleteSimpleTask(simpleTaskId2);
        taskManager.deleteSubTask(subTaskId5);
        taskManager.deleteEpicTask(epicTaskId6);

        //проверка на отсутствие повторов
        Set<Task> historySet = new HashSet<>(taskManager.getHistory());
        Assertions.assertEquals(historySet.size(), taskManager.getHistory().size());

        //Создаем второй HttpTaskManager и восстанавливаем его с сервера
        HttpTaskManager httpTaskManagerRestored = new HttpTaskManager(8078, true);

        // проверяем, что в обоих менеджерах таски лежат одинаково
        List<Task> simpleTaskList = taskManager.getSimpleTasks();
        List<Task> simpleTaskListRestored = httpTaskManagerRestored.getSimpleTasks();
        Assertions.assertEquals(simpleTaskList, simpleTaskListRestored);

        List<EpicTask> epicTaskList = taskManager.getEpicTasks();
        List<EpicTask> epicTaskListRestored = httpTaskManagerRestored.getEpicTasks();
        Assertions.assertEquals(epicTaskList, epicTaskListRestored);

        List<SubTask> subTaskList = taskManager.getSubTasks();
        List<SubTask> subTaskListRestored = httpTaskManagerRestored.getSubTasks();
        Assertions.assertEquals(subTaskList, subTaskListRestored);

        // проверяем историю
        List<Task> history = taskManager.getHistory();
        List<Task> historyRestored = httpTaskManagerRestored.getHistory();
        Assertions.assertEquals(history, historyRestored);

        // проверяем приоритеты
        List<Task> priorityList = taskManager.getPrioritizedTasks();
        List<Task> priorityListRestored = httpTaskManagerRestored.getPrioritizedTasks();
        Assertions.assertEquals(priorityList, priorityListRestored);
    }

    @Test
    public void restoreManagerFromEmptyServer() {
        clearManager();
        //Создаем первый HttpTaskManager - он должен быть пустой
        HttpTaskManager httpTaskManager = new HttpTaskManager(8078, false);
        //Создаем второй HttpTaskManager и восстанавливаем его с сервера (пустого)
        HttpTaskManager httpTaskManagerRestored = new HttpTaskManager(8078, true);

        // проверяем, что в обоих менеджерах таски лежат одинаково
        List<Task> simpleTaskList = httpTaskManager.getSimpleTasks();
        List<Task> simpleTaskListRestored = httpTaskManagerRestored.getSimpleTasks();
        Assertions.assertEquals(simpleTaskList, simpleTaskListRestored);

        List<Task> epicTaskList = httpTaskManager.getSimpleTasks();
        List<Task> epicTaskListRestored = httpTaskManagerRestored.getSimpleTasks();
        Assertions.assertEquals(epicTaskList, epicTaskListRestored);

        List<Task> subTaskList = httpTaskManager.getSimpleTasks();
        List<Task> subTaskListRestored = httpTaskManagerRestored.getSimpleTasks();
        Assertions.assertEquals(subTaskList, subTaskListRestored);

        // проверяем историю
        List<Task> history = httpTaskManager.getHistory();
        List<Task> historyRestored = httpTaskManagerRestored.getHistory();
        Assertions.assertEquals(0, history.size());
        Assertions.assertEquals(0, historyRestored.size());
    }
}