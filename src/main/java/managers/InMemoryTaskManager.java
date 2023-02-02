package managers;

import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {
    private Long countId; // does ++countId when new Task()
    // Во всех мапах Long == ID задачи. Поменяла всем на protected для доступа из наследника
    protected final Map<Long, Task> simpleTasks;
    protected final Map<Long, EpicTask> epicTasks;
    protected final Map<Long, SubTask> subTasks;
    // замечания - перед тем, как добавить в TreeSet объект с измененным состоянием - его надо сначала удалить.
    protected final TreeSet<Task> priority; // хранит все таски С УСТАНОВЛЕННЫМ ВРЕМЕНЕМ в порядке приоритета - времени начала
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private Map<LocalDateTime, Boolean> schedule;

    // Конструктор
    public InMemoryTaskManager() {
        countId = 0L;
        simpleTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
        priority = new TreeSet<>((Task task1, Task task2) -> task1.getStartTime().compareTo(task2.getStartTime()));
        createSchedule();
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

            TreeSet<Task> checkList = new TreeSet<>(priority);
            for (Task task : checkList) {
                if (task.getTaskType().equals(TaskTypes.TASK)) {
                    priority.remove(task);
                }
            }
        }
    }

    @Override
    public void deleteAllEpicTasks() {
        if (!epicTasks.isEmpty()) {
            subTasks.clear();
            epicTasks.clear();

            TreeSet<Task> checkList = new TreeSet<>(priority);
            for (Task task : checkList) {
                if (!task.getTaskType().equals(TaskTypes.TASK)) {
                    priority.remove(task);
                }
            }
        }
    }

    @Override
    public void deleteAllSubTasks() {
        if (!subTasks.isEmpty()) {
            subTasks.clear();
            for (EpicTask epic : epicTasks.values()) {
                epic.getSubTasksIds().clear();
                epic.setStatus(Status.NEW); // Без сабтасок эпики теперь пустые, и статус у них должен быть "новый"
                countEpicDuration(epic.getId());
            }

            TreeSet<Task> checkList = new TreeSet<>(priority);
            for (Task task : checkList) {
                if (task.getTaskType().equals(TaskTypes.SUBTASK)) {
                    priority.remove(task);
                }
            }
        }
    }

    @Override
    public void deleteSimpleTask(Long id) {
        priority.remove(getSimpleTaskByIdOrNull(id));
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
        priority.remove(getSubTaskByIdOrNull(id));
        Long epicId = subTasks.get(id).getEpicId();
        epicTasks.get(epicId).getSubTasksIds().remove(id); // удаляет сабтаску из списка внутри её эпика
        subTasks.remove(id);
        historyManager.remove(id);
        updateEpicStatus(epicId);
        countEpicDuration(epicId);
    }

    @Override
    public Task getSimpleTaskByIdOrNull(Long id) { // вызывать через if (!=null) !!!
        if (!simpleTasks.containsKey(id)) {
            return null;
        } else {
            historyManager.add(simpleTasks.get(id));
            return simpleTasks.get(id);
        }
    } // getSimpleTaskByIDorNull

    @Override
    public EpicTask getEpicTaskByIdOrNull(Long id) { // вызывать через if (!=null) !!!
        if (!epicTasks.containsKey(id)) {
            return null;
        } else {
            historyManager.add(epicTasks.get(id));
            return epicTasks.get(id);
        }
    } // getEpicTaskByIDorNull

    @Override
    public SubTask getSubTaskByIdOrNull(Long id) { // вызывать через if (!=null) !!!
        if (!subTasks.containsKey(id)) {
            return null;
        } else {
            historyManager.add(subTasks.get(id));
            return subTasks.get(id);
        }
    } // getSubTaskByIDorNull

    @Override
    public List<SubTask> getAllSubTasksOfEpicOrNull(Long epicId) {  // вызывать через if (!=null) !!!
        List<SubTask> subsOfThisEpic = new ArrayList<>();
        if (!epicTasks.containsKey(epicId)) {
            subsOfThisEpic = null;
        }
        for (SubTask subTask : subTasks.values()) {
            if (Objects.equals(subTask.getEpicId(), epicId)) {
                subsOfThisEpic.add(subTask);
            }
        }
        return subsOfThisEpic;
    }

    @Override
    public Long recordSimpleTask(Task task) {
        // Проверка: не записана ли уже такая таска
        for (Task taskIterated : simpleTasks.values()) {
            if (taskIterated.equals(task)) {
                break;
            }
        }

        // Проверка: не занято ли время, на которое претендует таска
        if (fastCollisionCheck(task)) {
            return null;
        }

        task.setId(generateId());
        task.setStatus(Status.NEW);

        simpleTasks.put(task.getId(), task);
        return task.getId();
    } // recordSimpleTask

    @Override
    public Long recordEpicTask(EpicTask epicTask) {
        // Проверка: не записана ли уже такая таска
        for (EpicTask taskIterated : epicTasks.values()) {
            if (taskIterated.equals(epicTask)) {
                break;
            }
        }

        // Проверка: не занято ли время, на которое претендует таска
        if (fastCollisionCheck(epicTask)) {
            return null;
        }

        epicTask.setId(generateId());
        epicTask.setStatus(Status.NEW);

        epicTasks.put(epicTask.getId(), epicTask);
        return epicTask.getId();
    } // recordEpicTask

    @Override
    public Long recordSubTask(SubTask subTask) {
        // Проверка: не занято ли время, на которое претендует таска
        if (fastCollisionCheck(subTask)) {
            return null;
        }

        // Проверка: не записана ли уже такая таска
        for (SubTask taskIterated : subTasks.values()) {
            if (taskIterated.equals(subTask)) {
                break;
            }
            if (!epicTasks.containsKey(subTask.getEpicId())) { // Не знаю, возможна ли попытка добавления подзадачи до создания эпика
                break;
            }
        }

        subTask.setId(generateId());
        subTask.setStatus(Status.NEW);

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
        countEpicDuration(subTask.getEpicId());
        return subTask.getId();
    } // recordSubTask

    // update записывает новый (переданный) объект на место старого, по ID
    @Override
    public void updateSimpleTask(Task task) {
        if (task.getStartTime() != null) {
            Task taskReserved = getSimpleTaskByIdOrNull(task.getId());
            LocalDateTime checkTime = taskReserved.getStartTime();
            // освободить место предыдущей версии этой таски в расписании
            while (checkTime.isBefore(taskReserved.getEndTime())) {
                schedule.put(checkTime, false);
                checkTime = checkTime.plusMinutes(15);
            }

            if (fastCollisionCheck(task)) {
                // если не удалось записать новую версию, в расписании восстановить бронь под старую
                checkTime = taskReserved.getStartTime();
                while (checkTime.isBefore(taskReserved.getEndTime())) {
                    schedule.put(checkTime, true);
                    checkTime = checkTime.plusMinutes(15);
                }

                return;
            }
        }
        simpleTasks.replace(task.getId(), task);
    } // updateSimpleTask

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        epicTasks.replace(epicTask.getId(), epicTask);
    } // updateEpicTask

    @Override
    public void updateSubTask(SubTask subTask) {
        if (subTask.getStartTime() != null) {
            SubTask subReserved = getSubTaskByIdOrNull(subTask.getId());
            LocalDateTime checkTime = subReserved.getStartTime();
            // освободить место предыдущей версии этой таски в расписании
            while (checkTime.isBefore(subReserved.getEndTime())) {
                schedule.put(checkTime, false);
                checkTime = checkTime.plusMinutes(15);
            }

            if (fastCollisionCheck(subTask)) {
                // если не удалось записать новую версию, в расписании восстановить бронь под старую
                checkTime = subReserved.getStartTime();
                while (checkTime.isBefore(subReserved.getEndTime())) {
                    schedule.put(checkTime, true);
                    checkTime = checkTime.plusMinutes(15);
                }

                return;
            }
        }
        subTasks.replace(subTask.getId(), subTask);
        updateEpicStatus(subTask.getEpicId());
        countEpicDuration(subTask.getEpicId());
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

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    private LocalDateTime findEpicStartTime(Long epicId) {
        EpicTask epicTask = epicTasks.get(epicId);
        LocalDateTime startTime = epicTask.getStartTime();
        List<SubTask> subsOfThisEpic = getAllSubTasksOfEpicOrNull(epicId);
        for (SubTask sub : subsOfThisEpic) {
            if (sub.getStartTime() != null) {
                if (startTime == null) {
                    startTime = sub.getStartTime();
                } else if (sub.getStartTime().isBefore(startTime)) {
                    startTime = sub.getStartTime();
                }
            }
        }
        epicTask.setStartTime(startTime);
        return startTime;
    } // findEpicStartTime

    private Duration countEpicDuration(Long epicId) {
        EpicTask epic = epicTasks.get(epicId);
        Duration duration = epic.getDuration();
        LocalDateTime startTime = findEpicStartTime(epicId);
        LocalDateTime endTime;
        if (epic.getDuration() == null) {
            endTime = startTime;
        } else {
            endTime = startTime.plus(epic.getDuration());
        }
        List<SubTask> subsOfThisEpic = getAllSubTasksOfEpicOrNull(epicId);
        for (SubTask sub : subsOfThisEpic) {
            if (sub.getDuration() == null && sub.getStartTime() == null) {
                break;
            } else if (endTime == null && sub.getDuration() != null && sub.getStartTime() != null) {
                startTime = sub.getStartTime();
                endTime = startTime.plus(sub.getDuration());
            } else {
                if (sub.getStartTime().isBefore(startTime)) {
                    startTime = sub.getStartTime();
                }
                if (sub.getEndTime().isAfter(endTime)) {
                    endTime = sub.getEndTime();
                }
            }
        }
        if (startTime != null && endTime != null) {
            duration = Duration.between(startTime, endTime);
            epic.setStartTime(startTime);
            epic.setDuration(duration);
            priority.add(epic);
        }
        return duration;
    } // countEpicDuration

    public List<Task> getPrioritizedTasks() {
        List<Task> result = new ArrayList<>(priority);
        return result;
    }

    private void createSchedule() {
        Map<LocalDateTime, Boolean> result = new HashMap<>();
        LocalDateTime startOfYear = LocalDateTime.of(2023, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime currentDate = startOfYear;
        LocalDateTime endOfYear = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0, 0);
        while (currentDate.isBefore(endOfYear)) {
            result.put(currentDate, false);
            currentDate = currentDate.plusMinutes(15);
        }
        schedule = result;
    }

    private boolean fastCollisionCheck(Task task) {
        boolean isCollision = false;
        if (task.getStartTime() == null && task.getDuration() == null) {
            return false; // время не установлено - разрешили
        }

        LocalDateTime checkTime = task.getStartTime();
        while (checkTime.isBefore(task.getEndTime())) {
            if (schedule.get(checkTime)) {
                isCollision = true;
            }
            checkTime = checkTime.plusMinutes(15);
        }
        if (!isCollision) {
            checkTime = task.getStartTime();
            while (checkTime.isBefore(task.getEndTime())) {
                schedule.put(checkTime, true);
                checkTime = checkTime.plusMinutes(15);
            }
        }
        priority.add(task);
        return isCollision;
    }

} // TaskManager