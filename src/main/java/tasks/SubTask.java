package tasks;

public class SubTask extends Task {
    long epicID; // Какому эпику принадлежит

    // Конструктор под создание
    public SubTask(String name, String description, long epicID) {
        super(name, description);
        this.epicID = epicID;
    }
    public long getEpicID() {
        return epicID;
    }
}
