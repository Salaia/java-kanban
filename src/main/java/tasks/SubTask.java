package tasks;

public class SubTask extends Task {
    Long epicId; // Какому эпику принадлежит

    // Конструктор под создание
    public SubTask(String name, String description, Long epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Long getEpicId() {
        return epicId;
    }
}
