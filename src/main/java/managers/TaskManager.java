package managers;

import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.util.List;

public interface TaskManager {

    List<Task> getHistory();

    List<Task> getSimpleTasks();

    List<EpicTask> getEpicTasks();

    List<SubTask> getSubTasks();

    void deleteAllSimpleTasks();

    void deleteAllEpicTasks();

    void deleteAllSubTasks();

    void deleteSimpleTask(Long id);

    void deleteEpicTask(Long id);

    void deleteSubTask(Long id);

    Task getSimpleTaskByIdOrNull(Long id);

    EpicTask getEpicTaskByIdOrNull(Long id);

    SubTask getSubTaskByIdOrNull(Long id);

    List<SubTask> getAllSubTasksOfEpicOrNull(Long epicId);

    Long recordSimpleTask(Task task);

    Long recordEpicTask(EpicTask epicTask);

    Long recordSubTask(SubTask subTask);

    void updateSimpleTask(Task task);

    void updateEpicTask(EpicTask epicTask);

    void updateSubTask(SubTask subTask);

    List<Task> getPrioritizedTasks();

} // TaskManager