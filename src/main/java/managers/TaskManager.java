package managers;

import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public interface TaskManager {
    public long generateID();
    public HashMap<Long, Task> getSimpleTasks();
    public HashMap<Long, EpicTask> getEpicTasks();
    public HashMap<Long, SubTask> getSubTasks();
    public void deleteAllSimpleTasks();
    public void deleteAllEpicTasks();
    public void deleteAllSubTasks();
    public void deleteSimpleTask(long ID);
    public void deleteEpicTask(long ID);
    public void deleteSubTask(long ID);
    public Task getSimpleTaskByIDorNull(long ID);
    public EpicTask getEpicTaskByIDorNull(long ID);
    public SubTask getSubTaskByIDorNull(long ID);
    public ArrayList<SubTask> getAllSubTasksOfEpicOrNull (long epicID);
    public long recordSimpleTask (Task task);
    public long recordEpicTask (EpicTask epicTask);
    public long recordSubTask (SubTask subTask);
    public void updateSimpleTask(Task task, long ID, Status status);
    public void updateEpicTask(EpicTask epicTask, long ID);
    public void updateSubTask(SubTask subTask, long ID, Status status);

} // TaskManager