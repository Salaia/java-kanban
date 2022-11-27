package managers;

import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class InMemoryTaskManager implements TaskManager {
    private Long countID; // does ++countID when new Task()
    // Во всех мапах Long == ID задачи
    private final HashMap<Long, Task> simpleTasks;
    private final HashMap<Long, EpicTask> epicTasks;
    private final HashMap<Long, SubTask> subTasks;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // Конструктор
    public InMemoryTaskManager() {
        countID = 0L;
        simpleTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
    }

    private Long generateID() {
        return ++countID;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public ArrayList<Task> getSimpleTasks() {
        return new ArrayList<>(simpleTasks.values());
    }

    @Override
    public ArrayList<EpicTask> getEpicTasks() {
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void deleteAllSimpleTasks() {
        if (!simpleTasks.isEmpty()) {
            simpleTasks.clear();
        }
    }

    @Override
    public void deleteAllEpicTasks() {
        if (!epicTasks.isEmpty()) {
            subTasks.clear();
            epicTasks.clear();
        }
    }

    @Override
    public void deleteAllSubTasks() {
        if (!subTasks.isEmpty()) {
            subTasks.clear();
            for (EpicTask epic : epicTasks.values()) {
                epic.subTasksIDs.clear();
                epic.setStatus(Status.NEW); // Без сабтасок эпики теперь пустые, и статус у них должен быть "новый"
            }
        }
    }

    @Override
    public void deleteSimpleTask(Long ID) {
        simpleTasks.remove(ID);
    } // deleteSimpleTask

    @Override
    public void deleteEpicTask(Long ID) {
        // убрала алгоритм квадратичной сложности 🙂
        for (SubTask subTask : subTasks.values()) {
            if (Objects.equals(subTask.getEpicID(), ID)) {
                subTasks.remove(subTask.getID());
            }
        }
        epicTasks.remove(ID);
        epicTasks.remove(ID);
    } // deleteEpicTask

    @Override
    public void deleteSubTask(Long ID) {
        Long epicID = subTasks.get(ID).getEpicID();
        epicTasks.get(epicID).subTasksIDs.remove(ID); // удаляет сабтаску из списка внутри её эпика
        subTasks.remove(ID);
        updateEpicStatus(epicID);
    }

    @Override
    public Task getSimpleTaskByIDorNull(Long ID) { // вызывать через if (!=null) !!!
        if (!simpleTasks.containsKey(ID)) {
            System.out.println("ID not found");
            return null;
        } else {
            historyManager.add(simpleTasks.get(ID));
            return simpleTasks.get(ID);
        }
    } // getSimpleTaskByIDorNull

    @Override
    public EpicTask getEpicTaskByIDorNull(Long ID) { // вызывать через if (!=null) !!!
        if (!epicTasks.containsKey(ID)) {
            System.out.println("ID not found");
            return null;
        } else {
            historyManager.add(epicTasks.get(ID));
            return epicTasks.get(ID);
        }
    } // getEpicTaskByIDorNull

    @Override
    public SubTask getSubTaskByIDorNull(Long ID) { // вызывать через if (!=null) !!!
        if (!subTasks.containsKey(ID)) {
            System.out.println("ID не найдено");
            return null;
        } else {
            historyManager.add(subTasks.get(ID));
            return subTasks.get(ID);
        }
    } // getSubTaskByIDorNull

    @Override
    public ArrayList<SubTask> getAllSubTasksOfEpicOrNull(Long epicID) {  // вызывать через if (!=null) !!!
        ArrayList<SubTask> subsOfThisEpic = new ArrayList<>();
        for (SubTask subTask : subTasks.values()) {
            for (Long epicSubsID : epicTasks.get(epicID).subTasksIDs) {
                if (Objects.equals(epicSubsID, subTask.getID())) {
                    subsOfThisEpic.add(subTask);
                }
            }
        }
        return subsOfThisEpic;
    }

    @Override
    public Long recordSimpleTask(Task task) {
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

    @Override
    public Long recordEpicTask(EpicTask epicTask) {
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

    @Override
    public Long recordSubTask(SubTask subTask) {
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
            Long epicID = subTask.getEpicID();
            EpicTask parentEpic = epicTasks.get(epicID);
            int finishedTasks = 0;
            for (Long taskID : parentEpic.subTasksIDs) {
                if (subTasks.get(taskID).getStatus().equals(Status.DONE)) {
                    finishedTasks++;
                }
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
    @Override
    public void updateSimpleTask(Task task) {
        simpleTasks.replace(task.getID(), task);
    } // updateSimpleTask

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        epicTasks.replace(epicTask.getID(), epicTask);
    } // updateEpicTask

    @Override
    public void updateSubTask(SubTask subTask) {
        subTasks.replace(subTask.getID(), subTask);
        updateEpicStatus(subTask.getEpicID());
    } // updateSubTask

    // Мне показалось, что операция достаточно объёмная, и пусть она будет отдельным методом
    private void updateEpicStatus(Long epicID) {
        EpicTask epicTask = epicTasks.get(epicID);
        int doneTasks = 0;
        int newTasks = 0;
        ArrayList<SubTask> subsOfThisEpic = getAllSubTasksOfEpicOrNull(epicID);

        for (SubTask subTask : subsOfThisEpic) {
            switch (subTask.getStatus()) {
                case DONE:
                    doneTasks++;
                    break;
                case NEW:
                    newTasks++;
                    break;
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