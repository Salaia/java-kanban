package tasks;

import java.util.ArrayList;

public class EpicTask extends Task {
    public ArrayList<Long> subTasksIDs;

    public EpicTask(String name, String description) {
        super(name, description);
        subTasksIDs = new ArrayList<>();
    }
}
