package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    Long epicId; // Какому эпику принадлежит

    // Конструктор под создание
    public SubTask(String name, String description, Long epicId) {
        super(name, description);
        this.epicId = epicId;
        setTaskType(TaskTypes.SUBTASK);
    }

    // Конструктор под восстановление из файла: все поля
    //         // id,type,name,status,description,epic
    public SubTask(Long id, TaskTypes type, String name, Status status, String description, Long epicId) {
        super(id, type, name, status, description);
        this.epicId = epicId;
        setTaskType(TaskTypes.SUBTASK);
    }

    // Конструктор для указания времени
    public SubTask(String name, String description, LocalDateTime startTime, Duration duration, Long epicId) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
        setTaskType(TaskTypes.SUBTASK);
    }

    public SubTask(Long id, TaskTypes type, String name, Status status, String description, LocalDateTime startTime, Duration duration, Long epicId) {
        super(id, type, name, status, description, startTime, duration);
        this.epicId = epicId;
    }

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }
}
