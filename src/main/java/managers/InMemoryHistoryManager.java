package managers;

import tasks.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private final ArrayList<Task> history;
    private static final int MAX_HISTORY_SIZE = 10;

    public InMemoryHistoryManager() {
        history = new ArrayList<>();
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }

    // должен помечать задачи как просмотренные
    @Override
    public void add(Task task) {
        if (history.size() < MAX_HISTORY_SIZE) {
            history.add(task);
        } else {
            history.remove(0);
            history.add(task);
        }
    }

}