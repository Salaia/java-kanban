import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    // У этого класса, думаю, не будет геттеров-сеттеров, у него методы, использующие свои приватные поля
    private int countID; // does ++countID when new Task()
    // Во всех мапах Integer == ID задачи
    private HashMap<Integer, Task> simpleTasks; // 0 in allTasks
    private HashMap<Integer, EpicTask> epicTasks; // 1 in allTasks
    private HashMap<Integer, SubTask> subTasks; // 2 in allTasks
    private ArrayList<HashMap> allTasks; // raw - все мапы разные...

    // Мне не хотелось это отправлять в конструктор, да и если у нас будет хранение и загрузка данных,
    // то этот метод будет запускаться в случае отсутствия сохранения или очистки всей истории
    public void generateTaskManager() {
        countID = 0;
        simpleTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
        allTasks = new ArrayList<>();

        allTasks.add(simpleTasks); // 0
        allTasks.add(epicTasks); // 1
        allTasks.add(subTasks); // 2
    } // generateTaskManager

    public int generateID() {
        return ++countID;
    }

    public ArrayList<HashMap> getAllTasks() {
        return allTasks;
    }

    public HashMap<Integer, Task> getSimpleTasks() {
        return simpleTasks;
    }

    public HashMap<Integer, EpicTask> getEpicTasks() {
        return epicTasks;
    }

    public HashMap<Integer, SubTask> getSubTasks() {
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

    // Вот это вообще не понимаю. Может, хоть переделать на удаление сабтасков одного эпика, по ID эпика?..
    public void deleteAllSubTasks() {
        if(!subTasks.isEmpty()) {
            subTasks.clear();
            for (EpicTask epic : epicTasks.values()) {
                epic.setStatus("NEW"); // Без сабтасок эпики теперь пустые, и статус у них должен быть "новый"
            }
        }
    }

    public void deleteSimpleTask(Task task) {
        for (Integer i : simpleTasks.keySet()) {
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
        int epicID = subTask.getEpicID();
        EpicTask parentEpic = epicTasks.get(epicID);
        subTasks.remove(subTask.getID()); // вот ладно бы этим дело ограничилось, но если это последняя незавершенная задача в эпике?
        if (parentEpic.subTasksIDs.isEmpty()) { // Если оказывается, что других подзадач с таким epicID не было
            epicTasks.get(epicID).setStatus("NEW"); // Если пустой эпик оставлять, то ему нужно поменять статус на "новый"
        }
        int finishedTasks = 0;
        for (Integer taskID : parentEpic.subTasksIDs) {
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

    // я сначала назвала create, но, если мы объект получаем, то какое тут создание...
    public void recordSimpleTask (Task task) {
        for (Task taskIterated : simpleTasks.values()) {
            if (taskIterated.equals(task)) {
                System.out.println("Такая задача уже существует.");
                break;
            }
        }
        simpleTasks.put(task.getID(), task);
    } // recordSimpleTask

    public void recordEpicTask (EpicTask epicTask) {
        for (EpicTask taskIterated : epicTasks.values()) {
            if (taskIterated.equals(epicTask)) {
                System.out.println("Такая задача уже существует.");
                break;
            }
        }
        epicTasks.put(epicTask.getID(), epicTask);
    } // recordEpicTask

    public void recordSubTask (SubTask subTask) {
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
            int epicID = subTask.getEpicID();
            EpicTask parentEpic = epicTasks.get(epicID);
            int finishedTasks = 0;
            for (Integer taskID : parentEpic.subTasksIDs) {
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
