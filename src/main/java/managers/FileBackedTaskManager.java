package managers;

import exceptions.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// родитель implements TaskManager
public class FileBackedTaskManager extends InMemoryTaskManager {
    File file;

    // Конструктор
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public FileBackedTaskManager(URL url) {
        Managers.getDefaultHttp();
    }

    @Override
    public void deleteAllSimpleTasks() {
        super.deleteAllSimpleTasks();
        save();
    }

    @Override
    public void deleteAllEpicTasks() {
        super.deleteAllEpicTasks();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteSimpleTask(Long id) {
        super.deleteSimpleTask(id);
        save();
    }

    @Override
    public void deleteEpicTask(Long id) {
        super.deleteEpicTask(id);
        save();
    }

    @Override
    public void deleteSubTask(Long id) {
        super.deleteSubTask(id);
        save();
    }

    @Override
    public Task getSimpleTaskByIdOrNull(Long id) {
        Task task = super.getSimpleTaskByIdOrNull(id);
        save();
        return task;

    }

    @Override
    public EpicTask getEpicTaskByIdOrNull(Long id) {
        EpicTask epicTask = super.getEpicTaskByIdOrNull(id);
        save();
        return epicTask;
    }

    @Override
    public SubTask getSubTaskByIdOrNull(Long id) {
        SubTask subTask = super.getSubTaskByIdOrNull(id);
        save();
        return subTask;
    }

    @Override
    public Long recordSimpleTask(Task task) {
        Long id = super.recordSimpleTask(task);
        save();
        return id;
    }

    @Override
    public Long recordEpicTask(EpicTask epicTask) {
        Long id = super.recordEpicTask(epicTask);
        save();
        return id;
    }

    @Override
    public Long recordSubTask(SubTask subTask) {
        Long id = super.recordSubTask(subTask);
        save();
        return id;
    }

    @Override
    public void updateSimpleTask(Task task) {
        super.updateSimpleTask(task);
        save();
    }

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        super.updateEpicTask(epicTask);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    // будет сохранять текущее состояние менеджера в указанный файл (file)
    protected void save() {
        String header = "id,type,name,status,description,date_time,duration,epic\n";
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file))) {
            fileWriter.write(header);
            for (Task task : getSimpleTasks()) {
                String taskLine = toString(task) + "\n";
                fileWriter.write(taskLine);
            }
            for (EpicTask epicTask : getEpicTasks()) {
                String taskLine = toString(epicTask) + "\n";
                fileWriter.write(taskLine);
            }
            for (SubTask subTask : getSubTasks()) {
                String taskLine = toString(subTask) + "\n";
                fileWriter.write(taskLine);
            }
            // создана ли вообще история
            // (если таски только создавали, но не вызывали - история пустая, а метод save() уже вызывается)
            if (super.getHistory() != null) {
                fileWriter.write("\n"); // Строка-разделитель перед историей
                fileWriter.write(historyToString(super.getHistoryManager()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerSaveException("ManagerSaveException");
        }
    } // save

    private String toString(Task task) {  // строка из задачи
        String result = "";
        // собираем данные, общие для всех задач
        result += task.getId() + "," + task.getTaskType() + "," + task.getName() +
                "," + task.getStatus() + "," + task.getDescription() + "," + task.getStartTime() + "," + task.getDuration();
        // если передана сабтаска - добавляем ИД её эпика
        if (task.getTaskType() == TaskTypes.SUBTASK) {
            SubTask sub = (SubTask) task;
            result += "," + sub.getEpicId();
        }
        return result;
    }

    private static Task fromString(String value) { // задача из строки
        String[] taskData = value.split(",");
        Task task;
        try {
            // id,type,name,status,description,startTime,duration,epic
            if (TaskTypes.valueOf(taskData[1]).equals(TaskTypes.SUBTASK)) {
                if (!taskData[5].equals("null")) { // Если не пустая дата старта
                    task = new SubTask(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                            Status.valueOf(taskData[3]), taskData[4], LocalDateTime.parse(taskData[5]), Duration.parse(taskData[6]), Long.parseLong(taskData[7]));
                } else {
                    task = new SubTask(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                            Status.valueOf(taskData[3]), taskData[4], Long.parseLong(taskData[7]));
                }
            } else if (TaskTypes.valueOf(taskData[1]).equals(TaskTypes.EPIC)) {
                if (!taskData[5].equals("null")) { // Если не пустая дата старта
                    task = new EpicTask(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                            Status.valueOf(taskData[3]), taskData[4], LocalDateTime.parse(taskData[5]), Duration.parse(taskData[6]));
                } else {
                    task = new EpicTask(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                            Status.valueOf(taskData[3]), taskData[4]);
                }
            } else {
                if (!taskData[5].equals("null")) { // Если не пустая дата старта
                    task = new Task(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                            Status.valueOf(taskData[3]), taskData[4], LocalDateTime.parse(taskData[5]), Duration.parse(taskData[6]));
                } else {
                    task = new Task(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                            Status.valueOf(taskData[3]), taskData[4]);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return task;
    }

    private static String historyToString(HistoryManager manager) { // сохранить историю в строку
        String result = "";
        for (Task task : manager.getHistory()) {
            result += task.getId() + ",";
        }
        return result;
    }

    private static List<Long> historyFromString(String value) { // восстановить историю из строки
        List<Long> result = new ArrayList<>();
        String[] ids = value.split(",");
        for (int i = 0; i < ids.length; i++) {
            try {
                result.add(Long.parseLong(ids[i]));
            } catch (NumberFormatException e) {
                return result;
            }
        }
        return result;
    }

    // будет восстанавливать данные менеджера из файла при запуске программы.
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String fileString = Files.readString(file.toPath()); //записали весь файл в 1 строку
            String[] fileLines = fileString.split("\n"); //разбили на массив построчно

            //!fileLines[i].isBlank() // пока не дойдём до пустой строки-разделителя
            for (int i = 1; i < fileLines.length && !fileLines[i].isBlank(); i++) { // первая строка - шапка, её пропускаем
                // сначала записать простые таски и эпики -
                // потом сабы, чтобы сабтаска не пыталась записаться до эпика
                Task task = fromString(fileLines[i]);
                assert task != null;
                // мапы в родителе сделала protected, тк у нас уже есть на них геттеры,
                // но те возвращают списки значений, а мне тут нужны сами мапы
                if (task.getTaskType().equals(TaskTypes.TASK)) {
                    fileBackedTaskManager.simpleTasks.put(task.getId(), task);
                } else if (task.getTaskType().equals(TaskTypes.EPIC)) {
                    EpicTask epicTask = (EpicTask) task;
                    fileBackedTaskManager.epicTasks.put(epicTask.getId(), epicTask);
                }
            } // for

            // итерируюсь ещё раз по тому же массиву ради сабтасок
            // плохо по ресурсам, но как иначе - я не придумала
            for (int i = 1; i < fileLines.length && !fileLines[i].isBlank(); i++) {
                Task task = fromString(fileLines[i]);
                assert task != null;
                if (task.getTaskType().equals(TaskTypes.SUBTASK)) {
                    SubTask subTask = (SubTask) task;
                    fileBackedTaskManager.subTasks.put(subTask.getId(), subTask);
                    //записаться в список сабтасок соответствующего эпика
                    fileBackedTaskManager.epicTasks.get(subTask.getEpicId()).getSubTasksIds().add(subTask.getId());
                }
            }

            // вытаскиваем историю из последней строчки
            String historyLine = fileLines[fileLines.length - 1];
            List<Long> historyList = historyFromString(historyLine);
            for (int i = 0; i < historyList.size(); i++) {
                // дальше я проверяю, в какой мапе TaskManager есть ид по данному индексу в списке-истории
                // после чего добавляю таску в историю HistoryManager
                if (fileBackedTaskManager.simpleTasks.containsKey(historyList.get(i))) {
                    fileBackedTaskManager.getHistoryManager().add(fileBackedTaskManager.simpleTasks.get(historyList.get(i)));
                } else if (fileBackedTaskManager.epicTasks.containsKey(historyList.get(i))) {
                    fileBackedTaskManager.getHistoryManager().add(fileBackedTaskManager.epicTasks.get(historyList.get(i)));
                } else if (fileBackedTaskManager.subTasks.containsKey(historyList.get(i))) {
                    fileBackedTaskManager.getHistoryManager().add(fileBackedTaskManager.subTasks.get(historyList.get(i)));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        fileBackedTaskManager.restorePriority();
        return fileBackedTaskManager;
    }

    protected void restorePriority() {
        for (Task task : simpleTasks.values()) {
            if (task.getStartTime() != null && task.getDuration() != null) {
                priority.add(task);
            } else {
                priorityTailIds.add(task.getId());
            }
        }

        for (SubTask sub : subTasks.values()) {
            if (sub.getStartTime() != null && sub.getDuration() != null) {
                priority.add(sub);
            } else {
                priorityTailIds.add(sub.getId());
            }
        }

        for (EpicTask epicTask : epicTasks.values()) {
            priorityTailIds.add(epicTask.getId());
        }
    }

} // FileBackedTaskManager
