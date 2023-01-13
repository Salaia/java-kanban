package managers;

import exceptions.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// родитель implements TaskManager
public class FileBackedTaskManager extends InMemoryTaskManager {
    File file;

    // Конструктор
    public FileBackedTaskManager(File file) {
        this.file = file;
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
        super.getSimpleTaskByIdOrNull(id); // переместить в истории
        save();
        return super.getSimpleTaskByIdOrNull(id); // получить таску

    }

    @Override
    public EpicTask getEpicTaskByIdOrNull(Long id) {
        super.getEpicTaskByIdOrNull(id);
        save();
        return super.getEpicTaskByIdOrNull(id);
    }

    @Override
    public SubTask getSubTaskByIdOrNull(Long id) {
        super.getSubTaskByIdOrNull(id);
        save();
        return super.getSubTaskByIdOrNull(id);
    }

    @Override
    public Long recordSimpleTask(Task task) {
        super.recordSimpleTask(task);
        save();
        return task.getId();
    }

    @Override
    public Long recordEpicTask(EpicTask epicTask) {
        super.recordEpicTask(epicTask);
        save();
        return epicTask.getId();
    }

    @Override
    public Long recordSubTask(SubTask subTask) {
        super.recordSubTask(subTask);
        save();
        return subTask.getId();
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
    private void save() { // throws ManagerSaveException
        String header = "id,type,name,status,description,epic\n";
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
            //throw new ManagerSaveException("ManagerSaveException"); кто как его ловить должен я не поняла :(
        }
    } // save

    private String toString(Task task) {  // строка из задачи
        String result = "";
        // собираем данные, общие для всех задач
        result += task.getId() + "," + task.getTaskType() + "," + task.getName() +
                "," + task.getStatus() + "," + task.getDescription();
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
            // id,type,name,status,description,epic
            // под это дело создала новый конструктор в Task, принимающий все параметры
            if (TaskTypes.valueOf(taskData[1]).equals(TaskTypes.SUBTASK)) {
                task = new SubTask(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                        Status.valueOf(taskData[3]), taskData[4], Long.parseLong(taskData[5]));
            } else if (TaskTypes.valueOf(taskData[1]).equals(TaskTypes.EPIC)) {
                task = new EpicTask(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                        Status.valueOf(taskData[3]), taskData[4]);
            } else {
                task = new Task(Long.parseLong(taskData[0]), TaskTypes.valueOf(taskData[1]), taskData[2],
                        Status.valueOf(taskData[3]), taskData[4]);
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
            result.add(Long.parseLong(ids[i]));
        }
        return result;
    }

    // будет восстанавливать данные менеджера из файла при запуске программы.
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String fileString = Files.readString(file.toPath());
            String[] fileLines = fileString.split("\n");

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
                    fileBackedTaskManager.getHistoryManager().add(fileBackedTaskManager.getSimpleTaskByIdOrNull(historyList.get(i)));
                } else if (fileBackedTaskManager.epicTasks.containsKey(historyList.get(i))) {
                    fileBackedTaskManager.getHistoryManager().add(fileBackedTaskManager.getEpicTaskByIdOrNull(historyList.get(i)));
                } else if (fileBackedTaskManager.subTasks.containsKey(historyList.get(i))) {
                    fileBackedTaskManager.getHistoryManager().add(fileBackedTaskManager.getSubTaskByIdOrNull(historyList.get(i)));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBackedTaskManager;
    }

} // FileBackedTaskManager
