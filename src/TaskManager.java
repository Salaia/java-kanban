import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    // У этого класса, думаю, не будет геттеров-сеттеров, у него методы, использующие свои приватные поля
    private int countID; // does ++countID when new Task()
    // Во всех мапах Integer == ID задачи
    private HashMap<Long, Task> simpleTasks;
    private HashMap<Long, EpicTask> epicTasks;
    private HashMap<Long, SubTask> subTasks;

    // Конструктор так конструктор :)
    public TaskManager() {
        countID = 0;
        simpleTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
    }

    public int generateID() {
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

    public void deleteAllEpicTasks() {
        if(!epicTasks.isEmpty()) {
            epicTasks.clear();
        }
    }

    public void deleteAllSubTasks() {
        if(!subTasks.isEmpty()) {
            subTasks.clear();
            for (EpicTask epic : epicTasks.values()) {
                epic.setStatus("NEW"); // Без сабтасок эпики теперь пустые, и статус у них должен быть "новый"
            }
        }
    }

    public void deleteSimpleTask(Task task) {
        for (Long i : simpleTasks.keySet()) {
            if (simpleTasks.get(i).getID() == task.getID()) {
                simpleTasks.remove(i);
                break;
            }
        }
    } // deleteSimpleTask

    public void deleteEpicTask(EpicTask epicTask) {
        ArrayList<SubTask> subTasksOfEpic = new ArrayList<>(); // удалили эпик - надо удалить вложенные подзадачи
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getEpicID() == epicTask.getID()) {
                subTasksOfEpic.add(subTask);
            }
        }
        for (SubTask subTaskToDelete : subTasks.values()) {
            for (SubTask subTaskOfEpic : subTasksOfEpic) {
                if (subTaskOfEpic.equals(subTaskToDelete)) {
                    subTasks.remove(subTaskToDelete.getID());
                }
            }
        }
        epicTasks.remove(epicTask.getID());
    } // deleteEpicTask

    public void deleteSubTask(SubTask subTask) {
        long epicID = subTask.getEpicID();
        EpicTask parentEpic = epicTasks.get(epicID);
        subTasks.remove(subTask.getID()); // вот ладно бы этим дело ограничилось, но если это последняя незавершенная задача в эпике?
        if (parentEpic.subTasksIDs.isEmpty()) { // Если оказывается, что других подзадач с таким epicID не было
            epicTasks.get(epicID).setStatus("NEW"); // Если пустой эпик оставлять, то ему нужно поменять статус на "новый"
        }
        int finishedTasks = 0;
        for (Long taskID : parentEpic.subTasksIDs) {
            if (subTasks.get(taskID).getStatus().equals("DONE")) { finishedTasks++;}
        }
        if (finishedTasks != 0 && finishedTasks == parentEpic.subTasksIDs.size()) {
            epicTasks.get(epicID).setStatus("DONE");
        }
    }

    public Task getSimpleTaskByIDorNull(int ID) { // вызывать через if (!=null) !!!
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

    public EpicTask getEpicTaskByIDorNull(int ID) { // вызывать через if (!=null) !!!
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

    public SubTask getSubTaskByIDorNull(int ID) { // вызывать через if (!=null) !!!
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

    // В ТЗ не сказано, получение по ID эпика или по объекту, я сделала ID
    public ArrayList<SubTask> getAllSubTasksOfEpicOrNull (int epicID) {  // вызывать через if (!=null) !!!
        ArrayList<SubTask> result = new ArrayList<>();
        for (SubTask task : subTasks.values()) {
            if (task.getID() == epicID) {
                result.add(task);
            }
        }
        return result;
    }

    public long recordSimpleTask (Task task) {
        for (Task taskIterated : simpleTasks.values()) {
            if (taskIterated.equals(task)) {
                System.out.println("Такая задача уже существует.");
                break;
            }
        }
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
        epicTasks.put(epicTask.getID(), epicTask);
        return epicTask.getID();
    } // recordEpicTask

    public long recordSubTask (SubTask subTask) {
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
        return subTask.getID();
    } // recordSubTask

    // update записывает новый (переданный) объект на место старого, по ID \
    // Если такого ID не было, значит, запишет новый (хотя из корректного GUI такой ситуации возникнуть не должно)
    public void updateSimpleTask(Task task) {
        simpleTasks.put(task.getID(), task);
    } // updateSimpleTask

    public void updateEpicTask(EpicTask epicTask) {
        epicTasks.put(epicTask.getID(), epicTask);
    } // updateEpicTask

    public void updateSubTask(SubTask subTask) {
        subTasks.put(subTask.getID(), subTask);

        if (subTask.getStatus().equals("IN_PROGRESS")) {
            epicTasks.get(subTask.getEpicID()).setStatus("IN_PROGRESS"); // даже если был он же, ну пусть перезапишется
        }

        // Если подзадача была завершена, нужно проверить, не последняя ли она в эпике, и если да, то и весь эпик пометить DONE
        if (subTask.getStatus().equals("DONE")) {
            long epicID = subTask.getEpicID();
            EpicTask parentEpic = epicTasks.get(epicID);
            int finishedTasks = 0;
            for (Long taskID : parentEpic.subTasksIDs) {
                if (subTasks.get(taskID).getStatus().equals("DONE")) { finishedTasks++;}
            }
            if (finishedTasks != 0 && finishedTasks == parentEpic.subTasksIDs.size()) {
                epicTasks.get(epicID).setStatus("DONE");
            } else {
                epicTasks.get(epicID).setStatus("IN_PROGRESS");
            }
        }
    } // updateSubTask

} // TaskManager
