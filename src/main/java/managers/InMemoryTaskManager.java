package managers;

import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.*;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {
    private Long countId; // does ++countId when new Task()
    // Во всех мапах Long == ID задачи
    private final Map<Long, Task> simpleTasks;
    private final Map<Long, EpicTask> epicTasks;
    private final Map<Long, SubTask> subTasks;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // Конструктор
    public InMemoryTaskManager() {
        countId = 0L;
        simpleTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
    }

    private Long generateId() {
        return ++countId;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getSimpleTasks() {
        return new ArrayList<>(simpleTasks.values());
    }

    @Override
    public List<EpicTask> getEpicTasks() {
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
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
                epic.getSubTasksIds().clear();
                epic.setStatus(Status.NEW); // Без сабтасок эпики теперь пустые, и статус у них должен быть "новый"
            }
        }
    }

    @Override
    public void deleteSimpleTask(Long id) {
        simpleTasks.remove(id);
        historyManager.remove(id);
    } // deleteSimpleTask

    @Override
    public void deleteEpicTask(Long id) {
        Map<Long, SubTask> copySubtasks = new HashMap<>(subTasks);
        for (SubTask subTask : copySubtasks.values()) {
            if (Objects.equals(subTask.getEpicId(), id)) {
                subTasks.remove(subTask.getId());
                historyManager.remove(subTask.getId());
            }
        }
        epicTasks.remove(id);
        historyManager.remove(id);
    } // deleteEpicTask

    @Override
    public void deleteSubTask(Long id) {
        Long epicId = subTasks.get(id).getEpicId();
        epicTasks.get(epicId).getSubTasksIds().remove(id); // удаляет сабтаску из списка внутри её эпика
        subTasks.remove(id);
        historyManager.remove(id);
        updateEpicStatus(epicId);
    }

    @Override
    public Task getSimpleTaskByIdOrNull(Long id) { // вызывать через if (!=null) !!!
        if (!simpleTasks.containsKey(id)) {
            System.out.println("ID not found");
            return null;
        } else {
            historyManager.add(simpleTasks.get(id));
            return simpleTasks.get(id);
        }
    } // getSimpleTaskByIDorNull

    @Override
    public EpicTask getEpicTaskByIdOrNull(Long id) { // вызывать через if (!=null) !!!
        if (!epicTasks.containsKey(id)) {
            System.out.println("ID not found");
            return null;
        } else {
            historyManager.add(epicTasks.get(id));
            return epicTasks.get(id);
        }
    } // getEpicTaskByIDorNull

    @Override
    public SubTask getSubTaskByIdOrNull(Long id) { // вызывать через if (!=null) !!!
        if (!subTasks.containsKey(id)) {
            System.out.println("ID не найдено");
            return null;
        } else {
            historyManager.add(subTasks.get(id));
            return subTasks.get(id);
        }
    } // getSubTaskByIDorNull

    @Override
    public List<SubTask> getAllSubTasksOfEpicOrNull(Long epicId) {  // вызывать через if (!=null) !!!
        List<SubTask> subsOfThisEpic = new ArrayList<>();
        for (SubTask subTask : subTasks.values()) {
            if (Objects.equals(subTask.getEpicId(), epicId)) {
                subsOfThisEpic.add(subTask);
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
        task.setId(generateId());
        task.setStatus(Status.NEW);
        simpleTasks.put(task.getId(), task);
        return task.getId();
    } // recordSimpleTask

    @Override
    public Long recordEpicTask(EpicTask epicTask) {
        for (EpicTask taskIterated : epicTasks.values()) {
            if (taskIterated.equals(epicTask)) {
                System.out.println("Такая задача уже существует.");
                break;
            }
        }
        epicTask.setId(generateId());
        epicTask.setStatus(Status.NEW);
        epicTasks.put(epicTask.getId(), epicTask);
        return epicTask.getId();
    } // recordEpicTask

    @Override
    public Long recordSubTask(SubTask subTask) {
        subTask.setId(generateId());
        subTask.setStatus(Status.NEW);
        for (SubTask taskIterated : subTasks.values()) {
            if (taskIterated.equals(subTask)) {
                System.out.println("Такая задача уже существует.");
                break;
            }
            if (!epicTasks.containsKey(subTask.getEpicId())) { // Не знаю, возможна ли попытка добавления подзадачи до создания эпика
                System.out.println("Для добавления подзадачи необходимо сначала создать задачу типа epic");
                break;
            }
        }
        // после проверок добавить задачу в общий список задач и в список подзадач эпика
        subTasks.put(subTask.getId(), subTask);
        epicTasks.get(subTask.getEpicId()).getSubTasksIds().add(subTask.getId()); // Уже была проверка на дублирование задачи

        // Если подзадача была завершена, нужно проверить, не последняя ли она в эпике, и если да, то и весь эпик пометить DONE
        if (subTask.getStatus().equals(Status.DONE)) {
            Long epicId = subTask.getEpicId();
            EpicTask parentEpic = epicTasks.get(epicId);
            int finishedTasks = 0;
            for (Long taskId : parentEpic.getSubTasksIds()) {
                if (subTasks.get(taskId).getStatus().equals(Status.DONE)) {
                    finishedTasks++;
                }
            }
            if (finishedTasks != 0 && finishedTasks == parentEpic.getSubTasksIds().size()) {
                epicTasks.get(epicId).setStatus(Status.DONE);
            } else {
                epicTasks.get(epicId).setStatus(Status.IN_PROGRESS);
            }
        }
        return subTask.getId();
    } // recordSubTask

    // update записывает новый (переданный) объект на место старого, по ID
    @Override
    public void updateSimpleTask(Task task) {
        simpleTasks.replace(task.getId(), task);
    } // updateSimpleTask

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        epicTasks.replace(epicTask.getId(), epicTask);
    } // updateEpicTask

    @Override
    public void updateSubTask(SubTask subTask) {
        subTasks.replace(subTask.getId(), subTask);
        updateEpicStatus(subTask.getEpicId());
    } // updateSubTask

    // Мне показалось, что операция достаточно объёмная, и пусть она будет отдельным методом
    private void updateEpicStatus(Long epicId) {
        EpicTask epicTask = epicTasks.get(epicId);
        int doneTasks = 0;
        int newTasks = 0;
        List<SubTask> subsOfThisEpic = getAllSubTasksOfEpicOrNull(epicId);

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