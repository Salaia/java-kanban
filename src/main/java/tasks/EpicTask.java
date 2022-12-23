package tasks;

import java.util.ArrayList;

public class EpicTask extends Task {
    private final ArrayList<Long> subTasksIds;

    public EpicTask(String name, String description) {
        super(name, description);
        subTasksIds = new ArrayList<>();
    }

    public ArrayList<Long> getSubTasksIds() {
        return subTasksIds;
    }

}
