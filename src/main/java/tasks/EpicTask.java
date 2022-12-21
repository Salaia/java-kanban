package tasks;

import java.util.ArrayList;

public class EpicTask extends Task {
    private final ArrayList<Long> subTasksIDs;

    public EpicTask(String name, String description) {
        super(name, description);
        subTasksIDs = new ArrayList<>();
    }

    public ArrayList<Long> getSubTasksIDs() {
        return subTasksIDs;
    }

}
