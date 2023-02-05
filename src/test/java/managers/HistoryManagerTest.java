package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;
import tasks.TaskTypes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class HistoryManagerTest<T extends HistoryManager> {
    protected HistoryManager historyManager;

    @BeforeEach
    void createManager() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void add() {
        Task simpleTaskId1 = new Task(1L, TaskTypes.TASK, "SimpleTaskTest", Status.NEW, "SimpleTask(ID=1)");
        historyManager.add(simpleTaskId1);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "Неверный размер истории.");
        Task taskFromHistory = history.get(0);
        assertEquals(simpleTaskId1, taskFromHistory, "Задачи не совпадают.");
        historyManager.add(simpleTaskId1);
        final List<Task> doubledHistory = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "Неверный размер истории.");
    }

    @Test
    void remove() {
        Task simpleTaskId1 = new Task(1L, TaskTypes.TASK, "SimpleTaskTest", Status.NEW, "SimpleTask(ID=1)");
        Task simpleTaskId2 = new Task(2L, TaskTypes.TASK, "SimpleTaskTest", Status.NEW, "SimpleTask(ID=2)");
        Task simpleTaskId3 = new Task(3L, TaskTypes.TASK, "SimpleTaskTest", Status.NEW, "SimpleTask(ID=3)");
        Task simpleTaskId4 = new Task(4L, TaskTypes.TASK, "SimpleTaskTest", Status.NEW, "SimpleTask(ID=4)");
        historyManager.remove(0L);
        final List<Task> emptyHistory = historyManager.getHistory();
        assertNull(emptyHistory, "История не пустая.");
        historyManager.add(simpleTaskId1);
        historyManager.add(simpleTaskId2);
        historyManager.add(simpleTaskId3);
        historyManager.add(simpleTaskId4);
        final List<Task> fullHistorySize4 = historyManager.getHistory();
        assertEquals(4, fullHistorySize4.size(), "Неверный размер истории.");
        historyManager.remove(simpleTaskId2.getId());
        final List<Task> removeMiddleHistorySize3 = historyManager.getHistory();
        assertEquals(3, removeMiddleHistorySize3.size(), "Неверный размер истории.");
        assertEquals(removeMiddleHistorySize3.get(0), fullHistorySize4.get(0), "Удалена неверная задача.");
        assertEquals(removeMiddleHistorySize3.get(1), fullHistorySize4.get(2), "Удалена неверная задача.");
        assertEquals(removeMiddleHistorySize3.get(2), fullHistorySize4.get(3), "Удалена неверная задача.");
        historyManager.remove(simpleTaskId1.getId());
        final List<Task> removedFirstHistorySize2 = historyManager.getHistory();
        assertEquals(2, removedFirstHistorySize2.size(), "Неверный размер истории.");
        assertEquals(removedFirstHistorySize2.get(0), fullHistorySize4.get(2), "Удалена неверная задача.");
        assertEquals(removedFirstHistorySize2.get(1), fullHistorySize4.get(3), "Удалена неверная задача.");
        historyManager.remove(simpleTaskId4.getId());
        final List<Task> removedLastHistorySize1 = historyManager.getHistory();
        assertEquals(1, removedLastHistorySize1.size(), "Неверный размер истории.");
        assertEquals(removedLastHistorySize1.get(0), fullHistorySize4.get(2), "Удалена неверная задача.");
        historyManager.remove(simpleTaskId3.getId());
        final List<Task> removedAllHistory = historyManager.getHistory();
        assertNull(removedAllHistory, "История не пустая.");
    }

    @Test
    void getHistory() {
        assertNull(historyManager.getHistory(), "История не пустая.");
        Task simpleTaskId1 = new Task(1L, TaskTypes.TASK, "SimpleTaskTest", Status.NEW, "SimpleTask(ID=1)");
        historyManager.add(simpleTaskId1);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "Неверный размер истории.");
        Task taskFromHistory = history.get(0);
        assertEquals(simpleTaskId1, taskFromHistory, "Задачи не совпадают.");
    }
}
