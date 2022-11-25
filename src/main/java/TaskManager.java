import Tasks.EpicTask;
import Tasks.Status;
import Tasks.SubTask;
import Tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private long countID; // does ++countID when new Task()
    // Во всех мапах Long == ID задачи
    private final HashMap<Long, Task> simpleTasks;
    private final HashMap<Long, EpicTask> epicTasks;
    private final HashMap<Long, SubTask> subTasks;

    // Конструктор
    public TaskManager() {
        countID = 0;
        simpleTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
    }

    public long generateID() {
        return ++countID;
    }

    public HashMap<Long, Task> getSimpleTasks() {
        return simpleTasks;
    }

    public HashMap<Long, EpicTask> getEpicTasks() {
        return epicTasks;
    }

    public HashMap<Long, SubTask> getSubTasks() {
        return subTasks;
    }

    public void deleteAllSimpleTasks() {
        if(!simpleTasks.isEmpty()) {
            simpleTasks.clear();
        }
    }

    // Добавила удаление сабтасок. Удалила все - ведь без всех эпиков сабтасок не будет тоже всех
    public void deleteAllEpicTasks() {
        if(!epicTasks.isEmpty()) {
            subTasks.clear();
            epicTasks.clear();
        }
    }

    public void deleteAllSubTasks() {
        if(!subTasks.isEmpty()) {
            subTasks.clear();
            for (EpicTask epic : epicTasks.values()) {
                epic.setStatus(Status.NEW); // Без сабтасок эпики теперь пустые, и статус у них должен быть "новый"
            }
        }
    }

    public void deleteSimpleTask(long ID) {
        simpleTasks.remove(ID);
    } // deleteSimpleTask

    public void deleteEpicTask(long ID) {
        // убрала алгоритм квадратичной сложности :)
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getEpicID() == ID) {
                subTasks.remove(subTask.getID());
            }
        }    epicTasks.remove(ID);
        epicTasks.remove(ID);
    } // deleteEpicTask

    public void deleteSubTask(long ID) {
        long epicID = subTasks.get(ID).getEpicID();
        subTasks.remove(ID);
        updateEpicStatus(epicID);
    }

    public Task getSimpleTaskByIDorNull(long ID) { // вызывать через if (!=null) !!!
        Task result = null;
            for (Task task : simpleTasks.values()) {
                if (task.getID() == ID) {
                    result = task;
                }
            }
        if (result == null) {
            System.out.println("ID not found");
        }
        return result;
    } // getSimpleTaskByIDorNull

    public EpicTask getEpicTaskByIDorNull(long ID) { // вызывать через if (!=null) !!!
        EpicTask result = null;
        for (EpicTask epic : epicTasks.values()) {
            if (epic.getID() == ID) {
                result = epic;
            }
        }
        if (result == null) {
            System.out.println("ID not found");
        }
        return result;
    } // getEpicTaskByIDorNull

    public SubTask getSubTaskByIDorNull(long ID) { // вызывать через if (!=null) !!!
        SubTask result = null;
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getID() == ID) {
                result = subTask;
            }
        }
        if (result == null) {
            System.out.println("ID не найдено");
        }
        return result;
    } // getSubTaskByIDorNull

    public ArrayList<SubTask> getAllSubTasksOfEpicOrNull (long epicID) {  // вызывать через if (!=null) !!!
        ArrayList<SubTask> subsOfThisEpic = new ArrayList<>();
        for (SubTask subTask : subTasks.values()) {
            for (Long epicSubsID : epicTasks.get(epicID).subTasksIDs) {
                if (epicSubsID == subTask.getID()) {
                    subsOfThisEpic.add(subTask);
                }
            }
        }
        return subsOfThisEpic;
    }

    public long recordSimpleTask (Task task) {
        for (Task taskIterated : simpleTasks.values()) {
            if (taskIterated.equals(task)) {
                System.out.println("Такая задача уже существует.");
                break;
            }
        }
        task.setID(generateID());
        task.setStatus(Status.NEW);
        simpleTasks.put(task.getID(), task);
        return task.getID();
    } // recordSimpleTask

    public long recordEpicTask (EpicTask epicTask) {
        for (EpicTask taskIterated : epicTasks.values()) {
            if (taskIterated.equals(epicTask)) {
                System.out.println("Такая задача уже существует.");
                break;
            }
        }
        epicTask.setID(generateID());
        epicTask.setStatus(Status.NEW);
        epicTasks.put(epicTask.getID(), epicTask);
        return epicTask.getID();
    } // recordEpicTask

    public long recordSubTask (SubTask subTask) {
        subTask.setID(generateID());
        subTask.setStatus(Status.NEW);
        for (SubTask taskIterated : subTasks.values()) {
            if (taskIterated.equals(subTask)) {
                System.out.println("Такая задача уже существует.");
                break;
            }
            if (!epicTasks.containsKey(subTask.getEpicID())) { // Не знаю, возможна ли попытка добавления подзадачи до создания эпика
                System.out.println("Для добавления подзадачи необходимо сначала создать задачу типа epic");
                break;
            }
        }
        // после проверок добавить задачу в общий список задач и в список подзадач эпика
        subTasks.put(subTask.getID(), subTask);
        epicTasks.get(subTask.getEpicID()).subTasksIDs.add(subTask.getID()); // Уже была проверка на дублирование задачи

        // Если подзадача была завершена, нужно проверить, не последняя ли она в эпике, и если да, то и весь эпик пометить DONE
        if (subTask.getStatus().equals(Status.DONE)) {
            long epicID = subTask.getEpicID();
            EpicTask parentEpic = epicTasks.get(epicID);
            int finishedTasks = 0;
            for (Long taskID : parentEpic.subTasksIDs) {
                if (subTasks.get(taskID).getStatus().equals(Status.DONE)) { finishedTasks++;}
            }
            if (finishedTasks != 0 && finishedTasks == parentEpic.subTasksIDs.size()) {
                epicTasks.get(epicID).setStatus(Status.DONE);
            } else {
                epicTasks.get(epicID).setStatus(Status.IN_PROGRESS);
            }
        }

        return subTask.getID();
    } // recordSubTask

    // update записывает новый (переданный) объект на место старого, по ID
    public void updateSimpleTask(Task task, long ID, Status status) {
        task.setStatus(status);
        task.setID(ID);
        simpleTasks.put(ID, task);
    } // updateSimpleTask

    public void updateEpicTask(EpicTask epicTask, long ID) {
        epicTask.setID(ID);
        epicTask.setStatus(epicTasks.get(ID).getStatus());
        epicTasks.replace(ID, epicTask);
    } // updateEpicTask

    public void updateSubTask(SubTask subTask, long ID, Status status) {
        subTask.setStatus(status);
        subTask.setID(ID);
        subTasks.replace(ID, subTask);
        updateEpicStatus(subTask.getEpicID());
    } // updateSubTask

    // Мне показалось, что операция достаточно объёмная, и пусть она будет отдельным методом
    private void updateEpicStatus(long epicID) {
        EpicTask epicTask = epicTasks.get(epicID);
        int doneTasks = 0;
        int newTasks = 0;
        int progressTasks = 0; // never accessed 'cause it's in "else"
        ArrayList<SubTask> subsOfThisEpic = getAllSubTasksOfEpicOrNull(epicID);

        for (SubTask subTask : subsOfThisEpic) {
            switch (subTask.getStatus()) {
                case DONE:
                    doneTasks++;
                case NEW:
                    newTasks++;
                case IN_PROGRESS:
                    progressTasks++;
            }
        } // на выходе должно быть посчитано, сколько сабтасок с каким статусом в эпике

        if (doneTasks == subsOfThisEpic.size()) {
            epicTask.setStatus(Status.DONE);
        } else if (newTasks == subsOfThisEpic.size() || subsOfThisEpic.size() == 0) {
            epicTask.setStatus(Status.NEW);
        } else {
            epicTask.setStatus(Status.IN_PROGRESS);
        }
    } // updateEpicStatus

} // TaskManager
