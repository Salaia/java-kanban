package managers;

import org.junit.jupiter.api.Assertions;
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

class FileBackedTaskManagerTest {

    @Test
    void MainTest() {
        TaskManager fileBackedTaskManager = new FileBackedTaskManager(
                new File("src/main/java/storage/TaskManagerSaved.csv"));

        // создаем разные таски
        Long taskID = fileBackedTaskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Long epic1ID = fileBackedTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long epic2ID = fileBackedTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = fileBackedTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Long subTask2InEpic2 = fileBackedTaskManager.recordSubTask(new SubTask("SubTask2 in epic2", "some description", epic2ID));
        Long subTask3InEpic2 = fileBackedTaskManager.recordSubTask(new SubTask("SubTask3 in epic2", "some description", epic2ID));

        // заполняем историю
        fileBackedTaskManager.getSimpleTaskByIdOrNull(taskID);
        fileBackedTaskManager.getSimpleTaskByIdOrNull(taskID);
        fileBackedTaskManager.getEpicTaskByIdOrNull(epic2ID);
        fileBackedTaskManager.getSubTaskByIdOrNull(subTask3InEpic2);
        fileBackedTaskManager.getSimpleTaskByIdOrNull(taskID);

        // какие-то таски удаляем
        fileBackedTaskManager.deleteSimpleTask(taskID);
        fileBackedTaskManager.deleteSubTask(subTask1InEpic2);
        fileBackedTaskManager.deleteEpicTask(epic1ID);

        //проверка на отсутствие повторов
        Set<Task> historySet = new HashSet<>(fileBackedTaskManager.getHistory());
        Assertions.assertEquals(historySet.size(), fileBackedTaskManager.getHistory().size());

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
        for (int i = 0; i < history.size(); i++) {
            Assertions.assertEquals(history.get(i), historyRestored.get(i));
        }
    }

    @Test
    public void emptyTesting() {
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