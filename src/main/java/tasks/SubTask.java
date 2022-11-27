package tasks;

public class SubTask extends Task {
    Long epicID; // Какому эпику принадлежит

    // Конструктор под создание
    public SubTask(String name, String description, Long epicID) {
        super(name, description);
        this.epicID = epicID;
    }

    public Long getEpicID() {
        return epicID;
    }
}
