package tasks;

import java.util.Objects;

public class Task {
    private String name;
    private String description;

    private Long id; // Счётчик countID в TaskManager. Интересно, мы его оставим как счётчик или потом заменим на hash
    private Status status;
    private TaskTypes taskType;

    // Конструктор под создание: только имя и описание
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.taskType = TaskTypes.TASK;
    }

    // Конструктор под восстановление из файла: все поля
    //         // id,type,name,status,description
    public Task(Long id, TaskTypes type, String name, Status status, String description) {
        this.id = id;
        this.taskType = type;
        this.name = name;
        this.status = status;
        this.description = description;
    }



    public TaskTypes getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypes taskType) {
        this.taskType = taskType;
    }


    public Status getStatus() {
        return status;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    } // setStatus

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name) && Objects.equals(description, task.description) && Objects.equals(id, task.id) && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }
}
