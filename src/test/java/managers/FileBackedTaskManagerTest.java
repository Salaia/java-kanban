package managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        ;
        initTasks();
    }

    @Test
    void normalFileBackedTesting() {

        // какие-то таски удаляем
        taskManager.deleteSimpleTask(task1ID);
        taskManager.deleteSubTask(subTask1InEpic2);
        taskManager.deleteEpicTask(epic1ID);

        //проверка на отсутствие повторов
        Set<Task> historySet = new HashSet<>(taskManager.getHistory());
        Assertions.assertEquals(historySet.size(), taskManager.getHistory().size());

        //Создаем второй fileBackedTaskManager и восстанавливаем его из файла
        FileBackedTaskManager fileBackedTaskManagerRestored = FileBackedTaskManager.loadFromFile(
                new File("src/main/java/storage/TaskManagerSaved.csv"));

        // проверяем, что в обоих менеджерах таски лежат одинаково
        List<Task> simpleTaskList = taskManager.getSimpleTasks();
        List<Task> simpleTaskListRestored = fileBackedTaskManagerRestored.getSimpleTasks();
        for (int i = 0; i < simpleTaskList.size(); i++) {
            Assertions.assertEquals(simpleTaskList.get(i), simpleTaskListRestored.get(i));
        }

        List<Task> epicTaskList = taskManager.getSimpleTasks();
        List<Task> epicTaskListRestored = fileBackedTaskManagerRestored.getSimpleTasks();
        for (int i = 0; i < epicTaskList.size(); i++) {
            Assertions.assertEquals(epicTaskList.get(i), epicTaskListRestored.get(i));
        }

        List<Task> subTaskList = taskManager.getSimpleTasks();
        List<Task> subTaskListRestored = fileBackedTaskManagerRestored.getSimpleTasks();
        for (int i = 0; i < subTaskList.size(); i++) {
            Assertions.assertEquals(subTaskList.get(i), subTaskListRestored.get(i));
        }

        // проверяем историю
        List<Task> history = taskManager.getHistory();
        List<Task> historyRestored = fileBackedTaskManagerRestored.getHistory();
        for (int i = 0; i < history.size(); i++) {
            Assertions.assertEquals(history.get(i), historyRestored.get(i));
        }

        // проверяем приоритеты
        List<Task> priorityList = taskManager.getPrioritizedTasks();
        List<Task> priorityListRestored = fileBackedTaskManagerRestored.getPrioritizedTasks();
        for (int i = 0; i < priorityListRestored.size(); i++) {
            Assertions.assertEquals(priorityList.get(i), priorityListRestored.get(i));
        }
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
        for (int i = 0; i < simpleTaskList.size(); i++) {
            Assertions.assertEquals(simpleTaskList.get(i), simpleTaskListRestored.get(i));
        }

        List<Task> epicTaskList = fileBackedTaskManager.getSimpleTasks();
        List<Task> epicTaskListRestored = fileBackedTaskManagerRestored.getSimpleTasks();
        for (int i = 0; i < epicTaskList.size(); i++) {
            Assertions.assertEquals(epicTaskList.get(i), epicTaskListRestored.get(i));
        }

        List<Task> subTaskList = fileBackedTaskManager.getSimpleTasks();
        List<Task> subTaskListRestored = fileBackedTaskManagerRestored.getSimpleTasks();
        for (int i = 0; i < subTaskList.size(); i++) {
            Assertions.assertEquals(subTaskList.get(i), subTaskListRestored.get(i));
        }

        // проверяем историю
        List<Task> history = fileBackedTaskManager.getHistory();
        List<Task> historyRestored = fileBackedTaskManagerRestored.getHistory();
        Assertions.assertNull(history);
        Assertions.assertNull(historyRestored);
    }
}