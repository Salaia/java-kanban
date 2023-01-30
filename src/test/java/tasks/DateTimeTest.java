package tasks;

import managers.FileBackedTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
    Мне настолько не понравилось писать тесты на отдельные методы,
    что на введение нового функционала с датой и временем мне захотелось
    создать отдельный файл под его тестирование
 */

public class DateTimeTest {

    FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(new File("src/main/java/storage/TaskManagerSaved.csv"));
    static TaskManager manager;
    static Duration duration;

    @BeforeEach
    void beforeEach() {
        manager = new FileBackedTaskManager(
                new File("src/main/java/storage/TaskManagerSaved.csv"));
        duration = Duration.ofMinutes(30);

    }

    @Test
    public void manageSimpleTasks() {
        // Создание
        Long taskId2 = manager.recordSimpleTask(new Task("name2", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 30), duration));
        Long taskId0 = manager.recordSimpleTask(new Task("name0", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 11, 30), duration));
        Long taskId1 = manager.recordSimpleTask(new Task("name1", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), duration));
        assertEquals(manager.getSimpleTaskByIdOrNull(taskId0).getStartTime(), LocalDateTime.of(2023, Month.JANUARY, 28, 11, 30));
        assertEquals(manager.getSimpleTaskByIdOrNull(taskId2).getDuration(), duration);

        // История
        manager.getSimpleTaskByIdOrNull(taskId1);
        manager.getSimpleTaskByIdOrNull(taskId2);

        //Проверяем удаление
        assertTrue(manager.getSimpleTasks().contains(manager.getSimpleTaskByIdOrNull(taskId1)));
        manager.deleteSimpleTask(taskId1);
        assertFalse(manager.getSimpleTasks().contains(manager.getSimpleTaskByIdOrNull(taskId1)));
        assertTrue(manager.getSimpleTasks().contains(manager.getSimpleTaskByIdOrNull(taskId0)));
    }

    @Test
    public void manageEpicAndSubTasks() {
        // Создание
        Long epicId1 = manager.recordEpicTask(new EpicTask("name", "description"));
        Long subId1_1 = manager.recordSubTask(new SubTask("name", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), duration, epicId1));
        Long subId1_2 = manager.recordSubTask(new SubTask("name", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 30), duration, epicId1));
        assertEquals(manager.getSubTaskByIdOrNull(subId1_1).getDuration(), duration);
        assertEquals(manager.getEpicTaskByIdOrNull(epicId1).getDuration(), Duration.ofMinutes(60));
        assertEquals(manager.getEpicTaskByIdOrNull(epicId1).getStartTime(), manager.getSubTaskByIdOrNull(subId1_1).getStartTime());

        // История
        List<Task> gotTasks = new ArrayList<>();
        gotTasks.add(manager.getSubTaskByIdOrNull(subId1_1));
        gotTasks.add(manager.getEpicTaskByIdOrNull(epicId1));
        List<Task> history = manager.getHistory();
        assertEquals(history.get(0), gotTasks.get(0));
        assertEquals(history.get(1), gotTasks.get(1));

        // Delete
        assertTrue(manager.getSubTasks().contains(manager.getSubTaskByIdOrNull(subId1_1)));
        manager.deleteSubTask(subId1_1);
        assertTrue(manager.getSubTasks().contains(manager.getSubTaskByIdOrNull(subId1_2))); // другая сабтаска осталась на месте
        assertTrue(manager.getEpicTasks().contains(manager.getEpicTaskByIdOrNull(epicId1))); // и их эпик тоже
        assertFalse(manager.getSubTasks().contains(manager.getSubTaskByIdOrNull(subId1_1)));

        assertTrue(manager.getEpicTasks().contains(manager.getEpicTaskByIdOrNull(epicId1)));
        manager.deleteEpicTask(epicId1); // после удаления эпика не осталось ни его самого, ни его сабтасок
        assertFalse(manager.getSubTasks().contains(manager.getSubTaskByIdOrNull(subId1_2)));
        assertFalse(manager.getEpicTasks().contains(manager.getEpicTaskByIdOrNull(epicId1)));
        assertFalse(manager.getSubTasks().contains(manager.getSubTaskByIdOrNull(subId1_1)));
    }

    @Test
    public void fillInCSV() {
        // Создание
        Long taskId2 = fileBackedTaskManager.recordSimpleTask(new Task("name2", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 30), duration));
        Long taskId0 = fileBackedTaskManager.recordSimpleTask(new Task("name0", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 11, 30), duration));
        Long taskId1 = fileBackedTaskManager.recordSimpleTask(new Task("name1", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), duration));

        // История
        fileBackedTaskManager.getSimpleTaskByIdOrNull(taskId1);
        fileBackedTaskManager.getSimpleTaskByIdOrNull(taskId2);

        // Создание
        Long epicId1 = fileBackedTaskManager.recordEpicTask(new EpicTask("name", "description"));
        Long subId1_1 = fileBackedTaskManager.recordSubTask(new SubTask("name", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 13, 0), duration, epicId1));
        Long subId1_2 = fileBackedTaskManager.recordSubTask(new SubTask("name", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 13, 30), duration, epicId1));

        // История
        fileBackedTaskManager.getEpicTaskByIdOrNull(epicId1);
        fileBackedTaskManager.getSubTaskByIdOrNull(subId1_1);

        // Хотелось хоть раз вызвать этот метод, раз уж я его зачем-то написала... он не используется после переписывания на быстрый доступ к приоритетам
        System.out.println(fileBackedTaskManager.getPrioritizedTasks());
    }

    @Test
    public void conflicts() {
        // Создаём таски на занятое место
        Long taskId0 = manager.recordSimpleTask(new Task("name0", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), duration));
        Long epicId1 = manager.recordEpicTask(new EpicTask("name", "description"));

        assertNull(manager.recordSimpleTask(new Task("name1", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), duration)));
        assertNull(manager.recordSubTask(new SubTask("name1", "description", LocalDateTime.of(2023, Month.JANUARY, 28, 12, 0), duration, epicId1)));
    }
}
