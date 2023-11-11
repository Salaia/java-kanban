package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EpicTask extends Task {
    private ArrayList<Long> subTasksIds = new ArrayList<>();

    public EpicTask(String name, String description) {
        super(name, description);
        //subTasksIds = new ArrayList<>();
    }

    public EpicTask(Long id, String name, Status status, String description) {
        super(id, name, status, description);
        //subTasksIds = new ArrayList<>();
    }

    public EpicTask(Long id, String name, String description) {
        super(id, name, description);
        //subTasksIds = new ArrayList<>();
    }

    public EpicTask(Long id, String name, Status status, String description, LocalDateTime startTime, Duration duration) {
        super(id, name, status, description, startTime, duration);
        //subTasksIds = new ArrayList<>();
    }

    public ArrayList<Long> getSubTasksIds() {
        return subTasksIds;
    }

    public void setSubTasksIds(ArrayList<Long> subTasksIds) {
        this.subTasksIds = subTasksIds;
    }

    @Override
    public TaskTypes getTaskType() {
        return TaskTypes.EPIC;
    }
}
