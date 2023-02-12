package managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @BeforeEach
    public void setUp() {
        taskManager = new FileBackedTaskManager(
                new File("src/main/java/storage/TaskManagerSaved.csv"));
        createTask();
    }

    @Test
    void restoreFileBackedTaskManagerFromFilledCSV() {
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

        //Создаем второй fileBackedTaskManager и восстанавливаем его из файла
        FileBackedTaskManager fileBackedTaskManagerRestored = FileBackedTaskManager.loadFromFile(
                new File("src/main/java/storage/TaskManagerSaved.csv"));

        // проверяем, что в обоих менеджерах таски лежат одинаково
        List<Task> simpleTaskList = taskManager.getSimpleTasks();
        List<Task> simpleTaskListRestored = fileBackedTaskManagerRestored.getSimpleTasks();
        Assertions.assertEquals(simpleTaskList, simpleTaskListRestored);

        List<EpicTask> epicTaskList = taskManager.getEpicTasks();
        List<EpicTask> epicTaskListRestored = fileBackedTaskManagerRestored.getEpicTasks();
        Assertions.assertEquals(epicTaskList, epicTaskListRestored);

        List<SubTask> subTaskList = taskManager.getSubTasks();
        List<SubTask> subTaskListRestored = fileBackedTaskManagerRestored.getSubTasks();
        Assertions.assertEquals(subTaskList, subTaskListRestored);

        // проверяем историю
        List<Task> history = taskManager.getHistory();
        List<Task> historyRestored = fileBackedTaskManagerRestored.getHistory();
        Assertions.assertEquals(history, historyRestored);

        // проверяем приоритеты
        List<Task> priorityList = taskManager.getPrioritizedTasks();
        List<Task> priorityListRestored = fileBackedTaskManagerRestored.getPrioritizedTasks();
        Assertions.assertEquals(priorityList, priorityListRestored);
    }

    @Test
    public void emptyCSVTesting() {
        TaskManager fileBackedTaskManager = new FileBackedTaskManager(
                new File("src/main/java/storage/TaskManagerSaved.csv"));
        try (BufferedWriter bf = Files.newBufferedWriter(Path.of("src/main/java/storage/TaskManagerSaved.csv"),
                StandardOpenOption.TRUNCATE_EXISTING)) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Создаем второй fileBackedTaskManager и восстанавливаем его из файла
        FileBackedTaskManager fileBackedTaskManagerRestored = FileBackedTaskManager.loadFromFile(
                new File("src/main/java/storage/TaskManagerSaved.csv"));

        // проверяем, что в обоих менеджерах таски лежат одинаково
        List<Task> simpleTaskList = fileBackedTaskManager.getSimpleTasks();
        List<Task> simpleTaskListRestored = fileBackedTaskManagerRestored.getSimpleTasks();
        Assertions.assertEquals(simpleTaskList, simpleTaskListRestored);

        List<EpicTask> epicTaskList = fileBackedTaskManager.getEpicTasks();
        List<EpicTask> epicTaskListRestored = fileBackedTaskManagerRestored.getEpicTasks();
        Assertions.assertEquals(epicTaskList, epicTaskListRestored);

        List<SubTask> subTaskList = fileBackedTaskManager.getSubTasks();
        List<SubTask> subTaskListRestored = fileBackedTaskManagerRestored.getSubTasks();
        Assertions.assertEquals(subTaskList, subTaskListRestored);

        // проверяем историю
        List<Task> history = fileBackedTaskManager.getHistory();
        List<Task> historyRestored = fileBackedTaskManagerRestored.getHistory();
        Assertions.assertEquals(0, history.size());
        Assertions.assertEquals(0, historyRestored.size());
    }
}
