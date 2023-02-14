package managers;

import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {
    protected Long countId; // does ++countId when new Task()
    // Во всех мапах Long == ID задачи. Поменяла всем на protected для доступа из наследника
    protected final Map<Long, Task> simpleTasks;
    protected final Map<Long, EpicTask> epicTasks;
    protected final Map<Long, SubTask> subTasks;
    // замечания - перед тем, как добавить в TreeSet объект с измененным состоянием - его надо сначала удалить.
    protected final TreeSet<Task> priority; // хранит все таски С УСТАНОВЛЕННЫМ ВРЕМЕНЕМ в порядке приоритета - времени начала
    protected final Set<Long> priorityTailIds;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private Map<LocalDateTime, Boolean> schedule;

    // Конструктор
    public InMemoryTaskManager() {
        countId = 0L;
        simpleTasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subTasks = new HashMap<>();
        priority = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        priorityTailIds = new TreeSet<>(Long::compareTo);
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
        if (simpleTasks.isEmpty()) {
            return;
        }
        // чистим расписание
        for (Task task : simpleTasks.values()) {
            if (task.getStartTime() != null) {
                LocalDateTime checkTime = task.getStartTime();
                while (checkTime.isBefore(task.getEndTime())) {
                    schedule.put(checkTime, false);
                    checkTime = checkTime.plusMinutes(15);
                }
            }
        }

        // чистим список приоритетов со временем
        TreeSet<Task> checkList = new TreeSet<>(priority);
        for (Task task : checkList) {
            if (task.getTaskType().equals(TaskTypes.TASK)) {
                priority.remove(task);
            }
        }

        // чистим список приоритетов без времени
        TreeSet<Long> checkListTail = new TreeSet<>(priorityTailIds);
        for (Long id : checkListTail) {
            if (findTask(id) != null) {
                if (findTask(id).getTaskType().equals(TaskTypes.TASK)) {
                    priorityTailIds.remove(id);
                }
            }
        }

        // чистим историю
        if (historyManager.getHistory() != null) {
            for (Task task : historyManager.getHistory()) {
                if (task.getTaskType().equals(TaskTypes.TASK)) {
                    historyManager.remove(task.getId());
                }
            }
        }
        simpleTasks.clear();
    }

    @Override
    public void deleteAllEpicTasks() {
        if (!epicTasks.isEmpty()) {

            // чистим расписание
            for (Task task : subTasks.values()) { // сами эпики не попадают в расписание, зато их сабы - да
                if (task.getStartTime() != null) {
                    LocalDateTime checkTime = task.getStartTime();
                    while (checkTime.isBefore(task.getEndTime())) {
                        schedule.put(checkTime, false);
                        checkTime = checkTime.plusMinutes(15);
                    }
                }
            }

            TreeSet<Task> checkList = new TreeSet<>(priority);
            for (Task task : checkList) {
                if (!task.getTaskType().equals(TaskTypes.SUBTASK)) {
                    priority.remove(task);
                }
            }

            // чистим список приоритетов без времени
            TreeSet<Long> checkListTail = new TreeSet<>(priorityTailIds);
            for (Long id : checkListTail) {
                if (findTask(id) != null) {
                    if (findTask(id).getTaskType().equals(TaskTypes.SUBTASK)) {
                        priorityTailIds.remove(id);
                    }
                }
            }

            if (historyManager.getHistory() != null) {
                for (Task task : historyManager.getHistory()) {
                    if ((task.getTaskType().equals(TaskTypes.EPIC) || (task.getTaskType().equals(TaskTypes.SUBTASK)))) {
                        historyManager.remove(task.getId());
                    }
                }
            }
            subTasks.clear();
            epicTasks.clear();
        }
    }

    @Override
    public void deleteAllSubTasks() {
        if (!subTasks.isEmpty()) {

            // чистим расписание
            for (Task task : subTasks.values()) {
                if (task.getStartTime() != null) {
                    LocalDateTime checkTime = task.getStartTime();
                    while (checkTime.isBefore(task.getEndTime())) {
                        schedule.put(checkTime, false);
                        checkTime = checkTime.plusMinutes(15);
                    }
                }
            }

            for (EpicTask epic : epicTasks.values()) {
                epic.getSubTasksIds().clear();
                epic.setStatus(Status.NEW); // Без сабтасок эпики теперь пустые, и статус у них должен быть "новый"
                calculateEpicDurationAndStartTime(epic.getId());
            }

            TreeSet<Task> checkList = new TreeSet<>(priority);
            for (Task task : checkList) {
                if (task.getTaskType().equals(TaskTypes.SUBTASK)) {
                    priority.remove(task);
                }
            }

            // чистим список приоритетов без времени
            TreeSet<Long> checkListTail = new TreeSet<>(priorityTailIds);
            for (Long id : checkListTail) {
                if (findTask(id) != null) {
                    if (findTask(id).getTaskType().equals(TaskTypes.SUBTASK)) {
                        priorityTailIds.remove(id);
                    }
                }
            }

            if (historyManager.getHistory() != null) {
                for (Task task : historyManager.getHistory()) {
                    if (task.getTaskType().equals(TaskTypes.SUBTASK)) {
                        historyManager.remove(task.getId());
                    }
                }
            }
            subTasks.clear();
        }
    }

    @Override
    public void deleteSimpleTask(Long id) {
        if (!simpleTasks.containsKey(id)) {
            return;
        }
        Task task = simpleTasks.get(id);
        // чистим расписание
        if (task.getStartTime() != null) {
            LocalDateTime checkTime = task.getStartTime();
            while (checkTime.isBefore(task.getEndTime())) {
                schedule.put(checkTime, false);
                checkTime = checkTime.plusMinutes(15);
            }
            priority.remove(getSimpleTaskByIdOrNull(id));
        } else priorityTailIds.remove(id);
        simpleTasks.remove(id);
        historyManager.remove(id);
    } // deleteSimpleTask

    @Override
    public void deleteEpicTask(Long id) {
        if (!epicTasks.containsKey(id)) {
            return;
        }
        Map<Long, SubTask> copySubtasks = new HashMap<>(subTasks);
        for (SubTask subTask : copySubtasks.values()) {
            if (Objects.equals(subTask.getEpicId(), id)) {
                // чистим расписание
                if (subTask.getStartTime() != null) {
                    LocalDateTime checkTime = subTask.getStartTime();
                    while (checkTime.isBefore(subTask.getEndTime())) {
                        schedule.put(checkTime, false);
                        checkTime = checkTime.plusMinutes(15);
                    }
                    priority.remove(subTask);
                } else priorityTailIds.remove(subTask.getId());
                subTasks.remove(subTask.getId());
                historyManager.remove(subTask.getId());
            }
        }
        epicTasks.remove(id);
        historyManager.remove(id);
        priorityTailIds.remove(id);
    } // deleteEpicTask

    @Override
    public void deleteSubTask(Long id) {
        if (!subTasks.containsKey(id)) {
            return;
        }
        SubTask task = subTasks.get(id);
        // чистим расписание
        if (task.getStartTime() != null) {
            LocalDateTime checkTime = task.getStartTime();
            while (checkTime.isBefore(task.getEndTime())) {
                schedule.put(checkTime, false);
                checkTime = checkTime.plusMinutes(15);
                priority.remove(subTasks.get(id));
            }
        } else {
            priorityTailIds.remove(id);
        }

        Long epicId = subTasks.get(id).getEpicId();
        epicTasks.get(epicId).getSubTasksIds().remove(id); // удаляет сабтаску из списка внутри её эпика
        subTasks.remove(id);
        historyManager.remove(id);
        updateEpicStatus(epicId);
        calculateEpicDurationAndStartTime(epicId);
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
        Set<SubTask> subsOfThisEpic = new HashSet<>();
        if (!epicTasks.containsKey(epicId)) {
            return null;
        }
        for (SubTask subTask : subTasks.values()) {
            if (Objects.equals(subTask.getEpicId(), epicId)) {
                subsOfThisEpic.add(subTask);
            }
        }
        return new ArrayList<>(subsOfThisEpic);
    }

    @Override
    public Long recordSimpleTask(Task task) {
        if (simpleTasks.containsKey(task.getId()) || // Проверка: не записана ли уже такая таска
                task.getTaskType() != TaskTypes.TASK) { // наследников сюда тоже не надо
            return null;
        }

        if (task.getStartTime() != null) {
            // Проверка: не занято ли время, на которое претендует таска
            if (fastCollisionCheck(task)) {
                return -1L;
            }
            priority.add(task);
        }

        task.setId(generateId());
        task.setStatus(Status.NEW);
        if (task.getStartTime() == null) {
            priorityTailIds.add(task.getId());
        }

        simpleTasks.put(task.getId(), task);
        return task.getId();
    } // recordSimpleTask

    @Override
    public Long recordEpicTask(EpicTask epicTask) {
        // Проверка: не записана ли уже такая таска
        if (epicTasks.containsKey(epicTask.getId())) {
            return null;
        }

        epicTask.setId(generateId());
        epicTask.setStatus(Status.NEW);

        epicTasks.put(epicTask.getId(), epicTask);
        priorityTailIds.add(epicTask.getId());
        return epicTask.getId();
    } // recordEpicTask

    @Override
    public Long recordSubTask(SubTask subTask) {
        if (subTask.getEpicId() == null || !epicTasks.containsKey(subTask.getEpicId())) {
            return -2L;
        }
        if (subTask.getStartTime() != null) {
            if (fastCollisionCheck(subTask)) { // Проверка: не занято ли время, на которое претендует таска
                return -1L;
            }
            priority.add(subTask);
        }
        subTask.setId(generateId());
        subTask.setStatus(Status.NEW);
        if (subTask.getStartTime() == null) {
            priorityTailIds.add(subTask.getId());
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
        calculateEpicDurationAndStartTime(subTask.getEpicId());
        return subTask.getId();
    } // recordSubTask

    // update записывает новый (переданный) объект на место старого, по ID
    @Override
    public void updateSimpleTask(Task task) {
        if (!simpleTasks.containsKey(task.getId())) {
            return;
        }
        Task taskReserved = getSimpleTaskByIdOrNull(task.getId());
        if (task.getStartTime() != null) {
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
            priority.add(task);
        } else {
            priorityTailIds.add(task.getId()); // была она там или нет - ид в сете может быть только 1 раз
        }
        simpleTasks.replace(task.getId(), task);
        if (getHistory().contains(taskReserved)) {
            historyManager.updateHistory(task);
        }
    } // updateSimpleTask

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        if (!epicTasks.containsKey(epicTask.getId())) {
            return;
        }
        EpicTask oldEpic = epicTasks.get(epicTask.getId());
        oldEpic.setName(epicTask.getName());
        oldEpic.setDescription(epicTask.getDescription());
        if (getHistory().contains(oldEpic)) {
            historyManager.updateHistory(epicTask);
        }

    } // updateEpicTask

    @Override
    public void updateSubTask(SubTask subTask) {
        if (!subTasks.containsKey(subTask.getId()) ||
                !Objects.equals(subTask.getEpicId(), subTasks.get(subTask.getId()).getEpicId())
                || !epicTasks.containsKey(subTask.getEpicId())) {
            return;
        }
        SubTask subReserved = getSubTaskByIdOrNull(subTask.getId());
        if (subTask.getStartTime() != null) {
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
            priority.add(subTask);
        } else {
            priorityTailIds.add(subTask.getId());
        }
        subTasks.replace(subTask.getId(), subTask);
        updateEpicStatus(subTask.getEpicId());
        calculateEpicDurationAndStartTime(subTask.getEpicId());
        if (getHistory().contains(subReserved)) {
            historyManager.updateHistory(subTask);
        }

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

        if (newTasks == subsOfThisEpic.size() || subsOfThisEpic.size() == 0) {
            epicTask.setStatus(Status.NEW);
        } else if (doneTasks == subsOfThisEpic.size()) {
            epicTask.setStatus(Status.DONE);
        } else {
            epicTask.setStatus(Status.IN_PROGRESS);
        }
    } // updateEpicStatus

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    private void calculateEpicDurationAndStartTime(Long epicId) {
        EpicTask epic = epicTasks.get(epicId);
        if (epic.getSubTasksIds().isEmpty()) { // нет сабтасок - нет времени в эпике
            epic.setDuration(null);
            epic.setStartTime(null);
            return;
        }

        // Duration.ZERO и LocalDateTime.MAX чтобы можно было методы вызывать
        Duration epicDuration = Duration.ZERO;
        LocalDateTime epicStart = LocalDateTime.MAX;
        boolean hasEpicDatedSubs = false;

        for (Long id : epicTasks.get(epicId).getSubTasksIds()) {
            SubTask subTask = subTasks.get(id);
            if (subTask.getDuration() != null && subTask.getStartTime() != null) {
                epicDuration = epicDuration.plus(subTask.getDuration());
                if (subTask.getStartTime().isBefore(epicStart)) {
                    epicStart = subTask.getStartTime();
                }
                hasEpicDatedSubs = true;
            }
        }

        if (!hasEpicDatedSubs) {
            epic.setDuration(null);
            epic.setStartTime(null);
        } else {
            epic.setDuration(epicDuration);
            epic.setStartTime(epicStart);
        }
    } // countEpicDuration

    public List<Task> getPrioritizedTasks() {
        List<Task> result = new ArrayList<>(priority);
        for (Long id : priorityTailIds) {
            if (findTask(id) != null) {
                result.add(findTask(id));
            }
        }
        return result;
    }

    private void createSchedule() {
        Map<LocalDateTime, Boolean> result = new HashMap<>();
        LocalDateTime currentDate = LocalDateTime.of(2023, Month.JANUARY, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0, 0);
        while (currentDate.isBefore(endOfYear)) {
            result.put(currentDate, false);
            currentDate = currentDate.plusMinutes(15);
        }
        schedule = result;
    }

    private boolean fastCollisionCheck(Task task) {
        if (task.getTaskType() == TaskTypes.EPIC) {
            priorityTailIds.add(task.getId());
            return false;
        }
        boolean isCollision = false;
        if (task.getStartTime() == null && task.getDuration() == null) {
            priorityTailIds.add(task.getId());
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

    public Task findTask(Long id) {
        if (!simpleTasks.isEmpty()) {
            for (Task task : simpleTasks.values()) {
                if (Objects.equals(id, task.getId())) {
                    return task;
                }
            }
        }

        if (!subTasks.isEmpty()) {
            for (SubTask subTask : subTasks.values()) {
                if (Objects.equals(id, subTask.getId())) {
                    return subTask;
                }
            }
        }

        if (!epicTasks.isEmpty()) {
            for (EpicTask epicTask : epicTasks.values()) {
                if (Objects.equals(id, epicTask.getId())) {
                    return epicTask;
                }
            }
        }

        return null; // если таски не нашлось ни в одной мапе, то у нас её нет
    }

} // TaskManager