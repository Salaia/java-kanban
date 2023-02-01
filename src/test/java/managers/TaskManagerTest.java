package managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;
import tasks.TaskTypes;

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


    //TODO у меня collision проверяет по 15 минут, когда задаёшь не кратное время - всё рушится
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
        //проверка, что у все сабтаски привязаны к своим эпикам
        for (SubTask sub : taskManager.getSubTasks()) {
            assertNotNull(sub.getEpicId());
        }
    }

    @Test
    void getHistory() {
        assertNotNull(taskManager.getHistory());
    }

    @Test
    void getSimpleTasks() {
        assertNotNull(taskManager.getSimpleTasks());
    }

    @Test
    void getEpicTasks() {
        assertNotNull(taskManager.getEpicTasks());
    }

    @Test
    void getSubTasks() {
        assertNotNull(taskManager.getSubTasks());
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
        assertEquals(TaskTypes.TASK, taskManager.getSimpleTaskByIdOrNull(task1ID).getTaskType());
    }

    @Test
    void getEpicTaskByIdOrNull() {
        assertEquals(TaskTypes.EPIC, taskManager.getEpicTaskByIdOrNull(epic1ID).getTaskType());
    }

    @Test
    void getSubTaskByIdOrNull() {
        assertEquals(TaskTypes.SUBTASK, taskManager.getSubTaskByIdOrNull(subTask1InEpic2).getTaskType());
    }

    @Test
    void getAllSubTasksOfEpicOrNull() {
        assertNotNull(taskManager.getAllSubTasksOfEpicOrNull(epic1ID));
        taskManager.deleteEpicTask(epic1ID);
        assertNull(taskManager.getAllSubTasksOfEpicOrNull(epic1ID));
    }

    @Test
    void recordSimpleTask() {
        assertEquals(2, taskManager.getSimpleTasks().size());
        Long newTaskId = taskManager.recordSimpleTask(new Task("n", "d", initialTime.plusDays(10), Duration.ofMinutes(120)));
        assertEquals(3, taskManager.getSimpleTasks().size());
        assertTrue(taskManager.getSimpleTasks().contains(taskManager.getSimpleTaskByIdOrNull(newTaskId)));
        taskManager.deleteSimpleTask(newTaskId);
    }

    @Test
    void recordEpicTask() {
        assertEquals(2, taskManager.getEpicTasks().size());
        Long newEpic = taskManager.recordEpicTask(new EpicTask("n", "d"));
        assertEquals(3, taskManager.getEpicTasks().size());
        assertTrue(taskManager.getEpicTasks().contains(taskManager.getEpicTaskByIdOrNull(newEpic)));
    }

    @Test
    void recordSubTask() {
        assertEquals(7, taskManager.getSubTasks().size());
        Long newSubId = taskManager.recordSubTask(new SubTask("n", "d", epic1ID));
        assertEquals(8, taskManager.getSubTasks().size());
        assertTrue(taskManager.getSubTasks().contains(taskManager.getSubTaskByIdOrNull(newSubId)));
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
        TreeSet<Task> checkTree = new TreeSet<>((Task task1, Task task2) -> task1.getStartTime().compareTo(task2.getStartTime()));
        checkTree.addAll(taskManager.getPrioritizedTasks());
        Task[] checkArray = checkTree.toArray(checkTree.toArray(new Task[0]));
        Task[] checkArray2 = taskManager.getPrioritizedTasks().toArray(checkTree.toArray(new Task[0]));
        assertArrayEquals(checkArray, checkArray2);
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