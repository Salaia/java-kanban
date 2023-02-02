package managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected Long task1ID;
    protected Long task2ID;
    protected Long epic1ID;
    protected Long epic2ID;
    protected Long subTask1InEpic1;
    protected Long subTask2InEpic1;
    protected Long subTask3InEpic1;
    protected Long subTask4InEpic1;
    protected Long subTask1InEpic2;
    protected Long subTask2InEpic2;
    protected Long subTask3InEpic2;

    protected LocalDateTime initialTime;

    protected void initTasks() {
        initialTime = LocalDateTime.of(2023, Month.JANUARY, 1, 12, 0);

        // создаем разные таски
        task1ID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription", initialTime, Duration.ofMinutes(15)));
        task2ID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription", initialTime.plusMinutes(15), Duration.ofMinutes(30)));
        epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", initialTime.plusMinutes(45), Duration.ofMinutes(45), epic2ID));
        subTask2InEpic2 = taskManager.recordSubTask(new SubTask("SubTask2 in epic2", "some description", initialTime.plusMinutes(90), Duration.ofMinutes(30), epic2ID));
        subTask3InEpic2 = taskManager.recordSubTask(new SubTask("SubTask3 in epic2", "some description", initialTime.plusMinutes(120), Duration.ofMinutes(15), epic2ID));
        subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", initialTime.plusMinutes(135), Duration.ofMinutes(15), epic1ID));
        subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", initialTime.plusMinutes(150), Duration.ofMinutes(30), epic1ID));
        subTask3InEpic1 = taskManager.recordSubTask(new SubTask("SubTask3 in epic1", "some description", initialTime.plusMinutes(180), Duration.ofMinutes(15), epic1ID));
        subTask4InEpic1 = taskManager.recordSubTask(new SubTask("SubTask4 in epic1", "some description", initialTime.plusMinutes(195), Duration.ofMinutes(15), epic1ID));

        // заполняем историю
        taskManager.getSimpleTaskByIdOrNull(task1ID);
        taskManager.getSimpleTaskByIdOrNull(task2ID);
        taskManager.getEpicTaskByIdOrNull(epic2ID);
        taskManager.getSubTaskByIdOrNull(subTask3InEpic2);
        taskManager.getSimpleTaskByIdOrNull(task1ID);
    }

    @AfterEach
    void afterEach() {
        taskManager.deleteAllSimpleTasks();
        taskManager.deleteAllEpicTasks();
    }

    @Test
    void getHistory() {
        List<Task> checkHistory = new ArrayList<>();
        checkHistory.add(taskManager.getSimpleTasks().get(1));
        checkHistory.add(taskManager.getEpicTasks().get(1));
        checkHistory.add(taskManager.getSubTasks().get(2));
        checkHistory.add(taskManager.getSimpleTasks().get(0));

        for (int i = 0; i < 4; i++) {
            assertEquals(checkHistory.get(i), taskManager.getHistory().get(i));
        }
    }

    @Test
    void getSimpleTasks() {
        List<Task> checkTasks = new ArrayList<>();
        checkTasks.add(taskManager.getSimpleTasks().get(0));
        checkTasks.add(taskManager.getSimpleTasks().get(1));
        for (int i = 0; i < checkTasks.size(); i++) {
            assertEquals(checkTasks.get(i), taskManager.getSimpleTasks().get(i));
        }

    }

    @Test
    void getEpicTasks() {
        List<Task> checkTasks = new ArrayList<>();
        checkTasks.add(taskManager.getEpicTasks().get(0));
        checkTasks.add(taskManager.getEpicTasks().get(1));
        for (int i = 0; i < checkTasks.size(); i++) {
            assertEquals(checkTasks.get(i), taskManager.getEpicTasks().get(i));
        }
    }

    @Test
    void getSubTasks() {
        List<Task> checkTasks = new ArrayList<>();
        for (int i = 0; i < taskManager.getSubTasks().size(); i++) {
            checkTasks.add(taskManager.getSubTasks().get(i));
        }
        for (int i = 0; i < checkTasks.size(); i++) {
            assertEquals(checkTasks.get(i), taskManager.getSubTasks().get(i));
        }

        //проверка, что у все сабтаски привязаны к своим эпикам
        for (SubTask sub : taskManager.getSubTasks()) {
            assertNotNull(sub.getEpicId());
        }
    }

    @Test
    void deleteAllSimpleTasks() {
        assertFalse(taskManager.getSimpleTasks().isEmpty());
        taskManager.deleteAllSimpleTasks();
        assertTrue(taskManager.getSimpleTasks().isEmpty());
    }

    @Test
    void deleteAllEpicTasks() {
        assertFalse(taskManager.getEpicTasks().isEmpty());
        taskManager.deleteAllEpicTasks();
        assertTrue(taskManager.getEpicTasks().isEmpty());
        assertTrue(taskManager.getSubTasks().isEmpty());
    }

    @Test
    void deleteAllSubTasks() {
        assertFalse(taskManager.getSubTasks().isEmpty());
        taskManager.deleteAllSubTasks();
        assertFalse(taskManager.getEpicTasks().isEmpty());
        assertTrue(taskManager.getSubTasks().isEmpty());
    }

    @Test
    void deleteSimpleTask() {
        // нормальное поведение
        assertTrue(taskManager.getSimpleTasks().contains(taskManager.getSimpleTaskByIdOrNull(task1ID)));
        taskManager.deleteSimpleTask(task1ID);
        assertFalse(taskManager.getSimpleTasks().contains(taskManager.getSimpleTaskByIdOrNull(task1ID)));
    }

    @Test
    void deleteEpicTask() {
        assertTrue(taskManager.getEpicTasks().contains(taskManager.getEpicTaskByIdOrNull(epic1ID)));
        taskManager.deleteEpicTask(epic1ID);
        assertFalse(taskManager.getEpicTasks().contains(taskManager.getEpicTaskByIdOrNull(epic1ID)));
    }

    @Test
    void deleteSubTask() {
        assertTrue(taskManager.getSubTasks().contains(taskManager.getSubTaskByIdOrNull(subTask1InEpic2)));
        taskManager.deleteSubTask(subTask1InEpic2);
        assertFalse(taskManager.getSubTasks().contains(taskManager.getSubTaskByIdOrNull(subTask1InEpic2)));
    }

    @Test
    void getSimpleTaskByIdOrNull() {
        Task checkTask = new Task(task1ID, TaskTypes.TASK, "SimpleTaskName", Status.NEW, "SimpleTaskDescription", initialTime, Duration.ofMinutes(15));
        assertEquals(checkTask, taskManager.getSimpleTaskByIdOrNull(task1ID));
    }

    @Test
    void getEpicTaskByIdOrNull() {
        EpicTask epic = new EpicTask(epic1ID, TaskTypes.EPIC, "EpicName1", Status.NEW, "Epic1descr", LocalDateTime.of(2023, Month.JANUARY, 1, 14, 15), Duration.ofMinutes(75));
        assertEquals(epic, taskManager.getEpicTaskByIdOrNull(epic1ID));
    }

    @Test
    void getSubTaskByIdOrNull() {
        SubTask checkSub = new SubTask(subTask1InEpic2, TaskTypes.SUBTASK, "SubTask1 in epic2", Status.NEW, "some description", initialTime.plusMinutes(45), Duration.ofMinutes(45), epic2ID);
        assertEquals(checkSub, taskManager.getSubTaskByIdOrNull(subTask1InEpic2));
    }

    @Test
    void getAllSubTasksOfEpicOrNull() {
        List<SubTask> checkList = new ArrayList<>(taskManager.getAllSubTasksOfEpicOrNull(epic1ID));
        for (int i = 0; i < checkList.size(); i++) {
            assertEquals(checkList.get(i), taskManager.getAllSubTasksOfEpicOrNull(epic1ID).get(i));
        }
    }

    @Test
    void recordSimpleTask() {
        Task checkTask = new Task(task1ID, TaskTypes.TASK, "SimpleTaskName", Status.NEW, "SimpleTaskDescription", initialTime, Duration.ofMinutes(15));
        assertEquals(checkTask, taskManager.getSimpleTaskByIdOrNull(task1ID));
    }

    @Test
    void recordEpicTask() {
        EpicTask epic = new EpicTask(epic1ID, TaskTypes.EPIC, "EpicName1", Status.NEW, "Epic1descr", LocalDateTime.of(2023, Month.JANUARY, 1, 14, 15), Duration.ofMinutes(75));
        assertEquals(epic, taskManager.getEpicTaskByIdOrNull(epic1ID));
    }

    @Test
    void recordSubTask() {
        SubTask checkSub = new SubTask(subTask1InEpic2, TaskTypes.SUBTASK, "SubTask1 in epic2", Status.NEW, "some description", initialTime.plusMinutes(45), Duration.ofMinutes(45), epic2ID);
        assertEquals(checkSub, taskManager.getSubTaskByIdOrNull(subTask1InEpic2));
    }

    @Test
    void updateSimpleTask() {
        Task newTask = taskManager.getSimpleTaskByIdOrNull(task1ID);
        assertEquals("SimpleTaskName", taskManager.getSimpleTaskByIdOrNull(task1ID).getName());
        newTask.setName("n");
        taskManager.updateSimpleTask(newTask);
        assertEquals("n", taskManager.getSimpleTaskByIdOrNull(task1ID).getName());
    }

    @Test
    void updateEpicTask() {
        EpicTask newEpic = taskManager.getEpicTaskByIdOrNull(epic1ID);
        assertEquals("EpicName1", taskManager.getEpicTaskByIdOrNull(epic1ID).getName());
        newEpic.setName("n");
        taskManager.updateEpicTask(newEpic);
        assertEquals("n", taskManager.getEpicTaskByIdOrNull(epic1ID).getName());
    }

    @Test
    void updateSubTask() {
        SubTask newSub = taskManager.getSubTaskByIdOrNull(subTask1InEpic2);
        assertEquals("SubTask1 in epic2", taskManager.getSubTaskByIdOrNull(subTask1InEpic2).getName());
        newSub.setName("n");
        taskManager.updateSubTask(newSub);
        assertEquals("n", taskManager.getSubTaskByIdOrNull(subTask1InEpic2).getName());
    }

    @Test
    void getPrioritizedTasks() {
        // Старый вариант в сторонке сохранила :)
        TreeSet<Task> checkTree = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        checkTree.addAll(taskManager.getPrioritizedTasks());
        List<Task> expected = new ArrayList(checkTree);
        List<Task> actual = taskManager.getPrioritizedTasks();
        assertEquals(expected, actual);
    }

    @Test
    public void manageEpicAndSubTasks() {
        // История
        List<Task> gotTasks = new ArrayList<>();
        gotTasks.add(taskManager.getSubTaskByIdOrNull(subTask1InEpic1));
        gotTasks.add(taskManager.getEpicTaskByIdOrNull(epic1ID));
        List<Task> history = taskManager.getHistory();
        assertEquals(history.get(history.size() - 2), gotTasks.get(0));
        assertEquals(history.get(history.size() - 1), gotTasks.get(1));

        // Delete
        assertTrue(taskManager.getSubTasks().contains(taskManager.getSubTaskByIdOrNull(subTask1InEpic1)));
        taskManager.deleteSubTask(subTask1InEpic1);
        assertTrue(taskManager.getSubTasks().contains(taskManager.getSubTaskByIdOrNull(subTask1InEpic2))); // другая сабтаска осталась на месте
        assertTrue(taskManager.getEpicTasks().contains(taskManager.getEpicTaskByIdOrNull(epic1ID))); // и их эпик тоже
        assertFalse(taskManager.getSubTasks().contains(taskManager.getSubTaskByIdOrNull(subTask1InEpic1)));

        assertTrue(taskManager.getEpicTasks().contains(taskManager.getEpicTaskByIdOrNull(epic1ID)));
        taskManager.deleteEpicTask(epic1ID); // после удаления эпика не осталось ни его самого, ни его сабтасок
        assertFalse(taskManager.getSubTasks().contains(taskManager.getSubTaskByIdOrNull(subTask1InEpic1)));
        assertFalse(taskManager.getEpicTasks().contains(taskManager.getEpicTaskByIdOrNull(epic1ID)));
        assertFalse(taskManager.getSubTasks().contains(taskManager.getSubTaskByIdOrNull(subTask1InEpic1)));
    }

    @Test
    public void conflicts() {
        // Создаём таски на занятое место
        Long taskId0 = taskManager.recordSimpleTask(new Task("name0", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), Duration.ofMinutes(15)));
        Long epicId1 = taskManager.recordEpicTask(new EpicTask("name", "description"));

        assertNull(taskManager.recordSimpleTask(new Task("name1", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), Duration.ofMinutes(15))));
        assertNull(taskManager.recordSubTask(new SubTask("name1", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), Duration.ofMinutes(15), epicId1)));
    }
}