package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EpicTask extends Task {
    private final ArrayList<Long> subTasksIds;

    public EpicTask(String name, String description) {
        super(name, description);
        subTasksIds = new ArrayList<>();
    }

    public EpicTask(Long id, TaskTypes type, String name, Status status, String description) {
        super(id, type, name, status, description);
        subTasksIds = new ArrayList<>();
    }

    public EpicTask(Long id, TaskTypes type, String name, Status status, String description, LocalDateTime startTime, Duration duration) {
        super(id, type, name, status, description, startTime, duration);
        subTasksIds = new ArrayList<>();
    }

    public ArrayList<Long> getSubTasksIds() {
        return subTasksIds;
    }

    @Override
    public TaskTypes getTaskType() {
        return TaskTypes.EPIC;
    }
}
