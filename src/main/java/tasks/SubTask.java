package tasks;

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
    }


        public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }
}
