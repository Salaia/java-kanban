package managers;

import tasks.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task);

    List<Task> getHistory();

    void remove(Long id); // для удаления задачи из просмотра. По ТЗ ID int, но у нас они все в Long
}
