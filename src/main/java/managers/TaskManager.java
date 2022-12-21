package managers;

import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    List<Task> getHistory();
    ArrayList<Task> getSimpleTasks();

    ArrayList<EpicTask> getEpicTasks();

    ArrayList<SubTask> getSubTasks();

    void deleteAllSimpleTasks();

    void deleteAllEpicTasks();

    void deleteAllSubTasks();

    void deleteSimpleTask(Long ID);

    void deleteEpicTask(Long ID);

    void deleteSubTask(Long ID);

    Task getSimpleTaskByIDorNull(Long ID);

    EpicTask getEpicTaskByIDorNull(Long ID);

    SubTask getSubTaskByIDorNull(Long ID);

    ArrayList<SubTask> getAllSubTasksOfEpicOrNull(Long epicID);

    Long recordSimpleTask(Task task);

    Long recordEpicTask(EpicTask epicTask);

    Long recordSubTask(SubTask subTask);

    void updateSimpleTask(Task task);

    void updateEpicTask(EpicTask epicTask);

    void updateSubTask(SubTask subTask);

} // TaskManager