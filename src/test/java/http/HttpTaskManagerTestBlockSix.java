package http;

import managers.HttpTaskManager;
import managers.Managers;
import org.junit.jupiter.api.*;
import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskManagerTestBlockSix extends HttpTaskManagerTest {

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
    void init() {
        taskManager = Managers.getDefaultHttp();
        clearManager();
        createTask();
    }

    @AfterEach
    void clear() {
        clearManager();
    }

    @Test
    void getEpicTaskByIdOrNullFailReturnNullInCauseOfWrongTaskTypes() {
        final EpicTask savedEpicTask = taskManager.getEpicTaskByIdOrNull(simpleTaskId2);
        assertNull(savedEpicTask, "Обращение по несуществующему ID возвращает задачу.");
    }

    @Test
    void getEpicTaskByIdOrNullFailReturnNullInCauseOfEmptyId() {
        final EpicTask savedEpicTask = taskManager.getEpicTaskByIdOrNull(null);
        assertNull(savedEpicTask, "Обращение по несуществующему ID возвращает задачу.");
    }

    @Test
    void deleteEpicTaskSuccessfullyRemovedEpicTaskAndSubTasksOfThisEpic() {
        taskManager.getEpicTaskByIdOrNull(epicTaskId3);
        assertNotNull(taskManager.getEpicTaskByIdOrNull(epicTaskId3), "Задача отсутствует.");
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteEpicTask(epicTaskId3);
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(1, testEpicTasks.size(), "Неверное количество задач.");
        assertNull(taskManager.getEpicTaskByIdOrNull(epicTaskId3), "Задача не удалена.");
        assertNotEquals(initialEpicTasks.size(), testEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(1, testSubTasks.size(), "Неверное количество задач.");
        assertNotEquals(initialSubTasks.size(), testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteEpicTaskFailInCauseOfWrongId() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteEpicTask(0L);
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks.size(), testEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteEpicTaskFailInCauseOfWrongTaskTypes() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteEpicTask(simpleTaskId2);
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks.size(), testEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void deleteEpicTaskFailInCauseOfEmptyId() {
        final List<EpicTask> initialEpicTasks = taskManager.getEpicTasks();
        final List<SubTask> initialSubTasks = taskManager.getSubTasks();
        taskManager.deleteEpicTask(null);
        final List<EpicTask> testEpicTasks = taskManager.getEpicTasks();
        assertEquals(2, testEpicTasks.size(), "Неверное количество задач.");
        assertEquals(initialEpicTasks.size(), testEpicTasks.size(), "Неверное количество задач.");
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertEquals(3, testSubTasks.size(), "Неверное количество задач.");
        assertEquals(initialSubTasks.size(), testSubTasks.size(), "Неверное количество задач.");
    }

    @Test
    void getSubTasksOfEpicSuccessfulReturnSubTasksOfEpicList() {
        taskManager.deleteEpicTask(epicTaskId6);
        SubTask expectedSubTask = new SubTask(subTaskId4, "First SubTask", Status.NEW,
                "SubTask(ID=4) of first EpicTask(ID=3) with DateTime", testStartTime2, duration2H, epicTaskId3);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        final List<Long> epicSubTasks = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds();
        assertEquals(testSubTasks.size(), epicSubTasks.size(), "Неверное количество задач.");
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(2, testSubTasks.size(), "Неверное количество задач.");
        SubTask subTaskFromEpicList = taskManager.getSubTaskByIdOrNull(
                taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds().get(0));
        assertEquals(expectedSubTask, subTaskFromEpicList, "Задачи не совпадают.");
        assertEquals(expectedSubTask, testSubTasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void getSubTasksOfEpicFailReturnEmptyListInCauseOfEmptySubTasksMap() {
        taskManager.deleteAllSubTasks();
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertTrue(testSubTasks.isEmpty(), "Список не пуст.");
        final List<Long> epicSubTasks = taskManager.getEpicTaskByIdOrNull(epicTaskId3).getSubTasksIds();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(0, testSubTasks.size(), "Неверное количество задач.");
        assertTrue(epicSubTasks.isEmpty(), "Список не пуст.");
    }

    @Test
    void getSubTasksOfEpicFailReturnEmptyListInCauseOfEmptySubTasksOfEpicList() {
        taskManager.deleteSubTask(subTaskId7);
        final List<SubTask> testSubTasks = taskManager.getSubTasks();
        assertFalse(testSubTasks.isEmpty(), "Список пуст.");
        final List<Long> epicSubTasks = taskManager.getEpicTaskByIdOrNull(epicTaskId6).getSubTasksIds();
        assertNotNull(testSubTasks, "Задачи нe возвращаются.");
        assertEquals(0, epicSubTasks.size(), "Неверное количество задач.");
        assertTrue(epicSubTasks.isEmpty(), "Список не пуст.");
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
